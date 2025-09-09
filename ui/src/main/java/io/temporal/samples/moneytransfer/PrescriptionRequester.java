package io.temporal.samples.moneytransfer;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.api.workflowservice.v1.WorkflowServiceGrpc;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.samples.moneytransfer.helper.ServerInfo;
import io.temporal.samples.moneytransfer.model.ExecutionScenario;
import io.temporal.samples.moneytransfer.model.PrescriptionInput;
import io.temporal.samples.moneytransfer.model.PrescriptionOutput;
import io.temporal.samples.moneytransfer.model.PrescriptionStatus;
import io.temporal.serviceclient.WorkflowServiceStubs;

import javax.net.ssl.SSLException;
import java.io.FileNotFoundException;

import static io.temporal.samples.moneytransfer.TemporalClient.getWorkflowServiceStubsWithHeaders;

public class PrescriptionRequester {

    public static PrescriptionOutput getWorkflowOutcome(String workflowId) throws FileNotFoundException, SSLException {
        WorkflowClient client = TemporalClient.get();
        WorkflowStub workflowStub = client.newUntypedWorkflowStub(workflowId);

        // Returns the result after waiting for the Workflow to complete.
        PrescriptionOutput result = workflowStub.getResult(PrescriptionOutput.class);
        return result;
    }

    public static PrescriptionStatus runQuery(String workflowId) throws FileNotFoundException, SSLException {
        WorkflowClient client = TemporalClient.get();
        System.out.println("Workflow STATUS: " + getWorkflowStatus(workflowId));

        WorkflowStub workflowStub = client.newUntypedWorkflowStub(workflowId);
        PrescriptionStatus result = workflowStub.query("prescriptionStatus", PrescriptionStatus.class);
        if ("WORKFLOW_EXECUTION_STATUS_FAILED".equals(getWorkflowStatus(workflowId))) {
            result.setWorkflowStatus("FAILED");
        }
        return result;
    }

    public static void runApproveSignal(String workflowId) {
        try {
            WorkflowClient client = TemporalClient.get();
            WorkflowStub workflowStub = client.newUntypedWorkflowStub(workflowId);
            workflowStub.signal("approvePrescription");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public static String runWorkflow(PrescriptionInput prescriptionInput, ExecutionScenario scenario)
            throws FileNotFoundException, SSLException {
        String referenceNumber = generateReferenceNumber(); // random reference number
        WorkflowClient client = TemporalClient.get();
        final String TASK_QUEUE = ServerInfo.getTaskqueue();
        String workflowType = scenario.getWorkflowType();
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(referenceNumber)
                .setTaskQueue(TASK_QUEUE)
                .build();
        WorkflowStub prescriptionWorkflow = client.newUntypedWorkflowStub(workflowType, options);
        prescriptionWorkflow.start(prescriptionInput);
        System.out.printf("\n\nPrescription fulfillment for %s (quantity: %d) requested\n", 
                         prescriptionInput.getMedicationName(), prescriptionInput.getQuantity());
        return referenceNumber;
    }

    @SuppressWarnings("CatchAndPrintStackTrace")
    public static void main(String[] args) throws Exception {
        PrescriptionInput params = new PrescriptionInput("RX12345", "PAT001", "Amoxicillin", 
                                                       30, "DR001", "PHARM001", "INS001");
        runWorkflow(params, ExecutionScenario.HAPPY_PATH);
        System.exit(0);
    }

    private static String generateReferenceNumber() {
        return String.format(
                "PRESCRIPTION-%s-%03d",
                (char) (Math.random() * 26 + 'A') +
                        "" +
                        (char) (Math.random() * 26 + 'A') +
                        (char) (Math.random() * 26 + 'A'),
                (int) (Math.random() * 999)
        );
    }

    private static String getWorkflowStatus(String workflowId) throws FileNotFoundException, SSLException {
        WorkflowServiceStubs service = getWorkflowServiceStubsWithHeaders();
        WorkflowServiceGrpc.WorkflowServiceBlockingStub stub = service.blockingStub();
        DescribeWorkflowExecutionRequest request = DescribeWorkflowExecutionRequest.newBuilder()
                .setNamespace(ServerInfo.getNamespace())
                .setExecution(WorkflowExecution.newBuilder().setWorkflowId(workflowId))
                .build();
        DescribeWorkflowExecutionResponse response = stub.describeWorkflowExecution(request);
        return response.getWorkflowExecutionInfo().getStatus().name();
    }
}
