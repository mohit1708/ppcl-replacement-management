<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Generate Courier Login" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<div class="page-content container-fluid px-3 py-3">
    <div class="page-header mb-3">
        <h1 class="page-title text-primary-d2">
            <i class="fas fa-truck mr-2"></i> Generate Courier Login
        </h1>
        <p class="text-grey-m1 mb-0">Manage courier login credentials. Generate or resend login details via WhatsApp.</p>
    </div>

    <!-- Search -->
    <div class="card mb-3">
        <div class="card-body py-2">
            <div class="row align-items-center">
                <div class="col-md-4">
                    <div class="input-group" style="max-width:300px;">
                        <div class="input-group-prepend">
                            <span class="input-group-text"><i class="fas fa-search"></i></span>
                        </div>
                        <input type="text" id="searchInput" class="form-control" placeholder="Search courier name or mobile...">
                    </div>
                </div>
                <div class="col-md-8 text-right">
                    <span id="courierCount" class="text-muted mr-3"></span>
                    <button class="btn btn-sm btn-outline-primary" onclick="loadCouriers()">
                        <i class="fas fa-sync-alt"></i> Refresh
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Courier Table -->
    <div class="card">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover mb-0" id="courierTable" style="font-size:0.875rem;">
                    <thead class="thead-light">
                        <tr>
                            <th>#</th>
                            <th>Name</th>
                            <th>Contact Person</th>
                            <th>Mobile</th>
                            <th>Login Status</th>
                            <th class="text-center">Actions</th>
                        </tr>
                    </thead>
                    <tbody id="courierTableBody">
                        <tr>
                            <td colspan="6" class="text-center py-4">
                                <i class="fas fa-spinner fa-spin fa-2x text-primary"></i>
                                <p class="mt-2 text-muted">Loading couriers...</p>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Confirmation Modal -->
<div class="modal fade" id="confirmModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-primary-tp1 border-0 radius-t-1">
                <h5 class="modal-title text-white" id="confirmModalTitle">Confirm Action</h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" id="confirmModalBody">
                Are you sure?
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-sm btn-primary" id="confirmModalBtn" onclick="executeAction()">
                    <i class="fas fa-check"></i> Confirm
                </button>
            </div>
        </div>
    </div>
</div>

</div>

