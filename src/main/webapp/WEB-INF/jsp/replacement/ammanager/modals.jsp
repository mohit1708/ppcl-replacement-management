<style>
    #commercialActionModal .commercial-printer-card {
        border: 1px solid #dee2e6 !important;
        box-shadow: none !important;
        margin-bottom: 0.75rem !important;
        border-radius: 4px !important;
        overflow: hidden !important;
    }
    #commercialActionModal .commercial-printer-card > .card-header {
        display: flex !important;
        align-items: center !important;
        justify-content: space-between !important;
        border-bottom: 1px solid #dee2e6 !important;
        padding: 6px 10px !important;
        background-color: #f8f9fa !important;
    }
    #commercialActionModal .commercial-printer-card > .card-body {
        padding: 10px 12px !important;
    }
    #commercialActionModal .tl-rec-box {
        background: #f0f4f8;
        border-left: 3px solid #2c7be5;
        border-radius: 3px;
        padding: 8px 10px;
        margin-bottom: 10px;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    #commercialActionModal .tl-rec-box .rec-text {
        font-size: 0.8rem;
        color: #495057;
    }
    #commercialActionModal .tl-rec-box .rec-label {
        font-size: 0.7rem;
        text-transform: uppercase;
        font-weight: 700;
        color: #2c7be5;
        margin-bottom: 2px;
    }
    #commercialActionModal .modal-header .close {
        position: absolute;
        right: 1rem;
        top: 0.5rem;
        font-size: 1.5rem;
        opacity: 0.9;
        color: #fff;
        text-shadow: none;
    }
    #commercialActionModal .decision-row {
        display: flex;
        align-items: flex-start;
        gap: 12px;
    }
    #commercialActionModal .decision-row .comments-col {
        flex: 1;
    }
    #commercialActionModal .printer-select-inline {
        margin-top: 6px;
    }
    #commercialActionModal .badge-sm {
        font-size: 0.7rem;
        padding: 3px 6px;
    }
    #commercialActionModal .btn-edit-sm {
        font-size: 0.7rem;
        padding: 2px 8px;
        background: #343a40;
        color: #fff;
        border-color: #343a40;
    }
    #commercialActionModal .btn-edit-sm:hover {
        background: #23272b;
        border-color: #1d2124;
    }
    #commercialActionModal .modal-dialog {
        max-width: 1000px !important;
        width: 1000px !important;
    }
    #commercialActionModal .hdr-meta {
        font-size: 0.75rem;
        color: #6c757d;
    }
</style>

