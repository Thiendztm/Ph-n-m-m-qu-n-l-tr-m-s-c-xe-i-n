// DOM Elements
const userInfoSection = document.getElementById('userInfoSection');
const editBtn = document.getElementById('editBtn');
const infoView = document.getElementById('infoView');
const infoEdit = document.getElementById('infoEdit');
const cancelEdit = document.getElementById('cancelEdit');
const editProfileForm = document.getElementById('editProfileForm');
const avatarImg = document.getElementById('avatarImg');
const previewImg = document.getElementById('previewImg');
const editAvatar = document.getElementById('editAvatar');
const displayName = document.getElementById('displayName');
const displayEmail = document.getElementById('displayEmail');
const displayPhone = document.getElementById('displayPhone');
const displayVehicle = document.getElementById('displayVehicle');
const editName = document.getElementById('editName');
const editEmail = document.getElementById('editEmail');
const editPhone = document.getElementById('editPhone');
const editVehicle = document.getElementById('editVehicle');

// Toggle edit mode
editBtn.addEventListener('click', () => {
    infoView.style.display = 'none';
    infoEdit.style.display = 'block';
    editName.value = displayName.textContent;
    editEmail.value = displayEmail.textContent;
    editPhone.value = displayPhone.textContent;
    editVehicle.value = displayVehicle.textContent;
});

cancelEdit.addEventListener('click', () => {
    infoEdit.style.display = 'none';
    infoView.style.display = 'block';
});

// Preview and upload image from device
editAvatar.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            previewImg.src = e.target.result;
            avatarImg.src = e.target.result; // Cập nhật ảnh ngay khi chọn
        };
        reader.readAsDataURL(file); // Truy cập bộ nhớ để tải ảnh
    }
});

// Save changes
editProfileForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    displayName.textContent = editName.value;
    displayEmail.textContent = editEmail.value;
    displayPhone.textContent = editPhone.value;
    displayVehicle.textContent = editVehicle.value;
    
    infoEdit.style.display = 'none';
    infoView.style.display = 'block';
    alert('Hồ sơ đã được cập nhật thành công!');
    // TODO: Gọi API để lưu dữ liệu
});