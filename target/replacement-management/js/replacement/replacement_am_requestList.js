




    let currentReqId = null;

    function viewFullRequest(reqId) {
        currentReqId = reqId;
        $('#fullRequestModal').modal('show');

        $.ajax({
            url: '${pageContext.request.contextPath}/views/replacement/am/getFullRequest',
            method: 'GET',
            data: { reqId: reqId },
            success: function (response) {
                if (response.success) {
                    displayFullRequest(response.data);
                }
            }
        });
    }

    function displayFullRequest(data) {
        let html = '<div class="card mb-3">';
        html += '<div class="card-header bg-info text-white"><strong>Request Info</strong></div>';
        html += '<div class="card-body">';
        html += '<div class="row">';
        html += '<div class="col-md-6"><strong>Client:</strong> ' + data.request.clientName + '</div>';
        html += '<div class="col-md-6"><strong>Location:</strong> ' + data.request.city + ', ' + data.request.branch + '</div>';
        html += '</div>';

        if (data.request.amManagerComments) {
            html += '<div class="mt-3"><strong>AM Manager Comments:</strong><br>';
            html += '<div class="alert alert-info">' + data.request.amManagerComments + '</div></div>';
        }
        html += '</div></div>';

        html += '<div class="card">';
        html += '<div class="card-header bg-primary text-white"><strong>Printers with Commercial Details</strong></div>';
        html += '<div class="card-body">';
        html += '<table class="table table-bordered table-sm">';
        html += '<thead class="thead-light"><tr>';
        html += '<th>Serial</th><th>Existing</th><th>TL Recommended</th><th>New Cost</th><th>New Rental</th><th>Justification</th>';
        html += '</tr></thead><tbody>';

        data.printers.forEach(function (p) {
            html += '<tr>';
            html += '<td>' + p.serial + '</td>';
            html += '<td>' + p.existingModel + '</td>';
            html += '<td>' + p.recommendedModel + '</td>';
            html += '<td>₹' + (p.newCost ? Number(p.newCost).toFixed(2) : 'N/A') + '</td>';
            html += '<td>₹' + (p.newRental ? Number(p.newRental).toFixed(2) : 'N/A') + '</td>';
            html += '<td>' + (p.justification || '-') + '</td>';
            html += '</tr>';
        });

        html += '</tbody></table></div></div>';

        html += '<div class="card mt-3">';
        html += '<div class="card-header bg-secondary text-white"><strong>AM Decision</strong></div>';
        html += '<div class="card-body">';
        html += '<div class="form-group">';
        html += '<label><strong>Action:</strong></label>';
        html += '<select class="form-control" id="amAction">';
        html += '<option value="">-- Select Action --</option>';
        html += '<option value="APPROVE">Approve</option>';
        html += '<option value="REJECT">Reject</option>';
        html += '</select>';
        html += '</div>';
        html += '<div class="form-group">';
        html += '<label><strong>Comments:</strong></label>';
        html += '<textarea class="form-control" id="amComments" rows="4" placeholder="Enter your decision comments"></textarea>';
        html += '</div></div></div>';

        $('#fullRequestBody').html(html);
    }

    function submitAMDecision() {
        const action = $('#amAction').val();
        const comments = $('#amComments').val().trim();

        if (!action) {
            alert('Please select an action (Approve/Reject)');
            return;
        }
        if (!comments) {
            alert('Please enter decision comments');
            return;
        }

        $.ajax({
            url: '${pageContext.request.contextPath}/views/replacement/am/approveRequest',
            method: 'POST',
            data: { reqId: currentReqId, action: action, comments: comments },
            success: function (response) {
                if (response.success) {
                    alert(response.message);
                    $('#fullRequestModal').modal('hide');
                    location.reload();
                }
            }
        });
    }

    function clearFilters() {
        window.location.href = '${pageContext.request.contextPath}/views/replacement/am/requestList';
    }
