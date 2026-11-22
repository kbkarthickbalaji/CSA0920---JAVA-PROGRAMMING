package com.medical.federated;

import com.medical.federated.config.SystemConfig;
import com.medical.federated.gui.DesktopDashboard;
import com.medical.federated.server.WebServer;
import com.medical.federated.service.BlockchainAuditService;
import com.medical.federated.service.DiagnosisEngine;
import com.medical.federated.service.FederatedLearningService;
import com.medical.federated.service.HospitalManager;
import com.medical.federated.service.ReportGeneratorService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;

/**
 * Main application launcher for the Java Capstone Project:
 * "Java-Based Privacy-Preserving Federated Medical Image Diagnosis System"
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("********************************************************************************");
        System.out.println("  " + SystemConfig.APP_TITLE);
        System.out.println("  Version: " + SystemConfig.APP_VERSION);
        System.out.println("********************************************************************************");

        // 1. Resolve Project Root, Web and Sample Directories
        File projectDir = new File(".").getAbsoluteFile();
        if (projectDir.getName().equals(".")) {
            projectDir = projectDir.getParentFile();
        }

        File webDir = new File(projectDir, "web");
        File samplesDir = new File(projectDir, "samples");

        // Fallback for nested bin or src executions
        if (!webDir.exists()) {
            webDir = new File("D:\\JAVA PRG\\federated-medical-diagnosis\\web");
            samplesDir = new File("D:\\JAVA PRG\\federated-medical-diagnosis\\samples");
        }

        System.out.println("[INFO] Web root directory: " + webDir.getAbsolutePath());
        System.out.println("[INFO] Sample scans directory: " + samplesDir.getAbsolutePath());

        // 2. Initialize Core Services
        HospitalManager hospitalManager = new HospitalManager();
        BlockchainAuditService blockchainService = new BlockchainAuditService();
        FederatedLearningService flService = new FederatedLearningService(hospitalManager, blockchainService);
        DiagnosisEngine diagnosisEngine = new DiagnosisEngine(flService, blockchainService);
        ReportGeneratorService reportService = new ReportGeneratorService();

        // 3. Start Web Server
        WebServer webServer = null;
        try {
            webServer = new WebServer(SystemConfig.HTTP_PORT, hospitalManager, flService,
                    diagnosisEngine, blockchainService, reportService, webDir, samplesDir);
            webServer.start();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // 4. Launch Desktop GUI (Swing) if display environment is available
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        if (!isHeadless) {
            try {
                // Apply look and feel
                try {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception ignored) {}

                final int port = SystemConfig.HTTP_PORT;
                SwingUtilities.invokeLater(() -> {
                    DesktopDashboard gui = new DesktopDashboard(hospitalManager, flService, diagnosisEngine, blockchainService, port);
                    gui.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[INFO] Desktop GUI could not be initialized: " + e.getMessage());
            }
        } else {
            System.out.println("[INFO] Running in headless mode. Web dashboard active at http://localhost:" + SystemConfig.HTTP_PORT);
        }

        // 5. Try opening the web dashboard automatically
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + SystemConfig.HTTP_PORT));
            }
        } catch (Exception ignored) {}

        System.out.println("[READY] Application is running. Press Ctrl+C in terminal to stop.");
    }
}
