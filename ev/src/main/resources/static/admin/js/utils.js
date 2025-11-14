function createModal(title, content, buttons = []) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
      <div class="modal">
        <div class="modal-header">
          <h2>${title}</h2>
          <button class="btn-icon" onclick="closeModal()"><i class="fa-solid fa-times"></i></button>
        </div>
        <div class="modal-body">${content}</div>
        <div class="modal-footer">
          ${buttons.map(b => `<button class="${b.class}" onclick="${b.onclick}">${b.text}</button>`).join('')}
        </div>
      </div>
    `;
    return modal;
  }
  
  function closeModal() {
    const modal = document.querySelector('.modal-overlay');
    if (modal) modal.remove();
  }
  
  function showNotification(message, type = 'info') {
    const noti = document.createElement('div');
    noti.className = `notification ${type}`;
    noti.innerHTML = `<i class="fa-solid fa-${type === 'success' ? 'check' : 'info'}-circle"></i><span>${message}</span>`;
    document.body.appendChild(noti);
    setTimeout(() => noti.classList.add('show'), 100);
    setTimeout(() => {
      noti.classList.remove('show');
      setTimeout(() => noti.remove(), 300);
    }, 3000);
  }
  
  export { createModal, closeModal, showNotification };