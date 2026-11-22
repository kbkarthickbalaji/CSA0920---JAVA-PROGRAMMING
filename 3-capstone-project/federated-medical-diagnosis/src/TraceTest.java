import com.medical.federated.model.*;
import com.medical.federated.service.*;
public class TraceTest {
    public static void main(String[] args) {
        System.out.println("Trace 1: init");
        HospitalManager hm = new HospitalManager();
        BlockchainAuditService bc = new BlockchainAuditService();
        System.out.println("Trace 2: flService");
        FederatedLearningService fl = new FederatedLearningService(hm, bc);
        System.out.println("Trace 3: calling executeFederatedRound");
        fl.executeFederatedRound(1);
        System.out.println("Trace 4: done executeFederatedRound");
        System.exit(0);
    }
}
