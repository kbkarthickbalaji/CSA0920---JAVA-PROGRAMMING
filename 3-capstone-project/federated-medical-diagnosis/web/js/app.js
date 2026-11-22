// Main Frontend Application Logic for Federated Medical Diagnosis System

let currentEndpoint = "/predict/skin";
let selectedSample = "skin_vascular_lesion.png";
let uploadedBase64 = null;
let lastDiagnosticResult = null;
let isAutoTraining = false;

document.addEventListener("DOMContentLoaded", () => {
  setupTabs();
  setupEndpoints();
  setupGradCamSlider();
  setupEventListeners();
  loadSamples();
  
  // Initial data load and continuous polling
  refreshAllData();
  setInterval(refreshAllData, 2000);
});

// Tab Navigation
function setupTabs() {
  document.querySelectorAll(".tab-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
      document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));
      
      btn.classList.add("active");
      const targetPane = document.getElementById(btn.dataset.tab);
      if (targetPane) targetPane.classList.add("active");
    });
  });
}

// Model Endpoint Chips
function setupEndpoints() {
  document.querySelectorAll(".endpoint-chip").forEach(chip => {
    chip.addEventListener("click", () => {
      document.querySelectorAll(".endpoint-chip").forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      currentEndpoint = chip.dataset.endpoint;
      
      // Auto-select corresponding sample if available
      if (currentEndpoint.includes("skin")) selectSample("skin_vascular_lesion.png");
      else if (currentEndpoint.includes("brain")) selectSample("brain_mri_glioma.png");
      else if (currentEndpoint.includes("pneumonia")) selectSample("chest_xray_pneumonia.png");
      else if (currentEndpoint.includes("covid")) selectSample("covid19_xray.png");
    });
  });
}

// Event Listeners
function setupEventListeners() {
  document.getElementById("btnRunRound").addEventListener("click", triggerSingleRound);
  document.getElementById("btnAutoTrain").addEventListener("click", triggerAutoTrain);
  document.getElementById("btnResetTrain").addEventListener("click", triggerReset);
  document.getElementById("btnRunInference").addEventListener("click", runDiagnosticInference);
  
  document.getElementById("fileUploadInput").addEventListener("change", handleFileUpload);
  document.getElementById("btnExportPdfDirect").addEventListener("click", openReportModal);
  document.getElementById("btnOpenReportModal").addEventListener("click", openReportModal);
  document.getElementById("modalCloseBtn").addEventListener("click", () => {
    document.getElementById("reportModal").style.display = "none";
  });
  
  window.addEventListener("click", (e) => {
    const modal = document.getElementById("reportModal");
    if (e.target === modal) modal.style.display = "none";
  });
}

// Load Sample Scans
async function loadSamples() {
  try {
    const res = await fetch("/api/samples");
    const samples = await res.json();
    const container = document.getElementById("samplesContainer");
    container.innerHTML = "";
    
    samples.forEach(s => {
      const thumb = document.createElement("div");
      thumb.className = `sample-thumbnail ${s.name === selectedSample ? "active" : ""}`;
      thumb.id = `sample-${s.name.replace(".", "_")}`;
      thumb.innerHTML = `
        <img src="/samples/${s.name}" alt="${s.name}">
        <div class="sample-label">${s.name.replace(/_/g, " ").replace(".png", "")}</div>
      `;
      thumb.addEventListener("click", () => selectSample(s.name));
      container.appendChild(thumb);
    });
  } catch (err) {
    console.error("Failed to load sample scans:", err);
  }
}

function selectSample(name) {
  selectedSample = name;
  uploadedBase64 = null;
  document.querySelectorAll(".sample-thumbnail").forEach(t => t.classList.remove("active"));
  const activeEl = document.getElementById(`sample-${name.replace(".", "_")}`);
  if (activeEl) activeEl.classList.add("active");
  document.getElementById("selectedFileName").textContent = `Selected: ${name}`;

  // Automatically update chip selection
  if (name.includes("skin")) setEndpointChip("/predict/skin");
  else if (name.includes("brain")) setEndpointChip("/predict/brain");
  else if (name.includes("chest") || name.includes("pneumonia")) setEndpointChip("/predict/pneumonia");
  else if (name.includes("covid")) setEndpointChip("/predict/covid");
}

