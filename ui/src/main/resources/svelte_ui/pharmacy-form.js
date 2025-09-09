// Pharmacy Fulfillment Form Handler
document.addEventListener('DOMContentLoaded', function () {
    // Find the form elements
    const fillButton = document.querySelector('button');
    const patientIdInput = document.getElementById('patient-id');
    const medicationSelect = document.getElementById('medication');
    const quantityInput = document.getElementById('quantity');
    const pharmacySelect = document.getElementById('pharmacy');
    const simulateSelect = document.getElementById('simulate');
    const scheduleCheckbox = document.getElementById('schedule-checkbox');

    if (fillButton) {
        fillButton.addEventListener('click', async function (e) {
            e.preventDefault();

            // Collect form data
            const formData = {
                prescriptionId: 'RX-' + Date.now(),
                patientId: patientIdInput.value || 'PAT001',
                medicationName: medicationSelect.value || 'Amoxicillin',
                quantity: parseInt(quantityInput.value) || 30,
                doctorId: 'DR001',
                pharmacyId: pharmacySelect.value || 'PHARM001',
                insuranceId: 'INS001',
                scenario: simulateSelect.value || 'HAPPY_PATH'
            };

            console.log('Submitting prescription fulfillment:', formData);

            try {
                // Submit to the backend
                const response = await fetch('/runWorkflow', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(formData)
                });

                if (response.ok) {
                    const result = await response.json();
                    console.log('Prescription fulfillment started:', result);

                    // Show success message
                    alert(`Prescription fulfillment started successfully!\nWorkflow ID: ${result.prescriptionId || result.transferId}`);

                    // Refresh the workflow list
                    loadWorkflowList();
                } else {
                    console.error('Error starting prescription fulfillment:', response.statusText);
                    alert('Error starting prescription fulfillment. Please try again.');
                }
            } catch (error) {
                console.error('Error submitting form:', error);
                alert('Error submitting form. Please check the console for details.');
            }
        });
    }

    // Function to load and display workflow list
    async function loadWorkflowList() {
        try {
            const response = await fetch('/listWorkflows');
            if (response.ok) {
                const workflows = await response.json();
                updateWorkflowTable(workflows);
            }
        } catch (error) {
            console.error('Error loading workflow list:', error);
        }
    }

    // Function to update the workflow table
    function updateWorkflowTable(workflows) {
        const tbody = document.querySelector('table tbody');
        if (tbody) {
            tbody.innerHTML = '';
            workflows.forEach(workflow => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                        <div class="flex items-center">
                            <div class="ml-3">
                                <p class="text-gray-900 whitespace-no-wrap">
                                    ${workflow.workflowId}
                                </p>
                            </div>
                        </div>
                    </td>
                    <td class="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                        <span class="relative inline-block px-3 py-1 font-semibold text-green-900 leading-tight">
                            <span aria-hidden class="absolute inset-0 bg-green-200 opacity-50 rounded-full"></span>
                            <span class="relative">${workflow.workflowStatus}</span>
                        </span>
                    </td>
                `;
                tbody.appendChild(row);
            });
        }
    }

    // Load workflow list on page load
    loadWorkflowList();

    // Refresh workflow list every 5 seconds
    setInterval(loadWorkflowList, 5000);
});
