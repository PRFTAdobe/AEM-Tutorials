(function($, ns) {
    "use strict";

    // Bind the click event handler to the submit button within the nav poc component dialog
    $(document).on("click", ".cq-dialog-submit", function (e) {
        // Fetch the component path from the current dialog
        var componentPath = $(this).closest(".cq-dialog").attr("action");

        // Make AJAX request to fetch the logo and startPath JCR property values
        $.ajax({
            type: 'GET',
            url: componentPath + ".json",
            success: function(data) {
                // Extract the logo and startPath property values from the JSON response
                var brandLogo = data["brandLogo"];
                var startPath = data["startPath"];
                // Make the main AJAX request
                $.ajax({
                    type: 'POST',
                    url: '/bin/navigationJsonUpdateServlet',
                    data: {
                        // Add the logo and startPath properties to the data object.
                        brandLogo: brandLogo,
                        startPath: startPath
                    },
                    success: function(msg) {
                        console.log('Successfully POSTED navigation content to the JSON update servlet.');
                    }
                });
            }
        });
    });
})(Granite.$, Granite.author);