function setEndpointChip(ep) {
  currentEndpoint = ep;
  document.querySelectorAll(".endpoint-chip").forEach(c => {
    c.classList.toggle("active", c.dataset.endpoint === ep);
  });
}

function handleFileUpload(e) {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (event) => {
    uploadedBase64 = event.target.result;
    selectedSample = null;
    document.querySelectorAll(".sample-thumbnail").forEach(t => t.classList.remove("active"));
    document.getElementById("selectedFileName").textContent = `Uploaded: ${file.name}`;
  };
  reader.readAsDataURL(file);
}

// Refresh Data from Java Server
async function refreshAllData() {
  try {
    const [statusRes, hospRes, roundsRes, logsRes, bcRes] = await Promise.all([
      fetch("/api/status"),
      fetch("/api/hospitals"),
      fetch("/api/rounds"),
      fetch("/api/logs"),
      fetch("/api/blockchain")
    ]);

    const status = await statusRes.json();
    const hospitals = await hospRes.json();
    const rounds = await roundsRes.json();
    const logs = await logsRes.json();
    const blocks = await bcRes.json();

    updateStatusNavbar(status);
    renderHospitals(hospitals);
    renderTerminalLogs(logs);
    renderRoundsTable(rounds);
    renderBlockchainTable(blocks);
    renderConvergenceChart(rounds, status);

  } catch (err) {
    console.warn("Polling server error:", err);
  }
}

function updateStatusNavbar(status) {
  document.getElementById("navRound").textContent = status.currentRound;
  document.getElementById("navAccuracy").textContent = (status.globalAccuracy * 100).toFixed(1) + "%";
  document.getElementById("navLoss").textContent = status.globalLoss.toFixed(4);
  document.getElementById("activeNodesCount").textContent = `${status.activeHospitals} / ${status.totalHospitals} Nodes`;
  document.getElementById("totalScansCount").textContent = `${status.totalScans.toLocaleString()} Scans`;
  document.getElementById("blockchainCount").textContent = `${status.blockchainBlocks} Blocks`;

  document.getElementById("flGlobalAccLabel").textContent = (status.globalAccuracy * 100).toFixed(2) + "%";
  document.getElementById("flGlobalAccBar").style.width = Math.min(100, status.globalAccuracy * 100) + "%";
  document.getElementById("flGlobalLossLabel").textContent = status.globalLoss.toFixed(4);
  document.getElementById("flGlobalLossBar").style.width = Math.min(100, (status.globalLoss / 1.0) * 100) + "%";

  const btnRound = document.getElementById("btnRunRound");
  if (status.isTraining) {
    btnRound.disabled = true;
    btnRound.textContent = "⏳ Training in progress...";
    document.getElementById("trainingStatusText").textContent = "Status: Federated training round executing...";
    document.getElementById("trainingStatusText").style.color = "var(--accent-amber)";
  } else {
    btnRound.disabled = false;
    btnRound.textContent = "⚡ Run Single Federated Round";
    document.getElementById("trainingStatusText").textContent = "Status: Ready for training round";
    document.getElementById("trainingStatusText").style.color = "var(--text-muted)";
  }
}

