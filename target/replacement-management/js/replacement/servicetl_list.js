
$('.view-approve-btn').click(function() {
    const reqId = $(this).data('id');
    $('#approveRequestId').val(reqId);

    // Load request details
    $.get('<%= request.getContextPath() %>/views/replacement/servicetl/action', {
        action: 'getDetails',
        requestId: reqId
    }, function(data) {
        let html = '<h6>Request #' + data.id + ' - ' + data.clientName + '</h6>';
        html += '<p><strong>Reason:</strong> ' + data.reasonName + '</p>';
        html += '<h6 class="mt-3">Printers to Review:</h6>';
        html += '<div class="table-responsive"><table class="table table-sm">';
        html += '<thead><tr><th>Location</th><th>Existing</th><th>Action</th></tr></thead><tbody>';

        data.printers.forEach((p, i) => {
            html += '<tr><td>' + p.location + ', ' + p.city + '</td>';
            html += '<td>' + (p.existingModelName || 'N/A') + '</td>';
            html += '<td>';
            html += '<select name="printers[' + i + '].recommendedPModelId" class="form-control form-control-sm"><option value="">Select Model</option>';
            // Add printer models options here from ${printerModels}
            html += '</select>';
            html += '<input type="hidden" name="printers[' + i + '].id" value="' + p.id + '">';
            html += '</td></tr>';
        });

        html += '</tbody></table></div>';
        $('#approveContent').html(html);
        $('#approveModal').modal('show');
    });
});

$('.view-reject-btn').click(function() {
    $('#rejectRequestId').val($(this).data('id'));
    $('#rejectModal').modal('show');
});

$('.view-forward-btn').click(function() {
    $('#forwardRequestId').val($(this).data('id'));
    $('#forwardModal').modal('show');
});
