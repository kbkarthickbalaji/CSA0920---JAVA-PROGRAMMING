package com.medical.federated.gui;

import com.medical.federated.config.SystemConfig;
import com.medical.federated.model.HospitalNode;
import com.medical.federated.service.BlockchainAuditService;
import com.medical.federated.service.DiagnosisEngine;
import com.medical.federated.service.FederatedLearningService;
import com.medical.federated.service.HospitalManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Native Java Desktop GUI Dashboard (Swing).
 * Allows hospital management, training triggers, and direct launch into the browser dashboard.
 */
public class DesktopDashboard extends JFrame {
    private final HospitalManager hospitalManager;
    private final FederatedLearningService flService;
    private final DiagnosisEngine diagnosisEngine;
    private final BlockchainAuditService blockchainService;
    private final int port;

    private JTable hospitalTable;
    private DefaultTableModel tableModel;
    private JTextArea logTextArea;
    private JLabel statusLabel;
    private JLabel roundLabel;
    private JLabel accuracyLabel;
    private JButton trainBtn;
    private Timer refreshTimer;

    public DesktopDashboard(HospitalManager hospitalManager, FederatedLearningService flService,
                            DiagnosisEngine diagnosisEngine, BlockchainAuditService blockchainService, int port) {
        this.hospitalManager = hospitalManager;
        this.flService = flService;
        this.diagnosisEngine = diagnosisEngine;
        this.blockchainService = blockchainService;
        this.port = port;

        initUI();
    }

    private void initUI() {
        setTitle(SystemConfig.APP_TITLE + " [Desktop Control Panel]");
        setSize(980, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(15, 23, 42)); // Dark theme

        // 1. Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(2, 6, 23));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(6, 182, 212), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel("🏥 Privacy-Preserving Federated Medical AI Platform");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(56, 189, 248));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        roundLabel = new JLabel("Round: 0");
        roundLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roundLabel.setForeground(Color.WHITE);

        accuracyLabel = new JLabel("Global Acc: 72.4%");
        accuracyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        accuracyLabel.setForeground(new Color(52, 211, 153));

        statsPanel.add(roundLabel);
        statsPanel.add(accuracyLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center: Hospital Table and Log Console
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setOpaque(false);

        // Hospital Table
        String[] columns = {"Node ID", "Hospital Name", "Medical Specialty", "Scans", "Status", "Local Acc", "Local Loss"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        hospitalTable = new JTable(tableModel);
        hospitalTable.setRowHeight(26);
        hospitalTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hospitalTable.setBackground(new Color(30, 41, 59));
        hospitalTable.setForeground(Color.WHITE);
        hospitalTable.getTableHeader().setBackground(new Color(15, 23, 42));
        hospitalTable.getTableHeader().setForeground(new Color(148, 163, 184));
        hospitalTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane tableScroll = new JScrollPane(hospitalTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                "Participating Hospital Nodes (Module 1 & 4)",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(148, 163, 184)
        ));
        tableScroll.getViewport().setBackground(new Color(30, 41, 59));
        centerPanel.add(tableScroll);

        // Log Console
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(new Color(2, 6, 23));
        logTextArea.setForeground(new Color(74, 222, 128)); // Terminal green
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logTextArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85)),
                "Live Multi-Hospital Federated Logs (FedAvg Server)",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(148, 163, 184)
        ));
        centerPanel.add(logScroll);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Controls Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomPanel.setBackground(new Color(15, 23, 42));

        statusLabel = new JLabel("Ready. Web Server running on port " + port);
        statusLabel.setForeground(new Color(148, 163, 184));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        trainBtn = new JButton("⚡ Run Federated Round (FedAvg)");
        trainBtn.setBackground(new Color(14, 165, 233));
        trainBtn.setForeground(Color.WHITE);
        trainBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        trainBtn.setFocusPainted(false);
        trainBtn.addActionListener(e -> triggerFederatedRound());

        JButton webBtn = new JButton("🌐 Open Full Web Dashboard");
        webBtn.setBackground(new Color(16, 185, 129));
        webBtn.setForeground(Color.WHITE);
        webBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        webBtn.setFocusPainted(false);
        webBtn.addActionListener(e -> openWebDashboard());

        JButton resetBtn = new JButton("🔄 Reset");
        resetBtn.setBackground(new Color(239, 68, 68));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> {
            flService.resetTraining();
            updateDashboardData();
        });

        bottomPanel.add(statusLabel);
        bottomPanel.add(resetBtn);
        bottomPanel.add(trainBtn);
        bottomPanel.add(webBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Setup timer to refresh data every 1000ms
        refreshTimer = new Timer(1000, e -> updateDashboardData());
        refreshTimer.start();
        updateDashboardData();
    }

    private void triggerFederatedRound() {
        if (flService.isTrainingInProgress()) {
            JOptionPane.showMessageDialog(this, "Training is currently in progress!", "Please wait", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        trainBtn.setEnabled(false);
        trainBtn.setText("⏳ Training in progress...");

        new Thread(() -> {
            try {
                flService.executeFederatedRound(5);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Training Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                );
            } finally {
                SwingUtilities.invokeLater(() -> {
                    trainBtn.setEnabled(true);
                    trainBtn.setText("⚡ Run Federated Round (FedAvg)");
                    updateDashboardData();
                });
            }
        }).start();
    }

    private void openWebDashboard() {
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:" + port));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open browser automatically.\nPlease visit: http://localhost:" + port,
                    "Open Dashboard", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void updateDashboardData() {
        roundLabel.setText("Round: " + flService.getCurrentRound());
        accuracyLabel.setText(String.format("Global Acc: %.2f%%", flService.getGlobalWeights().getAccuracy() * 100.0));

        // Update table
        tableModel.setRowCount(0);
        List<HospitalNode> hospitals = hospitalManager.getAllHospitals();
        for (HospitalNode h : hospitals) {
            tableModel.addRow(new Object[]{
                    h.getId(),
                    h.getName(),
                    h.getSpecialty(),
                    h.getDatasetSize(),
                    h.getStatus(),
                    String.format("%.2f%%", h.getLocalAccuracy() * 100.0),
                    String.format("%.4f", h.getLocalLoss())
            });
        }

        // Update logs
        List<String> logs = flService.getSystemLogs();
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, logs.size() - 25);
        for (int i = start; i < logs.size(); i++) {
            sb.append(logs.get(i)).append("\n");
        }
        logTextArea.setText(sb.toString());
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }
}
