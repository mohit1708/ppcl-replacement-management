
// Variables contextPath, requestId, clientEmail, clientMobile are set in the JSP

// Enable/disable location select based on print option
    document.querySelectorAll('input[name="printOption"]').forEach(radio => {
        radio.addEventListener('change', function() {
            document.getElementById('locationSelect').disabled = this.value !== 'selected';
        });
    });

    // Initialize location select enabled/disabled state based on currently checked radio
    const initialChecked = document.querySelector('input[name="printOption"]:checked');
    if(initialChecked) {
        document.getElementById('locationSelect').disabled = initialChecked.value !== 'selected';
    }

    // Allow click-to-toggle behavior for options
    const locationSelectElement = document.getElementById('locationSelect');
    if(locationSelectElement) {
        locationSelectElement.addEventListener('mousedown', function(e) {
            const target = e.target;
            if(target && target.tagName === 'OPTION') {
                e.preventDefault();
                target.selected = !target.selected;
                const changeEvent = new Event('change', {bubbles: true});
                locationSelectElement.dispatchEvent(changeEvent);
            }
        });
    }

    function refreshPreview() {
        location.reload();
    }

    function applyDigitalSignature() {
        if(confirm('Apply digital signature certificate (DSC)?')) {
            $.post(contextPath + '/am/replacementLetter', {
                action: 'applySignature',
                requestId: requestId
            }, function(response) {
                if(response.success) {
                    var data = response.data || {};
                    var signedBy = data.signedBy || 'System';
                    var signedAt = data.signedAt || new Date().toLocaleString();
                    var filePath = data.filePath || '';
                    var mergedLinks = data.mergedLinks || [];
                    var linksHtml = '';
                    if (mergedLinks.length) {
                        linksHtml = '<div class="mt-2 no-print">' + mergedLinks.map(function(link) {
                            return '<a class="d-inline-block mr-2" href="' + contextPath + '/' + link.filePath + '" target="_blank" rel="noopener">Signed copy ' + link.label + '</a>';
                        }).join('') + '</div>';
                    } else if (filePath) {
                        linksHtml = '<div class="mt-2 no-print"><a href="' + contextPath + '/' + filePath + '" target="_blank" rel="noopener">View signed PDF</a></div>';
                    }
                    document.getElementById('ppcSignature').innerHTML =
                        '<div class="text-center">' +
                            '<div class="text-primary-d2 font-bolder text-120">Digitally signed by PPCL</div>' +
                            '<small class="text-secondary-d1 font-600">Date : ' + signedAt + '</small>' +
                            linksHtml +
                        '</div>';
                    alert(response.message);
                } else {
                    alert(response.message || 'Failed to apply signature');
                }
            }, 'json');
        }
    }

    function sendViaEmail() {
        const email = prompt('Enter recipient email:', clientEmail || 'admin@client.com');
        if(email) {
            $.post(contextPath + '/am/replacementLetter', {
                action: 'sendEmail',
                requestId: requestId,
                email: email
            }, function(response) {
                alert(response.message);
            }, 'json');
        }
    }

    function printLetter() {
        const preview = document.getElementById('letterPreview');
        if(!preview) { window.print(); return; }

        const printContent = preview.cloneNode(true);
        printContent.querySelectorAll('.no-print').forEach(function(el) {
            el.parentNode.removeChild(el);
        });

        const printWindow = window.open('', '_blank', 'width=900,height=700');
        if(!printWindow) { alert('Please allow popups for this site.'); return; }

        var html = '<!doctype html><html><head><meta charset="utf-8"><title>Print - Replacement Letter</title>' +
            '<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">' +
            '<style>' +
                '@page { margin: 10mm; }' +
                'body{padding:20px;background:#fff;color:#222}' +
                '.signature-box{height:90px;display:flex;align-items:center;justify-content:center}' +
                '.no-print{display:none !important;}' +
                '@media print { .no-print, .no-print * { display:none !important; } }' +
                '* { -webkit-print-color-adjust: exact; print-color-adjust: exact; }' +
            '</style>' +
        '</head><body>' + printContent.outerHTML + '</body></html>';

        printWindow.document.write(html);
        printWindow.document.close();
        printWindow.focus();
        setTimeout(function() {
            printWindow.print();
        }, 500);
    }

    function sendViaWhatsApp() {
        const phone = prompt('Enter WhatsApp number:', clientMobile || '+91 98765 43210');
        if(phone) {
            $.post(contextPath + '/am/replacementLetter', {
                action: 'sendWhatsApp',
                requestId: requestId,
                phone: phone
            }, function(response) {
                alert(response.message);
            }, 'json');
        }
    }

    function sendDispatchNotification() {
        if(confirm('Send dispatch notification to client?')) {
            $.post(contextPath + '/am/replacementLetter', {
                action: 'sendDispatch',
                requestId: requestId
            }, function(response) {
                alert(response.message);
            }, 'json');
        }
    }
