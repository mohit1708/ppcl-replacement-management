<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Sidebar -->
<div id="sidebar" class="sidebar sidebar-fixed expandable sidebar-light" data-backdrop="true" data-dismiss="true" data-swipe="true">
    <div class="sidebar-inner">
        <div class="ace-scroll flex-grow-1 mt-1px" data-ace-scroll="{}">
            <!-- optional `nav` tag -->
            <nav class="pt-3" aria-label="Main">
                <ul class="nav flex-column has-active-border">
                    <!-- DASHBOARD -->
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/dashboard">
                            <i class="nav-icon fas fa-home"></i>
                            <span class="nav-text fadeable">Dashboard</span>
                        </a>
                    </li>

                    <!-- TL (TEAM LEAD) SECTION - Only visible to TL Lead users -->
                    <c:if test="${sessionScope.isTLLead}">
                    <li class="nav-item-caption">
                        <span class="nav-text text-uppercase text-80 text-grey-m2">TEAM LEAD</span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/tl/requestList">
                            <i class="nav-icon fas fa-clipboard-check"></i>
                            <span class="nav-text fadeable">TL Approvals</span>
                        </a>
                    </li>
                    </c:if>

                    <!-- AM / CRO SECTION - Visible to CRO and TL Support users -->
                    <c:if test="${sessionScope.isCRO || sessionScope.isTLSupport}">
                    <li class="nav-item-caption">
                        <span class="nav-text text-uppercase text-80 text-grey-m2">AM / CRO</span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/request?action=new">
                            <i class="nav-icon fas fa-plus-circle"></i>
                            <span class="nav-text fadeable">Create Request</span>
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/request?action=myList">
                            <i class="nav-icon fas fa-list-alt"></i>
                            <span class="nav-text fadeable">My Requests</span>
                        </a>
                    </li>
                    </c:if>

                    <!-- AM MANAGER SECTION - Only visible to AM Manager users -->
                    <c:if test="${sessionScope.isAMManager}">
                    <li class="nav-item-caption">
                        <span class="nav-text text-uppercase text-80 text-grey-m2">AM MANAGER</span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/ammanager/requestList">
                            <i class="nav-icon fas fa-dollar-sign"></i>
                            <span class="nav-text fadeable">Commercial Approvals</span>
                        </a>
                    </li>
                    </c:if>

                    <!-- ACCOUNTS / BILLING SECTION - Only visible to Account Billing users -->
                    <c:if test="${sessionScope.isAccountBillingUser}">
                    <li class="nav-item-caption">
                        <span class="nav-text text-uppercase text-80 text-grey-m2">ACCOUNTS</span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/accounts/creditNoteApproval">
                            <i class="nav-icon fas fa-check-circle"></i>
                            <span class="nav-text fadeable">Credit Note Approval</span>
                        </a>
                    </li>
                    </c:if>


                    <!-- COURIER - Only visible to TL or above -->
                    <c:if test="${sessionScope.isTLOrAbove}">
                    <li class="nav-item-caption">
                        <span class="nav-text text-uppercase text-80 text-grey-m2">COURIER</span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/courier-login/page">
                            <i class="nav-icon fas fa-user-lock"></i>
                            <span class="nav-text fadeable">Generate Courier Login</span>
                        </a>
                    </li>
                    </c:if>

                    <!-- LOGISTICS SECTION -->
                    <li class="nav-item-caption">
                        <span class="nav-text text-uppercase text-80 text-grey-m2">LOGISTICS</span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/logistics/pullbackManagement">
                            <i class="nav-icon fas fa-truck-loading"></i>
                            <span class="nav-text fadeable">Pullback Management</span>
                        </a>
                    </li>
                    <c:if test="${sessionScope.isRoleForCourierLoginValid}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/courier-pincode/page">
                            <i class="nav-icon fas fa-map-marker-alt"></i>
                            <span class="nav-text fadeable">Courier Pincode Mapping</span>
                        </a>
                    </li>
                    </c:if>

                    <!-- REPORTS -->
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/replacement/register">
                            <i class="nav-icon fas fa-book"></i>
                            <span class="nav-text fadeable">Replacement Register</span>
                        </a>
                    </li>
                </ul>
            </nav>
        </div><!-- /.ace-scroll -->
    </div>
</div>

<!-- Main Content -->
<div class="main-content">