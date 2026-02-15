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
        body { background: #f0f2f5; }

        /* Let the table auto-size columns, scroll horizontally when needed */
        #pickupTable {
            table-layout: auto;
            min-width: 1200px;
            border-collapse: separate;
            border-spacing: 0;
        }
        #pickupTable thead th {
            font-size: 0.78rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.4px;
            white-space: nowrap;
            padding: 11px 14px;
            vertical-align: middle;
            border-bottom: 2px solid #d4dbe2;
        }
        #pickupTable tbody td {
            font-size: 0.84rem;
            vertical-align: middle;
            padding: 10px 14px;
            border-bottom: 1px solid #edf0f4;
        }
        #pickupTable tbody tr:hover {
            background-color: #f0f5ff !important;
        }

        .table-responsive {
            overflow-x: auto;
            -webkit-overflow-scrolling: touch;
        }
    </style>
</head>
<body class="bgc-white">

    <div class="container-fluid px-3 py-3">

        <!-- Top Header Bar -->
        <div class="card acard mb-3 border-0 shadow-sm" style="border-radius:10px;">
            <div class="card-body py-3 px-4 bgc-primary-d1" style="border-radius:10px;">
                <div class="d-flex justify-content-between align-items-center flex-wrap">
                    <div>
                        <h5 class="mb-1 text-white font-bolder">
                            <i class="fas fa-clipboard-list mr-2"></i> Assigned Pickups
                        </h5>
                        <p class="mb-0 text-white-tp2 text-95">
                            Welcome, <strong class="text-white">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.courierName}">${sessionScope.courierName}</c:when>
                                    <c:when test="${not empty courierName}">${courierName}</c:when>
                                    <c:otherwise>Courier</c:otherwise>
                                </c:choose>
                            </strong>
                            &mdash; view all printer replacement pickups assigned to you.
                        </p>
                        <p class="mb-0 mt-1 text-white-tp3 text-85">
                            <i class="fas fa-phone mr-1"></i> ${sessionScope.courierMobile}
                        </p>
                    </div>
                    <a href="<%= request.getContextPath() %>/CourierLogout.do"
                       class="btn btn-sm btn-outline-white btn-bold radius-1" style="white-space:nowrap;">
                        <i class="fas fa-sign-out-alt mr-1"></i> Logout
                    </a>
                </div>
            </div>
        </div>

        <!-- Search & Count Bar -->
        <div class="card acard mb-3 shadow-sm" style="border-radius:10px;">
            <div class="card-body py-2 px-3">
                <div class="row align-items-center">
                    <div class="col-md-5">
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bgc-white border-r-0"><i class="fas fa-search text-grey-m1"></i></span>
                            </div>
                            <input type="text" id="locationSearch" class="form-control border-l-0"
                                   placeholder="Search by client name, location, serial no..." autocomplete="off">
                        </div>
                    </div>
                    <div class="col-md-7 text-md-right mt-2 mt-md-0">
                        <span class="text-grey-d1 text-90">
                            <i class="fas fa-list mr-1"></i>
                            Total: <strong class="text-primary-d2" id="visibleCount">${pullbacks.size()}</strong> records
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Data Table Card -->
        <div class="card acard shadow-sm" style="border-radius:10px;">
            <div class="card-header bgc-primary-d1 py-2" style="border-radius:10px 10px 0 0;">
                <h6 class="card-title text-white text-100 mb-0">
                    <i class="fas fa-table mr-1"></i> Pickup List
                    <span class="badge badge-light ml-2 text-dark text-85">${pullbacks.size()} records</span>
                </h6>
            </div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${not empty pullbacks}">
                        <div class="table-responsive">
                            <table class="table table-striped-primary table-borderless border-0 mb-0" id="pickupTable">
                                <thead>
                                    <tr class="bgc-primary-l4 text-dark-tp3">
                                        <th style="min-width:45px; text-align:center;">#</th>
                                        <th style="min-width:160px;">Client Name</th>
                                        <th style="min-width:140px;">Client Contact No.</th>
                                        <th style="min-width:260px;">Client Address</th>
                                        <th style="min-width:130px;">Client Location</th>
                                        <th style="min-width:130px;">Contact Person</th>
                                        <th style="min-width:140px;">Serial No.</th>
                                        <th style="min-width:150px;">Printer Model</th>
                                        <th style="min-width:100px; text-align:center;">Unused Cartridge</th>
                                        <th style="min-width:110px; text-align:center;">Dispatch Date</th>
                                    </tr>
                                </thead>
                                <tbody id="pickupBody">
                                    <c:forEach items="${pullbacks}" var="pb" varStatus="loop">
                                        <tr>
                                            <td class="row-num text-600 text-center">${loop.index + 1}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty pb.clientName}">
                                                        <span class="text-600 text-dark-m2">${pb.clientName}</span>
                                                    </c:when>
                                                    <c:otherwise><span class="text-grey-m1">-</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty pb.clientContactNumber}">
                                                        <i class="fas fa-phone-alt text-success-d2 mr-1 text-80"></i>
                                                        <span class="text-dark-m1">${pb.clientContactNumber}</span>
                                                    </c:when>
                                                    <c:otherwise><span class="text-grey-m1">-</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty pb.clientAddress}">
                                                        <span class="text-dark-m3 text-90">${pb.clientAddress}</span>
                                                    </c:when>
                                                    <c:otherwise><span class="text-grey-m1">-</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <i class="fas fa-map-marker-alt text-primary-d1 mr-1 text-80"></i>
                                                <span class="text-dark-m1 location-cell">${pb.location}</span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty pb.contactPerson}">
                                                        <i class="fas fa-user text-secondary mr-1 text-80"></i>
                                                        <span class="text-dark-m1">${pb.contactPerson}</span>
                                                    </c:when>
                                                    <c:otherwise><span class="text-grey-m1">-</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><code class="text-primary-d2">${pb.serialNo}</code></td>
                                            <td><span class="text-dark-m2">${pb.printerModelName}</span></td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${pb.unusedCartridge != null && pb.unusedCartridge > 0}">
                                                        <span class="badge bgc-info text-white px-2 py-1">${pb.unusedCartridge}</span>
                                                    </c:when>
                                                    <c:otherwise><span class="text-grey-m1">-</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${pb.dispatchDate != null}">
                                                        <span class="text-dark-m1 text-90">
                                                            <fmt:formatDate value="${pb.dispatchDate}" pattern="dd-MMM-yyyy"/>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise><span class="text-grey-m1">-</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <!-- Pagination -->
                        <div class="card-footer bgc-white d-flex justify-content-between align-items-center flex-wrap py-2 px-3 brc-secondary-l3" id="paginationWrapper">
                            <span class="text-grey-d1 text-90" id="pageInfo"></span>
                            <nav>
                                <ul class="pagination pagination-sm mb-0" id="paginationControls"></ul>
                            </nav>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5 text-grey-m1">
                            <i class="fas fa-inbox fa-4x d-block mb-3 text-grey-l1"></i>
                            <h5 class="text-grey-d1">No Pickups Assigned</h5>
                            <p class="text-grey-m1">There are currently no pullback requests assigned to you.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Search + Pagination -->
    <script>
        $(function() {
            var PAGE_SIZE = 15;
            var currentPage = 1;

            var $input = $('#locationSearch');
            var $allRows = $('#pickupBody tr');
            var $count = $('#visibleCount');

            function getFilteredRows() {
                var query = $input.val().toLowerCase().trim();
                if (!query) return $allRows.toArray();
                return $allRows.filter(function() {
                    return $(this).text().toLowerCase().indexOf(query) > -1;
                }).toArray();
            }

            function renderPage() {
                var filteredRows = getFilteredRows();
                var total = filteredRows.length;
                var totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
                if (currentPage > totalPages) currentPage = totalPages;

                var start = (currentPage - 1) * PAGE_SIZE;
                var end = start + PAGE_SIZE;

                $allRows.hide();
                $.each(filteredRows, function(i, row) {
                    if (i >= start && i < end) {
                        $(row).show();
                        $(row).find('.row-num').text(i + 1);
                    }
                });

                $count.text(total);

                if (total === 0) {
                    $('#pageInfo').text('No records found');
                } else {
                    $('#pageInfo').text('Showing ' + (start + 1) + ' \u2013 ' + Math.min(end, total) + ' of ' + total + ' records');
                }

                var $pg = $('#paginationControls');
                $pg.empty();

                if (totalPages <= 1) { $('#paginationWrapper').hide(); return; }
                $('#paginationWrapper').show();

                // Previous
                $pg.append('<li class="page-item ' + (currentPage === 1 ? 'disabled' : '') + '">' +
                    '<a class="page-link" href="#" data-page="' + (currentPage - 1) + '">&laquo;</a></li>');

                var pages = buildPageNumbers(currentPage, totalPages);
                $.each(pages, function(_, p) {
                    if (p === '...') {
                        $pg.append('<li class="page-item disabled"><span class="page-link">...</span></li>');
                    } else {
                        $pg.append('<li class="page-item ' + (p === currentPage ? 'active' : '') + '">' +
                            '<a class="page-link" href="#" data-page="' + p + '">' + p + '</a></li>');
                    }
                });

                // Next
                $pg.append('<li class="page-item ' + (currentPage === totalPages ? 'disabled' : '') + '">' +
                    '<a class="page-link" href="#" data-page="' + (currentPage + 1) + '">&raquo;</a></li>');
            }

            function buildPageNumbers(current, total) {
                var pages = [];
                if (total <= 7) {
                    for (var i = 1; i <= total; i++) pages.push(i);
                } else {
                    pages.push(1);
                    if (current > 3) pages.push('...');
                    var s = Math.max(2, current - 1);
                    var e = Math.min(total - 1, current + 1);
                    for (var j = s; j <= e; j++) pages.push(j);
                    if (current < total - 2) pages.push('...');
                    pages.push(total);
                }
                return pages;
            }

            $(document).on('click', '#paginationControls a.page-link', function(e) {
                e.preventDefault();
                var page = parseInt($(this).data('page'));
                if (isNaN(page) || page < 1) return;
                currentPage = page;
                renderPage();
            });

            $input.on('keyup', function() {
                currentPage = 1;
                renderPage();
            });

            renderPage();
        });
    </script>
</body>
</html>
