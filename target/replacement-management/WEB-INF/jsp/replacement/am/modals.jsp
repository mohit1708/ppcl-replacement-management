<!-- ============================================================================ -->
<!-- MAIN COMMERCIALS POPUP - Shows all printers in one table -->
<!-- ============================================================================ -->
<div class="modal fade modal-xl" id="commercialsPopup" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-primary-d1 border-0  radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2">
                    <i class="fas fa-print mr-1"></i> Printers & Commercials Management
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" id="commercialsBody" style="max-height: 75vh; overflow-y: auto;">
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
                <button type="button" class="btn btn-success btn-lg" onclick="submitCommercialsAction()">
                    <i class="fas fa-paper-plane"></i> Submit / Forward / Reject
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================================ -->
<!-- COMMERCIAL FORM MODAL - Add/Edit commercial -->
<!-- ============================================================================ -->
<div class="modal fade modal-lg" id="commercialModal" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-success-d1 border-0  radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2" id="commercialModalLabel">
                    <i class="fas fa-plus mr-1"></i> Add Commercial Details
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <!-- Printer Info -->
                <div id="commercialPrinterInfo" class="alert alert-info mb-3">
                    <!-- Loaded dynamically -->
                </div>

                <form id="commercialForm">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="card border-primary">
                                <div class="card-header bg-primary text-white">
                                    <strong><i class="fas fa-print"></i> Existing Printer</strong>
                                </div>
                                <div class="card-body">
                                    <div class="form-group">
                                        <label><strong>Purchase/Lease Cost (₹)</strong> <span class="text-danger">*</span></label>
                                        <input type="number" 
                                               class="form-control form-control-lg" 
                                               id="existingCost" 
                                               step="0.01"
                                               min="0"
                                               placeholder="Enter cost"
                                               onchange="calculateSavings()"
                                               required>
                                    </div>
                                    <div class="form-group mb-0">
                                        <label><strong>Monthly Rental (₹)</strong> <span class="text-danger">*</span></label>
                                        <input type="number" 
                                               class="form-control form-control-lg" 
                                               id="existingRental" 
                                               step="0.01"
                                               min="0"
                                               placeholder="Enter monthly rental"
                                               onchange="calculateSavings()"
                                               required>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="card border-success">
                                <div class="card-header bg-success text-white">
                                    <strong><i class="fas fa-star"></i> New Printer</strong>
                                </div>
                                <div class="card-body">
                                    <div class="form-group">
                                        <label><strong>Purchase/Lease Cost (₹)</strong> <span class="text-danger">*</span></label>
                                        <input type="number" 
                                               class="form-control form-control-lg" 
                                               id="newCost" 
                                               step="0.01"
                                               min="0"
                                               placeholder="Enter cost"
                                               onchange="calculateSavings()"
                                               required>
                                    </div>
                                    <div class="form-group mb-0">
                                        <label><strong>Monthly Rental (₹)</strong> <span class="text-danger">*</span></label>
                                        <input type="number" 
                                               class="form-control form-control-lg" 
                                               id="newRental" 
                                               step="0.01"
                                               min="0"
                                               placeholder="Enter monthly rental"
                                               onchange="calculateSavings()"
                                               required>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Savings Display -->
                    <div id="savingsDisplay" class="mt-3"></div>

                    <div class="form-group mt-3 mb-0">
                        <label><strong>Commercial Justification</strong> <span class="text-danger">*</span></label>
                        <textarea class="form-control" 
                                  id="commercialJustification" 
                                  rows="5" 
                                  placeholder="Provide detailed justification:
• Why is this replacement necessary?
• Cost-benefit analysis
• ROI calculation
• Business impact"
                                  required></textarea>
                        <small class="form-text text-muted">
                            <i class="fas fa-info-circle"></i> Minimum 20 characters required
                        </small>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    <i class="fas fa-times"></i> Cancel
                </button>
                <button type="button" class="btn btn-success btn-lg" id="saveCommercialBtn" onclick="saveCommercial()">
                    <i class="fas fa-save"></i> Save Commercial
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================================ -->
<!-- VIEW COMMERCIAL MODAL - Read-only -->
<!-- ============================================================================ -->
<div class="modal fade modal-lg" id="commercialViewModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-info-d1 border-0  radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2">
                    <i class="fas fa-file-invoice-dollar mr-1"></i> Commercial Details
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" id="commercialViewBody">
                <!-- Content loaded via AJAX -->
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
<!-- ACTION MODAL - Submit/Forward/Reject -->
<!-- ============================================================================ -->
<div class="modal fade modal-lg" id="amActionModal" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-warning-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110">
                    <i class="fas fa-paper-plane mr-1"></i> Finalize & Take Action
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="amActionForm">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i> 
                        <strong>Note:</strong> All new models and per-printer comments will be saved automatically.
                    </div>

                    <div class="form-group">
                        <label><strong>Action Type</strong> <span class="text-danger">*</span></label>
                        <select class="form-control form-control-lg" id="amActionType" onchange="handleActionTypeChange()" required>
                            <option value="">-- Select Action --</option>
                            <option value="SUBMIT">✅ Submit for Final Approval</option>
                            <option value="FORWARD">📤 Forward to Someone</option>
                            <option value="REJECT">❌ Reject Request</option>
                        </select>
                    </div>

                    <!-- Forward To (conditional) -->
                    <div class="form-group" id="amForwardToGroup" style="display: none;">
                        <label><strong>Forward To</strong> <span class="text-danger">*</span></label>
                        <select class="form-control form-control-lg" id="amForwardTo">
                            <option value="">-- Select Person --</option>
                            <c:forEach items="${reportingHierarchy}" var="manager">
                                <option value="${manager.id}">${fn:escapeXml(manager.name)} (${fn:escapeXml(manager.role)})</option>
                            </c:forEach>
                            <c:if test="${empty reportingHierarchy}">
                                <option value="" disabled>No managers found in hierarchy</option>
                            </c:if>
                        </select>
                    </div>

                    <!-- Reject Reason (conditional) -->
                    <div class="form-group" id="amRejectReasonGroup" style="display: none;">
                        <label><strong>Rejection Reason</strong> <span class="text-danger">*</span></label>
                        <textarea class="form-control" 
                                  id="amRejectReason" 
                                  rows="3" 
                                  placeholder="Explain why this request is being rejected"></textarea>
                        <div class="alert alert-danger mt-2 mb-0">
                            <i class="fas fa-exclamation-triangle"></i> 
                            <strong>Warning:</strong> Rejection requires raising a new replacement request
                        </div>
                    </div>

                    <div class="form-group mb-0">
                        <label><strong>General AM Comments</strong> <span class="text-danger">*</span></label>
                        <textarea class="form-control" 
                                  id="amGeneralComments" 
                                  rows="5" 
                                  placeholder="Enter your overall comments and recommendations for this request"
                                  required></textarea>
                        <small class="form-text text-muted">
                            Note: Per-printer comments are already captured in the table above
                        </small>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    <i class="fas fa-times"></i> Cancel
                </button>
                <button type="button" class="btn btn-primary btn-lg" id="submitFinalActionBtn" onclick="submitFinalAction()">
                    <i class="fas fa-check"></i> Submit Action
                </button>
            </div>
        </div>
    </div>
</div>
