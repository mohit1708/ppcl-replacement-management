// Application JavaScript for Replacement Management System

$(document).ready(function() {

    // Form validation
    $('form').on('submit', function(e) {
        const form = $(this)[0];
        if (!form.checkValidity()) {
            e.preventDefault();
            e.stopPropagation();
        }
        $(this).addClass('was-validated');
    });

    // Auto-dismiss alerts after 5 seconds
    setTimeout(function() {
        $('.alert:not(.alert-permanent)').fadeOut('slow');
    }, 5000);

});

// Show loading spinner
function showLoader() {
    if ($('#globalLoader').length === 0) {
        $('body').append(`
            <div id="globalLoader" style="position:fixed;top:0;left:0;width:100%;height:100%;
                 background:rgba(0,0,0,0.5);z-index:9999;display:flex;align-items:center;
                 justify-content:center;">
                <div class="spinner-border text-light" role="status">
                    <span class="sr-only">Loading...</span>
                </div>
            </div>
        `);
    }
}

// Hide loading spinner
function hideLoader() {
    $('#globalLoader').remove();
}

// Show success message
function showSuccess(message) {
    showMessage(message, 'success');
}

// Show error message
function showError(message) {
    showMessage(message, 'danger');
}

// Show message
function showMessage(message, type) {
    const alert = $(`
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    `);

    $('.container-fluid').prepend(alert);

    setTimeout(function() {
        alert.fadeOut('slow', function() {
            $(this).remove();
        });
    }, 5000);
}

// Format date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const options = { year: 'numeric', month: 'short', day: '2-digit' };
    return date.toLocaleDateString('en-US', options);
}

// Confirm action
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}
