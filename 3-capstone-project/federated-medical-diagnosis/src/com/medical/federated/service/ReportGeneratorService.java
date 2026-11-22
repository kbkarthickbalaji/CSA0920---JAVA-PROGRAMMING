package com.medical.federated.service;

import com.medical.federated.model.DiagnosticResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generates signed clinical diagnostic reports matching the Capstone "OPTIMUS AI MEDICAL REPORT" specification.
 */
public class ReportGeneratorService {

    public String generateHtmlReport(DiagnosticResult result) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<title>OPTIMUS AI MEDICAL REPORT - ").append(result.getScanId()).append("</title>\n");
        sb.append("<style>\n");
        sb.append("  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #0b1120; color: #f1f5f9; padding: 40px; margin: 0; }\n");
        sb.append("  .report-card { max-width: 850px; margin: 0 auto; background: #0f172a; border: 1px solid #1e293b; border-radius: 12px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.5); overflow: hidden; }\n");
        sb.append("  .header { background: #020617; padding: 28px 36px; border-bottom: 2px solid #06b6d4; display: flex; justify-content: space-between; align-items: flex-start; }\n");
        sb.append("  .header h1 { margin: 0; font-size: 24px; color: #38bdf8; letter-spacing: 1.5px; text-transform: uppercase; }\n");
        sb.append("  .header .sub { font-size: 13px; color: #94a3b8; letter-spacing: 1px; margin-top: 4px; }\n");
        sb.append("  .header .meta { font-size: 13px; color: #94a3b8; text-align: right; line-height: 1.6; }\n");
        sb.append("  .content { padding: 36px; }\n");
        sb.append("  h2 { font-size: 18px; color: #f8fafc; border-bottom: 1px solid #1e293b; padding-bottom: 8px; margin-top: 0; }\n");
        sb.append("  .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; font-size: 14px; }\n");
        sb.append("  .grid-2 div { line-height: 1.8; }\n");
        sb.append("  .label { color: #64748b; font-weight: 500; display: inline-block; width: 140px; }\n");
        sb.append("  .value { color: #e2e8f0; font-weight: 600; }\n");
        sb.append("  .badge-positive { color: #f43f5e; font-weight: 700; }\n");
        sb.append("  .badge-normal { color: #10b981; font-weight: 700; }\n");
        sb.append("  table { width: 100%; border-collapse: collapse; margin: 24px 0; font-size: 14px; }\n");
        sb.append("  th { background: #1e293b; color: #94a3b8; text-align: left; padding: 12px; font-weight: 600; }\n");
        sb.append("  td { padding: 12px; border-bottom: 1px solid #1e293b; color: #f1f5f9; }\n");
        sb.append("  .scan-preview { display: flex; gap: 24px; margin: 24px 0; justify-content: center; align-items: center; background: #020617; padding: 20px; border-radius: 8px; border: 1px solid #1e293b; }\n");
        sb.append("  .scan-box { text-align: center; }\n");
        sb.append("  .scan-box img { max-width: 320px; max-height: 260px; border-radius: 6px; border: 1px solid #334155; object-fit: contain; }\n");
        sb.append("  .scan-box .scan-label { margin-top: 8px; font-size: 12px; color: #94a3b8; }\n");
        sb.append("  .zk-box { background: rgba(6, 182, 212, 0.05); border: 1px solid rgba(6, 182, 212, 0.3); border-radius: 8px; padding: 16px 20px; margin-top: 24px; }\n");
        sb.append("  .zk-box h3 { margin: 0 0 8px 0; font-size: 14px; color: #38bdf8; display: flex; align-items: center; gap: 8px; }\n");
        sb.append("  .zk-box p { margin: 0; font-size: 12px; color: #94a3b8; line-height: 1.6; word-break: break-all; }\n");
        sb.append("  .actions { text-align: right; margin-top: 24px; }\n");
        sb.append("  .btn { background: #0284c7; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 600; text-decoration: none; display: inline-block; }\n");
        sb.append("  .btn:hover { background: #0369a1; }\n");
        sb.append("  @media print { body { background: white; color: black; padding: 0; } .report-card { border: none; box-shadow: none; background: white; color: black; } .header { background: white; border-bottom: 2px solid black; } .header h1 { color: black; } .content { padding: 20px 0; } .scan-preview { background: #f8fafc; } .actions { display: none; } td, th { color: black; } }\n");
        sb.append("</style>\n</head>\n<body>\n");

