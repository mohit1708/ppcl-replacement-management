<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Replacement Letter" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<div class="main-content-inner">
    <div class="page-content">
        <div class="page-header">
            <h1 class="page-title text-primary-d2">
                <i class="fas fa-file-alt text-dark-m3"></i>
                Generate Replacement Letter
                <small class="page-info text-secondary-d2">
                    <i class="fa fa-angle-double-right text-80"></i>
                    ${letterData.refNo}
                </small>
            </h1>
        </div>

        <div class="row mt-4">
            <div class="col-12">
                <!-- Letter Preview Card -->
                <div class="card bcard shadow-sm mb-4">
                    <div class="card-header bgc-success-d1 text-white d-flex justify-content-between align-items-center">
                        <h5 class="card-title text-120 mb-0">
                            <i class="fas fa-eye mr-1"></i> Letter Preview
                        </h5>
                        <button class="btn btn-xs btn-light-white radius-1" onclick="refreshPreview()">
                            <i class="fas fa-sync-alt"></i> Refresh
                        </button>
                    </div>
                    <div class="card-body bgc-grey-l4">
                        <div class="letter-preview bg-white border p-5 rounded shadow-sm mx-auto" id="letterPreview" style="max-width: 850px; min-height: 1000px; color: #333;">
                            <!-- Letter Header -->
                            <div class="row mb-5">
                                <div class="col-6">
                                    <div class="text-110">
                                        <strong class="text-dark">To,</strong><br>
                                        <span class="text-600">${letterData.client.name}</span><br>
                                        ${letterData.client.city}, ${letterData.client.state}
                                    </div>
                                </div>
                                <div class="col-6 text-right">
                                    <div class="text-110">
                                        <strong class="text-dark">REF NO: ${letterData.refNo}</strong><br>
                                        DATE: ${letterData.letterDate}
                                    </div>
                                </div>
                            </div>

                            <div class="mb-4">
                                <p><strong>CLIENT NAME:</strong> ${letterData.client.name}</p>
                                <p><strong>Subject:</strong> Replacement of Printer</p>
                            </div>

                            <p>Dear Sir/Ma'am,</p>
                            <p>This is to bring to your notice that, we are replacing the printer with another model for better and efficient service.</p>
                            <p>Kindly find the printer details mentioned below.</p>

                            <!-- Printer Details Table -->
                            <div class="table-responsive border brc-grey-l1 mt-4">
                                <table class="table table-bordered mb-0">
                                    <thead class="bgc-grey-l4 text-dark-m3">
                                        <tr>
                                            <th class="border-b-1">Sr. No.</th>
                                            <th class="border-b-1">Agreement Details</th>
                                            <th colspan="2" class="border-b-1 text-center">Old Printer Details</th>
                                            <th colspan="2" class="border-b-1 text-center">New Printer Details</th>
                                            <th class="border-b-1">Status</th>
                                        </tr>
                                        <tr>
                                            <th></th>
                                            <th></th>
                                            <th class="text-90">Printer Model</th>
                                            <th class="text-90">Serial Number</th>
                                            <th class="text-90">Printer Model</th>
                                            <th class="text-90">Serial Number</th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="printer" items="${letterData.printers}" varStatus="status">
                                            <tr>
                                                <td class="text-center">${status.index + 1}</td>
                                                <td>${printer.agreementNoMapped}
                                                    <c:if test="${not empty printer.agreementDate}">
                                                        <br><small class="text-muted">${printer.agreementDate}</small>
                                                    </c:if>
                                                </td>
                                                <td>${printer.existingModelName}</td>
                                                <td>${printer.existingSerial}</td>
                                                <td>
                                                    ${printer.newModelName}
                                                    <c:if test="${printer.isNew() and not empty printer.newSerial}">
                                                        <br><span class="text-muted text-90">(Box Pack)</span>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty printer.newSerial}">
                                                            ${printer.newSerial}
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-grey-m1">-</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${printer.printerStage == 'INSTALLED'}">
                                                            <span class="badge badge-primary arrowed-in">Installed</span>
                                                        </c:when>
                                                        <c:when test="${printer.printerStage == 'RETURNED'}">
                                                            <span class="badge badge-secondary arrowed-in">Returned</span>
                                                        </c:when>
                                                        <c:when test="${printer.printerStage == 'DELIVERED'}">
                                                            <span class="badge badge-success arrowed-in">Delivered</span>
                                                        </c:when>
                                                        <c:when test="${printer.printerStage == 'DISPATCHED'}">
                                                            <span class="badge badge-warning arrowed-in">Dispatched</span>
                                                        </c:when>
                                                        <c:when test="${printer.printerStage == 'ALLOTED'}">
                                                            <span class="badge badge-info arrowed-in">Alloted</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-secondary arrowed-in">Pending</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Cartridge Details -->
                            <c:if test="${not empty letterData.cartridges}">
                                <p class="mt-5"><strong>Cartridges to be pulled back:</strong></p>
                                <div class="table-responsive border brc-grey-l1">
                                    <table class="table table-bordered mb-0">
                                        <thead class="bgc-grey-l4 text-dark-m3">
                                            <tr>
                                                <th>Sr. No.</th>
                                                <th>Type</th>
                                                <th>Cartridge Model</th>
                                                <th>Quantity</th>
                                                <th>Modification (if any)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="cartridge" items="${letterData.cartridges}" varStatus="status">
                                                <tr>
                                                    <td class="text-center">${status.index + 1}</td>
                                                    <td>${cartridge.type}</td>
                                                    <td>${cartridge.model}</td>
                                                    <td>${cartridge.quantity}</td>
                                                    <td style="min-width: 150px;"></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:if>

                            <p class="mt-5">Kindly acknowledge the receipt.</p>

                            <!-- Signatures -->
                            <div class="row mt-5 pt-4">
                                <div class="col-6 text-center">
                                    <div class="signature-box border-1 brc-grey-m3 rounded p-4 mb-2 d-flex align-items-center justify-content-center" id="ppcSignature" style="height: 120px; background-color: #fcfcfc;">
                                        <c:if test="${letterData.replacementLetterGenerated}">
                                            <div class="text-center">
                                                <div class="text-primary-d2 font-bolder text-120">Digitally signed by PPCL</div>
                                                <small class="text-secondary-d1 font-600">Date : ${letterData.signedAt}</small>
                                                <c:choose>
                                                    <c:when test="${not empty mergedLinks}">
                                                        <div class="mt-1 no-print">
                                                            <c:forEach var="link" items="${mergedLinks}">
                                                                <a class="d-inline-block mr-2" href="${pageContext.request.contextPath}/${link.filePath}" target="_blank" rel="noopener">Signed copy ${link.label}</a>
                                                            </c:forEach>
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${not empty letterData.signedLetterPath}">
                                                        <div class="mt-1 no-print">
                                                            <a href="${pageContext.request.contextPath}/${letterData.signedLetterPath}" target="_blank" rel="noopener">View signed PDF</a>
                                                        </div>
                                                    </c:when>
                                                </c:choose>
                                            </div>
                                        </c:if>
                                    </div>
                                    <div class="mt-3">
                                        <p class="mb-0 font-bolder">Your's Truly,</p>
                                        <p>PowerPoint Cartridges Pvt Ltd</p>
                                    </div>
                                </div>
                                <div class="col-6 text-center">
                                    <div class="signature-box border-1 brc-grey-m3 rounded p-4 mb-2 d-flex align-items-center justify-content-center" style="height: 120px; background-color: #fcfcfc;">
                                        <span class="text-grey-m1 italic"><i class="fas fa-pen mr-1 text-120"></i> Client Signature</span>
                                    </div>
                                    <div class="mt-3">
                                        <p class="mb-0 font-bolder">Your's Truly,</p>
                                        <p>${letterData.client.name}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Action Buttons Card -->
                <div class="card bcard shadow-sm mb-5">
                    <div class="card-header bgc-warning-d2 text-white">
                        <h5 class="card-title text-120">
                            <i class="fas fa-paper-plane mr-1"></i> Actions
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-5">
                                <button class="btn btn-primary btn-block btn-lg radius-2 px-4 shadow-sm" onclick="applyDigitalSignature()" <c:if test="${letterData.replacementLetterGenerated}">disabled</c:if>>
                                    <i class="fas fa-shield-alt mr-2 text-110"></i> Apply Digital Signature (DSC)
                                </button>
                            </div>
                            <div class="col-md-7 d-flex justify-content-end align-items-center flex-wrap">
                                <button class="btn btn-outline-default btn-h-outline-primary btn-a-outline-primary btn-bold radius-1 px-3 m-1" onclick="sendViaEmail()">
                                    <i class="fas fa-envelope mr-1 text-110"></i> Email
                                </button>
                                <button class="btn btn-outline-default btn-h-outline-secondary btn-a-outline-secondary btn-bold radius-1 px-3 m-1" onclick="printLetter()">
                                    <i class="fas fa-print mr-1 text-110"></i> Print
                                </button>
                                <button class="btn btn-outline-success btn-bold radius-1 px-3 m-1" onclick="sendViaWhatsApp()">
                                    <i class="fab fa-whatsapp mr-1 text-110"></i> WhatsApp
                                </button>
                                <button class="btn btn-info btn-bold radius-1 px-3 m-1 shadow-sm" onclick="sendDispatchNotification()">
                                    <i class="fas fa-paper-plane mr-1 text-110"></i> Send Dispatch Notification
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

<script>
    var contextPath = '${pageContext.request.contextPath}';
    var requestId = '${letterData.requestId}';
    var clientEmail = '${letterData.client.emailId1}';
    var clientMobile = '${letterData.client.mobileNo}';
</script>
<script src="${pageContext.request.contextPath}/js/replacement/am_replacementLetter.js"></script>

<%@ include file="../common/footer.jsp" %>
