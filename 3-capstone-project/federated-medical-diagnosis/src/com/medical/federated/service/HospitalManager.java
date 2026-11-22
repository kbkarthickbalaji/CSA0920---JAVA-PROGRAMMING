package com.medical.federated.service;

import com.medical.federated.model.HospitalNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the participating hospital clients across regions.
 */
public class HospitalManager {
    private final Map<String, HospitalNode> hospitals = new LinkedHashMap<>();

    public HospitalManager() {
        // Initialize the 4 Hospital Servers matching the Capstone Slide 7 & Architecture
        registerHospital(new HospitalNode("HOSP-01", "North Hospital", "Brain MRI & Neuro-oncology", "North Region", 1420, 0.8125, 0.4569));
        registerHospital(new HospitalNode("HOSP-02", "South Hospital", "Pulmonary & Chest X-Ray", "South Region", 2150, 0.8396, 0.3965));
        registerHospital(new HospitalNode("HOSP-03", "East Hospital", "Dermatology & Skin Lesions", "East Region", 1840, 0.8375, 0.4297));
        registerHospital(new HospitalNode("HOSP-04", "West Hospital", "Infectious Diseases & COVID", "West Region", 1290, 0.7875, 0.5506));
    }

    public void registerHospital(HospitalNode node) {
        hospitals.put(node.getId(), node);
    }

    public List<HospitalNode> getAllHospitals() {
        return new ArrayList<>(hospitals.values());
    }

    public List<HospitalNode> getActiveHospitals() {
        List<HospitalNode> active = new ArrayList<>();
        for (HospitalNode node : hospitals.values()) {
            if (node.isActive()) {
                active.add(node);
            }
        }
        return active;
    }

    public HospitalNode getHospital(String id) {
        return hospitals.get(id);
    }

    public boolean toggleHospital(String id, boolean active) {
        HospitalNode node = hospitals.get(id);
        if (node != null) {
            node.setActive(active);
            return true;
        }
        return false;
    }

    public int getTotalScansAcrossNetwork() {
        int total = 0;
        for (HospitalNode node : hospitals.values()) {
            total += node.getDatasetSize();
        }
        return total;
    }
}
