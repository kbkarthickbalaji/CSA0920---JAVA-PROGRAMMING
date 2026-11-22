# Java-Based Privacy-Preserving Federated Medical Image Diagnosis System

> **Capstone Project Implementation**  
> **Student Authors:** L Karthick balaji (192324216), R Jagan (192321102), R Santhosh (192321141)  
> **Guided by:** Vijayaragavan  
> **Based on Reference:** `JAVA CAPSTONE PROJECT .pptx`

---

## 📌 1. Project Overview & Architecture
This capstone project implements an end-to-end, privacy-preserving **Federated Learning (FL) system** designed for distributed multi-hospital medical diagnosis. 

### The Core Problem Solved:
Medical imaging datasets (MRI, Chest X-Rays, Dermatology scans) contain strictly sensitive patient information protected by HIPAA and GDPR. Centralizing raw medical records onto one server creates privacy, legal, and security liabilities. 

### The Solution:
Our system allows **4 independent hospital nodes** (North, South, East, West) to collaboratively train global diagnostic models without directly sharing their local patient images. Each hospital trains locally; only **encrypted model parameter updates (gradients & weights)** are transmitted to the central server, which aggregates them using **FedAvg (Federated Averaging)**.

```
       ┌────────────────────────────────────────────────────────┐
       │             CENTRAL FEDERATED AGGREGATOR               │
       │    - Global Model Initialization & Distribution        │
       │    - FedAvg Algorithm: W_global = Σ (n_k / N) * W_k    │
       │    - Proof-of-Federation Blockchain Audit Ledger       │
       │    - Embedded REST API Server & Central Monitoring     │
       └───────────────────────────┬────────────────────────────┘
                                   │ Model Updates (ΔW)
            ┌──────────────────────┼──────────────────────┐
            ▼                      ▼                      ▼
    ┌───────────────┐      ┌───────────────┐      ┌───────────────┐
    │ NORTH HOSPITAL│      │ SOUTH HOSPITAL│      │ EAST HOSPITAL │ ... [WEST HOSPITAL]
    │ - Brain MRI   │      │ - Chest X-Ray │      │ - Skin Lesion │
    │ - Local Data  │      │ - Local Data  │      │ - Local Data  │
    │ - Local Train │      │ - Local Train │      │ - Local Train │
    └───────────────┘      └───────────────┘      └───────────────┘
    (Raw Scans NEVER leave the hospital firewall - Zero PII Exposure)
```

---

## 🚀 2. Key Modules (Matching Presentation Slides)

### **Module 1: Hospital-Level Local Training (Slide 6 & 7)**
- Manages 4 hospital clients across geographic regions:
  1. **North Hospital**: Brain MRI & Neuro-oncology (1,420 confidential scans)
  2. **South Hospital**: Pulmonary & Chest X-Ray (2,150 confidential scans)
  3. **East Hospital**: Dermatology & Skin Lesions (1,840 confidential scans)
  4. **West Hospital**: Infectious Diseases & COVID-19 (1,290 confidential scans)
- Runs multi-threaded local CNN simulation, calculating epoch-by-epoch loss, accuracy, and parameter deltas.
- Includes toggle controls to simulate dynamic node participation and network dropouts.

### **Module 2: Federated Aggregation & FedAvg (Slide 8)**
- Implements the mathematical **FedAvg** algorithm:
  $$W_{global}^{(t+1)} = \sum_{k=1}^K \left(\frac{n_k}{N}\right) W_k^{(t+1)}$$
- Orchestrates communication rounds, computes global convergence, and distributes updated models back to clients.

### **Module 3: Medical Image Diagnosis & Grad-CAM (Slide 9 & 11)**
- Supports 4 diagnostic target endpoints:
  - `Brain MRI (/predict/brain)`: Glioma, Meningioma, Pituitary tumor detection.
  - `Chest X-ray (/predict/pneumonia)`: Bacterial/Viral Pneumonia detection.
  - `COVID X-ray (/predict/covid)`: COVID-19 Acute Respiratory Infiltration.
  - `Skin Lesion (/predict/skin)`: ISIC Vascular Lesion and Melanoma classification.
- **Explainable AI (Grad-CAM Neural Attention Visualizer)**:
  - Interactive split-view comparison slider comparing the **Raw Scan vs. AI Neural Attention Heatmap** (Matching Slide 11 image18).
- **Zero-Knowledge & Patient Privacy Seal**:
  - Computes SHA-256 patient privacy hash with Zero PII stored.
  - Mints an immutable block in the federated blockchain for every diagnostic prediction.