<!-- ============================================
FULL REQUEST MODAL
============================================ -->
<div class="modal fade modal-xl" id="fullRequestModal" tabindex="-1" role="dialog" aria-labelledby="fullRequestModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="fullRequestModalLabel">
                    <i class="fas fa-file-alt mr-1"></i> Full Request Details
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-0">
                <div id="fullRequestBody" class="p-4">
                    <div class="text-center py-5 text-primary-m1">
                        <i class="fas fa-spinner fa-spin fa-3x mb-3"></i>
                        <p class="text-120">Loading...</p>
                    </div>
                </div>
                
                <!-- Comment History Section -->
                <div id="commentHistoryWrapper" class="px-4 pb-4">
                    <button type="button" class="btn btn-outline-info btn-bold btn-sm radius-1" onclick="toggleCommentHistory()">
                        <i class="fas fa-history mr-1"></i> View Comment History
                    </button>
                    <div id="commentHistorySection" style="display:none;" class="mt-3">
                        <h6 class="text-600 text-primary-d2 mb-3"><i class="fas fa-comments mr-1"></i> Comment History</h6>
                        <div id="commentHistoryLoading" class="text-center py-4" style="display:none;">
                            <i class="fas fa-spinner fa-spin fa-2x text-primary-m1"></i>
                        </div>
                        <div class="table-responsive border-1 brc-grey-l2 rounded-sm">
                            <table class="table table-sm table-bordered mb-0" id="commentHistoryTable" style="display:none;">
                                <thead class="bgc-grey-l4 text-dark-m3">
                                    <tr>
                                        <th class="border-0">User</th>
                                        <th class="border-0">Stage</th>
                                        <th class="border-0">Comments</th>
                                        <th class="border-0">Date/Time</th>
                                    </tr>
                                </thead>
                                <tbody id="commentHistoryBody" class="text-95">
                                </tbody>
                            </table>
                        </div>
                        <div id="noCommentsMsg" class="alert bgc-grey-l4 text-secondary-d2 border-none border-l-4 brc-grey-m1 mt-3" style="display:none;">
                            No comments found for this request.
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Close</button>
                <div class="ml-auto">
                    <button type="button" class="btn btn-outline-success btn-bold px-3 radius-1 mr-1" onclick="replyToRequest()">
                        <i class="fas fa-reply mr-1"></i> Submit Commercial
                    </button>
                    <button type="button" class="btn btn-outline-danger btn-bold px-3 radius-1 mr-1" onclick="rejectRequest()">
                        <i class="fas fa-times-circle mr-1"></i> Reject
                    </button>
                    <button type="button" class="btn btn-outline-warning btn-bold px-3 radius-1 mr-1" onclick="forwardRequest()">
                        <i class="fas fa-share mr-1"></i> Forward
                    </button>
                    <button type="button" class="btn btn-outline-info btn-bold px-3 radius-1 mr-1" onclick="viewCommunicationLogs(currentReqId)">
                        <i class="fas fa-history mr-1"></i> View Logs
                    </button>
                    <button type="button" class="btn btn-success btn-bold px-4 radius-1" onclick="submitAMManagerRequest()">
                        <i class="fas fa-paper-plane mr-1"></i> Submit to AM
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
CURRENT COMMERCIALS MODAL
============================================ -->
<div class="modal fade modal-xl" id="currentCommercialsModal" tabindex="-1" role="dialog" aria-labelledby="currentCommercialsModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-info-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="currentCommercialsModalLabel">
                    <i class="fas fa-dollar-sign mr-1"></i> Current Commercials (Last 6 Months)
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-0">
                <div id="currentCommercialsBody" class="p-4">
                    <div class="text-center py-5 text-info-m1">
                        <i class="fas fa-spinner fa-spin fa-3x mb-3"></i>
                        <p class="text-120">Loading...</p>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-secondary btn-bold px-4 radius-1" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
