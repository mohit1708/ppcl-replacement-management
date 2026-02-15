<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Assigned Pickups - Courier Portal</title>
    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace-font.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace.css">
    <style>
        body { background: #f4f6f9; }
    </style>
</head>
<body>

    <div class="container-fluid py-3" style="max-width:1200px;">

        <!-- Welcome card with courier name and logout -->
        <div class="card border-0 text-white mb-3" style="background:linear-gradient(135deg,#667eea 0%,#764ba2 100%); border-radius:12px;">
            <div class="card-body py-3 px-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap" style="gap:10px;">
                    <div>
                        <h4 class="mb-1 font-weight-bold">
                            <i class="fas fa-clipboard-list mr-2"></i> Assigned Pickups
                        </h4>
                        <p class="mb-0" style="opacity:0.85;">
                            Welcome,
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty sessionScope.courierName}">${sessionScope.courierName}</c:when>
                                    <c:when test="${not empty courierName}">${courierName}</c:when>
                                    <c:otherwise>Courier</c:otherwise>
                                </c:choose>
                            </strong>
                            — view all printer replacement pickups assigned to you.
                        </p>
                        <p class="mb-0 mt-1" style="opacity:0.65; font-size:0.8rem;">
                            <i class="fas fa-id-badge mr-1"></i> ID: ${sessionScope.courierId}
                            &nbsp;&nbsp;
                            <i class="fas fa-phone mr-1"></i> ${sessionScope.courierMobile}
                        </p>
                    </div>
                    <a href="<%= request.getContextPath() %>/CourierLogout.do"
                       class="btn btn-danger btn-sm font-weight-bold" style="white-space:nowrap;">
                        <i class="fas fa-sign-out-alt mr-1"></i> Logout
                    </a>
                </div>
            </div>
        </div>

        <!-- Search bar -->
        <div class="card shadow-sm mb-3" style="border-radius:10px;">
            <div class="card-body py-3">
                <div class="row align-items-center">
                    <div class="col-md-6">
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bg-white"><i class="fas fa-search text-muted"></i></span>
                            </div>
                            <input type="text" id="locationSearch" class="form-control"
                                   placeholder="Type to search by client location..." autocomplete="off">
                        </div>
                    </div>
                    <div class="col-md-6 text-md-right mt-2 mt-md-0">
                        <span class="text-muted text-95">
                            <i class="fas fa-list mr-1"></i>
                            Showing <strong id="visibleCount">${pullbacks.size()}</strong> of <strong>${pullbacks.size()}</strong> records
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Data table -->
        <div class="card shadow-sm" style="border-radius:10px;">
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${not empty pullbacks}">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0" id="pickupTable">
                                <thead class="thead-light">
                                    <tr>
                                        <th>#</th>
                                        <th>Client Location</th>
                                        <th>Contact Person</th>
                                        <th>Serial No.</th>
                                        <th>Printer Model</th>
                                        <th>Cartridge</th>
                                        <th>Dispatch Date</th>
                                    </tr>
                                </thead>
                                <tbody id="pickupBody">
                                    <c:forEach items="${pullbacks}" var="pb" varStatus="loop">
                                        <tr>
                                            <td class="row-num">${loop.index + 1}</td>
                                            <td class="location-cell">
                                                <i class="fas fa-map-marker-alt text-primary mr-1"></i>
                                                ${pb.location}
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty pb.contactPerson}">
                                                        <i class="fas fa-user text-secondary mr-1"></i>
                                                        ${pb.contactPerson}
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted">-</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><code>${pb.serialNo}</code></td>
                                            <td>${pb.printerModelName}</td>
                                            <td>
                                                <c:if test="${pb.emptyCartridge != null && pb.emptyCartridge > 0}">
                                                    <span class="badge badge-warning" style="font-size:0.75rem;">Empty: ${pb.emptyCartridge}</span>
                                                </c:if>
                                                <c:if test="${pb.unusedCartridge != null && pb.unusedCartridge > 0}">
                                                    <span class="badge badge-info" style="font-size:0.75rem;">Unused: ${pb.unusedCartridge}</span>
                                                </c:if>
                                                <c:if test="${(pb.emptyCartridge == null || pb.emptyCartridge == 0) && (pb.unusedCartridge == null || pb.unusedCartridge == 0)}">
                                                    <span class="text-muted">-</span>
                                                </c:if>
                                            </td>
                                            <td>
                                                <c:if test="${pb.dispatchDate != null}">
                                                    <fmt:formatDate value="${pb.dispatchDate}" pattern="dd-MMM-yyyy"/>
                                                </c:if>
                                                <c:if test="${pb.dispatchDate == null}">
                                                    <span class="text-muted">-</span>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5 text-muted">
                            <i class="fas fa-inbox fa-4x d-block mb-3"></i>
                            <h5>No Pickups Assigned</h5>
                            <p>There are currently no pullback requests assigned to you.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Filter table rows as user types -->
    <script>
        $(function() {
            var $input = $('#locationSearch');
            var $rows = $('#pickupBody tr');
            var $count = $('#visibleCount');

            $input.on('keyup', function() {
                var query = this.value.toLowerCase().trim();
                var visible = 0;
                var num = 0;

                $rows.each(function() {
                    var location = $(this).find('.location-cell').text().toLowerCase();
                    if (query === '' || location.indexOf(query) > -1) {
                        $(this).show();
                        visible++;
                        num++;
                        $(this).find('.row-num').text(num);
                    } else {
                        $(this).hide();
                    }
                });

                $count.text(visible);
            });
        });
    </script>
</body>
</html>
