package com.medical.federated.service;

import com.medical.federated.config.SystemConfig;
import com.medical.federated.model.FederatedRound;
import com.medical.federated.model.HospitalNode;
import com.medical.federated.model.ModelWeights;

import java.util.*;
import java.util.concurrent.*;

/**
 * Coordinates Federated Learning rounds and executes the FedAvg algorithm.
 * Aggregates local updates from participating hospitals without accessing raw patient data.
 */
public class FederatedLearningService {
    private final HospitalManager hospitalManager;
    private final BlockchainAuditService blockchainService;
    private final List<FederatedRound> roundHistory = Collections.synchronizedList(new ArrayList<>());
    private final List<String> systemLogs = Collections.synchronizedList(new ArrayList<>());
    
    private ModelWeights globalWeights;
    private int currentRound = 0;
    private volatile boolean isTrainingInProgress = false;
    private final Object logLock = new Object();
    private final Object trainingLock = new Object();
    private final ExecutorService trainingExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public FederatedLearningService(HospitalManager hospitalManager, BlockchainAuditService blockchainService) {
        this.hospitalManager = hospitalManager;
        this.blockchainService = blockchainService;
        this.globalWeights = new ModelWeights(SystemConfig.MODEL_PARAM_SIZE);
        this.globalWeights.setAccuracy(SystemConfig.INITIAL_ACCURACY);
        this.globalWeights.setLoss(SystemConfig.INITIAL_LOSS);
        this.globalWeights.setSampleCount(hospitalManager.getTotalScansAcrossNetwork());

        addSystemLog("Federated Learning Coordinator initialized with FedAvg algorithm.");
        addSystemLog("Initial Global Model loaded: Accuracy: 72.40% | Loss: 0.6850");
    }

    public void addSystemLog(String msg) {
        String logEntry = String.format("[%tT] %s", System.currentTimeMillis(), msg);
        synchronized (logLock) {
            systemLogs.add(logEntry);
            if (systemLogs.size() > 200) {
                systemLogs.remove(0);
            }
        }
    }

    /**
     * Executes one complete Federated Learning Round across all active hospital nodes.
     */
    public FederatedRound executeFederatedRound(int epochs) {
        synchronized (trainingLock) {
            if (isTrainingInProgress) {
                throw new IllegalStateException("A federated training round is already currently in progress.");
            }
            isTrainingInProgress = true;
        }
        currentRound++;
        addSystemLog(String.format("=== STARTING FEDERATED LEARNING ROUND #%d ===", currentRound));

        List<HospitalNode> activeHospitals = hospitalManager.getActiveHospitals();
        if (activeHospitals.isEmpty()) {
            isTrainingInProgress = false;
            addSystemLog("ERROR: No active hospital clients available for round " + currentRound);
            throw new IllegalStateException("No active hospitals available for federated training.");
        }

        addSystemLog(String.format("Broadcasting global model v%d to %d participating hospitals...", currentRound - 1, activeHospitals.size()));

        // Concurrently trigger local training on all active hospital nodes
        List<CompletableFuture<ModelWeights>> futures = new ArrayList<>();
        List<String> participatingNames = new ArrayList<>();

        for (HospitalNode hospital : activeHospitals) {
            participatingNames.add(hospital.getName());
            futures.add(CompletableFuture.supplyAsync(() -> {
                addSystemLog(String.format("[%s] Received train message - Local training started...", hospital.getName()));
                ModelWeights clientUpdate = hospital.performLocalTraining(epochs, globalWeights);
                addSystemLog(String.format("[%s] Local training completed. Transmitted encrypted model gradients.", hospital.getName()));
                return clientUpdate;
            }, trainingExecutor));
        }

        // Wait for all hospital nodes to complete local training
        List<ModelWeights> clientUpdates = new ArrayList<>();
        for (CompletableFuture<ModelWeights> future : futures) {
            try {
                ModelWeights res = future.get(30, TimeUnit.SECONDS);
                if (res != null) {
                    clientUpdates.add(res);
                }
            } catch (Exception e) {
                addSystemLog("Warning: A hospital node timed out during local epoch computation: " + e.getMessage());
            }
        }

        addSystemLog(String.format("Collected %d local model updates from participating hospital clients.", clientUpdates.size()));
        addSystemLog("Applying FedAvg Algorithm: W_global = Σ (n_k / N) * W_k...");

        // Perform FedAvg Aggregation
        this.globalWeights = ModelWeights.aggregateFedAvg(clientUpdates);

        // Calculate cryptographic hash of aggregated weights
        String weightsSignature = Arrays.toString(globalWeights.getWeights()).substring(0, 32);
        String aggregationHash = blockchainService.computeStringSha256("Round_" + currentRound + "_" + weightsSignature);

        addSystemLog(String.format("Global Model Updated: Round #%d | Global Accuracy: %.2f%% | Loss: %.4f",
                currentRound, globalWeights.getAccuracy() * 100.0, globalWeights.getLoss()));

        // Distribute updated global model back to all hospital nodes
        for (HospitalNode hospital : activeHospitals) {
            hospital.updateGlobalModel(globalWeights);
            addSystemLog(String.format("[%s] Received evaluated global model v%d.", hospital.getName(), currentRound));
        }

        // Mint Blockchain audit block for this round
        blockchainService.recordRoundSeal(currentRound, globalWeights.getAccuracy(), globalWeights.getLoss(),
                activeHospitals.size(), aggregationHash);
        addSystemLog(String.format("Minted Immutable Blockchain Seal for Round #%d. Hash: %s",
                currentRound, blockchainService.getLatestBlock().getHash().substring(0, 16) + "..."));

        int totalSamples = 0;
        for (HospitalNode h : activeHospitals) totalSamples += h.getDatasetSize();

        FederatedRound roundRecord = new FederatedRound(
                currentRound,
                participatingNames,
                globalWeights.getAccuracy(),
                globalWeights.getLoss(),
                totalSamples,
                aggregationHash,
                "COMPLETED"
        );

        roundHistory.add(roundRecord);
        isTrainingInProgress = false;
        addSystemLog(String.format("=== FEDERATED ROUND #%d FINISHED SUCCESSFULLY ===", currentRound));

        return roundRecord;
    }

    public void resetTraining() {
        this.currentRound = 0;
        this.roundHistory.clear();
        this.globalWeights = new ModelWeights(SystemConfig.MODEL_PARAM_SIZE);
        this.globalWeights.setAccuracy(SystemConfig.INITIAL_ACCURACY);
        this.globalWeights.setLoss(SystemConfig.INITIAL_LOSS);
        for (HospitalNode h : hospitalManager.getAllHospitals()) {
            h.updateGlobalModel(globalWeights);
            h.setStatus("Idle");
        }
        addSystemLog("Federated learning state reset to Round 0.");
    }

    public int getCurrentRound() { return currentRound; }
    public ModelWeights getGlobalWeights() { return globalWeights; }
    public boolean isTrainingInProgress() { return isTrainingInProgress; }
    public List<FederatedRound> getRoundHistory() { return new ArrayList<>(roundHistory); }
    public List<String> getSystemLogs() { return new ArrayList<>(systemLogs); }
}
