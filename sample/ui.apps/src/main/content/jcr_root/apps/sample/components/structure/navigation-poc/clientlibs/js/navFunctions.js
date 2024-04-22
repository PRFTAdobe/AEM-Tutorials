function toggleMobileNav() {
    var menu = document.getElementById("site-menu");
    
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