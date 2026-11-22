import com.medical.federated.model.*;
import com.medical.federated.service.*;
import java.io.File;
import java.nio.file.Files;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Running smoke test on Federated Medical AI Services...");
        
        HospitalManager hm = new HospitalManager();
        BlockchainAuditService bc = new BlockchainAuditService();
        FederatedLearningService fl = new FederatedLearningService(hm, bc);
        DiagnosisEngine de = new DiagnosisEngine(fl, bc);
        ReportGeneratorService rg = new ReportGeneratorService();
        
        System.out.println("Hospitals count: " + hm.getAllHospitals().size());
        System.out.println("Initial Global Accuracy: " + fl.getGlobalWeights().getAccuracy());
        
        // Test round execution
        System.out.println("Executing Federated Round 1...");
        FederatedRound round = fl.executeFederatedRound(3);
        System.out.println("Round 1 Acc: " + round.getGlobalAccuracy() + ", Loss: " + round.getGlobalLoss());
        System.out.println("Blockchain length: " + bc.getChainLength());
        System.out.println("Blockchain valid: " + bc.isChainValid());
        
        // Test diagnosis on sample image
        File sample = new File("D:\\JAVA PRG\\federated-medical-diagnosis\\samples\\skin_vascular_lesion.png");
        byte[] bytes = Files.readAllBytes(sample.toPath());
        DiagnosticResult diag = de.diagnoseImage(bytes, "ISIC_0024706.jpg", "/predict/skin");
        System.out.println("Diagnosis: " + diag.getPrimaryClassification() + " | Conf: " + diag.getConfidenceScore());
        System.out.println("Privacy Hash: " + diag.getSha256PrivacyHash());
        System.out.println("Grad-CAM length: " + diag.getGradCamHeatmapBase64().length());
        
        // Test report generation
        String reportHtml = rg.generateHtmlReport(diag);
        System.out.println("Report HTML size: " + reportHtml.length() + " chars");
        
        System.out.println("ALL SMOKE TESTS PASSED!");
    }
}
