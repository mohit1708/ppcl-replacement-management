




$(document).ready(function() {
    // Initialize pickup quantity fields based on initial radio button state
    $('.cart-pickup-radio:checked').each(function() {
        var index = $(this).data('index');
        var pickupQtyField = $('#pickupQty' + index);
        if ($(this).val() === 'no') {
            pickupQtyField.prop('disabled', true).val(0);
        } else {
            pickupQtyField.prop('disabled', false);
        }
    });

    // Initialize cartridge quantity fields based on initial radio button state
    $('.send-cartridge-radio:checked').each(function() {
        var index = $(this).data('index');
        var cartQtyField = $('#cartQty' + index);
        if ($(this).val() === 'no') {
            cartQtyField.prop('disabled', true).val(0);
        } else {
            cartQtyField.prop('disabled', false);
        }
    });

    // Toggle pickup quantity field based on cartridge pickup selection
    $('.cart-pickup-radio').change(function() {
        var index = $(this).data('index');
        var pickupQtyField = $('#pickupQty' + index);
        
        if ($(this).val() === 'yes') {
            pickupQtyField.prop('disabled', false).val(1);
        } else {
            pickupQtyField.prop('disabled', true).val(0);
        }
    });

    // Toggle cartridge quantity field based on send cartridge selection
    $('.send-cartridge-radio').change(function() {
        var index = $(this).data('index');
        var cartQtyField = $('#cartQty' + index);
        
        if ($(this).val() === 'yes') {
            cartQtyField.prop('disabled', false).val(1);
        } else {
            cartQtyField.prop('disabled', true).val(0);
        }
    });

    // Prevent non-numeric input in quantity fields
    $('input[type="number"]').on('keypress', function(e) {
        if (e.which < 48 || e.which > 57) {
            e.preventDefault();
        }
    }).on('input', function() {
        this.value = this.value.replace(/[^0-9]/g, '');
    });

    // Sync signatory dropdown to hidden field
    $('#signatorySelect').change(function() {
        $('#signatoryIdHidden').val($(this).val());
    });
    $('#signatoryIdHidden').val($('#signatorySelect').val());

    // Form submission
    $('#printerBookingForm').submit(function(e) {
        // Validate signatory
        if (!$('#signatorySelect').val()) {
            alert('Please select a signatory.');
            e.preventDefault();
            return false;
        }

        // Confirm booking - count order items from DOM
        var totalPrinters = $('.order-item').length || $('[class*="order-item"]').length || 1;
        if (!confirm('Book printer order for ' + totalPrinters + ' printers?\n\nThis will:\n• Generate order logs\n• Book pullback calls (auto-aligned to courier or ERP)\n• Link Replacement Request ID with Printer Order')) {
            e.preventDefault();
            return false;
        }

        // Disable submit button
        $('#bookOrderBtn').prop('disabled', true)
                          .html('<i class="fas fa-spinner fa-spin"></i> Processing...');
    });
});
