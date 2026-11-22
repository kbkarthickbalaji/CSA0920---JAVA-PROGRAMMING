package com.medical.federated.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the execution history and global performance of a Federated Training Round.
 */
public class FederatedRound {
    private final int roundNumber;
    private final long timestamp;
    private final List<String> participatingHospitals;
    private final double globalAccuracy;
    private final double globalLoss;
    private final int totalSamplesTrained;
    private final String aggregationHash;
    private final String status;

    public FederatedRound(int roundNumber, List<String> participatingHospitals, double globalAccuracy, 
                          double globalLoss, int totalSamplesTrained, String aggregationHash, String status) {
        this.roundNumber = roundNumber;
        this.timestamp = System.currentTimeMillis();
        this.participatingHospitals = new ArrayList<>(participatingHospitals);
        this.globalAccuracy = globalAccuracy;
        this.globalLoss = globalLoss;
        this.totalSamplesTrained = totalSamplesTrained;
        this.aggregationHash = aggregationHash;
        this.status = status;
    }

    public int getRoundNumber() { return roundNumber; }
    public long getTimestamp() { return timestamp; }
    public List<String> getParticipatingHospitals() { return participatingHospitals; }
    public double getGlobalAccuracy() { return globalAccuracy; }
    public double getGlobalLoss() { return globalLoss; }
    public int getTotalSamplesTrained() { return totalSamplesTrained; }
    public String getAggregationHash() { return aggregationHash; }
    public String getStatus() { return status; }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"roundNumber\":").append(roundNumber).append(",");
        sb.append("\"timestamp\":").append(timestamp).append(",");
        sb.append("\"globalAccuracy\":").append(String.format("%.4f", globalAccuracy)).append(",");
        sb.append("\"globalLoss\":").append(String.format("%.4f", globalLoss)).append(",");
        sb.append("\"totalSamplesTrained\":").append(totalSamplesTrained).append(",");
        sb.append("\"aggregationHash\":\"").append(aggregationHash).append("\",");
        sb.append("\"status\":\"").append(status).append("\",");
        sb.append("\"participatingHospitals\":[");
        for (int i = 0; i < participatingHospitals.size(); i++) {
            sb.append("\"").append(participatingHospitals.get(i)).append("\"");
            if (i < participatingHospitals.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }
}
