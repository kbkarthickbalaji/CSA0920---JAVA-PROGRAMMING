package com.medical.federated.config;

/**
 * System Configuration constants for Java Federated Medical AI System.
 * Matches specifications from Capstone Presentation.
 */
public class SystemConfig {
    public static final String APP_TITLE = "Java-Based Privacy-Preserving Federated Medical Image Diagnosis System";
    public static final String APP_VERSION = "2.0-LTS";
    public static final int HTTP_PORT = 8080;
    
    // Participating Hospital Names
    public static final String[] DEFAULT_HOSPITALS = {
        "North Hospital",  // Brain MRI & Neuro-oncology
        "South Hospital",  // Pulmonary & Chest X-Ray
        "East Hospital",   // Dermatology & Skin Lesions
        "West Hospital"    // Infectious Diseases & COVID-19
    };
    
    // Model configuration
    public static final int MODEL_PARAM_SIZE = 128; // Simulated feature weight vector length
    public static final double INITIAL_ACCURACY = 0.7240;
    public static final double INITIAL_LOSS = 0.6850;
    public static final double LEARNING_RATE = 0.015;
    public static final int DEFAULT_LOCAL_EPOCHS = 5;
    
    // Privacy and Cryptography
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int BLOCKCHAIN_DIFFICULTY = 2; // Proof-of-Federation difficulty
}
