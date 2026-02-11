<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Printer Order Booking - AM View" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<div class="main-content-inner">
    <div class="page-content">
        <div class="page-header">
            <h1 class="page-title text-primary-d2">
                <i class="fas fa-print text-dark-m3 mr-1"></i>
                Printer Order Booking
                <small class="page-info text-secondary-d2">
                    <i class="fa fa-angle-double-right text-80"></i>
                    REQ-${replacementRequest.id}
                </small>
            </h1>
        </div>

        <!-- Request Summary Card -->
        <div class="card bcard shadow-sm mb-4">
            <div class="card-header bgc-grey-l4 py-3 pl-3">
                <h5 class="card-title text-120 text-dark-m3 mb-0">
                    <i class="fas fa-file-alt text-blue-m2 mr-1"></i> Request Summary
                </h5>
            </div>
            <div class="card-body p-3">
                <div class="row text-95">
                    <div class="col-md-3">
                        <label class="text-600 mb-0">Client</label>
                        <div class="text-primary-d1 font-bolder">${replacementRequest.clientName}</div>
                    </div>
                    <div class="col-md-3">
                        <label class="text-600 mb-0">Total Printers</label>
                        <div class="font-bolder">${printerDetails.size()}</div>
                    </div>
                    <div class="col-md-3">
                        <label class="text-600 mb-0">Contract Type</label>
                        <div class="font-bolder">${replacementRequest.replacementType}</div>
                    </div>
                    <div class="col-md-3">
                        <label class="text-600 mb-0">PO Number</label>
                        <div class="font-bolder text-success-d1">${poNumber != null ? poNumber : 'To be generated'}</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Signatory Selection -->
        <div class="card bcard shadow-sm mb-4">
            <div class="card-body p-3">
                <div class="form-group mb-0">
                    <label class="text-600">
                        Signatory Selection <span class="text-danger">*</span>
                    </label>
                    <select class="form-control brc-on-focus brc-primary-m1" id="signatorySelect" required style="max-width: 400px;">
                        <option value="">Select Signatory (AM Manager or Higher)</option>
                        <c:forEach items="${signatories}" var="sig">
                            <option value="${sig.id}" ${sig.id == selectedSignatory ? 'selected' : ''}>
                                ${sig.name} (${sig.designation})
                            </option>
                        </c:forEach>
                    </select>
                    <small class="text-muted">Only one signatory per replacement request</small>
                </div>
            </div>
        </div>

        <!-- Order Items Form -->
        <form id="printerBookingForm" action="<%= request.getContextPath() %>/views/replacement/am/bookPrinterOrder" method="post">
            <input type="hidden" name="requestId" value="${replacementRequest.id}">
            <input type="hidden" name="signatoryId" id="signatoryIdHidden">

            <div class="card bcard shadow-sm mb-4">
                <div class="card-header bgc-primary-d1 text-white py-3 pl-3 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-120 mb-0">
                        <i class="fas fa-print mr-1"></i> Order Items (Consolidated by Location + Model)
                    </h5>
                    <div class="d-none" id="applyToAllContainer">
                        <button type="button" class="btn btn-xs btn-light-white radius-1" id="applyToAllBtn">
                            <i class="fas fa-clone mr-1"></i> Apply First Item Settings to All
                        </button>
                    </div>
                </div>
                <div class="card-body p-0">
                    <c:forEach items="${orderItems}" var="item" varStatus="status">
                        <div class="order-item p-3 border-b-1 brc-grey-l3">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <div>
                                    <span class="badge badge-lg bgc-primary-l2 text-primary-d2 border-1 brc-primary-m3 radius-1 px-3">Item ${status.index + 1}</span>
                                    <strong class="ml-2 text-110">${item.locationName}</strong>
                                    <span class="text-muted mx-2">|</span>
                                    <span class="text-secondary-d1">${item.newPrinterModel}</span>
                                </div>
                                <span class="badge badge-lg bgc-info-l2 text-info-d2 border-1 brc-info-m3 radius-1 px-3">Qty: ${item.quantity}</span>
                            </div>

                            <!-- Row 1: Read-only info -->
                            <div class="row">
                                <div class="col-md-3">
                                    <div class="form-group">
                                        <label class="text-600 text-90">Location</label>
                                        <input type="text" class="form-control bgc-grey-l4" value="${item.locationName}" readonly>
                                        <input type="hidden" name="items[${status.index}].locationId" value="${item.locationId}">
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="form-group">
                                        <label class="text-600 text-90">New Printer Model</label>
                                        <input type="text" class="form-control bgc-grey-l4" value="${item.newPrinterModel}" readonly>
                                        <input type="hidden" name="items[${status.index}].printerModelId" value="${item.printerModelId}">
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="form-group">
                                        <label class="text-600 text-90">Quantity</label>
                                        <input type="number" class="form-control bgc-grey-l4" value="${item.quantity}" readonly>
                                        <input type="hidden" name="items[${status.index}].quantity" value="${item.quantity}">
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="form-group">
                                        <label class="text-600 text-90">Printer Type</label>
                                        <input type="text" class="form-control bgc-grey-l4" value="${item.printerType}" readonly>
                                        <input type="hidden" name="items[${status.index}].printerType" value="${item.printerType}">
                                    </div>
                                </div>
                            </div>

                            <!-- Row 2: Editable fields -->
                            <div class="row">
                                <div class="col-md-3">
                                    <div class="form-group">
                                        <label class="text-600 text-90">Booking & Dispatch Branch</label>
                                        <select class="form-control brc-on-focus brc-primary-m1" name="items[${status.index}].dispatchBranchId" required>
                                            <c:forEach items="${dispatchBranches}" var="branch">
                                                <option value="${branch.id}" ${branch.id == item.suggestedBranchId ? 'selected' : ''}>
                                                    ${branch.name}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="form-group">
                                        <label class="text-600 text-90">Delivery Date</label>
                                        <input type="date" class="form-control brc-on-focus brc-primary-m1" 
                                               name="items[${status.index}].deliveryDate" 
                                               value="${item.defaultDeliveryDate}" required>
                                        <small class="text-muted">Today + 7 days</small>
                                    </div>
                                </div>
                                <input type="hidden" name="items[${status.index}].printerPrice" value="0">
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label class="text-600 text-90">Contact Person</label>
                                        <input type="text" class="form-control brc-on-focus brc-primary-m1" 
                                               name="items[${status.index}].contactPerson" 
                                               value="${item.contactPerson}" required>
                                    </div>
                                </div>
                            </div>

                            <!-- Row 3: Contact -->
                            <div class="row">
                                <div class="col-md-3">
                                    <div class="form-group">
                                        <label class="text-600 text-90">Contact Number</label>
                                        <input type="tel" class="form-control brc-on-focus brc-primary-m1" 
                                               name="items[${status.index}].contactNumber" 
                                               value="${item.contactNumber}" required>
                                    </div>
                                </div>
                            </div>

                            <!-- Cartridge & Accessories Section -->
                            <div class="bgc-secondary-l4 p-3 radius-1">
                                <h6 class="text-600 text-primary-d2 mb-3">
                                    <i class="fas fa-ink mr-1"></i> Cartridge & Accessories
                                </h6>
                                <div class="row">
                                    <div class="col-md-3">
                                        <div class="form-group mb-md-0">
                                            <label class="text-90">Cartridge Pickup (OLD)?</label>
                                            <div class="mt-1">
                                                <div class="custom-control custom-radio custom-control-inline">
                                                    <input type="radio" name="items[${status.index}].cartridgePickup" 
                                                           id="cartPickupYes${status.index}" value="yes"
                                                           class="custom-control-input cart-pickup-radio" data-index="${status.index}">
                                                    <label class="custom-control-label" for="cartPickupYes${status.index}">Yes</label>
                                                </div>
                                                <div class="custom-control custom-radio custom-control-inline">
                                                    <input type="radio" name="items[${status.index}].cartridgePickup" 
                                                           id="cartPickupNo${status.index}" value="no" checked
                                                           class="custom-control-input cart-pickup-radio" data-index="${status.index}">
                                                    <label class="custom-control-label" for="cartPickupNo${status.index}">No</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="form-group mb-md-0">
                                            <label class="text-90">Pickup Quantity</label>
                                            <input type="number" class="form-control form-control-sm brc-on-focus brc-primary-m1" 
                                                   name="items[${status.index}].pickupQuantity" 
                                                   id="pickupQty${status.index}" value="0" min="0" disabled>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="form-group mb-md-0">
                                            <label class="text-90">Send Cartridge with New?</label>
                                            <div class="mt-1">
                                                <div class="custom-control custom-radio custom-control-inline">
                                                    <input type="radio" name="items[${status.index}].sendCartridge" 
                                                           id="cartSendYes${status.index}" value="yes"
                                                           class="custom-control-input send-cartridge-radio" data-index="${status.index}">
                                                    <label class="custom-control-label" for="cartSendYes${status.index}">Yes</label>
                                                </div>
                                                <div class="custom-control custom-radio custom-control-inline">
                                                    <input type="radio" name="items[${status.index}].sendCartridge" 
                                                           id="cartSendNo${status.index}" value="no" checked
                                                           class="custom-control-input send-cartridge-radio" data-index="${status.index}">
                                                    <label class="custom-control-label" for="cartSendNo${status.index}">No</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="form-group mb-0">
                                            <label class="text-90">Cartridge Quantity</label>
                                            <input type="number" class="form-control form-control-sm brc-on-focus brc-primary-m1 cart-qty-input" 
                                                   name="items[${status.index}].cartridgeQuantity" 
                                                   id="cartQty${status.index}" value="0" min="0" disabled>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Installation Section (hidden, always sends yes) -->
                            <input type="hidden" name="items[${status.index}].installation" value="yes">
                        </div>
                    </c:forEach>

                    <!-- Empty state if no items -->
                    <c:if test="${empty orderItems}">
                        <div class="p-5 text-center text-secondary-m1">
                            <i class="fas fa-exclamation-triangle fa-4x mb-3 text-warning-m1"></i> 
                            <h5 class="text-grey-d1">No order items found for this replacement request.</h5>
                        </div>
                    </c:if>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="card bcard shadow-sm mb-4">
                <div class="card-body p-3">
                    <div class="alert alert-info bgc-info-l4 brc-info-m3 border-1 radius-1 mb-3">
                        <i class="fas fa-info-circle text-info-m1 mr-2"></i>
                        <strong>Pullback Call:</strong> System will auto-align to courier if pincode
                        is mapped, otherwise create ERP pullback call (1 call per printer with serial number).
                    </div>

                    <div class="d-flex justify-content-between align-items-center flex-wrap">
                        <div class="text-secondary-d2 mb-2 mb-md-0">
                            <strong>Total Items:</strong> ${orderItems.size()} combinations | 
                            <strong>Total Printers:</strong> ${totalPrinters}
                        </div>
                        <div>
                            <a href="<%= request.getContextPath() %>/views/replacement/am/requests" 
                               class="btn btn-outline-secondary radius-1 px-4 mr-2">
                                <i class="fas fa-arrow-left mr-1"></i> Back
                            </a>
                            <button type="submit" class="btn btn-success radius-1 px-4" id="bookOrderBtn">
                                <i class="fas fa-check mr-1"></i> Book Printer Order
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/replacement/am_printerBooking.js"></script>
<%@ include file="../common/footer.jsp" %>
