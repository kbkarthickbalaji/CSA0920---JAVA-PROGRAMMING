# 3. Capstone Project (CSA0920)

## Java-Based Privacy-Preserving Federated Medical Image Diagnosis System

This folder contains the complete Capstone Project source code, execution scripts, sample scans, and presentation slides.

---

### Directory Contents:
* 📊 **[`JAVA CAPSTONE PROJECT .pptx`](./JAVA%20CAPSTONE%20PROJECT%20.pptx)**: Project Presentation Slides (19 slides).
* 📁 **[`federated-medical-diagnosis/`](./federated-medical-diagnosis/)**: Complete Java implementation:
  * **Core Modules:**
    1. Hospital-Level Local Training (4 nodes: North, South, East, West)
    2. Federated Aggregation & FedAvg Algorithm ($W_{global} = \sum \frac{n_k}{N} W_k$)
    3. Medical Image Diagnosis with Grad-CAM Neural Attention Visualizer
    4. Centralized Monitoring & Dual Interface (Web Dashboard + Desktop Swing GUI)
  * **Security & Privacy:** SHA-256 Patient Privacy Hashing (Zero PII) & Blockchain Audit Ledger.
  * **Sample Scans:** Authentic pre-loaded scans for Skin Lesions, Brain MRI, Chest X-Ray, and COVID-19.

---

### How to Run the Capstone Project:
Navigate to the project folder and double-click `run.bat`:
```powershell
cd "3-capstone-project\federated-medical-diagnosis"
.\run.bat
```
Then open your browser to **`http://localhost:8080`**.