REPLY MODAL
============================================ -->
<div class="modal fade" id="replyModal" tabindex="-1" role="dialog" aria-labelledby="replyModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-success-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="replyModalLabel">
                    <i class="fas fa-reply mr-1"></i> Reply - Commercial Proposal
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4">
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

                <div class="form-group mb-0">
                    <label for="commercialComments" class="text-600 text-90 mb-1">Commercial Comments <span class="text-danger">*</span></label>
                    <p class="text-secondary-m2 text-85 italic mb-2">Required when selecting "NO". Include proposed rates, rental, or special terms.</p>
                    <textarea class="form-control brc-on-focus brc-success-m1" id="commercialComments" rows="4" placeholder="Provide commercial details..."></textarea>
                </div>

                <div class="alert bgc-success-l4 border-none border-l-4 brc-success-m2 mt-4 mb-0 py-2">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-info-circle text-success-m1 mr-2"></i>
                        <span class="text-success-d2 text-90"><strong>Note:</strong> This reply will be sent to the Account Manager for further review.</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-success btn-bold px-4 radius-1" onclick="submitReply()">
                    <i class="fas fa-paper-plane mr-1"></i> Send Reply
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
REJECT MODAL
============================================ -->
<div class="modal fade" id="rejectModal" tabindex="-1" role="dialog" aria-labelledby="rejectModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-danger-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="rejectModalLabel">
                    <i class="fas fa-times-circle mr-1"></i> Reject Replacement Request
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4">
                <div class="alert bgc-danger-l4 border-none border-l-4 brc-danger-m2 mb-4 py-2">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-exclamation-triangle text-danger-m1 mr-3 fa-2x"></i>
                        <span class="text-danger-d2 text-90"><strong>Warning:</strong> Rejecting this request will cancel the entire replacement flow.</span>
                    </div>
                </div>

                <div class="form-group mb-3">
                    <label for="rejectionReason" class="text-600 text-90 mb-1">Rejection Reason <span class="text-danger">*</span></label>
                    <select class="form-control brc-on-focus brc-danger-m1" id="rejectionReason" required>
                        <option value="">-- Select Reason --</option>
                        <option value="COMMERCIAL_NOT_VIABLE">Commercial terms not viable</option>
                        <option value="BUDGET_CONSTRAINTS">Budget constraints</option>
                        <option value="NO_BUSINESS_CASE">No Business Case</option>
                        <option value="HIGH_COST">Cost Too High</option>
                        <option value="INCOMPATIBLE">Incompatible with Current Fleet</option>
                        <option value="DUPLICATE">Duplicate Request</option>
                        <option value="PROCESS_ISSUE">Process/Documentation Issue</option>
                        <option value="OTHER">Other</option>
                    </select>
                </div>

                <div class="form-group mb-0">
                    <label for="rejectComments" class="text-600 text-90 mb-1">Comments <span class="text-danger">*</span></label>
                    <textarea class="form-control brc-on-focus brc-danger-m1" id="rejectComments" rows="3" required placeholder="Provide detailed rejection reason..."></textarea>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger btn-bold px-4 radius-1" onclick="submitReject()">
                    <i class="fas fa-ban mr-1"></i> Confirm Rejection
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
FORWARD MODAL
============================================ -->
<div class="modal fade" id="forwardModal" tabindex="-1" role="dialog" aria-labelledby="forwardModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-warning-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="forwardModalLabel">
                    <i class="fas fa-share mr-1"></i> Forward Request
                </h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4">
                <div class="form-group mb-3">
                    <label for="forwardTargetUser" class="text-600 text-90 mb-1">Select User to Forward To <span class="text-danger">*</span></label>
                    <select class="form-control brc-on-focus brc-warning-m1" id="forwardTargetUser" onchange="updateForwardRole()">
                        <option value="">-- Select User --</option>
                        <c:forEach items="${reportingHierarchy}" var="manager">
                            <option value="${manager.id}" data-role="${fn:escapeXml(manager.role)}">${fn:escapeXml(manager.name)} (${fn:escapeXml(manager.role)})</option>
                        </c:forEach>
                        <c:if test="${empty reportingHierarchy}">
                            <option value="" disabled>No managers found in hierarchy</option>
                        </c:if>
                    </select>
                    <small id="forwardRoleDisplay" class="form-text text-muted italic">Role: </small>
                </div>

                <div class="form-group mb-0">
                    <label for="forwardComments" class="text-600 text-90 mb-1">Comments (Optional)</label>
                    <textarea class="form-control brc-on-focus brc-warning-m1" id="forwardComments" rows="3" placeholder="Add any notes for the recipient..."></textarea>
                </div>

                <div class="alert bgc-warning-l4 border-none border-l-4 brc-warning-m2 mt-4 mb-0 py-2">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-info-circle text-warning-d1 mr-2"></i>
                        <span class="text-warning-d2 text-90"><strong>Note:</strong> The request will be forwarded with your comments. The recipient will be notified.</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning btn-bold px-4 radius-1" onclick="submitForward()">
                    <i class="fas fa-paper-plane mr-1"></i> Forward Request
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
COMMUNICATION LOGS MODAL
============================================ -->
<div class="modal fade modal-lg" id="commLogsModal" tabindex="-1" role="dialog" aria-labelledby="commLogsModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-secondary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="commLogsModalLabel">
                    <i class="fas fa-history mr-1"></i> Communication Audit Trail
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-0">
                <div id="commLogsBody" class="p-4">
                    <div class="text-center py-5 text-secondary-m1">
                        <i class="fas fa-spinner fa-spin fa-3x mb-3"></i>
                        <p class="text-120">Loading logs...</p>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-secondary btn-bold px-4 radius-1" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
