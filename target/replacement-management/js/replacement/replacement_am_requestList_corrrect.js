






    $(document).ready(function() {
        <c:if test="${!hasError && !empty requests}">
        $('#requestsTable').DataTable({
            "order": [[ 7, "desc" ], [ 5, "desc" ]],
            "pageLength": 25
        });
        </c:if>
    });

    function showPrintersPopup(reqId) {
        // Your existing AJAX code...
        alert('Printer details for request #' + reqId);
    }
