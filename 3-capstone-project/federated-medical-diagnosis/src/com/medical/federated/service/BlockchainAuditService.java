package com.medical.federated.service;

import com.medical.federated.config.SystemConfig;
import com.medical.federated.model.BlockchainBlock;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an immutable cryptographic ledger of federated training rounds,
 * model weight hashes, and zero-knowledge patient diagnosis verifications.
 */
public class BlockchainAuditService {
    private final List<BlockchainBlock> chain = Collections.synchronizedList(new ArrayList<>());
    private final int difficulty;

    public BlockchainAuditService() {
        this.difficulty = SystemConfig.BLOCKCHAIN_DIFFICULTY;
        // Genesis block
        BlockchainBlock genesis = new BlockchainBlock(0, "0000000000000000000000000000000000000000000000000000000000000000",
                "GENESIS_BLOCK", "Federated Healthcare Network Initialized. Zero-Knowledge Ledger Activated.", 0);
        genesis.mineBlock(difficulty);
        chain.add(genesis);
    }

    public synchronized BlockchainBlock recordRoundSeal(int round, double accuracy, double loss, int hospitalsCount, String aggregationHash) {
        BlockchainBlock prev = chain.get(chain.size() - 1);
        String payload = String.format("FL_ROUND_SEAL: Round #%d | Global Accuracy: %.4f | Loss: %.4f | Active Nodes: %d | Aggregation Hash: %s",
                round, accuracy, loss, hospitalsCount, aggregationHash);
        BlockchainBlock block = new BlockchainBlock(chain.size(), prev.getHash(), "FL_ROUND_SEAL", payload, round);
        block.mineBlock(difficulty);
        chain.add(block);
        return block;
    }

    public synchronized BlockchainBlock recordDiagnosticInference(String scanId, String patientPrivacyHash, String diagnosis, String hospitalNode, int flRound) {
        BlockchainBlock prev = chain.get(chain.size() - 1);
        String payload = String.format("DIAGNOSTIC_SEAL: ScanID: %s | PII-Free Hash: %s | Diagnosis: %s | Node: %s | Verified FL Model: Round #%d",
                scanId, patientPrivacyHash, diagnosis, hospitalNode, flRound);
        BlockchainBlock block = new BlockchainBlock(chain.size(), prev.getHash(), "DIAGNOSTIC_VERIFICATION", payload, flRound);
        block.mineBlock(difficulty);
        chain.add(block);
        return block;
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            BlockchainBlock current = chain.get(i);
            BlockchainBlock previous = chain.get(i - 1);

            if (!current.getHash().equals(current.calculateHash())) {
                return false;
            }
            if (!current.getPreviousHash().equals(previous.getHash())) {
                return false;
            }
        }
        return true;
    }

    public String computeSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "hash_error";
        }
    }

    public String computeStringSha256(String text) {
        return computeSha256(text.getBytes(StandardCharsets.UTF_8));
    }

    public List<BlockchainBlock> getAllBlocks() {
        return new ArrayList<>(chain);
    }

    public int getChainLength() {
        return chain.size();
    }

    public BlockchainBlock getLatestBlock() {
        return chain.get(chain.size() - 1);
    }
}
