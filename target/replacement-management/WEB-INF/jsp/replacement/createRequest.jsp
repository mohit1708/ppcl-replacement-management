<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Create Replacement Request" scope="request"/>
<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<div class="main-content-inner">
    <div class="page-content">


<div class="py-4">
    <h2 class="mb-4">
        <i class="fas fa-file-alt"></i> Create Replacement Request
    </h2>

    <!-- Database Error Message -->
    <c:if test="${not empty dbError}">
        <div class="alert alert-danger alert-dismissible fade show">
            <i class="fas fa-exclamation-triangle"></i>
            <strong>Database Error:</strong> ${dbError}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    </c:if>

    <!-- Duplicate Request Error Message -->
    <c:if test="${not empty duplicateError}">
        <div class="alert alert-danger alert-dismissible fade show">
            <i class="fas fa-exclamation-triangle"></i>
            <strong>Duplicate Request!</strong> ${duplicateError}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    </c:if>

    <!-- Progress Indicator -->
    <div class="card mb-4 bg-light">
        <div class="card-body p-3">
            <div class="row text-center">
                <div class="col step-indicator active" id="step1Indicator">
                    <div class="step-number">1</div>
                    <small>Basic Info</small>
                </div>
                <div class="col step-indicator" id="step2Indicator">
                    <div class="step-number">2</div>
                    <small>Contact</small>
                </div>
                <div class="col step-indicator" id="step3Indicator">
                    <div class="step-number">3</div>
                    <small>Locations</small>
                </div>
                <div class="col step-indicator" id="step4Indicator">
                    <div class="step-number">4</div>
                    <small>Printers</small>
                </div>
                <div class="col step-indicator" id="step5Indicator">
                    <div class="step-number">5</div>
                    <small>Comments</small>
                </div>
            </div>
        </div>
    </div>

    <form id="replacementForm" method="post"
          action="<%= request.getContextPath() %>/views/replacement/request">
        <input type="hidden" name="action" value="create">

        <!-- ============================================================ -->
        <!-- STEP 1: BASIC INFORMATION (Always Visible) -->
        <!-- ============================================================ -->
        <div class="card mb-4" id="step1Card">
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">
                    <i class="fas fa-info-circle"></i>
                    Step 1: Basic Information
                </h5>
            </div>
            <div class="card-body">
                <div class="row">
                    <!-- Client Dropdown -->
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="client">
                                Client <span class="text-danger">*</span>
                            </label>
                            <select name="clientId" id="client" class="form-control" required>
                                <option value="">Select Client</option>
                                <c:forEach items="${clients}" var="client">
                                    <!-- value is a representative CLIENT.ID (MIN(ID)) for this logical client -->
                                    <option value="${client.id}">
                                            ${client.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Replacement Type -->
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="replacementType">
                                Replacement Type <span class="text-danger">*</span>
                            </label>
                            <select name="replacementType" id="replacementType"
                                    class="form-control" required>
                                <option value="">Select Type</option>
                                <option value="DURING_CONTRACT">During Contract</option>
                                <option value="AFTER_CONTRACT">After Contract</option>
                            </select>
                            <small class="form-text text-muted">
                                This will be used as Contract Type during printer order booking
                            </small>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <!-- Reason -->
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="reason">
                                Reason <span class="text-danger">*</span>
                            </label>
                            <select name="reasonId" id="reason" class="form-control" required>
                                <option value="">Select Reason</option>
                                <c:forEach items="${reasons}" var="reason">
                                    <option value="${reason.id}">${reason.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Client Sign-In Location -->
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="signInLocation">
                                Client Sign-In Location <span class="text-danger">*</span>
                            </label>
                            <!-- populated via AJAX after Client selection. value = CLIENT.ID (branch row id) -->
                            <select name="signInBranchId" id="signInLocation" class="form-control" required>
                                <option value="">Select Sign-In Location</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <!-- TL Lead Dropdown -->
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="tlLead">
                                <i class="fas fa-user-tie text-primary"></i>
                                Assign TL Lead <span class="text-danger">*</span>
                            </label>
                            <select name="tlLeadId" id="tlLead" class="form-control" required>
                                <c:choose>
                                    <c:when test="${fn:length(tlLeads) == 1}">
                                        <c:forEach items="${tlLeads}" var="tl">
                                            <option value="${tl.id}" selected>${tl.name} (${tl.userId})
                                            </option>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="">Select TL Lead</option>
                                        <c:forEach items="${tlLeads}" var="tl">
                                            <option value="${tl.id}">${tl.name} (${tl.userId})</option>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </select>
                            <small class="form-text text-muted">
                                TL Lead will review and provide recommendation for this request
                            </small>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ============================================================ -->
        <!-- STEP 2: CONTACT DETAILS (Shows after client selection) -->
        <!-- ============================================================ -->
        <div class="card mb-4" id="step2Card" style="display:none;">
            <div class="card-header bg-info text-white">
                <h5 class="mb-0">
                    <i class="fas fa-user-circle"></i>
                    Step 2: Client Contact Details
                </h5>
            </div>
            <div class="card-body">
                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i>
                    <span id="contactSourceMsg">
                                    Contact details auto-filled from client master. You can edit if needed.
                                </span>
                </div>
                <div class="row">
                    <div class="col-md-4">
                        <div class="form-group">
                            <label for="contactName">
                                Contact Person <span class="text-danger">*</span>
                            </label>
                            <input type="text" name="clientContactName" id="contactName"
                                   class="form-control" required>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="form-group">
                            <label for="contactNumber">
                                Contact Number <span class="text-danger">*</span>
                            </label>
                            <input type="text" name="clientContactNumber" id="contactNumber"
                                   class="form-control" pattern="[0-9]{10}"
                                   title="10-digit mobile number" required>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="form-group">
                            <label for="contactEmail">Email (Optional)</label>
                            <input type="email" name="clientContactEmail" id="contactEmail"
                                   class="form-control">
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ============================================================ -->
        <!-- STEP 3: LOCATION SELECTION (Shows after client selection) -->
        <!-- ============================================================ -->
        <div class="card mb-4" id="step3Card" style="display:none;">
            <div class="card-header bg-success text-white">
                <h5 class="mb-0">
                    <i class="fas fa-map-marker-alt"></i>
                    Step 3: Select Locations for Replacement
                </h5>
            </div>
            <div class="card-body">
                <div class="alert alert-warning">
                    <i class="fas fa-info-circle"></i>
                    Select one or more locations where printer replacement is needed.
                    All printers at selected locations will be shown in next step.
                </div>

                <div class="form-group">
                    <label>
                        <i class="fas fa-building"></i> Select Location(s)
                    </label>
                    <div class="dropdown" id="locationDropdownContainer">
                        <button class="btn btn-outline-success dropdown-toggle w-100 text-left"
                                type="button" id="locationDropdownBtn"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <span id="locationDropdownLabel">Select locations...</span>
                        </button>
                        <div class="dropdown-menu w-100 p-2" aria-labelledby="locationDropdownBtn"
                             id="locationDropdownMenu" style="max-height: 300px; overflow-y: auto;">
                            <div class="dropdown-item-text border-bottom pb-2 mb-2">
                                <label class="form-check-label d-flex align-items-center mb-0 font-weight-bold"
                                       for="selectAllLocationsCheck" style="cursor: pointer;">
                                    <input type="checkbox" class="form-check-input mr-2 mt-0"
                                           id="selectAllLocationsCheck"
                                           onchange="toggleAllLocationCheckboxes()">
                                    Select All
                                </label>
                            </div>
                            <div id="locationCheckboxList">
                                <!-- Location checkboxes loaded via AJAX -->
                            </div>
                        </div>
                    </div>
                </div>

                <div id="selectedLocationsDisplay" class="mt-3" style="display:none;">
                    <strong>Selected:</strong> <span id="selectedLocationCount">0</span> location(s)
                    <div id="selectedLocationTags" class="mt-2">
                        <!-- Selected location tags will appear here -->
                    </div>
                </div>
            </div>
        </div>

        <!-- ============================================================ -->
        <!-- STEP 4: PRINTER SELECTION WITH NEW MODEL (Shows after location selection) -->
        <!-- ============================================================ -->
        <div class="card mb-4" id="step4Card" style="display:none;">
            <div class="card-header bg-warning text-dark">
                <h5 class="mb-0">
                    <i class="fas fa-print"></i>
                    Step 4: Select Printers & Specify New Models
                </h5>
            </div>
            <div class="card-body">
                <div class="alert alert-info">
                    <i class="fas fa-lightbulb"></i>
                    <strong>For each printer:</strong> Check to select for replacement, then optionally
                    specify a new model.
                </div>

                <!-- Filter Section -->
                <div class="row mb-3">
                    <div class="col-md-4">
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text"><i class="fas fa-search"></i></span>
                            </div>
                            <input type="text" id="printerFilter" class="form-control"
                                   placeholder="Filter by model, serial, location..."
                                   onkeyup="filterPrinters()">
                        </div>
                    </div>
                    <div class="col-md-3">
                        <select id="locationFilter" class="form-control" onchange="filterPrinters()">
                            <option value="">All Locations</option>
                        </select>
                    </div>
                    <div class="col-md-5 text-right">
                        <button type="button" class="btn btn-sm btn-outline-primary mr-2"
                                onclick="selectAllVisiblePrinters()">
                            <i class="fas fa-check-double"></i> Select All Visible
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-secondary"
                                onclick="clearPrinterSelection()">
                            <i class="fas fa-times"></i> Clear Selection
                        </button>
                        <span class="ml-3 badge badge-info" id="printerCountBadge">0 printers</span>
                    </div>
                </div>

                <!-- Printer List Table -->
                <div class="table-responsive">
                    <table class="table table-hover table-bordered" id="printersTable">
                        <thead class="thead-light">
                        <tr>
                            <th style="width:40px;">
                                <input type="checkbox" id="selectAllPrintersHeader"
                                       onchange="toggleAllVisiblePrinters()">
                            </th>
                            <th>Location</th>
                            <th>Current Model</th>
                            <th>Serial</th>
                            <th>AGR_PROD ID</th>
                            <th>New Model</th>
                        </tr>
                        </thead>
                        <tbody id="printersTableBody">
                        <!-- Printers loaded via AJAX -->
                        </tbody>
                    </table>
                </div>
                <div id="noPrintersMessage" class="text-center text-muted py-4" style="display:none;">
                    <i class="fas fa-filter fa-2x mb-2"></i>
                    <p>No printers match the current filter.</p>
                </div>
            </div>
        </div>

        <!-- ============================================================ -->
        <!-- STEP 5: COMMENTS (Shows after printers are selected) -->
        <!-- ============================================================ -->
        <div class="card mb-4" id="step5Card" style="display:none;">
            <div class="card-header bg-secondary text-white">
                <h5 class="mb-0">
                    <i class="fas fa-comments"></i>
                    Step 5: Additional Comments / Notes
                </h5>
            </div>
            <div class="card-body">
                <div class="form-group">
                    <label for="comments">
                        Comments or Special Instructions (Optional)
                    </label>
                    <textarea name="comments" id="comments" class="form-control" rows="5"
                              placeholder="Add any additional context, special requirements, or notes for the reviewers and approvers..."></textarea>
                    <small class="form-text text-muted">
                        <i class="fas fa-info-circle"></i>
                        This will be visible to all stakeholders in the approval workflow.
                    </small>
                </div>
            </div>
        </div>

        <!-- ============================================================ -->
        <!-- SUBMIT BUTTONS -->
        <!-- ============================================================ -->
        <div class="text-right mb-5" id="submitSection" style="display:none;">
            <a href="<%= request.getContextPath() %>/views/replacement/dashboard"
               class="btn btn-secondary btn-lg">
                <i class="fas fa-times"></i> Cancel
            </a>
            <button type="submit" class="btn btn-success btn-lg" id="submitBtn">
                <i class="fas fa-paper-plane"></i> Submit Replacement Request
            </button>
        </div>
    </form>

    <!-- ============================================================ -->
    <!-- EMBEDDED: MY REPLACEMENT REQUESTS -->
    <!-- ============================================================ -->
    <div class="my-requests-section" id="myRequestsSection">
        <jsp:include page="myRequests.jsp">
            <jsp:param name="embedded" value="true"/>
        </jsp:include>
    </div>

</div>


<%@ include file="common/footer.jsp" %>

<script>
    let selectedLocations = [];
    let allPrinterModels = []; // Will load from PMODEL table
    let formSubmitting = false; // Flag to prevent duplicate submissions

    // Saved form data from server (for duplicate error recovery)
    var savedFormData = null;
    <c:if test="${not empty savedFormData}">
    savedFormData = ${savedFormData};
    </c:if>

    $(document).ready(function () {
        console.log("✅ Page loaded");

        // Check for error parameters in URL
        checkUrlErrors();

        // Load all printer models for dropdowns
        loadAllPrinterModels();

        // Handle prefill data if present
        <c:if test="${isPrefill}">
        handlePrefillData();
        </c:if>

        // Restore form data if returning from duplicate error
        if (savedFormData) {
            restoreFormDataAfterError();
        }

        // Client selection handler (value = representative CLIENT.ID)
        $('#client').change(function () {
            const clientId = $(this).val();
            console.log("Client selected:", clientId);

            // Always clear previous state when client changes
            clearAllFormState();

            if (!clientId) {
                $('#signInLocation').html('<option value="">Select Sign-In Location</option>');
                return;
            }

            // Step 1b: load sign-in branches for this client
            loadClientBranches(clientId);
        });

        // Sign-in branch selection handler (value = CLIENT.ID)
        $('#signInLocation').change(function () {
            const branchId = $(this).val();
            const clientId = $('#client').val();

            if (!branchId) {
                // Reset downstream steps
                $('#step2Card, #step3Card, #step4Card, #step5Card, #submitSection').hide();
                $('#step2Indicator, #step3Indicator, #step4Indicator, #step5Indicator').removeClass('active');
                return;
            }

            loadBranchContact(branchId);
            // Now load all locations (branches) for this logical client
            loadClientLocations(clientId);
        });

        // Initialize Bootstrap dropdown manually for location dropdown
        $('#locationDropdownBtn').dropdown();
    });

    function hideAllSteps() {
        $('#step2Card, #step3Card, #step4Card, #step5Card, #submitSection').hide();
        $('#step2Indicator, #step3Indicator, #step4Indicator, #step5Indicator')
            .removeClass('active');
        selectedLocations = [];
    }

    // Clear all form state when client changes - prevents stale data
    function clearAllFormState() {
        // Hide all steps
        hideAllSteps();

        // Reset sign-in location dropdown
        $('#signInLocation').html('<option value="">Select Sign-In Location</option>');

        // Clear contact fields
        $('#contactName').val('');
        $('#contactNumber').val('');
        $('#contactEmail').val('');

        // Clear location checkboxes
        $('#locationCheckboxList').html('');
        $('#locationDropdownLabel').text('Select locations...');
        $('#selectAllLocationsCheck').prop('checked', false);
        $('#selectedLocationsDisplay').hide();

        // Clear printers table
        $('#printersTableBody').html('');
        allPrintersData = [];

        // Uncheck all printer checkboxes and reset header checkbox
        $('.printer-checkbox').prop('checked', false);
        $('#selectAllPrintersHeader').prop('checked', false);

        // Clear location filter dropdown
        $('#printerLocationFilter').html('<option value="">All Locations</option>');
        $('#printerTextFilter').val('');

        // Clear comments
        $('#comments').val('');

        // Reset submit button and submission flag
        formSubmitting = false;
        $('#submitBtn').prop('disabled', false).html('<i class="fas fa-paper-plane"></i> Submit Request');

        console.log("🧹 Form state cleared");
    }

    //not needed we can make ajax call to get printer models only for client and location combination
    function loadAllPrinterModels() {
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {action: 'getAllPrinterModels'},
            dataType: 'json',
            success: function (response) {
                console.log("Printer models loaded:", response);
                if (response.success) {
                    allPrinterModels = response.data;
                }
            },
            error: function () {
                console.error("Error loading printer models");
            }
        });
    }

    function loadClientBranches(clientId) {
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {action: 'getClientBranches', clientId: clientId},
            dataType: 'json',
            success: function (response) {
                console.log("✅ Branches loaded:", response);
                if (!response.success) return;

                // Populate sign-in dropdown with BRANCH + ADDRESS
                let options = '<option value="">Select Sign-In Location</option>';
                response.data.forEach(function (b) {
                    const addr = (b.address || '').trim();
                    const label = addr ? (b.branch) : (b.branch || '');
                    // IMPORTANT: build <option> using concatenation (avoid JS template literals)
                    // so JSP doesn't try to parse  as EL.
                    options += '<option value="' + b.id + '">' + escapeHtml(label) + '</option>';
                });
                $('#signInLocation').html(options);
            },
            error: function () {
                alert("Error loading client branches. Please try again.");
            }
        });
    }

    function loadBranchContact(branchId) {
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {action: 'getBranchDetails', branchId: branchId},
            dataType: 'json',
            success: function (response) {
                console.log("✅ Branch contact loaded:", response);
                if (response.success) {
                    $('#contactName').val(response.data.contactPerson || '');
                    $('#contactNumber').val(response.data.mobileNo || '');
                    $('#contactEmail').val(response.data.emailId1 || '');

                    $('#step2Card').fadeIn();
                    $('#step2Indicator').addClass('active');
                }
            },
            error: function () {
                alert("Error loading branch contact. Please try again.");
            }
        });
    }

    // tiny helper because we are injecting strings into <option>
    function escapeHtml(str) {
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }


    function loadClientLocations(clientId) {
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {action: 'getClientLocations', clientId: clientId},
            dataType: 'json',
            success: function (response) {
                console.log("✅ Locations loaded:", response);
                if (response.success && response.data.length > 0) {
                    displayLocations(response.data);
                    $('#step3Card').fadeIn();
                    $('#step3Indicator').addClass('active');
                } else {
                    alert("No locations found for this client!");
                }
            },
            error: function () {
                alert("Error loading locations. Please try again.");
            }
        });
    }

    let allLocationsData = []; // Store all locations for reference

    function displayLocations(locations) {
        allLocationsData = locations;
        let html = '';
        locations.forEach(function (loc) {
            const label = (loc.branch || 'Branch ' + loc.id) + ' - ' + (loc.city || 'N/A') + (loc.state ? ', ' + loc.state : '');
            html += '<div class="dropdown-item-text py-1">' +
                '<label class="form-check-label d-flex align-items-center mb-0" for="loc_' + loc.id + '" style="cursor: pointer;">' +
                '<input type="checkbox" class="form-check-input location-checkbox mr-2 mt-0" ' +
                'id="loc_' + loc.id + '" value="' + loc.id + '" ' +
                'data-location=\'' + JSON.stringify(loc).replace(/'/g, "&#39;") + '\' ' +
                'onchange="updateSelectedLocations()">' +
                escapeHtml(label) +
                '</label>' +
                '</div>';
        });

        $('#locationCheckboxList').html(html);

        // Prevent dropdown from closing when clicking checkboxes
        $('#locationDropdownMenu').on('click', function (e) {
            e.stopPropagation();
        });
    }

    function toggleAllLocationCheckboxes() {
        let isChecked = $('#selectAllLocationsCheck').is(':checked');
        $('.location-checkbox').prop('checked', isChecked);
        updateSelectedLocations();
    }

    function updateSelectedLocations() {
        selectedLocations = [];
        $('.location-checkbox:checked').each(function () {
            let locData = JSON.parse($(this).attr('data-location'));
            selectedLocations.push(locData);
        });

        console.log("Selected locations:", selectedLocations);

        // Update dropdown button label
        let totalLocations = $('.location-checkbox').length;
        let checkedCount = selectedLocations.length;

        if (checkedCount === 0) {
            $('#locationDropdownLabel').text('Select locations...');
        } else if (checkedCount === totalLocations) {
            $('#locationDropdownLabel').text('All locations selected (' + checkedCount + ')');
        } else {
            $('#locationDropdownLabel').text(checkedCount + ' location(s) selected');
        }

        // Update "Select All" checkbox state
        $('#selectAllLocationsCheck').prop('checked', checkedCount === totalLocations && totalLocations > 0);

        // Update display
        if (selectedLocations.length > 0) {
            $('#selectedLocationsDisplay').show();
            $('#selectedLocationCount').text(selectedLocations.length);
            renderSelectedLocationTags();
            loadPrintersForLocations();
        } else {
            $('#selectedLocationsDisplay').hide();
            $('#selectedLocationTags').empty();
            $('#step4Card, #step5Card, #submitSection').hide();
            $('#step4Indicator, #step5Indicator').removeClass('active');
        }
    }

    function renderSelectedLocationTags() {
        let tagsHtml = '';
        let totalLocations = $('.location-checkbox').length;

        // If all locations are selected, show single "All locations" badge
        if (selectedLocations.length === totalLocations && totalLocations > 0) {
            tagsHtml = '<span class="badge badge-success mr-2 mb-2 p-2" style="font-size: 0.9rem;">' +
                '<i class="fas fa-check-circle mr-1"></i>' +
                'All locations are selected (' + totalLocations + ')' +
                '</span>';
        } else {
            selectedLocations.forEach(function (loc) {
                const label = (loc.branch || 'Branch ' + loc.id) + ' - ' + (loc.city || 'N/A');
                tagsHtml += '<span class="badge badge-info mr-2 mb-2 p-2" style="font-size: 0.9rem;">' +
                    '<i class="fas fa-map-marker-alt mr-1"></i>' +
                    escapeHtml(label) +
                    '<button type="button" class="close ml-2 text-white" ' +
                    'style="font-size: 1rem; line-height: 1; opacity: 0.8;" ' +
                    'onclick="removeSelectedLocation(' + loc.id + ')" ' +
                    'title="Remove">&times;</button>' +
                    '</span>';
            });
        }
        $('#selectedLocationTags').html(tagsHtml);
    }

    function removeSelectedLocation(locId) {
        $('#loc_' + locId).prop('checked', false);
        updateSelectedLocations();
    }

    function loadPrintersForLocations() {
        let locationIds = selectedLocations.map(l => l.id);

        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {
                action: 'getPrintersByLocations',
                locationIds: JSON.stringify(locationIds)
            },
            dataType: 'json',
            success: function (response) {
                console.log("✅ Printers loaded:", response);
                if (response.success) {
                    displayPrintersGroupedByLocation(response.data);
                    $('#step4Card').fadeIn();
                    $('#step4Indicator').addClass('active');
                } else {
                    alert("No printers found for selected locations!");
                }
            },
            error: function () {
                alert("Error loading printers. Please try again.");
            }
        });
    }

    let allPrintersData = []; // Store all printers for filtering

    function displayPrintersGroupedByLocation(printers) {
        allPrintersData = printers;

        // Populate location filter dropdown
        let locationFilterOptions = '<option value="">All Locations</option>';
        selectedLocations.forEach(function (loc) {
            locationFilterOptions += '<option value="' + loc.id + '">' + escapeHtml(loc.branch || 'Branch ' + loc.id) + '</option>';
        });
        $('#locationFilter').html(locationFilterOptions);

        // Update printer count badge
        $('#printerCountBadge').text(printers.length + ' printers');

        renderPrintersTable(printers);
    }

    function renderPrintersTable(printers) {
        let html = '';

        printers.forEach(function (printer, idx) {
            // Find the location for this printer
            let loc = selectedLocations.find(l => l.id == printer.clientBrId) || {};
            let uniqueKey = printer.clientBrId + '_' + idx;
            let locationName = loc.branch || 'Branch ' + printer.clientBrId;

            // Check if printer is already in another replacement request
            let isDuplicate = printer.existingRequestId && printer.existingRequestId > 0;
            let rowClass = 'printer-row';
            let serialDisplay = '<code style="color: black;">' + escapeHtml(printer.serial || 'N/A') + '</code>';

            if (isDuplicate) {
                serialDisplay += '<br><a href="javascript:void(0)" onclick="openViewModal(' + printer.existingRequestId + ', \'\')" class="badge badge-danger" title="Click to view request">' +
                    '<i class="fas fa-exclamation-triangle"></i> REQ-' + printer.existingRequestId + '</a>' +
                    '<br><span class="text-danger font-weight-bold">Duplicate</span>';
            }

            html += '<tr class="' + rowClass + '" data-location="' + printer.clientBrId + '" ' +
                'data-model="' + escapeHtml(printer.modelName || '').toLowerCase() + '" ' +
                'data-serial="' + escapeHtml(printer.serial || '') + '" ' +
                'data-key="' + uniqueKey + '" ' +
                'data-duplicate="' + (isDuplicate ? 'true' : 'false') + '" ' +
                'data-location-name="' + escapeHtml(locationName).toLowerCase() + '">' +
                '<td class="text-center">' +
                '<input type="checkbox" class="printer-checkbox" ' +
                'name="printers[' + uniqueKey + '].selected" ' +
                'id="printer_' + uniqueKey + '" value="1" ' +
                (isDuplicate ? 'disabled title="Already in REQ-' + printer.existingRequestId + '"' : '') +
                'onchange="updatePrinterSelection()">' +
                '</td>' +
                '<td><i class="fas fa-building text-muted"></i> ' + escapeHtml(locationName) + '</td>' +
                '<td><span class="badge badge-secondary">' + escapeHtml(printer.modelName || 'N/A') + '</span></td>' +
                '<td>' + serialDisplay + '</td>' +
                '<td><small class="text-muted">' + escapeHtml(printer.agrProdId || 'N/A') + '</small></td>' +
                '<td>' +
                '<select class="form-control form-control-sm new-model-dropdown" ' +
                'name="printers[' + uniqueKey + '].newModelId" ' +
                'id="newModelDropdown_' + uniqueKey + '" ' +
                (isDuplicate ? 'disabled' : '') +
                'onchange="handleDropdownSelection(\'' + uniqueKey + '\')">' +
                '<option value="">-- Select --</option>' +
                buildPrinterModelOptions() +
                '</select>' +
                '<input type="text" class="form-control form-control-sm mt-1 new-model-text" ' +
                'name="printers[' + uniqueKey + '].newModelText" ' +
                'id="newModelText_' + uniqueKey + '" ' +
                'placeholder="Or type manually" ' +
                (isDuplicate ? 'disabled' : '') +
                'onkeyup="handleManualText(\'' + uniqueKey + '\')">' +
                '<input type="hidden" name="printers[' + uniqueKey + '].clientBrId" value="' + printer.clientBrId + '">' +
                '<input type="hidden" name="printers[' + uniqueKey + '].agrProdId" value="' + printer.agrProdId + '">' +
                '<input type="hidden" name="printers[' + uniqueKey + '].pModelId" value="' + printer.pModelId + '">' +
                '<input type="hidden" name="printers[' + uniqueKey + '].serial" value="' + printer.serial + '">' +
                '</td>' +
                '</tr>';
        });

        $('#printersTableBody').html(html);

        // Show/hide no printers message
        if (printers.length === 0) {
            $('#printersTable').hide();
            $('#noPrintersMessage').show();
        } else {
            $('#printersTable').show();
            $('#noPrintersMessage').hide();
        }
    }

    function filterPrinters() {
        let textFilter = ($('#printerFilter').val() || '').toLowerCase().trim();
        let locationFilter = $('#locationFilter').val();

        let visibleCount = 0;

        $('#printersTableBody tr.printer-row').each(function () {
            let row = $(this);
            let locationId = row.data('location');
            let model = row.data('model') || '';
            let serial = row.data('serial') || '';
            let locationName = row.data('location-name') || '';

            let matchesLocation = !locationFilter || locationId == locationFilter;
            let matchesText = !textFilter ||
                model.indexOf(textFilter) !== -1 ||
                serial.indexOf(textFilter) !== -1 ||
                locationName.indexOf(textFilter) !== -1;

            if (matchesLocation && matchesText) {
                row.show();
                visibleCount++;
            } else {
                row.hide();
            }
        });

        // Show/hide no results message
        if (visibleCount === 0 && allPrintersData.length > 0) {
            $('#printersTable').hide();
            $('#noPrintersMessage').show();
        } else {
            $('#printersTable').show();
            $('#noPrintersMessage').hide();
        }

        $('#printerCountBadge').text(visibleCount + ' of ' + allPrintersData.length + ' printers');
    }

    function selectAllVisiblePrinters() {
        $('#printersTableBody tr.printer-row:visible .printer-checkbox').prop('checked', true);
        updatePrinterSelection();
    }

    function toggleAllVisiblePrinters() {
        let isChecked = $('#selectAllPrintersHeader').is(':checked');
        $('#printersTableBody tr.printer-row:visible .printer-checkbox').prop('checked', isChecked);
        updatePrinterSelection();
    }

    function clearPrinterSelection() {
        $('.printer-checkbox').prop('checked', false);
        $('#selectAllPrintersHeader').prop('checked', false);
        updatePrinterSelection();
    }

    function buildPrinterModelOptions() {
        let options = '';
        allPrinterModels.forEach(function (model) {
            options += '<option value="' + model.id + '">' + escapeHtml(model.modelName) + '</option>';
        });
        return options;
    }

    function handleDropdownSelection(uniqueKey) {
        let dropdown = $('#newModelDropdown_' + uniqueKey);
        let textbox = $('#newModelText_' + uniqueKey);

        if (dropdown.val()) {
            textbox.val('').prop('disabled', true).addClass('bg-secondary text-white');
        } else {
            textbox.prop('disabled', false).removeClass('bg-secondary text-white');
        }
    }

    function handleManualText(uniqueKey) {
        let dropdown = $('#newModelDropdown_' + uniqueKey);
        let textbox = $('#newModelText_' + uniqueKey);

        if (textbox.val().trim()) {
            dropdown.val('').prop('disabled', true);
        } else {
            dropdown.prop('disabled', false);
        }
    }

    function updatePrinterSelection() {
        let selectedCount = $('.printer-checkbox:checked').length;

        if (selectedCount > 0) {
            $('#step5Card').fadeIn();
            $('#step5Indicator').addClass('active');
            $('#submitSection').fadeIn();
        } else {
            $('#step5Card').hide();
            $('#step5Indicator').removeClass('active');
            $('#submitSection').hide();
        }
    }

    $('#replacementForm').submit(function (e) {
        // Prevent duplicate submissions
        if (formSubmitting) {
            e.preventDefault();
            console.log("⚠️ Form already submitting, ignoring duplicate submit");
            return false;
        }

        let selectedPrinters = $('.printer-checkbox:checked').length;

        if (selectedPrinters === 0) {
            e.preventDefault();
            alert("⚠️ Please select at least one printer for replacement!");
            return false;
        }

        if (!$('#contactName').val() || !$('#contactNumber').val()) {
            e.preventDefault();
            alert("⚠️ Please provide client contact details!");
            return false;
        }

        // Mark form as submitting
        formSubmitting = true;

        $('#submitBtn').prop('disabled', true)
            .html('<i class="fas fa-spinner fa-spin"></i> Submitting...');
        return true;
    });

    // Handle prefilled data from URL parameters
    function handlePrefillData() {
        const prefillData = {
            clientId: '${prefillData.clientId}',
            signInBranchId: '${prefillData.signInBranchId}',
            contactName: '${prefillData.contactName}',
            contactEmail: '${prefillData.contactEmail}',
            contactPhone: '${prefillData.contactPhone}',
            reasonId: '${prefillData.reasonId}',
            comments: '${prefillData.comments}',
            tlId: '${prefillData.tlId}'
        };

        console.log("Prefilling form with:", prefillData);

        // Set client and clear stale state first
        if (prefillData.clientId) {
            $('#client').val(prefillData.clientId);
            clearAllFormState();
        }

        // Set dropdowns after clear
        if (prefillData.reasonId) {
            $('#reason').val(prefillData.reasonId);
        }
        if (prefillData.tlId) {
            $('#tlLead').val(prefillData.tlId);
        }
        if (prefillData.comments) {
            $('#comments').val(decodeURIComponent(prefillData.comments.replace(/\+/g, ' ')));
        }

        // Load branches via AJAX, then select sign-in location
        if (prefillData.clientId) {
            loadClientBranchesPrefill(prefillData.clientId, prefillData.signInBranchId, prefillData);
        }
    }

    function loadClientBranchesPrefill(clientId, signInBranchId, prefillData) {
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {action: 'getClientBranches', clientId: clientId},
            dataType: 'json',
            success: function (response) {
                if (!response.success) return;

                var options = '<option value="">Select Sign-In Location</option>';
                response.data.forEach(function (b) {
                    var addr = (b.address || '').trim();
                    var label = addr ? (b.branch) : (b.branch || '');
                    var selected = (String(b.id) === String(signInBranchId)) ? ' selected' : '';
                    options += '<option value="' + b.id + '"' + selected + '>' + escapeHtml(label) + '</option>';
                });
                $('#signInLocation').html(options);

                if (signInBranchId) {
                    $('#signInLocation').val(signInBranchId);
                    // Load contact details
                    loadBranchContact(signInBranchId);
                    // Load locations, then auto-select and load printers
                    loadLocationsPrefill(clientId, signInBranchId, prefillData);
                }

                // Override contact details after branch contact loads
                setTimeout(function () {
                    if (prefillData.contactName) {
                        $('#contactName').val(decodeURIComponent(prefillData.contactName.replace(/\+/g, ' ')));
                    }
                    if (prefillData.contactPhone) {
                        $('#contactNumber').val(prefillData.contactPhone);
                    }
                    if (prefillData.contactEmail) {
                        $('#contactEmail').val(decodeURIComponent(prefillData.contactEmail.replace(/\+/g, ' ')));
                    }
                }, 1000);
            },
            error: function () {
                alert("Error loading client branches. Please try again.");
            }
        });
    }

    function loadLocationsPrefill(clientId, signInBranchId, prefillData) {
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {action: 'getClientLocations', clientId: clientId},
            dataType: 'json',
            success: function (response) {
                if (!response.success || response.data.length === 0) return;

                displayLocations(response.data);
                $('#step3Card').fadeIn();
                $('#step3Indicator').addClass('active');

                // Auto-select the sign-in location checkbox
                $('#loc_' + signInBranchId).prop('checked', true);

                // Manually build selectedLocations without calling updateSelectedLocations
                // (which would trigger loadPrintersForLocations and cause a double load)
                selectedLocations = [];
                $('.location-checkbox:checked').each(function () {
                    var locData = JSON.parse($(this).attr('data-location'));
                    selectedLocations.push(locData);
                });
                $('#locationDropdownLabel').text(selectedLocations.length + ' location(s) selected');
                $('#selectedLocationsDisplay').show();
                $('#selectedLocationCount').text(selectedLocations.length);
                renderSelectedLocationTags();

                // Load printers with auto-select by serial
                loadPrintersPrefill(prefillData);
            }
        });
    }

    function loadPrintersPrefill(prefillData) {
        var locationIds = selectedLocations.map(function (l) { return l.id; });

        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {
                action: 'getPrintersByLocations',
                locationIds: JSON.stringify(locationIds)
            },
            dataType: 'json',
            success: function (response) {
                if (!response.success) return;

                displayPrintersGroupedByLocation(response.data);
                $('#step4Card').fadeIn();
                $('#step4Indicator').addClass('active');

                // Auto-select the printer matching the prefill serial
                if (prefillData.signInBranchId) {
                    var prefillSerial = '${prefillData.serial}';
                    if (prefillSerial) {
                        $('#printersTableBody tr.printer-row').each(function () {
                            var row = $(this);
                            if (row.data('serial') === prefillSerial && row.data('duplicate') !== 'true') {
                                row.find('.printer-checkbox').prop('checked', true);
                            }
                        });
                        updatePrinterSelection();

                        // Show step 5 if printer selected
                        if ($('.printer-checkbox:checked').length > 0) {
                            $('#step5Card').fadeIn();
                            $('#step5Indicator').addClass('active');
                            $('#submitSection').fadeIn();
                        }
                    }
                }
            }
        });
    }

    // Check URL for error parameters and display appropriate messages
    function checkUrlErrors() {
        const urlParams = new URLSearchParams(window.location.search);
        const error = urlParams.get('error');

        if (error === 'no_printers_selected') {
            alert('⚠️ Please select at least one printer for replacement!');
            // Clean URL without reloading
            const cleanUrl = window.location.pathname;
            window.history.replaceState({}, document.title, cleanUrl + '?action=new');
        }
    }

    // Restore form data after duplicate error
    function restoreFormDataAfterError() {
        if (!savedFormData) return;

        console.log("🔄 Restoring form data after duplicate error:", savedFormData);

        // Set basic form fields
        if (savedFormData.clientId) {
            $('#client').val(savedFormData.clientId);
        }
        if (savedFormData.replacementType) {
            $('#replacementType').val(savedFormData.replacementType);
        }
        if (savedFormData.reasonId) {
            $('#reason').val(savedFormData.reasonId);
        }
        if (savedFormData.tlLeadId) {
            $('#tlLead').val(savedFormData.tlLeadId);
        }
        if (savedFormData.comments) {
            $('#comments').val(savedFormData.comments);
        }
        if (savedFormData.contactName) {
            $('#contactName').val(savedFormData.contactName);
        }
        if (savedFormData.contactNumber) {
            $('#contactNumber').val(savedFormData.contactNumber);
        }
        if (savedFormData.contactEmail) {
            $('#contactEmail').val(savedFormData.contactEmail);
        }

        // Load branches for selected client, then continue restoration
        if (savedFormData.clientId) {
            $.ajax({
                url: '<%= request.getContextPath() %>/views/replacement/request',
                data: {action: 'getClientBranches', clientId: savedFormData.clientId},
                dataType: 'json',
                success: function (response) {
                    if (response.success) {
                        let options = '<option value="">Select Sign-In Location</option>';
                        response.data.forEach(function (b) {
                            const addr = (b.address || '').trim();
                            const label = addr ? (b.branch + ': ' + addr) : (b.branch || '');
                            const selected = (b.id == savedFormData.signInBranchId) ? ' selected' : '';
                            options += '<option value="' + b.id + '"' + selected + '>' + escapeHtml(label) + '</option>';
                        });
                        $('#signInLocation').html(options);

                        // Show step 2
                        $('#step2Card').fadeIn();
                        $('#step2Indicator').addClass('active');

                        // Load locations and printers
                        restoreLocationsAndPrinters();
                    }
                }
            });
        }
    }

    function restoreLocationsAndPrinters() {
        if (!savedFormData || !savedFormData.clientId) return;

        // Get unique location IDs from saved printers
        var locationIds = [];
        if (savedFormData.printers) {
            savedFormData.printers.forEach(function (p) {
                if (locationIds.indexOf(p.clientBrId) === -1) {
                    locationIds.push(p.clientBrId);
                }
            });
        }

        // Load locations
        $.ajax({
            url: '<%= request.getContextPath() %>/views/replacement/request',
            data: {action: 'getClientLocations', clientId: savedFormData.clientId},
            dataType: 'json',
            success: function (response) {
                if (response.success && response.data.length > 0) {
                    displayLocations(response.data);
                    $('#step3Card').fadeIn();
                    $('#step3Indicator').addClass('active');

                    // Check the locations that were selected
                    response.data.forEach(function (loc) {
                        if (locationIds.indexOf(loc.id) !== -1) {
                            $('#loc_' + loc.id).prop('checked', true);
                        }
                    });

                    // Update selected locations array
                    updateSelectedLocations();

                    // After printers are loaded, restore selections
                    setTimeout(function () {
                        restorePrinterSelections();
                    }, 500);
                }
            }
        });
    }

    function restorePrinterSelections() {
        if (!savedFormData || !savedFormData.printers) return;

        savedFormData.printers.forEach(function (p) {
            var uniqueKey = p.clientBrId + '_';

            // Find the printer row by serial
            $('#printersTableBody tr.printer-row').each(function () {
                var row = $(this);
                var serial = row.data('serial');

                if (serial === p.serial) {
                    var checkbox = row.find('.printer-checkbox');

                    // Don't check duplicate printers
                    if (!p.isDuplicate) {
                        checkbox.prop('checked', true);
                    }

                    // Highlight duplicate printers with warning
                    if (p.isDuplicate) {
                        row.addClass('table-danger');
                        row.find('td:first').append('<br><small class="text-danger"><i class="fas fa-exclamation-triangle"></i> Duplicate</small>');
                    }

                    // Restore new model selection
                    var rowKey = row.data('key');
                    if (p.newModelId) {
                        $('#newModelDropdown_' + rowKey).val(p.newModelId);
                        handleDropdownSelection(rowKey);
                    } else if (p.newModelText) {
                        $('#newModelText_' + rowKey).val(p.newModelText);
                        handleManualText(rowKey);
                    }
                }
            });
        });

        // Update printer selection state
        updatePrinterSelection();

        // Show step 5 if there are non-duplicate selections
        var selectedCount = $('.printer-checkbox:checked').length;
        if (selectedCount > 0) {
            $('#step5Card').fadeIn();
            $('#step5Indicator').addClass('active');
            $('#submitSection').fadeIn();
        }
    }