COMMERCIAL MODAL (For Edit - Future Use)
============================================ -->
<div class="modal fade" id="commercialModal" tabindex="-1" role="dialog" aria-labelledby="commercialModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="commercialModalLabel">
                    <i class="fas fa-edit mr-1"></i> Edit Commercial Details
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4">
                <div class="form-group mb-3">
                    <label for="commCost" class="text-600 text-90 mb-1">Cost per page (Black) <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <div class="input-group-prepend">
                            <span class="input-group-text bgc-white brc-primary-m3 text-primary">₹</span>
                        </div>
                        <input type="number" step="0.01" class="form-control brc-on-focus brc-primary-m1" id="commCost" placeholder="0.00">
                    </div>
                </div>

                <div class="form-group mb-3">
                    <label for="commColorRate" class="text-600 text-90 mb-1">Cost per page (Color) <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <div class="input-group-prepend">
                            <span class="input-group-text bgc-white brc-primary-m3 text-primary">₹</span>
                        </div>
                        <input type="number" step="0.01" class="form-control brc-on-focus brc-primary-m1" id="commColorRate" placeholder="0.00">
                    </div>
                </div>

                <div class="form-group mb-3">
                    <label for="commRental" class="text-600 text-90 mb-1">Monthly Rental <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <div class="input-group-prepend">
                            <span class="input-group-text bgc-white brc-primary-m3 text-primary">₹</span>
                        </div>
                        <input type="number" step="0.01" class="form-control brc-on-focus brc-primary-m1" id="commRental" placeholder="0.00">
                    </div>
                </div>

                <div class="form-group mb-0">
                    <label for="commJustification" class="text-600 text-90 mb-1">Justification <span class="text-danger">*</span></label>
                    <textarea class="form-control brc-on-focus brc-primary-m1" id="commJustification" rows="3" placeholder="Justify these commercial terms..."></textarea>
                </div>

                <div class="alert bgc-primary-l4 border-none border-l-4 brc-primary-m2 mt-4 mb-0 py-2">
                    <div class="d-flex align-items-center">
                        <i class="fas fa-info-circle text-primary-m1 mr-2"></i>
                        <span class="text-primary-d2 text-90"><strong>Note:</strong> Changes will be logged and require approval.</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l2">
                <button type="button" class="btn btn-secondary btn-bold px-4 radius-1" data-dismiss="modal">
                    <i class="fas fa-times mr-1"></i> Cancel
                </button>
                <button type="button" class="btn btn-primary btn-bold px-4 radius-1" onclick="submitCommercial()">
                    <i class="fas fa-save mr-1"></i> Save Commercial
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
COMMERCIAL ACTION MODAL
============================================ -->
<div class="modal fade" id="commercialActionModal" tabindex="-1" role="dialog" aria-labelledby="commercialActionModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="commercialActionModalLabel">
                    <i class="fas fa-dollar-sign mr-1"></i> Commercial Decision - <span id="commercialActionReqId"></span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-3">
                <div class="d-flex align-items-center mb-2 p-2" style="background:#eef3f9;border-radius:3px;font-size:0.8rem;color:#3b6998">
                    <i class="fas fa-info-circle mr-2"></i>
                    For each printer, choose to continue existing commercial or provide new terms.
                </div>

                <!-- Apply to All Button (hidden if only 1 printer) -->
                <div id="applyToAllContainer" class="mb-2 d-none text-right">
                    <button type="button" class="btn btn-primary btn-sm px-2" style="font-size:0.75rem" onclick="applyCommercialToAll()">
                        <i class="fas fa-clone mr-1"></i> Apply Printer 1 Commercials to All
                    </button>
                </div>

                <div id="commercialPrintersContainer">
                    <!-- Printer cards will be dynamically populated here -->
                    <div class="text-center py-5 text-primary-m1">
                        <i class="fas fa-spinner fa-spin fa-2x mb-2"></i>
                        <p class="text-110">Loading printer details...</p>
                    </div>
                </div>

                <div class="form-group mt-2 mb-0">
                    <label for="commercialOverallComments" style="font-size:0.8rem;font-weight:600;margin-bottom:2px">Overall Comments</label>
                    <textarea class="form-control form-control-sm" id="commercialOverallComments" rows="2" placeholder="Add any overall comments for the Account Manager..."></textarea>
                </div>
            </div>
            <div class="modal-footer py-2 justify-content-between" style="background:#f8f9fa;border-top:1px solid #dee2e6">
                <div>
                    <button type="button" class="btn btn-outline-danger btn-sm px-3 mr-1" onclick="openRejectFromCommercial()">
                        <i class="fas fa-times-circle mr-1"></i>Reject
                    </button>
                    <button type="button" class="btn btn-outline-primary btn-sm px-3" onclick="forwardFromCommercial()">
                        <i class="fas fa-share mr-1"></i>Forward
                    </button>
                </div>
                <div>
                    <button type="button" class="btn btn-secondary btn-sm px-3 mr-1" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-primary btn-sm px-3" onclick="approveAndNotifyAM()">
                        <i class="fas fa-check mr-1"></i>Approve & Notify AM
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Template for Commercial Printer Card (used by JavaScript) -->
<script type="text/template" id="commercialPrinterCardTemplate">
    <div class="card commercial-printer-card" data-printer-id="{{printerId}}">
        <div class="card-header">
            <div class="d-flex align-items-center">
                <span class="badge badge-primary badge-sm mr-2">Printer {{printerIndex}}</span>
                <span class="font-weight-bold text-dark mr-1" style="font-size:0.85rem">{{existingModel}}</span>
                <i class="fa fa-long-arrow-alt-right mx-1 text-muted" style="font-size:0.75rem"></i>
                <span class="badge badge-{{newModelBadgeClass}} badge-sm">{{newModel}}</span>
            </div>
            <div class="d-flex align-items-center">
                <span class="hdr-meta mr-2">Agmt: <strong>{{agreementNumber}}</strong></span>
                <button type="button" class="btn btn-outline-primary btn-edit-sm" onclick="viewPrinterCommercialDetail('{{printerId}}', '{{existingModel}}', '{{serialNumber}}')">
                    <i class="fas fa-dollar-sign mr-1"></i>Commercial
                </button>
            </div>
        </div>
        <div class="card-body">
            <div class="tl-rec-box">
                <div>
                    <div class="rec-label"><i class="fas fa-user-tie mr-1"></i>TL Recommendation</div>
                    <div class="rec-text"><strong>{{primaryRecommendation}}</strong></div>
                    <div class="rec-text text-muted" style="font-size:0.75rem">{{alternateRecommendation}}</div>
                </div>
                <button type="button" class="btn btn-outline-secondary btn-edit-sm" onclick="editPrinterRecommendation('{{printerId}}')">
                    <i class="fas fa-edit mr-1"></i>Edit Model
                </button>
            </div>
            <div class="form-group d-none printer-select-inline" id="printerSelectGroup_{{printerId}}">
                <select class="form-control form-control-sm" id="printerSelect_{{printerId}}" onchange="updateSelectedPrinter('{{printerId}}', this.value)">
                    {{printerOptions}}
                </select>
            </div>

            <div class="decision-row">
                <div>
                    <div class="custom-control custom-checkbox">
                        <input type="checkbox" class="custom-control-input" id="commercialDecision_{{printerId}}"
                               onchange="toggleCommercialComments('{{printerId}}', this.checked)">
                        <label class="custom-control-label" for="commercialDecision_{{printerId}}" style="font-size:0.85rem">
                            Continue with Existing Commercial
                        </label>
                    </div>
                    <small class="text-muted ml-4">Keep current commercial terms</small>
                </div>
                <div class="comments-col">
                    <label style="font-size:0.8rem;font-weight:600;margin-bottom:2px">Comments <span class="text-danger" id="commentsRequired_{{printerId}}">*</span></label>
                    <textarea class="form-control form-control-sm" id="printerComments_{{printerId}}" rows="2" placeholder="Required when not continuing with existing commercial"></textarea>
                </div>
            </div>
        </div>
    </div>
