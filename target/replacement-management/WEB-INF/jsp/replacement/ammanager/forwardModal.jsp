<!-- Forward Modal -->
<div class="modal fade" id="forwardModal" tabindex="-1" role="dialog" aria-labelledby="forwardModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-warning-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="forwardModalLabel">
                    <i class="fas fa-share mr-1"></i> Forward Request to Hierarchy
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4" id="forwardModalBody">
                <div class="form-group mb-3">
                    <label class="text-600 text-90">Forward To <span class="text-danger">*</span></label>
                    <select class="form-control brc-on-focus brc-warning-m1" id="forwardTargetUser" onchange="updateForwardRole()">
                        <option value="">-- Select User --</option>
                    </select>
                    <small class="form-text text-muted" id="forwardRoleDisplay"></small>
                </div>

                <div class="form-group mb-0">
                    <label class="text-600 text-90">Comments</label>
                    <textarea class="form-control brc-on-focus brc-warning-m1" id="forwardComments" rows="3" 
                              placeholder="Add any forwarding comments/notes"></textarea>
                </div>

                <div class="alert bgc-warning-l4 border-none border-l-4 brc-warning-m2 mt-4 mb-0 py-2">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-info-circle text-warning-d1 mr-2"></i>
                        <span class="text-warning-d2 text-90"><strong>Note:</strong> Request will be assigned to selected person. They will be notified.</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning btn-bold px-4 radius-1" onclick="submitForward()">
                    <i class="fas fa-share mr-1"></i> Forward Request
                </button>
            </div>
        </div>
    </div>
</div>