</script>

<style>
    /* Step Indicator Styles */
    .step-indicator {
        opacity: 0.4;
        transition: all 0.3s ease;
    }

    .step-indicator.active {
        opacity: 1;
    }

    .step-number {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background-color: #6c757d;
        color: white;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        margin-bottom: 5px;
    }

    .step-indicator.active .step-number {
        background-color: #007bff;
        box-shadow: 0 0 10px rgba(0, 123, 255, 0.5);
    }

    /* Location Dropdown Styles */
    #locationDropdownBtn {
        border: 2px solid #28a745;
    }

    #locationDropdownMenu .form-check-label {
        cursor: pointer;
        width: 100%;
    }

    #locationDropdownMenu .dropdown-item-text:hover {
        background-color: #f8f9fa;
    }

    /* Printer Table Styles */
    #printersTable .printer-checkbox:checked {
        accent-color: #ffc107;
    }

    #printersTableBody tr:has(.printer-checkbox:checked) {
        background-color: #fff3cd;
    }

    #printersTable th, #printersTable td {
        vertical-align: middle;
    }

    #printersTable .new-model-dropdown,
    #printersTable .new-model-text {
        max-width: 200px;
    }

    .location-section {
        animation: fadeIn 0.5s ease;
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    /* Embedded My Requests Section */
    .my-requests-section {
        margin-top: 2rem;
        border-top: 2px solid #dee2e6;
        padding-top: 2rem;
    }
</style>
