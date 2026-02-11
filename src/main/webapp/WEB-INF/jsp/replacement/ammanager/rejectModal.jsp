<!-- Reject Modal -->
<div class="modal fade" id="rejectModal" tabindex="-1" role="dialog" aria-labelledby="rejectModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-danger-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="rejectModalLabel">
                    <i class="fas fa-ban mr-1"></i> Reject Request
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4">
                <div class="form-group mb-3">
                    <label class="text-600 text-90">Rejection Reason <span class="text-danger">*</span></label>
                    <select class="form-control brc-on-focus brc-danger-m1" id="rejectionReason">
                        <option value="">-- Select Reason --</option>
                        <option value="Insufficient Information">Insufficient Information</option>
                        <option value="Commercial Not Viable">Commercial Not Viable</option>
                        <option value="Budget Constraints">Budget Constraints</option>
                        <option value="Timing Issue">Timing Issue</option>
                        <option value="Client Request">Client Request</option>
                        <option value="Other">Other</option>
                    </select>
                </div>

                <div class="form-group mb-0">
                    <label class="text-600 text-90">Comments <span class="text-danger">*</span></label>
                    <textarea class="form-control brc-on-focus brc-danger-m1" id="rejectComments" rows="3" 
                              placeholder="Provide detailed rejection comments"></textarea>
                </div>

                <div class="alert bgc-danger-l4 border-none border-l-4 brc-danger-m2 mt-4 mb-0 py-2">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-exclamation-triangle text-danger-m1 mr-2"></i>
                        <span class="text-danger-d2 text-90"><strong>Warning:</strong> Requester will be notified of rejection. This action cannot be undone.</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger btn-bold px-4 radius-1" onclick="submitReject()">
                    <i class="fas fa-check mr-1"></i> Confirm Rejection
                </button>
            </div>
        </div>
    </div>
</div>
