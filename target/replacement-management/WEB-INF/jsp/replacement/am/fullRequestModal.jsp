<!-- Full Request Details Modal -->
<div class="modal fade modal-xl" id="fullRequestModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-info-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110">
                    <i class="fas fa-file-alt mr-1"></i> Full Request Details - <span id="fullRequestReqId"></span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" id="fullRequestBody">
                <div class="text-center py-5">
                    <i class="fas fa-spinner fa-spin fa-3x"></i>
                    <p class="mt-3">Loading...</p>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    Close
                </button>
                <button type="button" class="btn btn-success" onclick="submitAMDecision()">
                    <i class="fas fa-check"></i> Submit Decision
                </button>
            </div>
        </div>
    </div>
</div>
