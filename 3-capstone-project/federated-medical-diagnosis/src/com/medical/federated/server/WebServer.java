package com.medical.federated.server;

import com.medical.federated.config.SystemConfig;
import com.medical.federated.service.BlockchainAuditService;
import com.medical.federated.service.DiagnosisEngine;
import com.medical.federated.service.FederatedLearningService;
import com.medical.federated.service.HospitalManager;
import com.medical.federated.service.ReportGeneratorService;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server hosting the Federated Learning REST API and Dashboard.
 */
public class WebServer {
    private final int port;
    private final HttpServer server;

    public WebServer(int port, HospitalManager hospitalManager, FederatedLearningService flService,
                     DiagnosisEngine diagnosisEngine, BlockchainAuditService blockchainService,
                     ReportGeneratorService reportService, File webDir, File samplesDir) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        ApiHandler handler = new ApiHandler(hospitalManager, flService, diagnosisEngine, blockchainService, reportService, webDir, samplesDir);
        this.server.createContext("/", handler);
        this.server.setExecutor(Executors.newCachedThreadPool());
    }

    public void start() {
        server.start();
        System.out.println("================================================================================");
        System.out.println(" 🏥 Federated Medical AI Platform Server running at: http://localhost:" + port);
        System.out.println("================================================================================");
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return port;
    }
}