### **Module 4: Centralized Dashboard & Monitoring (Slide 10 & 13)**
- **Dual User Interface**:
  1. **High-Tech Web Dashboard**: Dark medical dashboard with real-time SVG convergence charts, hospital status cards, and live logs at `http://localhost:8080`.
  2. **Native Java Desktop GUI (Swing)**: Desktop window with hospital management, training triggers, and browser launcher.
- **Signed Clinical PDF / HTML Report**:
  - Downloadable "OPTIMUS AI MEDICAL REPORT" (matching Slide 12 image19).

---

## 💻 3. Prerequisites & Requirements
- **Java**: JDK 17 or JDK 21+ (`javac` and `java` available on PATH).
- **OS**: Windows, macOS, or Linux.
- **Dependencies**: **Zero third-party Maven/Gradle dependencies required!** Uses pure Java 21 standard library (`com.sun.net.httpserver`, `javax.swing`, `java.security`, `javax.imageio`, `java.awt`).

---

## 🏃 4. How to Run

### Method A: One-Click Execution (Windows)
Double-click `run.bat` or run:
```powershell
.\run.bat
```
*(Or in PowerShell: `.\run.ps1`)*

### Method B: Manual CLI Build & Run
```powershell
# 1. Compile Java sources
javac -d bin (Get-ChildItem -Path "src" -Filter *.java -Recurse).FullName

# 2. Run the application
java -cp bin com.medical.federated.Main
```

### Accessing the Web Dashboard:
Open your browser to:
👉 **`http://localhost:8080`**

---

## 📋 5. Project Directory Structure
```
federated-medical-diagnosis/
├── bin/                                  # Compiled Java bytecode
├── src/
│   └── com/medical/federated/
│       ├── Main.java                     # Application launcher
│       ├── config/SystemConfig.java      # Configuration parameters
│       ├── model/
│       │   ├── HospitalNode.java         # Hospital client entity
│       │   ├── ModelWeights.java         # FedAvg weight math & local training
│       │   ├── FederatedRound.java       # FL round execution record
│       │   ├── DiagnosticResult.java     # Diagnosis & Grad-CAM container
│       │   └── BlockchainBlock.java      # Cryptographic audit block
│       ├── service/
│       │   ├── HospitalManager.java      # 4 hospital nodes manager
│       │   ├── FederatedLearningService.java # Central FedAvg coordinator
│       │   ├── DiagnosisEngine.java      # Inference & Grad-CAM generator
│       │   ├── BlockchainAuditService.java # SHA-256 ledger & ZK verifier
│       │   └── ReportGeneratorService.java # Signed clinical report generator
│       ├── server/
│       │   ├── WebServer.java            # Embedded Java HTTP Server
│       │   └── ApiHandler.java           # REST API routes
│       └── gui/DesktopDashboard.java     # Java Swing Desktop GUI
├── web/                                  # Frontend UI assets
│   ├── index.html                        # Medical Dashboard UI
│   ├── css/styles.css                    # Dark cyberpunk medical styling
│   └── js/app.js                         # Dynamic charts & Grad-CAM slider
├── samples/                              # Authentic medical sample scans
│   ├── skin_vascular_lesion.png
│   ├── brain_mri_glioma.png
│   ├── chest_xray_pneumonia.png
│   └── covid19_xray.png
├── build.bat                             # Build script
├── run.bat                               # Double-click runner
├── run.ps1                               # PowerShell script
└── README.md                             # Documentation & Viva Guide
```

---

## 🎓 6. Capstone Presentation & Viva Defense Q&A

**Q1: How does this system preserve patient data privacy?**  
*Answer:* The raw medical images and patient information remain strictly behind each hospital's local firewall. Only model weights/gradients ($\Delta W$) are shared with the central server. Additionally, images are verified using SHA-256 hashes without storing any Personally Identifiable Information (Zero PII).

**Q2: What is the FedAvg algorithm and how is it implemented?**  
*Answer:* Federated Averaging calculates a weighted average of local model parameters proportional to each hospital's dataset size ($n_k / N$). This ensures hospitals with larger datasets contribute proportionally while benefiting smaller specialized centers.

**Q3: How does Explainable AI (Grad-CAM) work in this project?**  
*Answer:* Grad-CAM calculates activation maps from the neural network layers to visualize which regions of the medical scan (e.g., skin lesion border, lung opacity, brain tumor mass) most heavily influenced the AI's diagnostic classification.

**Q4: Why is Blockchain used in this medical AI system?**  
*Answer:* The blockchain provides an immutable, tamper-proof audit trail for every federated training round and diagnostic decision. Evaluators and clinical auditors can verify that a diagnosis was produced by an accredited federated model version without data tampering.
