package io.temporal.samples.moneytransfer;

import io.temporal.samples.moneytransfer.activities.ServiceProvisioningActivitiesImpl;
import io.temporal.samples.moneytransfer.util.ServerInfo;
import io.temporal.samples.moneytransfer.workflows.ServiceProvisioningWorkflowImpl;
import io.temporal.samples.moneytransfer.workflows.ServiceProvisioningWorkflowScenarios;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class AccountTransferWorker {

    @SuppressWarnings("CatchAndPrintStackTrace")
    public static void main(String[] args) throws Exception {
        final String TASK_QUEUE = ServerInfo.getTaskqueue();

        WorkerFactory factory = WorkerFactory.newInstance(TemporalClient.get());

        Worker worker = factory.newWorker(TASK_QUEUE);
        // worker.registerWorkflowImplementationTypes(AccountTransferWorkflowImpl.class);
        // worker.registerWorkflowImplementationTypes(AccountTransferWorkflowScenarios.class);
        // worker.registerActivitiesImplementations(new AccountTransferActivitiesImpl());
        worker.registerWorkflowImplementationTypes(ServiceProvisioningWorkflowImpl.class);
        worker.registerWorkflowImplementationTypes(ServiceProvisioningWorkflowScenarios.class);
        worker.registerActivitiesImplementations(new ServiceProvisioningActivitiesImpl());

        factory.start();
        System.out.println("Worker started for task queue: " + TASK_QUEUE);
    }
}
