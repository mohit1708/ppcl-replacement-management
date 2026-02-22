<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Sidebar -->
<div id="sidebar" class="sidebar sidebar-fixed expandable sidebar-light" data-backdrop="true" data-dismiss="true" data-swipe="true">
    <div class="sidebar-inner">
        <div class="ace-scroll flex-grow-1 mt-1px" data-ace-scroll="{}">
            <!-- optional `nav` tag -->
            <nav class="pt-3" aria-label="Main">
                <ul class="nav flex-column has-active-border">
                    <c:set var="prevSection" value="" />
                    <c:forEach var="item" items="${sessionScope.menuItems}">

                        <%-- Render section header when section changes --%>
                        <c:if test="${not empty item.sectionLabel && item.sectionLabel != prevSection}">
                        <li class="nav-item-caption">
                            <span class="nav-text text-uppercase text-80 text-grey-m2">${item.sectionLabel}</span>
                        </li>
                        </c:if>
                        <c:set var="prevSection" value="${item.sectionLabel}" />

                        <%-- Menu item --%>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}${item.url}">
                                <i class="nav-icon ${item.iconClass}"></i>
                                <span class="nav-text fadeable">${item.label}</span>
                            </a>
                        </li>

                    </c:forEach>
                </ul>
            </nav>
        </div><!-- /.ace-scroll -->
    </div>
</div>

<!-- Main Content -->
<div class="main-content">
