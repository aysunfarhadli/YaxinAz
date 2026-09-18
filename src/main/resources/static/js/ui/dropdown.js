const dropdown = (() => {
  const openMenus = new Set();

  document.addEventListener('click', (e) => {
    openMenus.forEach((menu) => {
      if (!menu.contains(e.target) && !menu.trigger.contains(e.target)) {
        menu.remove();
        openMenus.delete(menu);
      }
    });
  });

  /** attach(triggerEl, () => htmlString) - toggles a menu positioned relative to a wrapper. */
  function attach(triggerEl, renderHtml) {
    const wrapper = triggerEl.closest('[data-dropdown-wrapper]') || triggerEl.parentElement;
    if (getComputedStyle(wrapper).position === 'static') wrapper.style.position = 'relative';

    triggerEl.addEventListener('click', (e) => {
      e.stopPropagation();
      const existing = wrapper.querySelector('.dropdown-menu');
      if (existing) {
        existing.remove();
        openMenus.delete(existing);
        return;
      }
      const menu = document.createElement('div');
      menu.className = 'dropdown-menu';
      menu.innerHTML = renderHtml();
      menu.trigger = triggerEl;
      wrapper.appendChild(menu);
      openMenus.add(menu);
    });
  }

  return { attach };
})();
