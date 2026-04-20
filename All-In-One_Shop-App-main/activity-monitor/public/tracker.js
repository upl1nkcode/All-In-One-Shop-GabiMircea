/**
 * AllInOne Shop — Activity Tracker
 *
 * Drop this script into any page to start tracking user activity.
 * It communicates with the activity-monitor server (default port 4000).
 *
 * Quick embed:
 *   <script src="http://localhost:4000/tracker.js"></script>
 *
 * Manual event:
 *   window.ActivityTracker.track('ADD_TO_WISHLIST', { productId: '123' });
 */
(function () {
  'use strict';

  const SERVER = (window.__ACTIVITY_MONITOR_URL__ || 'http://localhost:4000');
  const SESSION_COOKIE = 'session_id';

  // ── Cookie helpers ────────────────────────────────────────
  function getCookie(name) {
    const m = document.cookie.match(
      new RegExp('(?:^|; )' + name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '=([^;]*)')
    );
    return m ? decodeURIComponent(m[1]) : null;
  }

  function setCookie(name, value, days) {
    const exp = new Date(Date.now() + days * 864e5).toUTCString();
    document.cookie = name + '=' + encodeURIComponent(value)
      + '; expires=' + exp + '; path=/; SameSite=Lax';
  }

  // ── Session ───────────────────────────────────────────────
  function getOrCreateSession() {
    let sid = getCookie(SESSION_COOKIE);
    if (!sid) {
      sid = 'cs-' + Date.now() + '-' + Math.random().toString(36).slice(2, 9);
      setCookie(SESSION_COOKIE, sid, 1);
    }
    return sid;
  }

  const sessionId = getOrCreateSession();

  // ── Send event to server ──────────────────────────────────
  function send(type, payload) {
    fetch(SERVER + '/api/events', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ type, payload: payload || {} }),
    }).catch(function () { /* never break the app */ });
  }

  // ── Trackers ──────────────────────────────────────────────
  function trackPageView() {
    send('PAGE_VIEW', {
      path: window.location.pathname,
      search: window.location.search,
      referrer: document.referrer,
      title: document.title,
    });
  }

  function trackSearchInput() {
    document.querySelectorAll('input[type="search"], input[placeholder*="earch" i]')
      .forEach(function (input) {
        var debounce;
        input.addEventListener('input', function () {
          clearTimeout(debounce);
          debounce = setTimeout(function () {
            if (input.value.trim().length >= 2) {
              send('SEARCH', { query: input.value.trim() });
            }
          }, 800);
        });
      });
  }

  function trackProductClicks() {
    document.addEventListener('click', function (e) {
      var card = e.target.closest('[data-product-id], .product-card, article');
      if (!card) return;
      var name = card.querySelector('h2, h3, .product-name, [data-product-name]');
      var link = card.querySelector('a');
      send('PRODUCT_CLICK', {
        productId: card.dataset.productId || null,
        productName: name ? name.textContent.trim() : null,
        href: link ? link.href : null,
      });
    });
  }

  function trackPreferences() {
    document.addEventListener('change', function (e) {
      var el = e.target;
      if (el.tagName !== 'SELECT' && el.type !== 'radio' && el.type !== 'checkbox') return;
      var label = el.name || el.id || el.getAttribute('aria-label') || null;
      if (label) {
        send('PREFERENCE_UPDATE', { key: label, value: el.value || el.checked });
      }
    });
  }

  // ── Session duration on unload ────────────────────────────
  var startTime = Date.now();
  window.addEventListener('beforeunload', function () {
    var duration = Math.round((Date.now() - startTime) / 1000);
    if (navigator.sendBeacon) {
      navigator.sendBeacon(
        SERVER + '/api/events',
        JSON.stringify({ type: 'SESSION_DURATION', payload: { seconds: duration, path: window.location.pathname } })
      );
    }
  });

  // ── Public API ────────────────────────────────────────────
  window.ActivityTracker = {
    sessionId: sessionId,
    send: send,
    track: function (type, data) { send(type, data); },
  };

  // ── Init ──────────────────────────────────────────────────
  function init() {
    trackPageView();
    trackSearchInput();
    trackProductClicks();
    trackPreferences();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
