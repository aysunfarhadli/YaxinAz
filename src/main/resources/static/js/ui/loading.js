const loading = (() => {
  function setButtonLoading(button, isLoading) {
    if (!button) return;
    if (isLoading) {
      button.classList.add('is-loading');
      button.disabled = true;
      if (!button.querySelector('.spinner')) {
        const spinner = document.createElement('span');
        spinner.className = 'spinner';
        button.prepend(spinner);
      }
    } else {
      button.classList.remove('is-loading');
      button.disabled = false;
    }
  }

  function skeletonRows(count = 3, linesPer = 2) {
    let html = '';
    for (let i = 0; i < count; i++) {
      html += '<div class="card card-compact" style="margin-bottom:12px">';
      for (let j = 0; j < linesPer; j++) {
        html += `<div class="skeleton skeleton-line ${j === linesPer - 1 ? 'w-40' : 'w-60'}"></div>`;
      }
      html += '</div>';
    }
    return html;
  }

  function emptyState(container, { icon = '📭', title, message, actionHtml = '' }) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon" aria-hidden="true">${icon}</div>
        <h4>${title}</h4>
        <p>${message}</p>
        ${actionHtml}
      </div>`;
  }

  return { setButtonLoading, skeletonRows, emptyState };
})();
