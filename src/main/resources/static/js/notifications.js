const notifications = (() => {
  let unread = 0;

  function updateBadge() {
    const badge = document.getElementById('notif-badge');
    if (badge) badge.hidden = unread <= 0;
  }

  async function refreshUnreadCount() {
    try {
      const res = await api.get('/notifications/unread-count');
      unread = res.unreadCount;
      updateBadge();
    } catch (e) { /* non-fatal */ }
  }

  function timeAgo(iso) {
    const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
    if (seconds < 60) return 'just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    return `${Math.floor(hours / 24)}d ago`;
  }

  function renderPanel(items) {
    if (!items.length) {
      return '<div class="empty-state" style="padding:24px"><div class="empty-icon">🔔</div><p>No notifications yet.</p></div>';
    }
    return items.map((n) => `
      <button class="notif-item" data-id="${n.id}" style="display:flex;flex-direction:column;align-items:flex-start;gap:2px;padding:10px 12px;border-radius:8px;width:100%;text-align:left;${n.read ? '' : 'background:var(--color-primary-soft)'}">
        <span style="font-weight:700;font-size:13px">${n.title}</span>
        <span style="font-size:12px;color:var(--color-text-secondary)">${n.message}</span>
        <span style="font-size:11px;color:var(--color-text-muted)">${timeAgo(n.createdAt)}</span>
      </button>`).join('');
  }

  async function openPanel(anchorWrapper) {
    const menu = document.createElement('div');
    menu.className = 'dropdown-menu';
    menu.style.width = '340px';
    menu.innerHTML = `
      <div class="row-between" style="padding:6px 8px 10px">
        <strong style="font-size:13px">Notifications</strong>
        <button class="link text-sm" id="notif-mark-all">Mark all read</button>
      </div>
      <div id="notif-list" style="max-height:360px;overflow-y:auto;display:flex;flex-direction:column;gap:2px">
        ${loading.skeletonRows(3, 2)}
      </div>`;
    anchorWrapper.appendChild(menu);

    const close = (e) => {
      if (!menu.contains(e.target) && e.target.id !== 'notif-bell') {
        menu.remove();
        document.removeEventListener('click', close);
      }
    };
    setTimeout(() => document.addEventListener('click', close), 0);

    menu.querySelector('#notif-mark-all').addEventListener('click', async () => {
      await api.patch('/notifications/read-all');
      unread = 0;
      updateBadge();
      menu.querySelectorAll('.notif-item').forEach((el) => (el.style.background = ''));
    });

    try {
      const page = await api.get('/notifications', { query: { size: 10 } });
      menu.querySelector('#notif-list').innerHTML = renderPanel(page.content);
      menu.querySelectorAll('.notif-item').forEach((el) => {
        el.addEventListener('click', async () => {
          await api.patch(`/notifications/${el.dataset.id}/read`);
          el.style.background = '';
          await refreshUnreadCount();
        });
      });
    } catch (e) {
      menu.querySelector('#notif-list').innerHTML = '<p class="text-sm text-muted" style="padding:12px">Could not load notifications.</p>';
    }
  }

  function init() {
    const bell = document.getElementById('notif-bell');
    if (!bell) return;
    refreshUnreadCount();

    bell.addEventListener('click', (e) => {
      e.stopPropagation();
      const wrapper = bell.closest('[data-dropdown-wrapper]');
      const existing = wrapper.querySelector('.dropdown-menu');
      if (existing) {
        existing.remove();
        return;
      }
      openPanel(wrapper);
    });

    realtime.connect((notification) => {
      unread += 1;
      updateBadge();
      toast.info(notification.title);
    });
  }

  return { init, refreshUnreadCount };
})();
