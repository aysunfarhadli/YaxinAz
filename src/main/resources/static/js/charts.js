/**
 * Thin Chart.js wrappers using the design system's palette (spec section 112). Requires Chart.js to
 * already be loaded on the page (admin dashboard pages include it via CDN script tag).
 */
const charts = (() => {
  const palette = ['#2563eb', '#0f9b8e', '#7c5cff', '#e07a1f', '#d9412f', '#b8860b', '#8a91a3'];

  const priorityColors = {
    LOW: '#1a9c5a', MEDIUM: '#b8860b', HIGH: '#e07a1f', CRITICAL: '#d9412f',
  };

  const baseOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { font: { family: 'Plus Jakarta Sans, Inter, sans-serif' } } } },
  };

  function donut(canvas, labels, data) {
    return new Chart(canvas, {
      type: 'doughnut',
      data: { labels, datasets: [{ data, backgroundColor: palette, borderWidth: 0 }] },
      options: { ...baseOptions, cutout: '65%' },
    });
  }

  function priorityBar(canvas, dataByPriority) {
    const order = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const labels = (typeof i18n !== 'undefined' && i18n.enumLabel) ? order.map((p) => i18n.enumLabel('IssuePriority', p)) : order;
    return new Chart(canvas, {
      type: 'bar',
      data: {
        labels,
        datasets: [{ data: order.map((p) => dataByPriority[p] || 0), backgroundColor: order.map((p) => priorityColors[p]) }],
      },
      options: { ...baseOptions, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } },
    });
  }

  function line(canvas, labels, data, color = '#2563eb') {
    return new Chart(canvas, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          data, borderColor: color, backgroundColor: color + '22',
          tension: 0.35, fill: true, pointRadius: 3,
        }],
      },
      options: { ...baseOptions, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } },
    });
  }

  return { donut, priorityBar, line, palette, priorityColors };
})();