<script>
    var allCouriers = [];
    var pendingAction = null;
    var pendingCourierId = null;

    $(document).ready(function() {
        loadCouriers();
        $('#searchInput').on('keyup', function() {
            filterTable($(this).val());
        });
    });

    function loadCouriers() {
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/courier-login/list',
            method: 'GET',
            dataType: 'json',
            success: function(resp) {
                if (resp.success) {
                    allCouriers = resp.data;
                    renderTable(allCouriers);
                } else {
                    showToast('Error loading couriers: ' + resp.message, 'error');
                }
            },
            error: function() {
                showToast('Failed to load couriers. Please try again.', 'error');
            }
        });
    }

    function renderTable(couriers) {
        var tbody = $('#courierTableBody');
        tbody.empty();

        if (!couriers || couriers.length === 0) {
            tbody.html('<tr><td colspan="6" class="text-center py-4 text-muted">' +
                '<i class="fas fa-inbox fa-3x mb-3 d-block"></i>No couriers found</td></tr>');
            $('#courierCount').text('');
            return;
        }

        $('#courierCount').text('Total: ' + couriers.length + ' couriers');

        $.each(couriers, function(i, c) {
            var loginExists = (c.status === 'LOGIN_CREATED');
            var statusBadge = loginExists
                ? '<span class="badge badge-success" style="font-size:0.75rem; padding:4px 8px;"><i class="fas fa-check-circle"></i> Login Created</span>'
                : '<span class="badge badge-secondary" style="font-size:0.75rem; padding:4px 8px;"><i class="fas fa-minus-circle"></i> No Login</span>';

            var actions = '';
            if (loginExists) {
                actions = '<button class="btn btn-sm btn-outline-info" style="min-width:120px;" onclick="confirmResend(' + c.id + ', \'' + escapeHtml(c.name) + '\')">' +
                    '<i class="fas fa-paper-plane"></i> Resend Login</button>';
            } else {
                actions = '<button class="btn btn-sm btn-primary" style="min-width:120px;" onclick="confirmGenerate(' + c.id + ', \'' + escapeHtml(c.name) + '\')">' +
                    '<i class="fas fa-key"></i> Generate Login</button>';
            }

            tbody.append(
                '<tr>' +
                '<td>' + (i + 1) + '</td>' +
                '<td><strong>' + escapeHtml(c.name || '') + '</strong></td>' +
                '<td>' + escapeHtml(c.contactPerson || '-') + '</td>' +
                '<td>' + (c.mobile || '-') + '</td>' +
                '<td>' + statusBadge + '</td>' +
                '<td class="text-center">' + actions + '</td>' +
                '</tr>'
            );
        });
    }

    function filterTable(searchTerm) {
        if (!searchTerm || searchTerm.trim() === '') {
            renderTable(allCouriers);
            return;
        }
        var term = searchTerm.toLowerCase();
        var filtered = allCouriers.filter(function(c) {
            return (c.name && c.name.toLowerCase().indexOf(term) !== -1) ||
                   (c.mobile && String(c.mobile).indexOf(term) !== -1) ||
                   (c.contactPerson && c.contactPerson.toLowerCase().indexOf(term) !== -1);
        });
        renderTable(filtered);
    }

    function confirmGenerate(courierId, courierName) {
        pendingAction = 'generate';
        pendingCourierId = courierId;
        $('#confirmModalTitle').text('Generate Courier Login');
        $('#confirmModalBody').html(
            '<p>Are you sure you want to generate login credentials for <strong>' + courierName + '</strong>?</p>' +
            '<p class="text-muted small mb-0"><i class="fas fa-info-circle"></i> A passcode will be generated and sent via WhatsApp.</p>'
        );
        $('#confirmModalBtn').text(' Generate').prepend('<i class="fas fa-key"></i>');
        $('#confirmModal').modal('show');
    }

    function confirmResend(courierId, courierName) {
        pendingAction = 'resend';
        pendingCourierId = courierId;
        $('#confirmModalTitle').text('Resend Login Details');
        $('#confirmModalBody').html(
            '<p>Are you sure you want to resend login details to <strong>' + courierName + '</strong>?</p>' +
            '<p class="text-muted small mb-0"><i class="fas fa-info-circle"></i> Existing credentials will be resent via WhatsApp.</p>'
        );
        $('#confirmModalBtn').text(' Resend').prepend('<i class="fas fa-paper-plane"></i>');
        $('#confirmModal').modal('show');
    }

    function executeAction() {
        if (!pendingAction || !pendingCourierId) return;

        var url = '<%= request.getContextPath() %>/views/replacement/courier-login/' + pendingAction;
        var btn = $('#confirmModalBtn');
        btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Processing...');

        $.ajax({
            url: url,
            method: 'POST',
            data: { courierId: pendingCourierId },
            dataType: 'json',
            success: function(resp) {
                $('#confirmModal').modal('hide');
                if (resp.success) {
                    showToast(resp.message, 'success');
                    loadCouriers();
                } else {
                    showToast(resp.message, 'error');
                }
            },
            error: function() {
                $('#confirmModal').modal('hide');
                showToast('Failed to perform action. Please try again.', 'error');
            },
            complete: function() {
                btn.prop('disabled', false);
                pendingAction = null;
                pendingCourierId = null;
            }
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        return $('<div>').text(text).html();
    }

    function showToast(message, type) {
        var bgClass = type === 'success' ? 'bg-success' : 'bg-danger';
        var icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
        var toast = $('<div class="position-fixed" style="top:20px;right:20px;z-index:10000;">' +
            '<div class="alert ' + bgClass + ' text-white shadow-lg" style="min-width:300px;">' +
            '<i class="fas ' + icon + ' mr-2"></i>' + message +
            '<button type="button" class="close text-white ml-3" onclick="$(this).closest(\'.position-fixed\').remove()">&times;</button>' +
            '</div></div>');
        $('body').append(toast);
        setTimeout(function() { toast.fadeOut(function() { toast.remove(); }); }, 5000);
    }
</script>

<%@ include file="../common/footer.jsp" %>
