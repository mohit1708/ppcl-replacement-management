<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Pending Reviews" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>


    <h2 class="mb-4">Pending Reviews - Service TL</h2>

    <div class="card">
        <div class="card-body">
            <table class="table table-hover">
                <thead class="thead-light">
                    <tr>
                        <th>Request ID</th>
                        <th>Client</th>
                        <th>Type</th>
                        <th>Reason</th>
                        <th>Locations</th>
                        <th>Created Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${pendingRequests}" var="req">
                    <tr>
                        <td><strong>#${req.id}</strong></td>
                        <td>${req.clientName}</td>
                        <td><span class="badge badge-info">${req.replacementType}</span></td>
                        <td>${req.reasonName}</td>
                        <td>
                            <span class="badge badge-secondary">
                                <i class="fas fa-map-marker-alt"></i> ${req.printers.size()} Locations
                            </span>
                        </td>
                        <td>${req.createdAt}</td>
                        <td>
                            <button class="btn btn-sm btn-success view-approve-btn" data-id="${req.id}">
                                <i class="fas fa-check"></i> Approve
                            </button>
                            <button class="btn btn-sm btn-danger view-reject-btn" data-id="${req.id}">
                                <i class="fas fa-times"></i> Reject
                            </button>
                            <button class="btn btn-sm btn-info view-forward-btn" data-id="${req.id}">
                                <i class="fas fa-share"></i> Forward
                            </button>
                        </td>
                    </tr>
                    </c:forEach>
                    <c:if test="${empty pendingRequests}">
                    <tr>
                        <td colspan="7" class="text-center text-muted py-4">
                            <i class="fas fa-inbox fa-3x mb-3 d-block"></i>
                            No pending reviews
                        </td>
                    </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Approve Modal -->
<div class="modal fade" id="approveModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-success-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">Approve Replacement Request</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <form id="approveForm" method="post" action="<%= request.getContextPath() %>/views/replacement/servicetl/action">
                <input type="hidden" name="action" value="approve">
                <input type="hidden" name="requestId" id="approveRequestId">
                <div class="modal-body">
                    <div id="approveContent">Loading...</div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-success">
                        <i class="fas fa-check"></i> Approve Request
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Reject Modal -->
<div class="modal fade" id="rejectModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-danger-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">Reject Replacement Request</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <form id="rejectForm" method="post" action="<%= request.getContextPath() %>/views/replacement/servicetl/action">
                <input type="hidden" name="action" value="reject">
                <input type="hidden" name="requestId" id="rejectRequestId">
                <div class="modal-body">
                    <div class="alert alert-warning">
                        <i class="fas fa-exclamation-triangle"></i> 
                        Are you sure you want to reject this request?
                    </div>
                    <div class="form-group">
                        <label>Rejection Reason <span class="text-danger">*</span></label>
                        <textarea name="comment" class="form-control" rows="4" required 
                                  placeholder="Please provide reason for rejection..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-danger">
                        <i class="fas fa-times"></i> Reject Request
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Forward Modal -->
<div class="modal fade" id="forwardModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-info-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">Forward Request</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <form id="forwardForm" method="post" action="<%= request.getContextPath() %>/views/replacement/servicetl/action">
                <input type="hidden" name="action" value="forward">
                <input type="hidden" name="requestId" id="forwardRequestId">
                <div class="modal-body">
                    <div class="form-group">
                        <label>Forward To <span class="text-danger">*</span></label>
                        <select name="forwardToRole" class="form-control" required>
                            <option value="">Select Role</option>
                            <option value="AM_MANAGER">AM Manager</option>
                            <option value="SERVICE_TL">Another Service TL</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Comments</label>
                        <textarea name="comment" class="form-control" rows="3" 
                                  placeholder="Add any comments..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-info">
                        <i class="fas fa-share"></i> Forward Request
                    </button>
                </div>
            </form>
        </div>
    </div>


<script src="../../../js/replacement/servicetl_list.js"></script>

<%@ include file="../common/footer.jsp" %>