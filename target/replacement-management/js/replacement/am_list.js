
$('.review-btn').click(function() {
    const reqId = $(this).data('id');
    $('#reviewRequestId').val(reqId);

    // Load request details for commercial review
    $.get('<%= request.getContextPath() %>/views/replacement/am/action', {
        action: 'getDetails',
        requestId: reqId
    }, function(data) {
        let html = '<div class="mb-3">';
        html += '<h6>Request #' + data.id + ' - ' + data.clientName + '</h6>';
        html += '<p><strong>Type:</strong> ' + data.replacementType + ' | ';
        html += '<strong>Reason:</strong> ' + data.reasonName + '</p>';
        html += '</div>';

        html += '<h6>Printers for Commercial Review:</h6>';
        html += '<div class="table-responsive"><table class="table table-bordered">';
        html += '<thead class="thead-light"><tr>';
        html += '<th>Location</th><th>Existing</th><th>Recommended</th>';
        html += '<th>Replace with Existing</th><th>Commercial Comment</th>';
        html += '</tr></thead><tbody>';

        data.printers.forEach((p, i) => {
            html += '<tr>';
            html += '<td>' + p.location + ', ' + p.city + '</td>';
            html += '<td>' + (p.existingModelName || 'N/A') + '<br><small>' + (p.existingSerial || '') + '</small></td>';
            html += '<td><strong>' + (p.recommendedModelName || 'N/A') + '</strong></td>';
            html += '<td>';
            html += '<select name="printers[' + i + '].replaceWithExisting" class="form-control form-control-sm">';
            html += '<option value="0">No - New Model</option>';
            html += '<option value="1">Yes - Use Existing</option>';
            html += '</select>';
            html += '</td>';
            html += '<td>';
            html += '<textarea name="printers[' + i + '].commercialComment" class="form-control form-control-sm" rows="2"></textarea>';
            html += '<input type="hidden" name="printers[' + i + '].id" value="' + p.id + '">';
            html += '</td>';
            html += '</tr>';
        });

        html += '</tbody></table></div>';

        html += '<div class="mt-3">';
        html += '<label><strong>Overall Comments:</strong></label>';
        html += '<textarea name="overallComment" class="form-control" rows="3"></textarea>';
        html += '</div>';

        $('#reviewContent').html(html);
        $('#reviewModal').modal('show');
    });
});

$('#rejectCommercialBtn').click(function() {
    if (confirm('Are you sure you want to reject this commercial review?')) {
        $('#reviewForm input[name="action"]').val('reject');
        $('#reviewForm').submit();
    }
});
