










let selectedLocations = [];
let allPrinterModels = []; // Will load from PMODEL table

$(document).ready(function() {
    console.log("✅ Page loaded");

    // Load all printer models for dropdowns
     loadAllPrinterModels();

    // Client selection handler (value = representative CLIENT.ID)
    $('#client').change(function() {
        const clientId = $(this).val();
        console.log("Client selected:", clientId);

        if (!clientId) {
            hideAllSteps();
            $('#signInLocation').html('<option value="">Select Sign-In Location</option>');
            return;
        }

        // Step 1b: load sign-in branches for this client
        loadClientBranches(clientId);
    });

    // Sign-in branch selection handler (value = CLIENT.ID)
    $('#signInLocation').change(function() {
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
});

function hideAllSteps() {
    $('#step2Card, #step3Card, #step4Card, #step5Card, #submitSection').hide();
    $('#step2Indicator, #step3Indicator, #step4Indicator, #step5Indicator')
        .removeClass('active');
    selectedLocations = [];
}
//not needed we can make ajax call to get printer models only for client and location combination
function loadAllPrinterModels() {
    $.ajax({
        url: '<%= request.getContextPath() %>/views/replacement/request',
        data: { action: 'getAllPrinterModels' },
        dataType: 'json',
        success: function(response) {
            console.log("Printer models loaded:", response);
            if (response.success) {
                allPrinterModels = response.data;
            }
        },
        error: function() {
            console.error("Error loading printer models");
        }
    });
}

function loadClientBranches(clientId) {
    $.ajax({
        url: '<%= request.getContextPath() %>/views/replacement/request',
        data: { action: 'getClientBranches', clientId: clientId },
        dataType: 'json',
        success: function(response) {
            console.log("✅ Branches loaded:", response);
            if (!response.success) return;

            // Populate sign-in dropdown with BRANCH + ADDRESS
            let options = '<option value="">Select Sign-In Location</option>';
            response.data.forEach(function(b) {
                const addr = (b.address || '').trim();
                const label = addr ? (b.branch + ': ' + addr) : (b.branch || '');
                // IMPORTANT: build <option> using concatenation (avoid JS template literals)
                // so JSP doesn't try to parse  as EL.
                options += '<option value="' + b.id + '">' + escapeHtml(label) + '</option>';
            });
            $('#signInLocation').html(options);
        },
        error: function() {
            showAppAlert("Error loading client branches. Please try again.", 'danger');
        }
    });
}

function loadBranchContact(branchId) {
    $.ajax({
        url: '<%= request.getContextPath() %>/views/replacement/request',
        data: { action: 'getBranchDetails', branchId: branchId },
        dataType: 'json',
        success: function(response) {
            console.log("✅ Branch contact loaded:", response);
            if (response.success) {
                $('#contactName').val(response.data.contactPerson || '');
                $('#contactNumber').val(response.data.mobileNo || '');
                $('#contactEmail').val(response.data.emailId1 || '');

                $('#step2Card').fadeIn();
                $('#step2Indicator').addClass('active');
            }
        },
        error: function() {
            showAppAlert("Error loading branch contact. Please try again.", 'danger');
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
        data: { action: 'getClientLocations', clientId: clientId },
        dataType: 'json',
        success: function(response) {
            console.log("✅ Locations loaded:", response);
            if (response.success && response.data.length > 0) {
                displayLocations(response.data);
                $('#step3Card').fadeIn();
                $('#step3Indicator').addClass('active');
            } else {
                showAppAlert("No locations found for this client!", 'warning');
            }
        },
        error: function() {
            showAppAlert("Error loading locations. Please try again.", 'danger');
        }
    });
}

let allLocationsData = []; // Store all locations for reference

function displayLocations(locations) {
    allLocationsData = locations;
    let html = '';
    locations.forEach(function(loc) {
        const label = (loc.branch || 'Branch ' + loc.id) + ' - ' + (loc.city || 'N/A') + (loc.state ? ', ' + loc.state : '');
        html += '<div class="dropdown-item-text">' +
                    '<div class="form-check">' +
                        '<input type="checkbox" class="form-check-input location-checkbox" ' +
                               'id="loc_' + loc.id + '" value="' + loc.id + '" ' +
                               'data-location=\'' + JSON.stringify(loc).replace(/'/g, "&#39;") + '\' ' +
                               'onchange="updateSelectedLocations()">' +
                        '<label class="form-check-label" for="loc_' + loc.id + '">' +
                            escapeHtml(label) +
                        '</label>' +
                    '</div>' +
                '</div>';
    });

    $('#locationCheckboxList').html(html);
    
    // Prevent dropdown from closing when clicking checkboxes
    $('#locationDropdownMenu').on('click', function(e) {
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
    $('.location-checkbox:checked').each(function() {
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
        loadPrintersForLocations();
    } else {
        $('#selectedLocationsDisplay').hide();
        $('#step4Card, #step5Card, #submitSection').hide();
        $('#step4Indicator, #step5Indicator').removeClass('active');
    }
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
        success: function(response) {
            console.log("✅ Printers loaded:", response);
            if (response.success) {
                displayPrintersGroupedByLocation(response.data);
                $('#step4Card').fadeIn();
                $('#step4Indicator').addClass('active');
            } else {
                showAppAlert("No printers found for selected locations!", 'warning');
            }
        },
        error: function() {
            showAppAlert("Error loading printers. Please try again.", 'danger');
        }
    });
}

let allPrintersData = []; // Store all printers for filtering

function displayPrintersGroupedByLocation(printers) {
    allPrintersData = printers;
    
    // Populate location filter dropdown
    let locationFilterOptions = '<option value="">All Locations</option>';
    selectedLocations.forEach(function(loc) {
        locationFilterOptions += '<option value="' + loc.id + '">' + escapeHtml(loc.branch || 'Branch ' + loc.id) + '</option>';
    });
    $('#locationFilter').html(locationFilterOptions);
    
    // Update printer count badge
    $('#printerCountBadge').text(printers.length + ' printers');
    
    renderPrintersTable(printers);
}

function renderPrintersTable(printers) {
    let html = '';
    
    printers.forEach(function(printer, idx) {
        // Find the location for this printer
        let loc = selectedLocations.find(l => l.id == printer.clientBrId) || {};
        let uniqueKey = printer.clientBrId + '_' + idx;
        let locationName = loc.branch || 'Branch ' + printer.clientBrId;
        
        html += '<tr class="printer-row" data-location="' + printer.clientBrId + '" ' +
                'data-model="' + escapeHtml(printer.modelName || '').toLowerCase() + '" ' +
                'data-serial="' + escapeHtml(printer.serial || '').toLowerCase() + '" ' +
                'data-location-name="' + escapeHtml(locationName).toLowerCase() + '">' +
            '<td class="text-center">' +
                '<input type="checkbox" class="printer-checkbox" ' +
                       'name="printers[' + uniqueKey + '].selected" ' +
                       'id="printer_' + uniqueKey + '" value="1" ' +
                       'onchange="updatePrinterSelection()">' +
            '</td>' +
            '<td><i class="fas fa-building text-muted"></i> ' + escapeHtml(locationName) + '</td>' +
            '<td><span class="badge badge-secondary">' + escapeHtml(printer.modelName || 'N/A') + '</span></td>' +
            '<td><code>' + escapeHtml(printer.serial || 'N/A') + '</code></td>' +
            '<td><small class="text-muted">' + escapeHtml(printer.agrProdId || 'N/A') + '</small></td>' +
            '<td>' +
                '<select class="form-control form-control-sm new-model-dropdown" ' +
                        'name="printers[' + uniqueKey + '].newModelId" ' +
                        'id="newModelDropdown_' + uniqueKey + '" ' +
                        'onchange="handleDropdownSelection(\'' + uniqueKey + '\')">' +
                    '<option value="">-- Select --</option>' +
                    buildPrinterModelOptions() +
                '</select>' +
                '<input type="text" class="form-control form-control-sm mt-1 new-model-text" ' +
                       'name="printers[' + uniqueKey + '].newModelText" ' +
                       'id="newModelText_' + uniqueKey + '" ' +
                       'placeholder="Or type manually" ' +
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
    
    $('#printersTableBody tr.printer-row').each(function() {
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
    allPrinterModels.forEach(function(model) {
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

$('#replacementForm').submit(function(e) {
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

    $('#submitBtn').prop('disabled', true)
                   .html('<i class="fas fa-spinner fa-spin"></i> Submitting...');
    return true;
});
