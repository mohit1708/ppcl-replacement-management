<!-- Full Request Details Modal with Actions -->
<div class="modal fade" id="fullRequestModal" tabindex="-1" role="dialog" aria-labelledby="fullRequestModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="fullRequestModalLabel">
                    <i class="fas fa-file-alt mr-1"></i> Replacement Request - AM Manager
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-0" id="fullRequestBody">
                <div class="text-center py-5 text-primary-m1">
                    <i class="fas fa-spinner fa-spin fa-3x mb-3"></i>
                    <p class="text-120">Loading...</p>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Close</button>
                <div class="ml-auto">
                    <button type="button" class="btn btn-outline-primary btn-bold px-3 radius-1 mr-1" onclick="replyToRequest()">
                        <i class="fas fa-reply mr-1"></i> Reply
                    </button>
                    <button type="button" class="btn btn-outline-danger btn-bold px-3 radius-1 mr-1" onclick="rejectRequest()">
                        <i class="fas fa-times-circle mr-1"></i> Reject
                    </button>
                    <button type="button" class="btn btn-outline-info btn-bold px-3 radius-1 mr-1" onclick="forwardRequest()">
                        <i class="fas fa-share mr-1"></i> Forward
                    </button>
                    <button type="button" class="btn btn-success btn-bold px-4 radius-1" onclick="submitAMManagerRequest()">
                        <i class="fas fa-paper-plane mr-1"></i> Submit to AM
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