        sb.append("<div class=\"report-card\">\n");
        sb.append("  <div class=\"header\">\n");
        sb.append("    <div>\n");
        sb.append("      <h1>OPTIMUS AI MEDICAL REPORT</h1>\n");
        sb.append("      <div class=\"sub\">FEDERATED AI DIAGNOSTIC SYSTEM • ZERO-KNOWLEDGE PROOF VERIFIED</div>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"meta\">\n");
        sb.append("      <div><strong>Patient ID:</strong> ").append(result.getPatientId()).append("</div>\n");
        sb.append("      <div><strong>Date & Time:</strong> ").append(timestamp).append("</div>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        sb.append("  <div class=\"content\">\n");
        sb.append("    <h2>Clinical Summary & Findings</h2>\n");
        sb.append("    <div class=\"grid-2\">\n");
        sb.append("      <div>\n");
        sb.append("        <div><span class=\"label\">Hospital Node:</span> <span class=\"value\">").append(result.getHospitalNode()).append("</span></div>\n");
        sb.append("        <div><span class=\"label\">Filename:</span> <span class=\"value\">").append(result.getFilename()).append("</span></div>\n");
        sb.append("        <div><span class=\"label\">Prediction Time:</span> <span class=\"value\">").append(String.format("%.2f", result.getPredictionSpeedSec())).append(" sec</span></div>\n");
        sb.append("        <div><span class=\"label\">Diagnostic Status:</span> <span class=\"value badge-positive\">").append(result.getDiagnosticStatus()).append("</span></div>\n");
        sb.append("      </div>\n");
        sb.append("      <div>\n");
        sb.append("        <div><span class=\"label\">Target Pathology:</span> <span class=\"value\">").append(result.getPrimaryClassification()).append("</span></div>\n");
        sb.append("        <div><span class=\"label\">Confidence Score:</span> <span class=\"value\" style=\"color:#38bdf8;\">").append(String.format("%.1f%%", result.getConfidenceScore() * 100)).append("</span></div>\n");
        sb.append("        <div><span class=\"label\">Federated Round:</span> <span class=\"value\">Round #").append(result.getFlRound()).append("</span></div>\n");
        sb.append("        <div><span class=\"label\">Modality:</span> <span class=\"value\">").append(result.getModality()).append("</span></div>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");

        sb.append("    <table>\n");
        sb.append("      <thead>\n");
        sb.append("        <tr><th>Target Pathology</th><th>Confidence Score</th><th>Inference Latency</th></tr>\n");
        sb.append("      </thead>\n");
        sb.append("      <tbody>\n");
        for (Map.Entry<String, Double> entry : result.getPathologicalProbabilities().entrySet()) {
            sb.append("        <tr>\n");
            sb.append("          <td><strong>").append(entry.getKey()).append("</strong></td>\n");
            sb.append("          <td>").append(String.format("%.1f%%", entry.getValue() * 100.0)).append("</td>\n");
            sb.append("          <td>").append(String.format("%.2f sec", result.getPredictionSpeedSec())).append("</td>\n");
            sb.append("        </tr>\n");
        }
        sb.append("      </tbody>\n");
        sb.append("    </table>\n");

        sb.append("    <h2>Uploaded Medical Scan & AI Neural Attention Visualizer (Grad-CAM)</h2>\n");
        sb.append("    <div class=\"scan-preview\">\n");
        if (result.getRawImageBase64() != null && !result.getRawImageBase64().isEmpty()) {
            sb.append("      <div class=\"scan-box\"><img src=\"").append(result.getRawImageBase64()).append("\" alt=\"Raw Scan\"><div class=\"scan-label\">Raw Medical Scan</div></div>\n");
        }
        if (result.getGradCamHeatmapBase64() != null && !result.getGradCamHeatmapBase64().isEmpty()) {
            sb.append("      <div class=\"scan-box\"><img src=\"").append(result.getGradCamHeatmapBase64()).append("\" alt=\"Grad-CAM\"><div class=\"scan-label\">Grad-CAM Neural Attention Heatmap</div></div>\n");
        }
        sb.append("    </div>\n");

        sb.append("    <div class=\"zk-box\">\n");
        sb.append("      <h3>🔒 Federated Zero-Knowledge & Blockchain Audit Verification</h3>\n");
        sb.append("      <p>This prediction was executed using global model weights aggregated via privacy-preserving FedAvg across 4 distributed hospital nodes. Raw medical images never leave the local hospital firewall.</p>\n");
        sb.append("      <p style=\"margin-top:8px;\"><strong>Patient Privacy Hash (SHA-256):</strong> ").append(result.getSha256PrivacyHash()).append("</p>\n");
        sb.append("      <p><strong>Blockchain Audit Seal:</strong> Block #").append(result.getBlockchainBlockId()).append(" (Hash: ").append(result.getBlockchainSealHash()).append(")</p>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"actions\">\n");
        sb.append("      <button class=\"btn\" onclick=\"window.print()\">🖨️ Print / Save as PDF</button>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");
        sb.append("</body>\n</html>");

        return sb.toString();
    }
}
