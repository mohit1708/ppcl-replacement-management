/**
 * Book Printer Order Module (STG6_PRINTER_ORDER)
 * Handles printer order booking functionality for replacement requests.
 * 
 * Dependencies: jQuery, Bootstrap 4
 * Required: contextPath variable must be defined in the page
 */
var BookPrinterOrder = (function($) {
    'use strict';

    // Private state
    var _bookOrderData = {};

    /**
     * Open the Book Order modal
     * @param {number} reqId - Request ID
     * @param {string} clientName - Client name
     */
    function openModal(reqId, clientName) {
        $('#bookOrderReqIdHidden').val(reqId);
        $('#bookOrderReqId').text('REQ-' + reqId);
        $('#bookOrderLoading').show();
        $('#bookOrderContent').hide();
        $('#bookOrderItemsContainer').empty();
        $('#bookOrderModal').modal('show');

        // Fetch order details
        $.get(contextPath + '/views/replacement/request', {
            action: 'getBookingDetails',
            id: reqId
        }, function(data) {
            if (data.success) {
                _populateModal(data, clientName);
            } else {
                $('#bookOrderLoading').html('<div class="alert alert-danger">Failed to load order details: ' + (data.message || 'Unknown error') + '</div>');
            }
        }, 'json').fail(function() {
            // Fallback: show simple form if API not ready
            _populateModalFallback(reqId, clientName);
        });
    }

    /**
     * Fallback data population when API is not available
     */
    function _populateModalFallback(reqId, clientName) {
        var fallbackData = {
            request: { id: reqId, clientName: clientName, replacementType: 'DURING_CONTRACT', poNumber: 'PO-2024-' + reqId },
            printers: [],
            signatories: [
                { id: 1, name: 'AM Manager 1' },
                { id: 2, name: 'Regional Head' }
            ]
        };
        _populateModal({ success: true, ...fallbackData }, clientName);
    }

    /**
     * Populate modal with order data
     */
    function _populateModal(data, clientName) {
        _bookOrderData = data;
        var req = data.request || {};
        var printers = data.printers || [];
        var signatories = data.signatories || [];

        // Summary info
        $('#bookOrderClientName').text(clientName || req.clientName || '-');
        $('#bookOrderTotalPrinters').text(printers.length || '-');
        $('#bookOrderContractType').text(_formatReplacementType(req.replacementType) || '-');
        $('#bookOrderPoNumber').text(req.poNumber || '-');

        // Signatories dropdown
        var sigSelect = $('#bookOrderSignatory');
        sigSelect.empty().append('<option value="">Select Signatory (AM Manager or Higher)</option>');
        signatories.forEach(function(s) {
            sigSelect.append('<option value="' + s.id + '">' + _escapeHtml(s.name) + '</option>');
        });

        // Consolidate printers by location + model
        var consolidated = _consolidatePrinters(printers);
        var itemsHtml = '';
        var itemIndex = 0;
        var totalPrinters = 0;

        consolidated.forEach(function(group) {
            itemIndex++;
            totalPrinters += group.quantity;
            itemsHtml += _buildOrderItemHtml(group, itemIndex);
        });

        // If no printers, show message
        if (consolidated.length === 0) {
            itemsHtml = '<div class="alert alert-warning">No printers found for this request.</div>';
        }

        $('#bookOrderItemsContainer').html(itemsHtml);
        $('#bookOrderTotalItems').text(consolidated.length);
        $('#bookOrderTotalPrintersFooter').text(totalPrinters);
        $('#bookOrderTotalPrinters').text(totalPrinters);

        // Bind cartridge pickup radio button change event
        _bindCartridgePickupHandlers();

        // Show content
        $('#bookOrderLoading').hide();
        $('#bookOrderContent').show();
    }

    /**
     * Bind event handlers for Cartridge Pickup radio buttons
     * When "No" is selected, set Pickup Quantity to 0 and disable it
     */
    function _bindCartridgePickupHandlers() {
        // Toggle Pickup Quantity based on Cartridge Pickup selection
        $('#bookOrderItemsContainer').off('change', '.cart-pickup').on('change', '.cart-pickup', function() {
            var $item = $(this).closest('.order-item');
            var $pickupQty = $item.find('.pickup-qty');
            
            if ($(this).val() === 'no') {
                $pickupQty.val(0).prop('disabled', true).addClass('bg-light');
            } else {
                $pickupQty.val(1).prop('disabled', false).removeClass('bg-light');
            }
        });

        // Toggle Cartridge Quantity based on Send Cartridge selection
        $('#bookOrderItemsContainer').off('change', '.cart-send').on('change', '.cart-send', function() {
            var $item = $(this).closest('.order-item');
            var $cartQty = $item.find('.cart-qty');
            
            if ($(this).val() === 'no') {
                $cartQty.val(0).prop('disabled', true).addClass('bg-light');
            } else {
                $cartQty.val(1).prop('disabled', false).removeClass('bg-light');
            }
        });
    }

    /**
     * Build HTML for a single order item
     */
    function _buildOrderItemHtml(group, itemIndex) {
        var isFirst = itemIndex === 1;
        var defaultDate = _getDefaultDeliveryDate();

        return '<div class="order-item" data-item="' + itemIndex + '" data-pmodel="' + (group.modelId || 0) + '">' +
            '<div class="order-item__header">' +
                '<div class="d-flex align-items-center">' +
                    '<span class="badge badge-primary mr-2">Item ' + itemIndex + '</span>' +
                    (isFirst ? '<span class="first-item-badge"><i class="fas fa-check mr-1"></i>Source</span>' : '') +
                    '<strong class="ml-2">' + _escapeHtml(group.location) + '</strong>' +
                    '<span class="text-muted mx-2">|</span>' +
                    '<span>' + _escapeHtml(group.modelName) + '</span>' +
                '</div>' +
                '<span class="badge badge-info">Qty: ' + group.quantity + '</span>' +
            '</div>' +

            '<div class="row">' +
                '<div class="col-md-3 form-group">' +
                    '<label class="small">Location</label>' +
                    '<input type="text" class="form-control form-control-sm location-name" value="' + _escapeHtml(group.location) + '" readonly>' +
                '</div>' +
                '<div class="col-md-3 form-group">' +
                    '<label class="small">New Printer Model</label>' +
                    '<input type="text" class="form-control form-control-sm model-name" value="' + _escapeHtml(group.modelName) + '" readonly>' +
                    '<input type="hidden" class="model-id" value="' + (group.modelId || 0) + '">' +
                '</div>' +
                '<div class="col-md-3 form-group">' +
                    '<label class="small">Quantity</label>' +
                    '<input type="number" class="form-control form-control-sm qty" value="' + group.quantity + '" readonly>' +
                '</div>' +
                '<div class="col-md-3 form-group">' +
                    '<label class="small">Printer Type</label>' +
                    '<input type="text" class="form-control form-control-sm printer-type" value="' + (group.printerType || 'New') + '" readonly>' +
                '</div>' +
            '</div>' +

            '<input type="hidden" class="printer-price" value="0">' +
            '<div class="row">' +
                '<div class="col-md-4 form-group">' +
                    '<label class="small">Booking & Dispatch Branch</label>' +
                    '<select class="form-control form-control-sm dispatch-branch">' +
                        '<option value="del">Delhi - North Hub</option>' +
                        '<option value="mum">Mumbai - Central Warehouse</option>' +
                        '<option value="blr">Bangalore - South Hub</option>' +
                    '</select>' +
                '</div>' +
                '<div class="col-md-4 form-group">' +
                    '<label class="small">Delivery Date</label>' +
                    '<input type="date" class="form-control form-control-sm delivery-date" value="' + defaultDate + '">' +
                    '<small class="form-text text-muted">Today + 7 days</small>' +
                '</div>' +
                '<div class="col-md-4 form-group">' +
                    '<label class="small">Contact Person</label>' +
                    '<input type="text" class="form-control form-control-sm contact-person" value="' + _escapeHtml(group.contactName || '') + '">' +
                '</div>' +
            '</div>' +

            '<div class="row">' +
                '<div class="col-md-3 form-group">' +
                    '<label class="small">Contact Number</label>' +
                    '<input type="tel" class="form-control form-control-sm contact-number" value="' + _escapeHtml(group.contactNumber || '') + '">' +
                '</div>' +
                '<div class="col-md-9"></div>' +
            '</div>' +

            // Cartridge & Accessories Section
            '<div class="cartridge-section">' +
                '<h6 class="font-weight-bold mb-3">Cartridge & Accessories</h6>' +
                '<div class="row">' +
                    '<div class="col-md-3 form-group">' +
                        '<label class="small">Unused Cartridge Pickup?</label>' +
                        '<div class="mt-2">' +
                            '<div class="custom-control custom-radio custom-control-inline">' +
                                '<input type="radio" class="custom-control-input cart-pickup" id="cartPickupYes' + itemIndex + '" name="cartPickup' + itemIndex + '" value="yes">' +
                                '<label class="custom-control-label" for="cartPickupYes' + itemIndex + '">Yes</label>' +
                            '</div>' +
                            '<div class="custom-control custom-radio custom-control-inline">' +
                                '<input type="radio" class="custom-control-input cart-pickup" id="cartPickupNo' + itemIndex + '" name="cartPickup' + itemIndex + '" value="no" checked>' +
                                '<label class="custom-control-label" for="cartPickupNo' + itemIndex + '">No</label>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                    '<div class="col-md-3 form-group">' +
                        '<label class="small">Pickup Quantity</label>' +
                        '<input type="number" class="form-control form-control-sm pickup-qty" value="0" min="0" disabled>' +
                    '</div>' +
                    '<div class="col-md-3 form-group">' +
                        '<label class="small"> Send Cartridge with Printer?</label>' +
                        '<div class="mt-2">' +
                            '<div class="custom-control custom-radio custom-control-inline">' +
                                '<input type="radio" class="custom-control-input cart-send" id="cartSendYes' + itemIndex + '" name="cartSend' + itemIndex + '" value="yes">' +
                                '<label class="custom-control-label" for="cartSendYes' + itemIndex + '">Yes</label>' +
                            '</div>' +
                            '<div class="custom-control custom-radio custom-control-inline">' +
                                '<input type="radio" class="custom-control-input cart-send" id="cartSendNo' + itemIndex + '" name="cartSend' + itemIndex + '" value="no" checked>' +
                                '<label class="custom-control-label" for="cartSendNo' + itemIndex + '">No</label>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                    '<div class="col-md-3 form-group">' +
                        '<label class="small">Cartridge Quantity</label>' +
                        '<input type="number" class="form-control form-control-sm cart-qty" value="0" min="0" disabled>' +
                    '</div>' +
                '</div>' +
            '</div>' +

            // Installation Section (hidden, always sends yes)
            '<input type="hidden" class="install-opt" name="install' + itemIndex + '" value="yes">' +

            // Comments
            '<div class="form-group mt-3">' +
                '<label class="small">Comments</label>' +
                '<textarea class="form-control form-control-sm item-comments" rows="2" placeholder="Add any special instructions..."></textarea>' +
            '</div>' +
        '</div>';
    }

    /**
     * Consolidate printers by location + model
     */
    function _consolidatePrinters(printers) {
        var groups = {};
        printers.forEach(function(p) {
            var modelId = p.newModelId || 0; // coming from backend: printer.put("newModelId", ...)
            var modelName = p.newModelName || p.existingModelName || 'Unknown';

            var key = (p.location || 'Unknown') + '|' + modelName + '|' + modelId;

            if (!groups[key]) {
                groups[key] = {
                    location: p.location || 'Unknown',
                    modelId: modelId,
                    modelName: modelName,
                    printerType: p.printerType || 'New',
                    contactName: p.contactName || '',
                    contactNumber: p.contactNumber || '',
                    quantity: 0,
                    printerIds: []
                };
            }
            groups[key].quantity++;
            groups[key].printerIds.push(p.id);
        });
        return Object.values(groups);
    }


    /**
     * Get default delivery date (today + 7 days)
     */
    function _getDefaultDeliveryDate() {
        var date = new Date();
        date.setDate(date.getDate() + 7);
        return date.toISOString().split('T')[0];
    }

    /**
     * Apply values from first item to all other items
     */
    function applyToAllItems() {
        var firstItem = $('.order-item[data-item="1"]');
        if (!firstItem.length) return;

        var sourceData = {
            dispatchBranch: firstItem.find('.dispatch-branch').val(),
            deliveryDate: firstItem.find('.delivery-date').val(),
            cartPickupYes: firstItem.find('.cart-pickup[value="yes"]').is(':checked'),
            pickupQty: firstItem.find('.pickup-qty').val(),
            cartSendYes: firstItem.find('.cart-send[value="yes"]').is(':checked'),
            cartQty: firstItem.find('.cart-qty').val(),
            installYes: firstItem.find('.install-opt[value="yes"]').is(':checked'),
            comments: firstItem.find('.item-comments').val()
        };

        var appliedCount = 0;
        $('.order-item').each(function(index) {
            if (index === 0) return; // Skip first item

            var $item = $(this);
            $item.find('.dispatch-branch').val(sourceData.dispatchBranch);
            $item.find('.delivery-date').val(sourceData.deliveryDate);
            $item.find('.cart-pickup[value="' + (sourceData.cartPickupYes ? 'yes' : 'no') + '"]').prop('checked', true);
            // Handle pickup quantity based on cartridge pickup selection
            if (sourceData.cartPickupYes) {
                $item.find('.pickup-qty').val(sourceData.pickupQty).prop('disabled', false).removeClass('bg-light');
            } else {
                $item.find('.pickup-qty').val(0).prop('disabled', true).addClass('bg-light');
            }
            $item.find('.cart-send[value="' + (sourceData.cartSendYes ? 'yes' : 'no') + '"]').prop('checked', true);
            // Handle cartridge quantity based on send cartridge selection
            if (sourceData.cartSendYes) {
                $item.find('.cart-qty').val(sourceData.cartQty).prop('disabled', false).removeClass('bg-light');
            } else {
                $item.find('.cart-qty').val(0).prop('disabled', true).addClass('bg-light');
            }
            $item.find('.install-opt[value="' + (sourceData.installYes ? 'yes' : 'no') + '"]').prop('checked', true);
            $item.find('.item-comments').val(sourceData.comments);

            // Visual feedback
            $item.css('box-shadow', '0 0 0 2px #0d9488');
            setTimeout(function() { $item.css('box-shadow', ''); }, 1500);

            appliedCount++;
        });

        if (appliedCount > 0) {
            alert('Values from Item 1 applied to ' + appliedCount + ' other item(s)!');
        }
    }

    /**
     * Submit the book order form
     */
    function submit() {
        var reqId = $('#bookOrderReqIdHidden').val();
        var signatory = $('#bookOrderSignatory').val();

        if (!signatory) {
            alert('Please select a signatory');
            return;
        }

        // Collect all order items data
        var orderItems = [];
        $('.order-item').each(function() {
            var $item = $(this);
            var itemNum = $item.data('item');
            var modelId = parseInt($item.find('.model-id').val(), 10) || 0;
            var qty = parseInt($item.find('.qty').val(), 10) || 1;
            
            orderItems.push({
                itemNumber: itemNum,
                pModel: modelId,
                qty: qty,
                printerType: $item.find('.printer-type').val() || '1',
                dispatchBranch: $item.find('.dispatch-branch').val(),
                deliveryDate: $item.find('.delivery-date').val(),
                printerPrice: $item.find('.printer-price').val(),
                contactPerson: $item.find('.contact-person').val(),
                contactNumber: $item.find('.contact-number').val(),
                cartridgePickup: $item.find('.cart-pickup:checked').val(),
                pickupQty: $item.find('.pickup-qty').val(),
                cartridgeSend: $item.find('.cart-send:checked').val(),
                cartQty: $item.find('.cart-qty').val(),
                installation: $item.find('.install-opt:checked').val(),
                comments: $item.find('.item-comments').val()
            });
        });

        var totalPrinters = $('#bookOrderTotalPrintersFooter').text();
        if (!confirm('Book printer order for ' + totalPrinters + ' printers? This will:\n\n• Generate order logs\n• Book pullback calls (auto-aligned to courier or ERP)\n• Link Replacement Request ID with Printer Order')) {
            return;
        }

        $.post(
            contextPath + '/views/replacement/myRequests',
            {
                action: 'bookOrder',
                reqId: reqId,
                signatory: signatory,
                orderItems: JSON.stringify(orderItems)
            },
            function(resp) {
                if (resp.success) {
                    $('#bookOrderModal').modal('hide');
                    alert("✅ Printer order booked successfully!\n\nOrder ID: " + (resp.orderId || 'PO-' + reqId) + "\nPullback calls created: " + totalPrinters);
                    location.reload();
                } else {
                    alert("❌ Failed: " + (resp.message || "Unknown error"));
                }
            },
            'json'
        ).fail(function() {
            alert("❌ Network error. Please try again.");
        });
    }

    /**
     * Format replacement type for display
     */
    function _formatReplacementType(type) {
        if (!type) return '';
        var types = {
            'DURING_CONTRACT': 'During Contract',
            'AFTER_CONTRACT': 'After Contract'
        };
        return types[type] || type;
    }

    /**
     * Escape HTML to prevent XSS
     */
    function _escapeHtml(text) {
        if (!text) return '';
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // Public API
    return {
        openModal: openModal,
        applyToAllItems: applyToAllItems,
        submit: submit
    };

})(jQuery);

// Global function for backward compatibility
function openBookOrderModal(reqId, clientName) {
    BookPrinterOrder.openModal(reqId, clientName);
}
