<!-- replyModal.jsp - UPDATED WITH PER-PRINTER COMMERCIAL COMMENTS -->
<div class="modal fade" id="replyModal" tabindex="-1" role="dialog" aria-labelledby="replyModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-success-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="replyModalLabel">
                    <i class="fas fa-reply mr-1"></i> Reply - Commercial Comment Per Printer
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4" style="max-height: 600px; overflow-y: auto;">

                <!-- Replace with Existing Commercial? -->
                <div class="form-group mb-4">
                    <label class="text-600 text-90 mb-2">Replace with Existing Commercial?</label>
                    <div class="custom-control custom-radio">
                        <input type="radio" class="custom-control-input" id="replaceYes" name="replaceExisting" value="Yes">
                        <label class="custom-control-label" for="replaceYes">
                            <strong class="text-success-d1">YES</strong> - Use existing commercial terms
                        </label>
                    </div>
                    <div class="custom-control custom-radio">
                        <input type="radio" class="custom-control-input" id="replaceNo" name="replaceExisting" value="No">
                        <label class="custom-control-label" for="replaceNo">
                            <strong class="text-danger-d1">NO</strong> - Suggest new commercial terms
                        </label>
                    </div>
                </div>

                <hr class="brc-grey-l3" />

                <!-- Printers Section -->
                <div class="form-group">
                    <label class="text-600 text-90 mb-1">Select Printers & Add Commercial Comments</label>
                    <p class="text-secondary-m2 text-85 mb-3 italic">Check the printers you want to reply for, add comments for each</p>

                    <!-- Container for printer rows -->
                    <div id="printersContainer" class="border-1 brc-grey-l2 rounded-sm p-3 bgc-grey-l4">
                        <!-- Printer rows will be populated here by JavaScript -->
                        <div class="text-center py-4 text-secondary-m1">
                            <i class="fas fa-spinner fa-spin fa-2x mb-2"></i><br>
                            Loading printers...
                        </div>
                    </div>
                </div>

                <div class="alert bgc-info-l4 border-none border-l-4 brc-info-m2 mt-4 mb-0 py-2">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-info-circle text-info-m1 mr-2"></i>
                        <span class="text-info-d2 text-90"><strong>Note:</strong> This reply will be sent to the Account Manager with per-printer commercial comments.</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-success btn-bold px-4 radius-1" onclick="submitReplyWithComments()">
                    <i class="fas fa-paper-plane mr-1"></i> Send Reply
                </button>
            </div>
        </div>
    </div>
</div>

<link rel="stylesheet" href="../../../../css/replacement/replacement_ammanager_replyModal.css">

<script src="../../../../js/replacement/replacement_ammanager_replyModal.js"></script>
