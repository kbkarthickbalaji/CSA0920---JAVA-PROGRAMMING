package com.medical.federated.server;

import com.medical.federated.model.BlockchainBlock;
import com.medical.federated.model.DiagnosticResult;
import com.medical.federated.model.FederatedRound;
import com.medical.federated.model.HospitalNode;
import com.medical.federated.service.BlockchainAuditService;
import com.medical.federated.service.DiagnosisEngine;
import com.medical.federated.service.FederatedLearningService;
import com.medical.federated.service.HospitalManager;
import com.medical.federated.service.ReportGeneratorService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Handles all REST API requests and serves static assets for the web dashboard.
 */
public class ApiHandler implements HttpHandler {
    private final HospitalManager hospitalManager;
    private final FederatedLearningService flService;
    private final DiagnosisEngine diagnosisEngine;
    private final BlockchainAuditService blockchainService;
    private final ReportGeneratorService reportService;
    private final File webDir;
    private final File samplesDir;
    private DiagnosticResult lastResult;

    public ApiHandler(HospitalManager hospitalManager, FederatedLearningService flService,
                      DiagnosisEngine diagnosisEngine, BlockchainAuditService blockchainService,
                      ReportGeneratorService reportService, File webDir, File samplesDir) {
        this.hospitalManager = hospitalManager;
        this.flService = flService;
        this.diagnosisEngine = diagnosisEngine;
        this.blockchainService = blockchainService;
        this.reportService = reportService;
        this.webDir = webDir;
        this.samplesDir = samplesDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Enable CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if (path.startsWith("/api/")) {
                handleApi(exchange, path, method);
            } else {
                handleStatic(exchange, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    private void handleApi(HttpExchange exchange, String path, String method) throws IOException {
        switch (path) {
            case "/api/status":
                if ("GET".equalsIgnoreCase(method)) {
                    double acc = flService.getGlobalWeights().getAccuracy();
                    double loss = flService.getGlobalWeights().getLoss();
                    int round = flService.getCurrentRound();
                    boolean training = flService.isTrainingInProgress();
                    int activeHosp = hospitalManager.getActiveHospitals().size();
                    int totalHosp = hospitalManager.getAllHospitals().size();
                    int totalScans = hospitalManager.getTotalScansAcrossNetwork();
                    int chainLen = blockchainService.getChainLength();

                    String json = String.format(
                        "{\"currentRound\":%d,\"globalAccuracy\":%.4f,\"globalLoss\":%.4f,\"isTraining\":%b,\"activeHospitals\":%d,\"totalHospitals\":%d,\"totalScans\":%d,\"blockchainBlocks\":%d}",
                        round, acc, loss, training, activeHosp, totalHosp, totalScans, chainLen
                    );
                    sendJsonResponse(exchange, 200, json);
                }
                break;

            case "/api/hospitals":
                if ("GET".equalsIgnoreCase(method)) {
                    List<HospitalNode> list = hospitalManager.getAllHospitals();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        sb.append(list.get(i).toJson());
                        if (i < list.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                }
                break;

            case "/api/hospitals/toggle":
                if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    String id = extractJsonString(body, "id");
                    boolean active = body.contains("\"active\":true") || body.contains("\"active\": true");
                    boolean success = hospitalManager.toggleHospital(id, active);
                    sendJsonResponse(exchange, 200, "{\"success\":" + success + "}");
                }
                break;

            case "/api/train/round":
                if ("POST".equalsIgnoreCase(method)) {
                    if (flService.isTrainingInProgress()) {
                        sendJsonResponse(exchange, 400, "{\"error\":\"Training already in progress\"}");
                        return;
                    }
                    new Thread(() -> {
                        try {
                            flService.executeFederatedRound(5);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    sendJsonResponse(exchange, 200, "{\"message\":\"Federated round initiated\"}");
                }
                break;

            case "/api/train/reset":
                if ("POST".equalsIgnoreCase(method)) {
                    flService.resetTraining();
                    sendJsonResponse(exchange, 200, "{\"message\":\"Training state reset\"}");
                }
                break;

            case "/api/logs":
                if ("GET".equalsIgnoreCase(method)) {
                    List<String> logs = flService.getSystemLogs();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < logs.size(); i++) {
                        sb.append("\"").append(escapeJson(logs.get(i))).append("\"");
                        if (i < logs.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                }
                break;

            case "/api/rounds":
                if ("GET".equalsIgnoreCase(method)) {
                    List<FederatedRound> rounds = flService.getRoundHistory();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < rounds.size(); i++) {
                        sb.append(rounds.get(i).toJson());
                        if (i < rounds.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                }
                break;

            case "/api/blockchain":
                if ("GET".equalsIgnoreCase(method)) {
                    List<BlockchainBlock> blocks = blockchainService.getAllBlocks();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < blocks.size(); i++) {
                        sb.append(blocks.get(i).toJson());
                        if (i < blocks.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                }
                break;

            case "/api/diagnose":
                if ("POST".equalsIgnoreCase(method)) {
                    String body = readRequestBody(exchange);
                    String base64Data = extractJsonString(body, "image");
                    String filename = extractJsonString(body, "filename");
                    String endpoint = extractJsonString(body, "endpoint");
                    String sampleName = extractJsonString(body, "sample");

                    byte[] imageBytes = null;
                    if (sampleName != null && !sampleName.isEmpty()) {
                        File sampleFile = new File(samplesDir, sampleName);
                        if (sampleFile.exists()) {
                            imageBytes = Files.readAllBytes(sampleFile.toPath());
                            if (filename == null || filename.isEmpty()) filename = sampleName;
                        }
                    }

                    if (imageBytes == null && base64Data != null && !base64Data.isEmpty()) {
                        if (base64Data.contains(",")) {
                            base64Data = base64Data.split(",")[1];
                        }
                        imageBytes = Base64.getDecoder().decode(base64Data);
                    }

                    if (imageBytes == null || imageBytes.length == 0) {
                        sendJsonResponse(exchange, 400, "{\"error\":\"No image data provided\"}");
                        return;
                    }

                    DiagnosticResult result = diagnosisEngine.diagnoseImage(imageBytes, filename, endpoint);
                    this.lastResult = result;
                    sendJsonResponse(exchange, 200, result.toJson());
                }
                break;

            case "/api/report":
                if ("GET".equalsIgnoreCase(method)) {
                    if (lastResult == null) {
                        sendHtmlResponse(exchange, 200, "<h3>No diagnostic report available yet. Please run a diagnosis first.</h3>");
                        return;
                    }
                    String html = reportService.generateHtmlReport(lastResult);
                    sendHtmlResponse(exchange, 200, html);
                }
                break;

            case "/api/samples":
                if ("GET".equalsIgnoreCase(method)) {
                    File[] files = samplesDir.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
                    StringBuilder sb = new StringBuilder("[");
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            sb.append("{\"name\":\"").append(files[i].getName()).append("\",\"size\":").append(files[i].length()).append("}");
                            if (i < files.length - 1) sb.append(",");
                        }
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                }
                break;

            default:
                sendJsonResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    private void handleStatic(HttpExchange exchange, String path) throws IOException {
        if ("/".equals(path) || path.isEmpty()) {
            path = "/index.html";
        }

        File file;
        if (path.startsWith("/samples/")) {
            file = new File(samplesDir, path.substring("/samples/".length()));
        } else {
            file = new File(webDir, path.startsWith("/") ? path.substring(1) : path);
        }

        if (!file.exists() || file.isDirectory()) {
            sendJsonResponse(exchange, 404, "{\"error\":\"File not found: " + path + "\"}");
            return;
        }

        String mime = getMimeType(file.getName());
        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getMimeType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendHtmlResponse(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"";
        int start = json.indexOf(pattern);
        if (start == -1) {
            pattern = "\"" + key + "\":\"";
            start = json.indexOf(pattern);
        }
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
