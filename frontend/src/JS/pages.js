$(function() {
    $('header nav a').click(function() {
        var $linkClicked = $(this).attr('href');
        document.location.hash = $linkClicked;
        if (!$(this).hasClass("active")) {
            $("header nav a").removeClass("active");
            $(this).addClass("active");
            $('#main-content section').hide();
            $($linkClicked).fadeIn();
            return false;
        }
        else {
            return false;
        }
    });
    var hash = window.location.hash;
    hash = hash.replace(/^#/, '');
    switch (hash) {
        case 'agents' :
            $("#" + hash + "-link").trigger("click");
            break;
        case 'logs' :
            $("#" + hash + "-link").trigger("click");
            break;
        case 'stats' :
            $("#" + hash + "-link").trigger("click");
            break;
    }
});
