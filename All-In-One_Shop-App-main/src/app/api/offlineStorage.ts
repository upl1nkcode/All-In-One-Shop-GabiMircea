/**
 * Offline Storage Module
 *
 * Caches API responses in localStorage and queues mutating operations
 * so they can be replayed when the network connection is restored.
 */

// ── Cache helpers ───────────────────────────────────────────

const CACHE_PREFIX = 'aios_cache_';
const QUEUE_KEY = 'aios_pending_ops';

export function cacheSet(key: string, data: unknown): void {
  try {
    localStorage.setItem(
      CACHE_PREFIX + key,
      JSON.stringify({ data, ts: Date.now() }),
    );
  } catch {
    // localStorage full — silently fail
  }
}

export function cacheGet<T>(key: string, maxAgeMs = 1000 * 60 * 30): T | null {
  try {
    const raw = localStorage.getItem(CACHE_PREFIX + key);
    if (!raw) return null;
    const { data, ts } = JSON.parse(raw) as { data: T; ts: number };
    if (Date.now() - ts > maxAgeMs) {
      localStorage.removeItem(CACHE_PREFIX + key);
      return null;
    }
    return data;
  } catch {
    return null;
  }
}

export function cacheClear(): void {
  const keys = Object.keys(localStorage).filter(k => k.startsWith(CACHE_PREFIX));
  keys.forEach(k => localStorage.removeItem(k));
}

// ── Offline operation queue ─────────────────────────────────

export interface PendingOperation {
  id: string;
  endpoint: string;
  method: string;
  body?: string;
  createdAt: number;
}

function getQueue(): PendingOperation[] {
  try {
    const raw = localStorage.getItem(QUEUE_KEY);
    return raw ? (JSON.parse(raw) as PendingOperation[]) : [];
  } catch {
    return [];
  }
}

function saveQueue(queue: PendingOperation[]): void {
  try {
    localStorage.setItem(QUEUE_KEY, JSON.stringify(queue));
  } catch {
    // localStorage full
  }
}

export function enqueueOperation(op: Omit<PendingOperation, 'id' | 'createdAt'>): void {
  const queue = getQueue();
  queue.push({
    ...op,
    id: crypto.randomUUID?.() ?? `${Date.now()}-${Math.random()}`,
    createdAt: Date.now(),
  });
  saveQueue(queue);
}

export function getPendingCount(): number {
  return getQueue().length;
}

/**
 * Replays all queued operations against the live API.
 * Returns the count of successfully synced operations.
 */
export async function syncPendingOperations(
  apiBaseUrl: string,
  authToken: string | null,
): Promise<{ synced: number; failed: number }> {
  const queue = getQueue();
  if (queue.length === 0) return { synced: 0, failed: 0 };

  let synced = 0;
  let failed = 0;
  const remaining: PendingOperation[] = [];

  for (const op of queue) {
    try {
      const headers: Record<string, string> = { 'Content-Type': 'application/json' };
      if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

      const res = await fetch(`${apiBaseUrl}${op.endpoint}`, {
        method: op.method,
        headers,
        body: op.body,
      });

      if (res.ok) {
        synced++;
      } else {
        // Non-retryable server error — drop to avoid infinite loop
        failed++;
      }
    } catch {
      // Network still down — keep in queue for next sync
      remaining.push(op);
    }
  }

  saveQueue(remaining);
  return { synced, failed };
}
