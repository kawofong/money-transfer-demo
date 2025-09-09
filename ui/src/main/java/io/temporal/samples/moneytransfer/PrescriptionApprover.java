package io.temporal.samples.moneytransfer;

import static io.temporal.samples.moneytransfer.PrescriptionRequester.runApproveSignal;

public class PrescriptionApprover {

    public static void main(String[] args) {
        String workflowId = args[0];
        System.out.println("Signaling prescription workflow: " + workflowId);
        runApproveSignal(workflowId);
    }
}
