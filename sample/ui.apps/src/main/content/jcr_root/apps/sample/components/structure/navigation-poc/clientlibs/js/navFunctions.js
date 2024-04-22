var menu = document.getElementById("site-menu");

function toggleMobileNav() {    
    if (menu.style.display === "none") {
      menu.style.display = "block";
    } else {
      menu.style.display = "none";
    }
}

window.addEventListener('load', function() {
    var menuButton = document.getElementById("mobile-nav");
    menuButton.addEventListener('click', toggleMobileNav); 
});

window.addEventListener('resize', function() {
    var vw = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0)
    if (vw < 1000) {
      menu.style.display = "none";
    } else if (vw > 1000) {
      menu.style.display = "block";
    }
});