</div><!-- /.main-content -->
</div><!-- /.main-container -->
</div><!-- /.body-container -->

<!-- App Alert Modal -->
<div class="modal fade" id="appAlertModal" tabindex="-1" role="dialog" data-backdrop="false" style="z-index:99999;">
    <div class="modal-dialog modal-dialog-centered" role="document" style="max-width:440px;">
        <div class="modal-content border-0 shadow" style="border-radius:12px;overflow:hidden;">
            <div class="modal-header py-3 px-4 border-0" id="appAlertHeader">
                <h6 class="modal-title font-weight-bold text-white" id="appAlertTitle">
                    <i id="appAlertIcon" class="mr-2"></i><span id="appAlertTitleText"></span>
                </h6>
            </div>
            <div class="modal-body px-4 py-4">
                <p class="mb-0" id="appAlertMessage" style="font-size:15px;line-height:1.6;"></p>
            </div>
            <div class="modal-footer border-0 px-4 pb-3 pt-0">
                <button type="button" class="btn px-4" id="appAlertOkBtn" data-dismiss="modal">OK</button>
            </div>
        </div>
    </div>
</div>

<!-- App Confirm Modal -->
<div class="modal fade" id="appConfirmModal" tabindex="-1" role="dialog" data-backdrop="false" style="z-index:99999;">
    <div class="modal-dialog modal-dialog-centered" role="document" style="max-width:440px;">
        <div class="modal-content border-0 shadow" style="border-radius:12px;overflow:hidden;">
            <div class="modal-header py-3 px-4 border-0" style="background:#007bff;">
                <h6 class="modal-title font-weight-bold text-white">
                    <i class="fas fa-question-circle mr-2"></i><span>Confirm</span>
                </h6>
            </div>
            <div class="modal-body px-4 py-4">
                <p class="mb-0" id="appConfirmMessage" style="font-size:15px;line-height:1.6;"></p>
            </div>
            <div class="modal-footer border-0 px-4 pb-3 pt-0">
                <button type="button" class="btn btn-secondary px-4" data-dismiss="modal">No</button>
                <button type="button" class="btn btn-primary px-4" id="appConfirmYesBtn">Yes</button>
            </div>
        </div>
    </div>
</div>

<!-- include ace.js -->
<script src="<%= request.getContextPath() %>/ace-v3.1.1/dist/js/ace.js"></script>

<script src="<%= request.getContextPath() %>/js/replacement/app.js"></script>
<script>
    // Hide page loader when DOM is ready
    $(document).ready(function() {
        $('#pageLoader').addClass('hidden');
    });
    
    // Also hide on window load (for images/resources)
    $(window).on('load', function() {
        $('#pageLoader').addClass('hidden');
    });

    // ========== Global Alert Popup ==========
    function showAppAlert(message, type) {
        type = type || 'info';
        message = message.replace(/^[✅❌⚠️🔔]\s*/, '');

        var config = {
            success: { bg: '#28a745', icon: 'fas fa-check-circle',        title: 'Success',  btn: 'btn-success' },
            danger:  { bg: '#dc3545', icon: 'fas fa-times-circle',        title: 'Error',    btn: 'btn-danger'  },
            warning: { bg: '#e6a817', icon: 'fas fa-exclamation-triangle', title: 'Warning',  btn: 'btn-warning' },
            info:    { bg: '#007bff', icon: 'fas fa-info-circle',          title: 'Info',     btn: 'btn-primary' }
        };
        var c = config[type] || config.info;

        $('#appAlertHeader').css('background', c.bg);
        $('#appAlertIcon').attr('class', c.icon + ' mr-2');
        $('#appAlertTitleText').text(c.title);
        $('#appAlertMessage').text(message);
        $('#appAlertOkBtn').attr('class', 'btn px-4 ' + c.btn);

        $('#appAlertModal').modal('show');
    }

    // ========== Global Confirm Popup ==========
    function showAppConfirm(message, onConfirm) {
        $('#appConfirmMessage').text(message);
        $('#appConfirmYesBtn').off('click').on('click', function() {
            $('#appConfirmModal').modal('hide');
            if (typeof onConfirm === 'function') onConfirm();
        });
        $('#appConfirmModal').modal('show');
    }
</script>
</body>
</html>
