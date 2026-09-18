/**
 * Renders the shared authenticated-app shell (sidebar + topbar, spec section 94) into
 * <div id="app-shell"></div> and returns a reference to the #page-content element every page
 * should render its own content into. Call appShell.render() after auth.requireAuth().
 */
const appShell = (() => {
  const NAV_ITEMS = [
    { key: 'dashboard', href: '/dashboard.html', label: 'dashboard', icon: '🏠' },
    { key: 'issues', href: '/issues.html', label: 'issues', icon: '🛠️' },
    { key: 'feed', href: '/feed.html', label: 'feed', icon: '📰' },
    { key: 'polls', href: '/polls.html', label: 'polls', icon: '📊' },
    { key: 'events', href: '/events.html', label: 'events', icon: '📅' },
    { key: 'lostfound', href: '/lostfound.html', label: 'lostFound', icon: '🔎' },
    { key: 'services', href: '/services.html', label: 'services', icon: '🧰' },
    { key: 'notifications', href: '/notifications.html', label: 'notifications', icon: '🔔' },
    { key: 'profile', href: '/profile.html', label: 'profile', icon: '👤' },
  ];
  const ADMIN_ITEMS = [
    { key: 'admin-dashboard', href: '/admin/dashboard.html', label: 'admin', icon: '📈' },
    { key: 'admin-operations', href: '/admin/operations.html', label: 'operationsCenter', icon: '🛰️' },
  ];
  const BOTTOM_NAV_ITEMS = [
    { key: 'dashboard', href: '/dashboard.html', label: 'navHome', icon: '🏠' },
    { key: 'issues', href: '/issues.html', label: 'issues', icon: '🛠️' },
    { key: 'report', href: '/issues.html?report=1', label: 'navReport', icon: '➕' },
    { key: 'feed', href: '/feed.html', label: 'navCommunity', icon: '📰' },
    { key: 'profile', href: '/profile.html', label: 'profile', icon: '👤' },
  ];

  function initials(user) {
    return `${(user.firstName || '?')[0]}${(user.lastName || '?')[0]}`.toUpperCase();
  }

  function navLinkHtml(item, activeKey) {
    return `<a href="${item.href}" class="${item.key === activeKey ? 'active' : ''}">
      <span class="nav-icon" aria-hidden="true">${item.icon}</span>
      <span data-i18n="${item.label}">${i18n.t(item.label)}</span>
    </a>`;
  }

  /**
   * Fetches the user's communities exactly once per page load and resolves the "current" one
   * (previously selected, or the first one). Cached as a shared promise so both the topbar
   * selector UI and any page's data loading can await the SAME resolution instead of racing it -
   * a page that read currentCommunityId() synchronously right after appShell.render() would
   * sometimes see nothing, because the topbar's own fetch hadn't finished yet. Caught by an actual
   * headless-browser test, not by reading the code.
   */
  let communitiesPromise = null;

  function resolveCommunities() {
    if (!communitiesPromise) {
      communitiesPromise = api.get('/communities/mine').catch(() => []);
    }
    return communitiesPromise;
  }

  /** Awaitable - every page that needs "the current community" should use this, not currentCommunityId(). */
  async function ensureCommunityId() {
    const communities = await resolveCommunities();
    if (!communities.length) return null;
    const stored = localStorage.getItem(window.YAXINAZ_CONFIG.communityStorageKey);
    const current = communities.find((c) => String(c.id) === stored) ? stored : String(communities[0].id);
    localStorage.setItem(window.YAXINAZ_CONFIG.communityStorageKey, current);
    return current;
  }

  async function loadCommunitySelector(user) {
    const select = document.getElementById('community-select');
    if (!select) return;
    const communities = await resolveCommunities();
    if (!communities.length) {
      select.innerHTML = `<option value="">${i18n.t('noCommunitiesYet')}</option>`;
      return;
    }
    const current = await ensureCommunityId();
    select.innerHTML = communities.map((c) => `<option value="${c.id}" ${String(c.id) === current ? 'selected' : ''}>${c.name}</option>`).join('');
    select.addEventListener('change', () => {
      localStorage.setItem(window.YAXINAZ_CONFIG.communityStorageKey, select.value);
      location.reload();
    });
  }

  /**
   * Synchronous, best-effort read of the last-resolved community id (e.g. from localStorage set
   * on a previous page). Fine for non-critical UI, but any page whose FIRST load must have the
   * community id (to fetch issues, posts, etc.) should await ensureCommunityId() instead.
   */
  function currentCommunityId() {
    return localStorage.getItem(window.YAXINAZ_CONFIG.communityStorageKey);
  }

  function render({ activeNav = '', title = '' } = {}) {
    const user = auth.currentUser();
    const root = document.getElementById('app-shell');
    if (!root || !user) return null;

    const isAdmin = auth.hasAnyRole('PLATFORM_ADMIN', 'COMMUNITY_ADMIN');
    const navHtml = NAV_ITEMS.map((item) => navLinkHtml(item, activeNav)).join('');
    const adminNavHtml = isAdmin
      ? `<div class="sidebar-section-label">${i18n.t('adminSectionLabel')}</div>${ADMIN_ITEMS.map((item) => navLinkHtml(item, activeNav)).join('')}`
      : '';

    root.innerHTML = `
      <div class="sidebar-backdrop" id="sidebar-backdrop"></div>
      <div class="app-shell">
        <aside class="sidebar" id="sidebar">
          <div class="sidebar-brand">
            <span class="mark">Y</span>
            <span>Yaxın<span style="color:var(--color-primary)">.az</span></span>
          </div>
          <nav class="sidebar-nav">
            ${navHtml}
            ${adminNavHtml}
          </nav>
          <div class="sidebar-footer">
            <button class="btn btn-ghost btn-block" id="logout-btn">↩ <span data-i18n="logout">${i18n.t('logout')}</span></button>
          </div>
        </aside>
        <div class="main-content">
          <header class="topbar">
            <button class="icon-btn mobile-menu-btn" id="mobile-menu-btn" aria-label="Open menu">☰</button>
            <select class="input community-select" id="community-select" aria-label="Select community"><option>${i18n.t('loadingCommunities')}</option></select>
            <div class="topbar-search">
              <input class="input" type="search" placeholder="${i18n.t('searchPlaceholder')}" id="global-search" aria-label="Search issues, posts, providers" />
            </div>
            <div class="topbar-actions">
              <div class="row" data-dropdown-wrapper style="position:relative">
                <button class="icon-btn" id="notif-bell" aria-label="Notifications">
                  🔔
                  <span class="notif-dot" id="notif-badge" hidden></span>
                </button>
              </div>
              <div class="row" data-dropdown-wrapper style="position:relative">
                <button class="user-menu-trigger" id="user-menu-btn">
                  <span class="avatar">${initials(user)}</span>
                </button>
              </div>
            </div>
          </header>
          <main class="page" id="page-content"></main>
        </div>
      </div>
      <nav class="bottom-nav">
        <div class="bottom-nav-items">
          ${BOTTOM_NAV_ITEMS.map((item) => `<a href="${item.href}" class="${item.key === activeNav ? 'active' : ''}"><span aria-hidden="true">${item.icon}</span><span>${i18n.t(item.label)}</span></a>`).join('')}
        </div>
      </nav>
    `;

    document.getElementById('logout-btn').addEventListener('click', () => auth.logout());

    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.getElementById('sidebar-backdrop');
    mobileMenuBtn.addEventListener('click', () => {
      sidebar.classList.add('open');
      backdrop.classList.add('open');
    });
    backdrop.addEventListener('click', () => {
      sidebar.classList.remove('open');
      backdrop.classList.remove('open');
    });

    dropdown.attach(document.getElementById('user-menu-btn'), () => `
      <a href="/profile.html">👤 ${i18n.t('dropdownProfile')}</a>
      <a href="/notifications.html">🔔 ${i18n.t('dropdownNotifications')}</a>
      <div class="divider" style="margin:4px 0"></div>
      <button id="dropdown-logout">↩ ${i18n.t('dropdownLogout')}</button>
    `);
    document.addEventListener('click', (e) => {
      if (e.target.id === 'dropdown-logout') auth.logout();
    });

    document.getElementById('global-search').addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && e.target.value.trim()) {
        location.href = `/search.html?q=${encodeURIComponent(e.target.value.trim())}`;
      }
    });

    loadCommunitySelector(user);
    notifications.init();

    return document.getElementById('page-content');
  }

  return { render, currentCommunityId, ensureCommunityId };
})();
