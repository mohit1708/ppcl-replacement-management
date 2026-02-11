
    // Store printer comments globally
    let printerComments = {};
    let selectedPrinters = [];

    // Called when Reply button is clicked
    function prepareReplyModal() {
        console.log("📋 prepareReplyModal called");

        // Reset state
        printerComments = {};
        selectedPrinters = [];

        // Load current printers from fullRequestModal
        if (window.currentPrinters && window.currentPrinters.length > 0) {
            populatePrintersInReplyModal(window.currentPrinters);
        } else {
            // Fallback: fetch printers from AJAX
            $.ajax({
                url: '${pageContext.request.contextPath}/views/replacement/ammanager/getFullRequest',
                method: 'GET',
                data: { reqId: currentReqId },
                success: function(response) {
                    if (response.success && response.data.printers) {
                        window.currentPrinters = response.data.printers;
                        populatePrintersInReplyModal(response.data.printers);
                    }
                }
            });
        }
    }

    function populatePrintersInReplyModal(printers) {
        console.log("🖨️ populatePrintersInReplyModal with", printers.length, "printers");

        let html = '';

        if (!printers || printers.length === 0) {
            html = '<div class="text-center text-muted">No printers found</div>';
        } else {
            printers.forEach(function(printer, index) {
                const printerId = 'printer_' + index;

                html += '<div class="printer-card" id="' + printerId + '_card">';

                // Header with checkbox and printer info
                html += '<div class="printer-header">';
                html += '<input type="checkbox" class="printer-checkbox" id="' + printerId + '_check" ';
                html += 'onchange="togglePrinterComment(this, ' + index + ')">';
                html += '<div class="printer-info">';
                html += '<div><strong>Serial:</strong> <span class="badge badge-serial">' + printer.serial + '</span></div>';
                html += '<div class="printer-info-text">';
                html += '<strong>Existing:</strong> ' + printer.existingModel;
                html += ' <strong class="ml-3">Recommended:</strong> <span class="badge badge-model">' + printer.recommendedModel + '</span>';
                html += '</div>';
                html += '<div class="printer-info-text">';
                html += '<strong>Agreement:</strong> ' + (printer.agreementNo || 'Active');
                html += ' | <strong>Rates:</strong> ₹' + Number(printer.blackRate).toFixed(2) + ' (B) | ₹' + Number(printer.colorRate).toFixed(2) + ' (C) | ₹' + Number(printer.rentalAmount).toFixed(2) + ' (R)';
                html += '</div>';
                html += '</div>';
                html += '</div>';

                // Comment section (hidden by default)
                html += '<div class="printer-comment-section" id="' + printerId + '_comments">';
                html += '<label class="small"><strong>Commercial Comment for this Printer</strong></label>';
                html += '<textarea class="form-control printer-comment-textarea" id="' + printerId + '_text" ';
                html += 'placeholder="Enter commercial proposal/comment for ' + printer.serial + '...">';
                html += '</textarea>';
                html += '</div>';

                html += '</div>';
            });
        }

        $('#printersContainer').html(html);
        console.log("✅ Printers populated in reply modal");
    }

    function togglePrinterComment(checkbox, index) {
        const printerId = 'printer_' + index;
        const commentSection = $('#' + printerId + '_comments');
        const card = $('#' + printerId + '_card');

        console.log("🔄 togglePrinterComment index:", index, "checked:", checkbox.checked);

        if (checkbox.checked) {
            commentSection.addClass('show');
            card.addClass('checked');

            // Add to selected list
            if (!selectedPrinters.includes(index)) {
                selectedPrinters.push(index);
            }
        } else {
            commentSection.removeClass('show');
            card.removeClass('checked');

            // Remove from selected list
            selectedPrinters = selectedPrinters.filter(i => i !== index);

            // Clear comment
            $('#' + printerId + '_text').val('');
            delete printerComments['printer_' + index];
        }

        console.log("📝 Selected printers:", selectedPrinters);
    }

    function submitReplyWithComments() {
        console.log("📤 submitReplyWithComments called");

        const replaceExisting = $('input[name="replaceExisting"]:checked').val();

        if (!replaceExisting) {
            alert('Please select Yes or No for replacement option');
            return;
        }

        // Collect comments from selected printers
        const replyData = {
            reqId: currentReqId,
            replaceExisting: replaceExisting,
            printerComments: {},
            selectedPrinterCount: 0
        };

        console.log("🔍 Collecting comments from", selectedPrinters.length, "selected printers");

        selectedPrinters.forEach(function(index) {
            const printerId = 'printer_' + index;
            const comment = $('#' + printerId + '_text').val().trim();
            const printer = window.currentPrinters[index];

            if (printer) {
                replyData.printerComments[printer.serial] = {
                    printerSerial: printer.serial,
                    existingModel: printer.existingModel,
                    recommendedModel: printer.recommendedModel,
                    comment: comment
                };
                replyData.selectedPrinterCount++;
            }
        });

        // Validation
        if (replyData.selectedPrinterCount === 0) {
            alert('Please select at least one printer to reply for');
            return;
        }

        // Check if all selected printers have comments (if replaceExisting = "No")
        if (replaceExisting === 'No') {
            for (let serial in replyData.printerComments) {
                if (!replyData.printerComments[serial].comment) {
                    alert('Commercial comments are mandatory for all selected printers when replying "No"');
                    return;
                }
            }
        }

        console.log("✅ Reply data prepared:", replyData);

        // Send to server
        $.ajax({
            url: '${pageContext.request.contextPath}/views/replacement/ammanager/replyRequest',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(replyData),
            success: function(response) {
                console.log("✅ replyRequest success:", response);
                if (response.success) {
                    alert(response.message);
                    $('#replyModal').modal('hide');
                    $('#fullRequestModal').modal('hide');
                    location.reload();
                } else {
                    alert('Error: ' + response.message);
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ replyRequest error:", error);

                // Fallback: Send as form data if JSON fails
                let commentStr = '';
                for (let serial in replyData.printerComments) {
                    commentStr += serial + ': ' + replyData.printerComments[serial].comment + '\n';
                }

                $.ajax({
                    url: '${pageContext.request.contextPath}/views/replacement/ammanager/replyRequest',
                    method: 'POST',
                    data: {
                        reqId: currentReqId,
                        commercialComments: commentStr,
                        replaceExisting: replaceExisting,
                        printerCount: replyData.selectedPrinterCount,
                        notifyAM: 'true'
                    },
                    success: function(response) {
                        console.log("✅ replyRequest success (form data):", response);
                        if (response.success) {
                            alert(response.message);
                            $('#replyModal').modal('hide');
                            $('#fullRequestModal').modal('hide');
                            location.reload();
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error("❌ Final error:", error);
                        alert('Error submitting reply: ' + error);
                    }
                });
            }
        });
    }