function renderHospitals(hospitals) {
  const container = document.getElementById("hospitalCardsContainer");
  container.innerHTML = "";

  hospitals.forEach(h => {
    const card = document.createElement("div");
    card.className = "card";
    
    let badgeClass = "badge-idle";
    if (h.status === "Training" || h.status === "Uploading Updates") badgeClass = "badge-training";
    else if (!h.active || h.status === "Offline") badgeClass = "badge-offline";

    card.innerHTML = `
      <div class="card-header">
        <div>
          <div class="card-title">${h.name}</div>
          <div class="card-subtitle">${h.specialty}</div>
        </div>
        <span class="hospital-badge ${badgeClass}">${h.status}</span>
      </div>

      <div class="meter-group">
        <div class="meter-label">
          <span>Local Model Accuracy</span>
          <strong>${(h.localAccuracy * 100).toFixed(2)}%</strong>
        </div>
        <div class="meter-bar">
          <div class="meter-fill emerald" style="width: ${h.localAccuracy * 100}%;"></div>
        </div>
      </div>

      <div class="meter-group">
        <div class="meter-label">
          <span>Local Loss</span>
          <strong>${h.localLoss.toFixed(4)}</strong>
        </div>
        <div class="meter-bar">
          <div class="meter-fill" style="width: ${Math.min(100, (h.localLoss / 0.8) * 100)}%; background: var(--accent-rose);"></div>
        </div>
      </div>

      <div class="card-footer">
        <span>Scans: <strong>${h.datasetSize.toLocaleString()}</strong></span>
        <label class="switch">
          <input type="checkbox" ${h.active ? "checked" : ""} onchange="toggleHospital('${h.id}', this.checked)">
          <span class="slider"></span>
        </label>
      </div>
    `;
    container.appendChild(card);
  });
}

async function toggleHospital(id, active) {
  try {
    await fetch("/api/hospitals/toggle", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id, active })
    });
    refreshAllData();
  } catch (err) {
    console.error("Failed to toggle hospital node:", err);
  }
}

function renderTerminalLogs(logs) {
  const term1 = document.getElementById("dashboardTerminal");
  const term2 = document.getElementById("trainingTerminal");
  
  const html = logs.map(l => {
    let cls = "term-line";
    if (l.includes("Federated") || l.includes("FedAvg")) cls += " fedavg";
    else if (l.includes("Hospital")) cls += " node";
    else if (l.includes("Blockchain") || l.includes("Seal")) cls += " info";
    return `<div class="${cls}">${escapeHtml(l)}</div>`;
  }).join("");

  term1.innerHTML = html;
  term2.innerHTML = html;
  term1.scrollTop = term1.scrollHeight;
  term2.scrollTop = term2.scrollHeight;
}

function renderRoundsTable(rounds) {
  const tbody = document.getElementById("roundsTableBody");
  if (!rounds || rounds.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-dim);">No federated rounds executed yet. Click 'Run Single Federated Round' to begin.</td></tr>`;
    return;
  }

  tbody.innerHTML = rounds.slice().reverse().map(r => `
    <tr>
      <td><strong>Round #${r.roundNumber}</strong></td>
      <td>${r.participatingHospitals.join(", ")}</td>
      <td>${r.totalSamplesTrained.toLocaleString()}</td>
      <td style="color: var(--accent-emerald); font-weight: 600;">${(r.globalAccuracy * 100).toFixed(2)}%</td>
      <td style="color: var(--accent-rose);">${r.globalLoss.toFixed(4)}</td>
      <td style="font-family: monospace; font-size: 11px; color: var(--accent-cyan);">${r.aggregationHash.substring(0, 16)}...</td>
      <td><span class="hospital-badge badge-idle">${r.status}</span></td>
    </tr>
  `).join("");
}

function renderBlockchainTable(blocks) {
  const tbody = document.getElementById("blockchainTableBody");
  if (!blocks || blocks.length === 0) return;

  tbody.innerHTML = blocks.slice().reverse().map(b => `
    <tr>
      <td><strong>Block #${b.index}</strong></td>
      <td>${new Date(b.timestamp).toLocaleTimeString()}</td>
      <td><span class="hospital-badge badge-idle">${b.eventType}</span></td>
      <td style="max-width: 320px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${escapeHtml(b.dataPayload)}</td>
      <td style="font-family: monospace; font-size: 11px; color: var(--accent-emerald);">${b.hash.substring(0, 16)}...</td>
      <td style="font-family: monospace; font-size: 11px; color: var(--text-dim);">${b.previousHash.substring(0, 12)}...</td>
    </tr>
  `).join("");
}

