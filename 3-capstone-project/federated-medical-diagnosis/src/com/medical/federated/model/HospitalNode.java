package com.medical.federated.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents an autonomous Hospital client node in the federated network.
 * Preserves strict patient data privacy: local images and patient data are kept confidential,
 * and only model gradient/weight updates are transmitted to the coordinator.
 */
public class HospitalNode {
    private final String id;
    private final String name;
    private final String specialty;
    private final String region;
    private final int datasetSize;
    private boolean active;
    private String status; // "Idle", "Training", "Uploading Updates", "Offline"
    private double localAccuracy;
    private double localLoss;
    private int trainingProgress; // 0 - 100%
    private ModelWeights localWeights;
    private final List<String> recentLogs;
    private final Random rng;

    public HospitalNode(String id, String name, String specialty, String region, int datasetSize, double initialAcc, double initialLoss) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.region = region;
        this.datasetSize = datasetSize;
        this.active = true;
        this.status = "Idle";
        this.localAccuracy = initialAcc;
        this.localLoss = initialLoss;
        this.trainingProgress = 0;
        this.localWeights = new ModelWeights(128);
        this.localWeights.setAccuracy(initialAcc);
        this.localWeights.setLoss(initialLoss);
        this.localWeights.setSampleCount(datasetSize);
        this.recentLogs = Collections.synchronizedList(new ArrayList<>());
        this.rng = new Random(id.hashCode());
        addLog("Initialized node with " + datasetSize + " local confidential scans.");
    }

    public synchronized void addLog(String message) {
        String entry = String.format("[%s] %s", name, message);
        recentLogs.add(entry);
        if (recentLogs.size() > 50) {
            recentLogs.remove(0);
        }
    }

    /**
     * Executes local model training simulation on node's private dataset.
     */
    public synchronized ModelWeights performLocalTraining(int epochs, ModelWeights globalWeights) {
        if (!active) {
            status = "Offline";
            return null;
        }

        status = "Training";
        trainingProgress = 0;
        addLog("Received global model parameters. Starting local training...");

        // Start with current global weights
        this.localWeights = new ModelWeights(
            globalWeights.getWeights(),
            globalWeights.getBias(),
            this.datasetSize,
            globalWeights.getAccuracy(),
            globalWeights.getLoss()
        );

        for (int e = 1; e <= epochs; e++) {
            try {
                Thread.sleep(150); // Simulate training latency
            } catch (InterruptedException ignored) {}

            this.localWeights = this.localWeights.simulateLocalTraining(1, datasetSize, rng);
            this.localAccuracy = this.localWeights.getAccuracy();
            this.localLoss = this.localWeights.getLoss();
            this.trainingProgress = (int) (((double) e / epochs) * 100);

            // Log format matching Slide 7 image13:
            // "20/20 4s 212ms/step - accuracy: 0.8375 - loss: 0.4297"
            addLog(String.format("Epoch %d/%d - 20/20 step - accuracy: %.4f - loss: %.4f",
                    e, epochs, localAccuracy, localLoss));
        }

        status = "Uploading Updates";
        addLog(String.format("Local training completed. Prepared model update (ΔW) for FedAvg aggregation."));
        trainingProgress = 100;
        status = "Idle";

        return this.localWeights;
    }

    public void updateGlobalModel(ModelWeights newGlobalWeights) {
        this.localWeights = new ModelWeights(
            newGlobalWeights.getWeights(),
            newGlobalWeights.getBias(),
            this.datasetSize,
            newGlobalWeights.getAccuracy(),
            newGlobalWeights.getLoss()
        );
        this.localAccuracy = newGlobalWeights.getAccuracy();
        this.localLoss = newGlobalWeights.getLoss();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getRegion() { return region; }
    public int getDatasetSize() { return datasetSize; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) {
        this.active = active;
        this.status = active ? "Idle" : "Offline";
        addLog("Node status updated: " + this.status);
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getLocalAccuracy() { return localAccuracy; }
    public double getLocalLoss() { return localLoss; }
    public int getTrainingProgress() { return trainingProgress; }
    public List<String> getRecentLogs() { return new ArrayList<>(recentLogs); }
    public ModelWeights getLocalWeights() { return localWeights; }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"specialty\":\"%s\",\"region\":\"%s\",\"datasetSize\":%d,\"active\":%b,\"status\":\"%s\",\"localAccuracy\":%.4f,\"localLoss\":%.4f,\"trainingProgress\":%d}",
            id, name, specialty, region, datasetSize, active, status, localAccuracy, localLoss, trainingProgress
        );
    }
}
