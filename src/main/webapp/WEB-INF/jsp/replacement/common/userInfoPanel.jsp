<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- User Info Panel for Sidebar - Include this in any sidebar --%>
<div class="user-info-panel">
    <div class="user-avatar">
        <c:choose>
            <c:when test="${not empty sessionScope.userName}">
                ${fn:toUpperCase(fn:substring(sessionScope.userName, 0, 2))}
            </c:when>
            <c:otherwise>??</c:otherwise>
        </c:choose>
    </div>
    <div class="user-details">
        <div class="user-name">
            <c:choose>
                <c:when test="${not empty sessionScope.userName}">${sessionScope.userName}</c:when>
                <c:otherwise>Guest User</c:otherwise>
            </c:choose>
        </div>
        <div class="user-role">
            <c:choose>
                <c:when test="${not empty sessionScope.userRole}">${sessionScope.userRole}</c:when>
                <c:when test="${not empty sessionScope.roleCode}">${sessionScope.roleCode}</c:when>
                <c:otherwise>-</c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<style>
    .user-info-panel {
        padding: 1rem;
        border-bottom: 1px solid rgba(255,255,255,0.1);
        display: flex;
        align-items: center;
        gap: 10px;
    }
    .user-avatar {
        width: 40px;
        height: 40px;
        background: #007bff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: bold;
        font-size: 0.9rem;
    }
    .user-details {
        flex: 1;
        overflow: hidden;
    }
    .user-name {
        color: #fff;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .user-role {
        color: rgba(255,255,255,0.6);
        font-size: 0.8rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
</style>