</script>

<!-- ============================================
COMMERCIAL DETAIL MODAL
============================================ -->
<div class="modal fade" id="commercialDetailModal" tabindex="-1" role="dialog" aria-labelledby="commercialDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="commercialDetailModalLabel">
                    <i class="fas fa-list-alt mr-1"></i> Commercial Details - <span id="commercialDetailReqId"></span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-0">
                <div class="table-responsive">
                    <table class="table table-bordered table-striped text-95 mb-0" id="commercialDetailTable">
                        <thead class="bgc-grey-l4 text-grey-d2">
                            <tr>
                                <th class="border-0">Agreement No.</th>
                                <th class="border-0">Serial No.</th>
                                <th class="border-0">Rent</th>
                                <th class="border-0">Free Prints</th>
                                <th class="border-0">A4 Rate</th>
                                <th class="border-0">View Details</th>
                            </tr>
                        </thead>
                        <tbody id="commercialDetailTableBody">
                            <tr>
                                <td colspan="6" class="text-center py-4">
                                    <i class="fas fa-spinner fa-spin fa-2x text-primary-m1 mb-2"></i><br>
                                    Loading...
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l2">
                <button type="button" class="btn btn-secondary btn-bold px-4" data-dismiss="modal">
                    <i class="fas fa-times mr-1"></i> Close
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================
PRINTER COMMERCIAL MODAL
============================================ -->
<div class="modal fade" id="printerCommercialModal" tabindex="-1" role="dialog" aria-labelledby="printerCommercialModalLabel" aria-hidden="true" data-backdrop="true" data-keyboard="true">
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="printerCommercialModalLabel">
                    <i class="fas fa-file-invoice-dollar mr-1"></i> Commercial Details
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4">
                <!-- Product Details Header -->
                <div class="alert bgc-primary-l4 border-none border-l-4 brc-primary-m1 mb-4">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h5 class="text-primary-d2 font-bold mb-1" id="commercialProductHeader">1] Product: -- (--)</h5>
                            <div class="text-dark-m3 text-105"><i class="fas fa-map-marker-alt mr-1 text-danger-m2"></i> <strong>Location:</strong> <span id="commercialLocation">--</span></div>
                        </div>
                        <div class="col-md-4 text-md-right mt-2 mt-md-0">
                            <div class="text-90 text-secondary-d1">Commercial Type:</div>
                            <div id="commercialType" class="text-120 text-primary-d2 font-bolder">--</div>
                        </div>
                    </div>
                </div>

                <!-- Commercial Details Table (4-column layout) -->
                <div class="table-responsive border-1 brc-grey-l2 rounded-sm overflow-hidden">
                    <table class="table table-bordered mb-0" id="commercialDetailsTable">
                        <tbody id="commercialDetailsTableBody">
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l2">
                <button type="button" class="btn btn-secondary btn-bold px-4" data-dismiss="modal">
                    <i class="fas fa-times mr-1"></i> Close
                </button>
            </div>
        </div>
    </div>
</div>