// Render SVG Convergence Chart
function renderConvergenceChart(rounds, status) {
  const svg = document.getElementById("convergenceChartSvg");
  const w = svg.clientWidth || 500;
  const h = 220;
  const padding = 35;

  let points = [{ round: 0, acc: 0.724, loss: 0.685 }];
  if (rounds && rounds.length > 0) {
    rounds.forEach(r => points.push({ round: r.roundNumber, acc: r.globalAccuracy, loss: r.globalLoss }));
  }

  const maxRound = Math.max(5, points.length - 1);
  const getX = (round) => padding + (round / maxRound) * (w - 2 * padding);
  const getYAcc = (acc) => h - padding - ((acc - 0.65) / 0.35) * (h - 2 * padding);
  const getYLoss = (loss) => h - padding - (loss / 0.8) * (h - 2 * padding);

  let accPath = "";
  let lossPath = "";

  points.forEach((p, i) => {
    const x = getX(p.round);
    const yAcc = getYAcc(p.acc);
    const yLoss = getYLoss(p.loss);
    if (i === 0) {
      accPath += `M ${x} ${yAcc}`;
      lossPath += `M ${x} ${yLoss}`;
    } else {
      accPath += ` L ${x} ${yAcc}`;
      lossPath += ` L ${x} ${yLoss}`;
    }
  });

  svg.innerHTML = `
    <!-- Grid Lines -->
    <line x1="${padding}" y1="${h - padding}" x2="${w - padding}" y2="${h - padding}" stroke="#334155" stroke-width="1"/>
    <line x1="${padding}" y1="${padding}" x2="${padding}" y2="${h - padding}" stroke="#334155" stroke-width="1"/>

    <!-- Accuracy Line (Emerald) -->
    <path d="${accPath}" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round"/>
    
    <!-- Loss Line (Rose) -->
    <path d="${lossPath}" fill="none" stroke="#f43f5e" stroke-width="2" stroke-dasharray="4 4" stroke-linecap="round"/>

    <!-- Points -->
    ${points.map(p => `
      <circle cx="${getX(p.round)}" cy="${getYAcc(p.acc)}" r="4" fill="#10b981"/>
      <circle cx="${getX(p.round)}" cy="${getYLoss(p.loss)}" r="3" fill="#f43f5e"/>
    `).join("")}

    <!-- Axis Labels -->
    <text x="${w - padding}" y="${h - 10}" fill="#94a3b8" font-size="10" text-anchor="end">Rounds</text>
    <text x="${padding + 5}" y="${padding + 10}" fill="#10b981" font-size="11" font-weight="600">Accuracy (—)</text>
    <text x="${padding + 90}" y="${padding + 10}" fill="#f43f5e" font-size="11">Loss (---)</text>
  `;
}

// Training Triggers
async function triggerSingleRound() {
  try {
    await fetch("/api/train/round", { method: "POST" });
    refreshAllData();
  } catch (err) {
    console.error("Round trigger failed:", err);
  }
}

async function triggerAutoTrain() {
  if (isAutoTraining) return;
  isAutoTraining = true;
  const btn = document.getElementById("btnAutoTrain");
  btn.disabled = true;
  btn.textContent = "⏳ Running Auto-Rounds...";

  for (let i = 0; i < 5; i++) {
    await triggerSingleRound();
    await new Promise(r => setTimeout(r, 2500));
  }

  isAutoTraining = false;
  btn.disabled = false;
  btn.textContent = "▶️ Run 5 Auto-Rounds";
}

async function triggerReset() {
  if (confirm("Reset Federated Learning model back to Round 0?")) {
    await fetch("/api/train/reset", { method: "POST" });
    refreshAllData();
  }
}

