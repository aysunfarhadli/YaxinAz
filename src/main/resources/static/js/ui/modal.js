const modal = (() => {
  let activeBackdrop = null;

  function close() {
    if (activeBackdrop) {
      activeBackdrop.remove();
      activeBackdrop = null;
      document.removeEventListener('keydown', onKeydown);
    }
  }

  function onKeydown(e) {
    if (e.key === 'Escape') close();
  }

  /** open({title, bodyHtml, actions: [{label, className, onClick}]}) */
  function open({ title, bodyHtml, actions = [] }) {
    close();
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';
    backdrop.addEventListener('click', (e) => { if (e.target === backdrop) close(); });

    const box = document.createElement('div');
    box.className = 'modal';
    box.setAttribute('role', 'dialog');
    box.setAttribute('aria-modal', 'true');
    box.innerHTML = `
      <div class="modal-header">
        <h3>${title}</h3>
        <button class="icon-btn modal-close" aria-label="Close">&times;</button>
      </div>
      <div class="modal-body">${bodyHtml}</div>
      <div class="modal-actions"></div>
    `;
    box.querySelector('.modal-close').addEventListener('click', close);

    const actionsEl = box.querySelector('.modal-actions');
    actions.forEach((action) => {
      const btn = document.createElement('button');
      btn.className = action.className || 'btn btn-outline';
      btn.textContent = action.label;
      btn.addEventListener('click', () => action.onClick && action.onClick(close));
      actionsEl.appendChild(btn);
    });

    backdrop.appendChild(box);
    document.body.appendChild(backdrop);
    document.addEventListener('keydown', onKeydown);
    activeBackdrop = backdrop;
    return { close };
  }

  /** confirm({title, message, confirmLabel, danger}) -> Promise<boolean> */
  function confirmAction({ title = 'Are you sure?', message, confirmLabel = 'Confirm', danger = false }) {
    return new Promise((resolve) => {
      open({
        title,
        bodyHtml: `<p>${message}</p>`,
        actions: [
          { label: 'Cancel', className: 'btn btn-ghost', onClick: (done) => { done(); resolve(false); } },
          {
            label: confirmLabel,
            className: danger ? 'btn btn-danger' : 'btn btn-primary',
            onClick: (done) => { done(); resolve(true); },
          },
        ],
      });
    });
  }

  return { open, close, confirm: confirmAction };
})();
