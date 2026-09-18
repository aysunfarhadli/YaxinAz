/**
 * Real-time notification push (spec section 56). Requires sockjs-client + stomp.js to be loaded on
 * the page first (see the <script> tags in dashboard.html etc.) - if they're missing, this
 * degrades silently to nothing (the REST notification endpoints remain the source of truth; the
 * socket is just a live nudge, matching WebSocketConfig's doc comment on the backend).
 */
const realtime = (() => {
  let client = null;

  function connect(onNotification) {
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
      console.warn('SockJS/Stomp not loaded - skipping realtime connection');
      return;
    }
    const user = auth.currentUser();
    if (!user) return;

    try {
      const socket = new SockJS(window.YAXINAZ_CONFIG.wsUrl);
      client = Stomp.over(socket);
      client.debug = null;
      client.connect({}, () => {
        client.subscribe(`/topic/notifications/${user.id}`, (message) => {
          try {
            onNotification(JSON.parse(message.body));
          } catch (e) {
            console.warn('Failed to parse notification payload', e);
          }
        });
      }, (error) => {
        console.warn('WebSocket connection failed, will rely on polling/manual refresh', error);
      });
    } catch (e) {
      console.warn('Realtime connection unavailable', e);
    }
  }

  function disconnect() {
    if (client) {
      try { client.disconnect(); } catch (e) { /* ignore */ }
      client = null;
    }
  }

  return { connect, disconnect };
})();
