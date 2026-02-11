</div><!-- /.main-content -->
</div><!-- /.main-container -->
</div><!-- /.body-container -->



<!-- include ace.js -->
<script src="<%= request.getContextPath() %>/ace-v3.1.1/dist/js/ace.js"></script>

<script src="<%= request.getContextPath() %>/js/replacement/app.js"></script>
<script>
    // Hide page loader when DOM is ready
    $(document).ready(function() {
        $('#pageLoader').addClass('hidden');
    });
    
    // Also hide on window load (for images/resources)
    $(window).on('load', function() {
        $('#pageLoader').addClass('hidden');
    });
</script>
</body>
</html>