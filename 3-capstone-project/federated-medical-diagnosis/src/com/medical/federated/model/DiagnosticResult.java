package com.medical.federated.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates the results of a federated AI medical image diagnosis,
 * including classification probabilities, Grad-CAM attention heatmap,
 * patient privacy SHA-256 hash, and blockchain audit seal.
 */
public class DiagnosticResult {
    private String scanId;
    private String patientId;
    private String filename;
    private String modality;
    private String primaryClassification;
    private double confidenceScore;
    private String diagnosticStatus; // "POSITIVE", "NEGATIVE", "NORMAL"
    private double predictionSpeedSec;
    private String sha256PrivacyHash;
    private String hospitalNode;
    private int flRound;
    private int blockchainBlockId;
    private String blockchainSealHash;
    private Map<String, Double> pathologicalProbabilities = new LinkedHashMap<>();
    private String rawImageBase64;
    private String gradCamHeatmapBase64;

    public DiagnosticResult() {}

    public String getScanId() { return scanId; }
    public void setScanId(String scanId) { this.scanId = scanId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }

    public String getPrimaryClassification() { return primaryClassification; }
    public void setPrimaryClassification(String primaryClassification) { this.primaryClassification = primaryClassification; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getDiagnosticStatus() { return diagnosticStatus; }
    public void setDiagnosticStatus(String diagnosticStatus) { this.diagnosticStatus = diagnosticStatus; }

    public double getPredictionSpeedSec() { return predictionSpeedSec; }
    public void setPredictionSpeedSec(double predictionSpeedSec) { this.predictionSpeedSec = predictionSpeedSec; }

    public String getSha256PrivacyHash() { return sha256PrivacyHash; }
    public void setSha256PrivacyHash(String sha256PrivacyHash) { this.sha256PrivacyHash = sha256PrivacyHash; }

    public String getHospitalNode() { return hospitalNode; }
    public void setHospitalNode(String hospitalNode) { this.hospitalNode = hospitalNode; }

    public int getFlRound() { return flRound; }
    public void setFlRound(int flRound) { this.flRound = flRound; }

    public int getBlockchainBlockId() { return blockchainBlockId; }
    public void setBlockchainBlockId(int blockchainBlockId) { this.blockchainBlockId = blockchainBlockId; }

    public String getBlockchainSealHash() { return blockchainSealHash; }
    public void setBlockchainSealHash(String blockchainSealHash) { this.blockchainSealHash = blockchainSealHash; }

    public Map<String, Double> getPathologicalProbabilities() { return pathologicalProbabilities; }
    public void setPathologicalProbabilities(Map<String, Double> pathologicalProbabilities) { this.pathologicalProbabilities = pathologicalProbabilities; }

    public String getRawImageBase64() { return rawImageBase64; }
    public void setRawImageBase64(String rawImageBase64) { this.rawImageBase64 = rawImageBase64; }

    public String getGradCamHeatmapBase64() { return gradCamHeatmapBase64; }
    public void setGradCamHeatmapBase64(String gradCamHeatmapBase64) { this.gradCamHeatmapBase64 = gradCamHeatmapBase64; }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"scanId\":\"").append(scanId).append("\",");
        sb.append("\"patientId\":\"").append(patientId).append("\",");
        sb.append("\"filename\":\"").append(filename).append("\",");
        sb.append("\"modality\":\"").append(modality).append("\",");
        sb.append("\"primaryClassification\":\"").append(primaryClassification).append("\",");
        sb.append("\"confidenceScore\":").append(String.format("%.4f", confidenceScore)).append(",");
        sb.append("\"diagnosticStatus\":\"").append(diagnosticStatus).append("\",");
        sb.append("\"predictionSpeedSec\":").append(String.format("%.2f", predictionSpeedSec)).append(",");
        sb.append("\"sha256PrivacyHash\":\"").append(sha256PrivacyHash).append("\",");
        sb.append("\"hospitalNode\":\"").append(hospitalNode).append("\",");
        sb.append("\"flRound\":").append(flRound).append(",");
        sb.append("\"blockchainBlockId\":").append(blockchainBlockId).append(",");
        sb.append("\"blockchainSealHash\":\"").append(blockchainSealHash).append("\",");
        sb.append("\"pathologicalProbabilities\":{");
        int count = 0;
        for (Map.Entry<String, Double> entry : pathologicalProbabilities.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":").append(String.format("%.4f", entry.getValue()));
            if (++count < pathologicalProbabilities.size()) sb.append(",");
        }
        sb.append("},");
        sb.append("\"rawImageBase64\":\"").append(rawImageBase64 != null ? rawImageBase64 : "").append("\",");
        sb.append("\"gradCamHeatmapBase64\":\"").append(gradCamHeatmapBase64 != null ? gradCamHeatmapBase64 : "").append("\"");
        sb.append("}");
        return sb.toString();
    }
}
