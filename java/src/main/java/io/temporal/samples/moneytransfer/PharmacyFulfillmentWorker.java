package io.temporal.samples.moneytransfer;

import io.temporal.samples.moneytransfer.activities.PharmacyFulfillmentActivitiesImpl;
import io.temporal.samples.moneytransfer.util.ServerInfo;
import io.temporal.samples.moneytransfer.workflows.PharmacyFulfillmentWorkflowImpl;
import io.temporal.samples.moneytransfer.workflows.PharmacyFulfillmentWorkflowScenarios;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class PharmacyFulfillmentWorker {

    @SuppressWarnings("CatchAndPrintStackTrace")
    public static void main(String[] args) throws Exception {
        final String TASK_QUEUE = ServerInfo.getTaskqueue();

        WorkerFactory factory = WorkerFactory.newInstance(TemporalClient.get());

        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(PharmacyFulfillmentWorkflowImpl.class);
        worker.registerWorkflowImplementationTypes(PharmacyFulfillmentWorkflowScenarios.class);
        worker.registerActivitiesImplementations(new PharmacyFulfillmentActivitiesImpl());

        factory.start();
        System.out.println("Pharmacy Fulfillment Worker started for task queue: " + TASK_QUEUE);
    }
}
