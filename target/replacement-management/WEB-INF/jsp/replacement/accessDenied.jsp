<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Access Denied" scope="request"/>
<%@ include file="common/header.jsp" %>


    <div class="card border-danger">
        <div class="card-header bg-danger text-white">
            <h5 class="mb-0"><i class="fas fa-ban"></i> Access Denied</h5>
        </div>
        <div class="card-body text-center py-5">
            <i class="fas fa-lock fa-5x text-danger mb-4"></i>
            <h4 class="text-danger">${error != null ? error : 'Access Denied'}</h4>
            <p class="text-muted">${errorDetails != null ? errorDetails : 'You do not have permission to access this page.'}</p>
            <a href="<%= request.getContextPath() %>/views/replacement/dashboard" class="btn btn-primary mt-3">
                <i class="fas fa-home"></i> Go to Dashboard
            </a>
        </div>
    </div>


<%@ include file="common/footer.jsp" %>
