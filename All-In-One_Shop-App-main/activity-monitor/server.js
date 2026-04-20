'use strict';

const express = require('express');
const cookieParser = require('cookie-parser');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 4000;
const DATA_FILE = path.join(__dirname, 'data', 'events.json');

// ── Ensure data directory exists ──────────────────────────────
fs.mkdirSync(path.join(__dirname, 'data'), { recursive: true });
if (!fs.existsSync(DATA_FILE)) {
  fs.writeFileSync(DATA_FILE, JSON.stringify([], null, 2));
}

// ── Middleware ────────────────────────────────────────────────
app.use(cookieParser());
app.use(cors({ origin: true, credentials: true }));
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// ── Helpers ───────────────────────────────────────────────────
function loadEvents() {
  try {
    return JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
  } catch {
    return [];
  }
}

function saveEvent(event) {
  const events = loadEvents();
  events.push(event);
  // Keep last 10 000 events to avoid unbounded growth
  const trimmed = events.slice(-10000);
  fs.writeFileSync(DATA_FILE, JSON.stringify(trimmed, null, 2));
}

// ── Session cookie middleware ─────────────────────────────────
app.use('/api', (req, res, next) => {
  if (!req.cookies.session_id) {
    const sessionId = uuidv4();
    res.cookie('session_id', sessionId, {
      maxAge: 30 * 60 * 1000,
      httpOnly: false,
      sameSite: 'lax',
    });
    req.sessionId = sessionId;
  } else {
    res.cookie('session_id', req.cookies.session_id, {
      maxAge: 30 * 60 * 1000,
      httpOnly: false,
      sameSite: 'lax',
    });
    req.sessionId = req.cookies.session_id;
  }
  next();
});

// ── POST /api/events ──────────────────────────────────────────
app.post('/api/events', (req, res) => {
  const { type, payload } = req.body;
  if (!type) return res.status(400).json({ error: 'Missing event type' });

  const event = {
    id: uuidv4(),
    sessionId: req.sessionId || req.cookies.session_id || 'anonymous',
    type,
    payload: payload || {},
    timestamp: new Date().toISOString(),
    userAgent: req.headers['user-agent'] || '',
    ip: req.ip,
  };

  saveEvent(event);

  // Persist preference as a long-lived cookie
  if (type === 'PREFERENCE_UPDATE' && payload?.key && payload?.value !== undefined) {
    res.cookie(`pref_${payload.key}`, String(payload.value), {
      maxAge: 90 * 24 * 60 * 60 * 1000,
      httpOnly: false,
      sameSite: 'lax',
    });
  }

  res.json({ ok: true, eventId: event.id });
});

// ── GET /api/stats ────────────────────────────────────────────
app.get('/api/stats', (req, res) => {
  const events = loadEvents();

  const byType = events.reduce((acc, e) => {
    acc[e.type] = (acc[e.type] || 0) + 1;
    return acc;
  }, {});

  const sessions = new Set(events.map(e => e.sessionId));

  const topSearches = Object.entries(
    events.filter(e => e.type === 'SEARCH').map(e => e.payload.query).filter(Boolean)
      .reduce((acc, q) => { acc[q] = (acc[q] || 0) + 1; return acc; }, {})
  ).sort((a, b) => b[1] - a[1]).slice(0, 10).map(([query, count]) => ({ query, count }));

  const topPages = Object.entries(
    events.filter(e => e.type === 'PAGE_VIEW').map(e => e.payload.path).filter(Boolean)
      .reduce((acc, p) => { acc[p] = (acc[p] || 0) + 1; return acc; }, {})
  ).sort((a, b) => b[1] - a[1]).slice(0, 10).map(([path, count]) => ({ path, count }));

  const topProducts = Object.entries(
    events.filter(e => e.type === 'PRODUCT_CLICK').map(e => e.payload.productName).filter(Boolean)
      .reduce((acc, n) => { acc[n] = (acc[n] || 0) + 1; return acc; }, {})
  ).sort((a, b) => b[1] - a[1]).slice(0, 10).map(([productName, count]) => ({ productName, count }));

  res.json({
    totalEvents: events.length,
    uniqueSessions: sessions.size,
    eventsByType: byType,
    topSearches,
    topPages,
    topProducts,
    recentEvents: events.slice(-20).reverse(),
  });
});

// ── GET /api/events ───────────────────────────────────────────
app.get('/api/events', (req, res) => {
  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 50;
  const events = loadEvents().slice().reverse();
  res.json({ total: events.length, page, size, data: events.slice(page * size, (page + 1) * size) });
});

// ── DELETE /api/events ────────────────────────────────────────
app.delete('/api/events', (req, res) => {
  fs.writeFileSync(DATA_FILE, JSON.stringify([], null, 2));
  res.json({ ok: true });
});

// ── Start ─────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`\nActivity Monitor running at http://localhost:${PORT}`);
  console.log(`  Dashboard : http://localhost:${PORT}/dashboard.html`);
  console.log(`  Stats API : http://localhost:${PORT}/api/stats`);
  console.log(`  Tracker   : http://localhost:${PORT}/tracker.js\n`);
});
