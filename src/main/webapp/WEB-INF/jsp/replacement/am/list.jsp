<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Commercial Reviews" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<div class="main-content-inner">
    <div class="page-content">
        <!-- Page Header -->
        <div class="page-header mb-4">
            <h1 class="page-title text-primary-d2">
                <i class="fas fa-dollar-sign text-dark-m3 mr-1"></i>
                Pending Commercial Reviews
                <small class="page-info text-secondary-d2">
                    <i class="fa fa-angle-double-right text-80"></i>
                    AM Manager
                </small>
            </h1>
        </div>

        <div class="card bcard shadow-sm">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-bordered table-hover text-95">
                        <thead class="bgc-grey-l4 text-dark-m3">
                            <tr>
                                <th>Request ID</th>
                                <th>Client</th>
                                <th>Type</th>
                                <th>Locations</th>
                                <th>Service TL</th>
                                <th>Created Date</th>
                                <th class="text-center">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${pendingRequests}" var="req">
                            <tr>
                                <td class="text-600 text-primary-d2">#${req.id}</td>
                                <td>${req.clientName}</td>
                                <td><span class="badge badge-lg bgc-info-l2 text-info-d2 border-1 brc-info-m3 radius-1 px-3">${req.replacementType}</span></td>
                                <td>
                                    <span class="badge badge-lg bgc-secondary-l2 text-secondary-d2 border-1 brc-secondary-m3 radius-1 px-3">
                                        <i class="fas fa-map-marker-alt mr-1"></i> ${req.printers.size()} Locations
                                    </span>
                                </td>
                                <td>${req.requesterUserId}</td>
                                <td>${req.createdAt}</td>
                                <td class="text-center">
                                    <button class="btn btn-sm btn-primary radius-1 review-btn" data-id="${req.id}">
                                        <i class="fas fa-dollar-sign mr-1"></i> Review
                                    </button>
                                </td>
                            </tr>
                            </c:forEach>
                            <c:if test="${empty pendingRequests}">
                            <tr>
                                <td colspan="7" class="text-center text-secondary-m1 py-5">
                                    <i class="fas fa-inbox fa-4x mb-3 text-grey-l1 d-block"></i>
                                    <h5 class="text-grey-d1">No pending commercial reviews</h5>
                                </td>
                            </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Commercial Review Modal -->
<div class="modal fade" id="reviewModal" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-primary-d1 border-0 radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2 font-bolder">Commercial Review</h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <form id="reviewForm" method="post" action="<%= request.getContextPath() %>/views/replacement/am/action">
                <input type="hidden" name="action" value="review">
                <input type="hidden" name="requestId" id="reviewRequestId">
                <div class="modal-body">
                    <div id="reviewContent">Loading...</div>
                </div>
                <div class="modal-footer bgc-grey-l4">
                    <button type="button" class="btn btn-outline-secondary radius-1" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-danger radius-1" id="rejectCommercialBtn">
                        <i class="fas fa-times mr-1"></i> Reject
                    </button>
                    <button type="submit" class="btn btn-success radius-1">
                        <i class="fas fa-check mr-1"></i> Approve
                    </button>
                </div>
            </form>
        </div>
    </div>


<script src="../../../js/replacement/am_list.js"></script>

<%@ include file="../common/footer.jsp" %>