 const selectMenus = document.querySelectorAll(".select-menu");
        selectMenus.forEach((menu) => {
            const select = menu.querySelector(".select");
            const optionList = menu.querySelector(".option-list");
            const options = menu.querySelectorAll(".option");

            select.addEventListener("click", () => {
                optionList.classList.toggle("active");
                select.querySelector(".fa-angle-down").classList.toggle("fa-angle-up");
            });

            options.forEach((option) => {
                option.addEventListener("click", () => {
                    options.forEach((opt) => opt.classList.remove("selected"));
                    select.querySelector("span").innerHTML = option.innerHTML;
                    option.classList.add("selected");
                    optionList.classList.remove("active");
                    select.querySelector(".fa-angle-down").classList.remove("fa-angle-up");
                    filterMarkers(); // Gọi hàm lọc khi chọn option
                });
            });
        });

// Menu mobile
const menuIcon = document.getElementById("menu-icon");
const navBar = document.querySelector('.header__nav');
menuIcon.addEventListener('click',()=>{
   menuIcon.classList.toggle('fa-xmark');
   navBar.classList.toggle('active');
});

// Đóng menu khi click vào một link trên menu (mobile)
document.querySelectorAll('.navbar__link').forEach(link => {
  link.addEventListener('click', () => {
    navBar.classList.remove('active');
    menuIcon.classList.remove('fa-xmark');
  });
});