










    const contextPath = '${pageContext.request.contextPath}';
    let currentReqId = null;
    let currentPrinters = [];
    let hierarchyUsers = [];

    $(document).ready(function() {
        if ($('#requestsTable tbody tr').length > 0 && !$('#requestsTable').hasClass('dataTable')) {
            $('#requestsTable').DataTable({
                order: [[5, 'desc']],
                pageLength: 10,
                language: {
                    emptyTable: "No pending requests"
                }
            });
        }
        console.log("✅ AM Manager requestList.jsp loaded successfully!");
    });

    function clearFilters() {
        window.location.href = contextPath + '/views/replacement/ammanager/requestList';
    }

    // ==================== VIEW COMMERCIAL DETAIL ====================
    function viewCommercialDetail(reqId, clientName) {
        console.log("📋 viewCommercialDetail called with reqId: " + reqId);
        currentReqId = reqId;
        $('#commercialDetailReqId').text('REQ-' + reqId);
        $('#commercialDetailModal').modal('show');
        
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getPrinterDetails',
            method: 'GET',
            data: { reqId: reqId },
            success: function(response) {
                console.log("✅ getPrinterDetails success:", response);
                if (response.success) {
                    currentPrinters = response.data.printers || [];
                    displayCommercialDetailTable(response.data.printers);
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getPrinterDetails error:", error);
                showAppAlert('Error loading printer details: ' + error, 'danger');
            }
        });
    }

    function displayCommercialDetailTable(printers) {
        let html = '';
        if (!printers || printers.length === 0) {
            html = '<tr><td colspan="6" class="text-center text-muted">No printers found</td></tr>';
        } else {
            printers.forEach(function(p) {
                html += '<tr>';
                html += '<td><strong>' + escapeHtml(p.existingModel || '') + '</strong></td>';
                html += '<td><code>' + escapeHtml(p.serial || '') + '</code></td>';
                html += '<td>' + escapeHtml(p.newModelName || p.newModelText || 'TBD');
                html += ' <span class="badge badge-' + (p.printerType === 'NEW' ? 'success' : 'warning') + '">' + (p.printerType || 'New') + '</span></td>';
                html += '<td>' + escapeHtml(p.city || '') + ' / ' + escapeHtml(p.location || '') + '</td>';
                html += '<td>' + escapeHtml(p.comments || '-') + '</td>';
                html += '<td><button class="btn btn-outline-secondary btn-sm" onclick="viewPrinterCommercial(' + p.agrProdId + ', \'' + escapeHtml(p.existingModel || '') + '\', \'' + escapeHtml(p.serial || '') + '\')">';
                html += '<i class="fas fa-dollar-sign"></i> View Commercial</button></td>';
                html += '</tr>';
            });
        }
        $('#commercialDetailTableBody').html(html);
    }

    // ==================== OPEN COMMERCIAL ACTION ====================
    function openCommercialAction(reqId, clientName) {
        console.log("💰 openCommercialAction called with reqId: " + reqId);
        currentReqId = reqId;
        $('#commercialActionReqId').text('REQ-' + reqId + ' (' + clientName + ')');
        $('#commercialActionModal').modal('show');
        
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getPrinterDetails',
            method: 'GET',
            data: { reqId: reqId },
            success: function(response) {
                console.log("✅ getPrinterDetails success:", response);
                if (response.success) {
                    currentPrinters = response.data.printers || [];
                    displayCommercialPrinterCards(response.data.printers);
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getPrinterDetails error:", error);
                showAppAlert('Error loading printer details: ' + error, 'danger');
            }
        });
    }SELECT * FROM printer_order_item WHERE order_id=17021

    function displayCommercialPrinterCards(printers) {
        let html = '';
        if (!printers || printers.length === 0) {
            html = '<div class="alert alert-warning">No printers found for this request.</div>';
        } else {
            var template = $('#commercialPrinterCardTemplate').html();
            printers.forEach(function(p, idx) {
                var card = template
                    .replace(/\{\{printerId\}\}/g, p.id)
                    .replace(/\{\{printerIndex\}\}/g, (idx + 1))
                    .replace(/\{\{existingModel\}\}/g, escapeHtml(p.existingModel || ''))
                    .replace(/\{\{newModel\}\}/g, escapeHtml(p.newModelName || p.newModelText || 'TBD'))
                    .replace(/\{\{newModelBadgeClass\}\}/g, p.printerType === 'NEW' ? 'success' : 'warning')
                    .replace(/\{\{serialNumber\}\}/g, escapeHtml(p.serial || ''))
                    .replace(/\{\{agreementNumber\}\}/g, escapeHtml(p.agreementNo || 'Active'))
                    .replace(/\{\{primaryRecommendation\}\}/g, escapeHtml(p.newModelName || p.newModelText || 'TBD'))
                    .replace(/\{\{alternateRecommendation\}\}/g, p.alternateModel ? 'Alternate: ' + escapeHtml(p.alternateModel) : 'No alternate provided')
                    .replace(/\{\{editDisabled\}\}/g, p.alternateModel ? '' : 'disabled title="No alternate available"')
                    .replace(/\{\{printerOptions\}\}/g, buildPrinterOptions(p));
                html += card;
            });
        }
        $('#commercialPrintersContainer').html(html);
    }

    function buildPrinterOptions(printer) {
        var options = '<option value="' + (printer.newModelId || '') + '" selected>' + escapeHtml(printer.newModelName || printer.newModelText || 'TBD') + ' (Primary)</option>';
        if (printer.alternateModel) {
            options += '<option value="' + (printer.alternateModelId || '') + '">' + escapeHtml(printer.alternateModel) + ' (Alternate)</option>';
        }
        return options;
    }

    function editPrinterRecommendation(printerId) {
        var selectGroup = $('#printerSelectGroup_' + printerId);
        selectGroup.toggleClass('d-none');
    }

    function updateSelectedPrinter(printerId, value) {
        console.log('Printer ' + printerId + ' changed to model ID: ' + value);
    }

    function toggleCommercialComments(printerId, isChecked) {
        var required = $('#commentsRequired_' + printerId);
        if (isChecked) {
            // Checkbox is checked = Continue with existing commercial, comments optional
            required.addClass('d-none');
        } else {
            // Checkbox is unchecked = New terms required, comments mandatory
            required.removeClass('d-none');
        }
    }

    // ==================== VIEW PRINTER COMMERCIAL ====================
    function viewPrinterCommercial(agrProdId, modelName, serial) {
        console.log("📊 viewPrinterCommercial called with agrProdId: " + agrProdId);
        $('#printerCommercialTitle').text(modelName + ' (' + serial + ')');
        
        // Reset to loading state
        resetPrinterCommercialModal();
        
        $('#printerCommercialModal').modal('show');
        
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getPrinterHistory',
            method: 'GET',
            data: { agrProdId: agrProdId },
            success: function(response) {
                console.log("✅ getPrinterHistory success:", response);
                if (response.success && response.data) {
                    displayPrinterCommercial(response.data);
                } else {
                    showPrinterCommercialError('No data available');
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getPrinterHistory error:", error);
                showPrinterCommercialError('Error loading data: ' + error);
            }
        });
    }
    
    function resetPrinterCommercialModal() {
        $('#agreementNumber').text('Loading...');
        $('#contractStart').text('--');
        $('#contractEnd').text('--');
        $('#billingType').text('--');
        $('#monoRate').text('--');
        $('#colorRate').text('--');
        $('#billingGrid').html('<div class="text-center text-muted"><i class="fas fa-spinner fa-spin"></i> Loading...</div>');
        $('#avgMonthlyBilling').text('₹--');
        $('#avgMonthlyPages').text('--');
    }
    
    function showPrinterCommercialError(message) {
        $('#agreementNumber').text('--');
        $('#billingGrid').html('<div class="text-center text-danger">' + escapeHtml(message) + '</div>');
    }

    function displayPrinterCommercial(data) {
        if (data.agreement) {
            $('#agreementNumber').text(data.agreement.agreementNo || '--');
            $('#contractStart').text(data.agreement.startDate || '--');
            $('#contractEnd').text(data.agreement.endDate || '--');
            $('#billingType').text(data.agreement.billingType || 'Per Page');
            $('#monoRate').text('₹' + (data.agreement.monoRate || '0.00'));
            $('#colorRate').text('₹' + (data.agreement.colorRate || '0.00'));
        }
        
        if (data.billingHistory && data.billingHistory.length > 0) {
            var tableHtml = '<div class="table-responsive"><table class="table table-bordered table-sm text-center mb-0">';
            tableHtml += '<thead class="thead-light"><tr><th class="bg-secondary text-white">Month</th>';
            data.billingHistory.forEach(function(b) {
                tableHtml += '<th>' + escapeHtml(b.month) + '</th>';
            });
            tableHtml += '</tr></thead><tbody>';
            tableHtml += '<tr><td class="font-weight-bold bg-light">Amount</td>';
            data.billingHistory.forEach(function(b) {
                tableHtml += '<td>₹' + formatNumber(b.amount) + '</td>';
            });
            tableHtml += '</tr><tr><td class="font-weight-bold bg-light">Pages</td>';
            data.billingHistory.forEach(function(b) {
                tableHtml += '<td>' + formatNumber(b.pages) + '</td>';
            });
            tableHtml += '</tr></tbody></table></div>';
            $('#billingGrid').html(tableHtml);
        } else {
            $('#billingGrid').html('<div class="text-center text-muted">No billing history available</div>');
        }
        
        $('#avgMonthlyBilling').text('₹' + formatNumber(data.avgMonthlyBilling || 0));
        $('#avgMonthlyPages').text(formatNumber(data.avgMonthlyPages || 0));
    }

    function viewPrinterCommercialDetail(printerId, modelName, serial) {
        viewPrinterCommercial(printerId, modelName, serial);
    }

    // ==================== APPROVE AND NOTIFY ====================
    function approveAndNotifyAM() {
        console.log("✅ approveAndNotifyAM called");
        
        var printerDecisions = [];
        var hasError = false;
        
        $('.commercial-printer-card').each(function() {
            var printerId = $(this).data('printer-id');
            var isChecked = $('#commercialDecision_' + printerId).is(':checked');
            var decision = isChecked ? 'yes' : 'no';
            var comments = $('#printerComments_' + printerId).val();
            
            if (!isChecked && !comments.trim()) {
                hasError = true;
                showAppAlert('Comments are required when not continuing with existing commercial terms', 'warning');
                return false;
            }
            
            printerDecisions.push({
                printerId: printerId,
                continueExisting: decision,
                comments: comments
            });
        });
        
        if (hasError) return;
        
        showAppConfirm('Approve commercial terms and notify Account Manager?', function() {
            var overallComments = $('#commercialOverallComments').val();
            
            $.ajax({
                url: contextPath + '/views/replacement/ammanager/approveRequest',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    reqId: currentReqId,
                    printerDecisions: printerDecisions,
                    overallComments: overallComments
                }),
                success: function(response) {
                    console.log("✅ approveRequest success:", response);
                    if (response.success) {
                        showAppAlert('Commercial terms approved. Account Manager has been notified.', 'success');
                        $('#commercialActionModal').modal('hide');
                        setTimeout(function() { location.reload(); }, 10000);
                    } else {
                        showAppAlert(response.message || 'Failed to approve', 'danger');
                    }
                },
                error: function(xhr, status, error) {
                    console.error("❌ approveRequest error:", error);
                    showAppAlert('Error approving request: ' + error, 'danger');
                }
            });
        });
    }

    // ==================== REJECT FROM COMMERCIAL ====================
    function openRejectFromCommercial() {
        $('#rejectModal').modal('show');
    }

    function submitReject() {
        console.log("📤 submitReject called");
        var rejectionReason = $('#rejectionReason').val();
        var rejectComments = $('#rejectComments').val().trim();
        
        if (!rejectionReason) {
            showAppAlert('Please select rejection reason', 'warning');
            return;
        }
        if (!rejectComments) {
            showAppAlert('Please enter rejection comments', 'warning');
            return;
        }
        
        showAppConfirm('Are you sure you want to REJECT this request?', function() {
            $.ajax({
                url: contextPath + '/views/replacement/ammanager/rejectRequest',
                method: 'POST',
                data: {
                    reqId: currentReqId,
                    rejectionReason: rejectionReason,
                    comments: rejectComments
                },
                success: function(response) {
                    console.log("✅ rejectRequest success:", response);
                    if (response.success) {
                        showAppAlert(response.message, 'success');
                        $('#rejectModal').modal('hide');
                        $('#commercialActionModal').modal('hide');
                        setTimeout(function() { location.reload(); }, 10000);
                    }
                },
                error: function(xhr, status, error) {
                    console.error("❌ rejectRequest error:", error);
                    showAppAlert('Error rejecting request: ' + error, 'danger');
                }
            });
        });
    }

    // ==================== FORWARD FROM COMMERCIAL ====================
    function forwardFromCommercial() {
        if (hierarchyUsers.length === 0) {
            loadHierarchyUsers();
        }
        $('#forwardModal').modal('show');
    }

    function loadHierarchyUsers() {
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getHierarchyUsers',
            method: 'GET',
            success: function(response) {
                if (response.success) {
                    hierarchyUsers = response.data;
                    populateForwardUsers();
                }
            }
        });
    }

    function populateForwardUsers() {
        var html = '<option value="">-- Select User --</option>';
        hierarchyUsers.forEach(function(u) {
            html += '<option value="' + u.userId + '" data-role="' + u.role + '">';
            html += u.name + ' (' + u.role + ')</option>';
        });
        $('#forwardTargetUser').html(html);
    }

    function updateForwardRole() {
        var selected = $('#forwardTargetUser option:selected');
        var role = selected.data('role');
        $('#forwardRoleDisplay').text('Role: ' + (role || ''));
    }

    function submitForward() {
        var targetUserId = $('#forwardTargetUser').val();
        var targetRole = $('#forwardTargetUser option:selected').data('role');
        var forwardComments = $('#forwardComments').val().trim();
        
        if (!targetUserId) {
            showAppAlert('Please select target user', 'warning');
            return;
        }
        
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/forwardRequest',
            method: 'POST',
            data: {
                reqId: currentReqId,
                targetUserId: targetUserId,
                targetRole: targetRole,
                comments: forwardComments
            },
            success: function(response) {
                if (response.success) {
                    showAppAlert(response.message, 'success');
                    $('#forwardModal').modal('hide');
                    $('#commercialActionModal').modal('hide');
                    setTimeout(function() { location.reload(); }, 10000);
                }
            },
            error: function(xhr, status, error) {
                showAppAlert('Error forwarding request: ' + error, 'danger');
            }
        });
    }

    // ==================== UTILITY FUNCTIONS ====================
    function escapeHtml(text) {
        if (!text) return '';
        return text.toString()
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatNumber(num) {
        if (!num) return '0';
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }
