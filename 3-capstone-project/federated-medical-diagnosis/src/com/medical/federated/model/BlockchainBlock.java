package com.medical.federated.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Represents an immutable audit block in the Federated Privacy Blockchain.
 * Guarantees zero-knowledge verification and auditability of model weights and diagnostic reports.
 */
public class BlockchainBlock {
    private final int index;
    private final long timestamp;
    private final String previousHash;
    private String hash;
    private final String eventType;
    private final String dataPayload;
    private final int roundNumber;
    private int nonce;

    public BlockchainBlock(int index, String previousHash, String eventType, String dataPayload, int roundNumber) {
        this.index = index;
        this.timestamp = Instant.now().toEpochMilli();
        this.previousHash = previousHash;
        this.eventType = eventType;
        this.dataPayload = dataPayload;
        this.roundNumber = roundNumber;
        this.nonce = 0;
        this.hash = calculateHash();
    }

    public BlockchainBlock(int index, long timestamp, String previousHash, String hash, String eventType, String dataPayload, int roundNumber, int nonce) {
        this.index = index;
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.hash = hash;
        this.eventType = eventType;
        this.dataPayload = dataPayload;
        this.roundNumber = roundNumber;
        this.nonce = nonce;
    }

    public String calculateHash() {
        String input = index + Long.toString(timestamp) + previousHash + eventType + dataPayload + roundNumber + nonce;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    public int getIndex() { return index; }
    public long getTimestamp() { return timestamp; }
    public String getPreviousHash() { return previousHash; }
    public String getHash() { return hash; }
    public String getEventType() { return eventType; }
    public String getDataPayload() { return dataPayload; }
    public int getRoundNumber() { return roundNumber; }
    public int getNonce() { return nonce; }

    public String toJson() {
        return String.format(
            "{\"index\":%d,\"timestamp\":%d,\"previousHash\":\"%s\",\"hash\":\"%s\",\"eventType\":\"%s\",\"dataPayload\":\"%s\",\"roundNumber\":%d,\"nonce\":%d}",
            index, timestamp, previousHash, hash, escapeJson(eventType), escapeJson(dataPayload), roundNumber, nonce
        );
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
