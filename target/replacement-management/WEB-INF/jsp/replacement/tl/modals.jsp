<!-- ============================================================================ -->
<!-- PRINTER DETAILS MODAL -->
<!-- ============================================================================ -->
<div class="modal fade modal-xl" id="printerDetailsModal" tabindex="-1" role="dialog" aria-labelledby="printerDetailsModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="printerDetailsModalLabel">
                    <i class="fas fa-print mr-1"></i> Printer Details
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" id="printerDetailsBody">
                <!-- Content loaded via AJAX -->
                <div class="text-center py-5">
                    <i class="fas fa-spinner fa-spin fa-3x"></i>
                    <p class="mt-3">Loading...</p>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    <i class="fas fa-times"></i> Close
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================================ -->
<!-- PRINTER HISTORY MODAL -->
<!-- ============================================================================ -->
<div class="modal fade modal-xl" id="printerHistoryModal" tabindex="-1" role="dialog" aria-labelledby="printerHistoryModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-info-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="printerHistoryModalLabel">
                    <i class="fas fa-history mr-1"></i> Printer History
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" id="printerHistoryBody">
                <!-- Content loaded via AJAX -->
                <div class="text-center py-5">
                    <i class="fas fa-spinner fa-spin fa-3x"></i>
                    <p class="mt-3">Loading...</p>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    <i class="fas fa-times"></i> Close
                </button>
            </div>
        </div>
    </div>
</div>
