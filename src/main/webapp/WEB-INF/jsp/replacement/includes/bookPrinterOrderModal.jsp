<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 
    Book Printer Order Modal Component (STG6_PRINTER_ORDER)
    Include this JSP where printer order booking is needed.
    Requires: jQuery, Bootstrap 4, contextPath variable
--%>

<style>
    .order-item {
        background: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 8px;
        padding: 1rem;
        margin-bottom: 1rem;
    }
    .order-item__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
        padding-bottom: 0.75rem;
        border-bottom: 1px solid #dee2e6;
    }
    .apply-all-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 0.375rem 0.75rem;
        background: linear-gradient(135deg, #0d9488, #0f766e);
        color: white;
        border: none;
        border-radius: 6px;
        font-size: 0.875rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
    }
    .apply-all-btn:hover {
        background: linear-gradient(135deg, #0f766e, #115e59);
        transform: translateY(-1px);
    }
    .apply-all-notice {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.375rem 0.75rem;
        background: #d1ecf1;
        color: #0c5460;
        border-radius: 6px;
        font-size: 0.75rem;
    }
    .first-item-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 2px 8px;
        background: #ccfbf1;
        color: #0f766e;
        border-radius: 4px;
        font-size: 0.75rem;
        font-weight: 500;
        margin-left: 8px;
    }
    .cartridge-section, .install-section {
        background: white;
        border: 1px solid #dee2e6;
        border-radius: 6px;
        padding: 1rem;
        margin-top: 1rem;
    }
    .cartridge-section .custom-control-input,
    .cartridge-section .form-check-input {
        width: 1rem;
        height: 1rem;
        margin-top: 0.2rem;
    }
    .cartridge-section .custom-control {
        padding-left: 1.5rem;
    }
    .cartridge-section .custom-control-label::before,
    .cartridge-section .custom-control-label::after {
        width: 1rem;
        height: 1rem;
        top: 0.15rem;
        left: -1.5rem;
    }
    .install-section {
        border-left: 3px solid #0d9488;
    }
    .order-summary-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 1rem;
        border-top: 1px solid #dee2e6;
    }
</style>

<div class="modal fade modal-xl" id="bookOrderModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-primary-d1 border-0  radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2">
                    <i class="fas fa-shopping-cart mr-1"></i> Printer Order Booking - <span id="bookOrderReqId">REQ-0000</span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" style="max-height: 75vh; overflow-y: auto;">
                <input type="hidden" id="bookOrderReqIdHidden">

                <!-- Loading State -->
                <div id="bookOrderLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p class="mt-2">Loading order details...</p>
                </div>

                <!-- Order Content (hidden initially) -->
                <div id="bookOrderContent" style="display: none;">
                    <!-- Request Summary -->
                    <div class="card mb-3">
                        <div class="card-header bg-light py-2">
                            <h6 class="mb-0"><i class="fas fa-file-alt mr-2"></i>Request Summary</h6>
                        </div>
                        <div class="card-body py-2">
                            <div class="row">
                                <div class="col-md-3">
                                    <small class="text-muted">Client</small>
                                    <div class="font-weight-bold" id="bookOrderClientName">-</div>
                                </div>
                                <div class="col-md-3">
                                    <small class="text-muted">Total Printers</small>
                                    <div class="font-weight-bold" id="bookOrderTotalPrinters">-</div>
                                </div>
                                <div class="col-md-3">
                                    <small class="text-muted">Contract Type</small>
                                    <div class="font-weight-bold" id="bookOrderContractType">-</div>
                                </div>
                                <div class="col-md-3">
                                    <small class="text-muted">PO Number</small>
                                    <div class="font-weight-bold" id="bookOrderPoNumber">-</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Signatory Selection -->
                    <div class="card mb-3">
                        <div class="card-body py-2">
                            <div class="form-group mb-0">
                                <label class="font-weight-bold">Signatory Selection <span class="text-danger">*</span></label>
                                <select class="form-control" id="bookOrderSignatory" required style="max-width: 400px;">
                                    <option value="">Select Signatory (AM Manager or Higher)</option>
                                </select>
                                <small class="form-text text-muted">Only one signatory per replacement request</small>
                            </div>
                        </div>
                    </div>

                    <!-- Order Items Section -->
                    <div class="card mb-3">
                        <div class="card-header bg-light py-2 d-flex justify-content-between align-items-center">
                            <h6 class="mb-0"><i class="fas fa-print mr-2"></i>Order Items (Consolidated by Location + Model)</h6>
                            <div class="d-flex align-items-center">
                                <div class="apply-all-notice mr-2">
                                    <i class="fas fa-info-circle"></i>
                                    Fill Item 1, then click Apply to copy values to all items
                                </div>
                                <button type="button" class="apply-all-btn" onclick="BookPrinterOrder.applyToAllItems()">
                                    <i class="fas fa-copy"></i> Apply to All
                                </button>
                            </div>
                        </div>
                        <div class="card-body" id="bookOrderItemsContainer">
                            <!-- Order items will be dynamically populated here -->
                        </div>
                    </div>

                    <!-- Pullback Info Alert -->
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle mr-2"></i>
                        <strong>Pullback Call:</strong> System will auto-align to courier if pincode is mapped, otherwise create ERP pullback call (1 call per printer with serial number).
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="order-summary-footer w-100">
                    <div class="text-muted">
                        <strong>Total Items:</strong> <span id="bookOrderTotalItems">0</span> combinations | 
                        <strong>Total Printers:</strong> <span id="bookOrderTotalPrintersFooter">0</span>
                    </div>
                    <div>
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-primary ml-2" onclick="BookPrinterOrder.submit()">
                            <i class="fas fa-check"></i> Book Printer Order
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    window.contextPath = '${pageContext.request.contextPath}';
</script>

<script src="${pageContext.request.contextPath}/js/replacement/bookPrinterOrder.js?v=2"></script>