// Diagnostic Inference (Module 3)
async function runDiagnosticInference() {
  const btn = document.getElementById("btnRunInference");
  btn.disabled = true;
  btn.textContent = "🔬 RUNNING TENSORFLOW INFERENCE...";

  try {
    const payload = {
      endpoint: currentEndpoint,
      filename: uploadedBase64 ? "uploaded_scan.png" : selectedSample,
      sample: selectedSample,
      image: uploadedBase64
    };

    const res = await fetch("/api/diagnose", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const result = await res.json();
    lastDiagnosticResult = result;
    displayDiagnosticResult(result);

  } catch (err) {
    alert("Inference execution error: " + err.message);
  } finally {
    btn.disabled = false;
    btn.textContent = "✨ RUN TENSORFLOW / FEDERATED INFERENCE";
  }
}

function displayDiagnosticResult(res) {
  const section = document.getElementById("diagnosisResultSection");
  section.style.display = "block";
  section.scrollIntoView({ behavior: "smooth" });

  document.getElementById("resPrimaryDisease").textContent = res.primaryClassification;
  document.getElementById("resDiagnosticBadge").textContent = res.diagnosticStatus;
  document.getElementById("resConfidence").textContent = (res.confidenceScore * 100).toFixed(1) + "%";
  document.getElementById("resLatency").textContent = res.predictionSpeedSec.toFixed(2) + " sec";
  document.getElementById("resPrivacyHash").textContent = res.sha256PrivacyHash;
  document.getElementById("resHospitalNode").textContent = res.hospitalNode;
  document.getElementById("resFlRound").textContent = `Round #${res.flRound}`;
  document.getElementById("resBlockHash").textContent = res.blockchainSealHash;

  // Render Pathologies Bars
  const barsContainer = document.getElementById("pathologyBarsContainer");
  barsContainer.innerHTML = "";
  for (const [disease, prob] of Object.entries(res.pathologicalProbabilities)) {
    const pct = (prob * 100).toFixed(1);
    const barEl = document.createElement("div");
    barEl.style.marginBottom = "10px";
    barEl.innerHTML = `
      <div style="display:flex; justify-content:space-between; font-size:12px; margin-bottom:4px;">
        <span>${disease}</span>
        <strong>${pct}%</strong>
      </div>
      <div class="meter-bar">
        <div class="meter-fill" style="width: ${pct}%; background: ${prob > 0.5 ? 'linear-gradient(90deg, #f43f5e, #fb7185)' : 'linear-gradient(90deg, #0284c7, #06b6d4)'};"></div>
      </div>
    `;
    barsContainer.appendChild(barEl);
  }

  // Update Grad-CAM Images
  document.getElementById("gradCamRawImg").src = res.rawImageBase64;
  document.getElementById("gradCamHeatmapImg").src = res.gradCamHeatmapBase64;

  // Reset slider position to 50%
  setSliderSplit(50);
}

// Grad-CAM Split Slider Handler
function setupGradCamSlider() {
  const container = document.getElementById("gradCamBox");
  const slider = document.getElementById("gradCamSlider");

  let isDragging = false;

  const updatePosition = (clientX) => {
    const rect = container.getBoundingClientRect();
    let x = clientX - rect.left;
    let pct = (x / rect.width) * 100;
    pct = Math.max(0, Math.min(100, pct));
    setSliderSplit(pct);
  };

  container.addEventListener("mousedown", (e) => {
    isDragging = true;
    updatePosition(e.clientX);
  });

  window.addEventListener("mousemove", (e) => {
    if (isDragging) updatePosition(e.clientX);
  });

  window.addEventListener("mouseup", () => {
    isDragging = false;
  });

  // Touch support for mobile/tablets
  container.addEventListener("touchmove", (e) => {
    if (e.touches.length > 0) updatePosition(e.touches[0].clientX);
  });
}

function setSliderSplit(pct) {
  const slider = document.getElementById("gradCamSlider");
  const heatmapImg = document.getElementById("gradCamHeatmapImg");
  const label = document.getElementById("sliderSplitLabel");

  if (slider) slider.style.left = pct + "%";
  if (heatmapImg) heatmapImg.style.clipPath = `polygon(0 0, ${pct}% 0, ${pct}% 100%, 0 100%)`;
  if (label) label.textContent = Math.round(pct) + "% Split";
}

// Report Modal
async function openReportModal() {
  const modal = document.getElementById("reportModal");
  const container = document.getElementById("modalReportContainer");
  modal.style.display = "flex";

  try {
    const res = await fetch("/api/report");
    const html = await res.text();
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = `<p style="color:red">Failed to load report: ${err.message}</p>`;
  }
}

function escapeHtml(str) {
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
