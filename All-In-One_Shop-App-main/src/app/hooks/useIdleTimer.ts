import { useEffect, useRef, useCallback } from 'react';

const IDLE_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes
const WARNING_BEFORE_MS = 5 * 60 * 1000; // warn 5 minutes before logout

const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keypress', 'scroll', 'touchstart', 'click'] as const;

interface UseIdleTimerOptions {
  onIdle: () => void;
  onWarning: () => void;
  enabled: boolean;
}

export function useIdleTimer({ onIdle, onWarning, enabled }: UseIdleTimerOptions) {
  const idleTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const warningTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const warned = useRef(false);

  const clearTimers = useCallback(() => {
    if (idleTimer.current) clearTimeout(idleTimer.current);
    if (warningTimer.current) clearTimeout(warningTimer.current);
  }, []);

  const resetTimers = useCallback(() => {
    clearTimers();
    warned.current = false;

    warningTimer.current = setTimeout(() => {
      if (!warned.current) {
        warned.current = true;
        onWarning();
      }
    }, IDLE_TIMEOUT_MS - WARNING_BEFORE_MS);

    idleTimer.current = setTimeout(() => {
      onIdle();
    }, IDLE_TIMEOUT_MS);
  }, [clearTimers, onIdle, onWarning]);

  useEffect(() => {
    if (!enabled) {
      clearTimers();
      return;
    }

    resetTimers();

    const handleActivity = () => resetTimers();

    ACTIVITY_EVENTS.forEach(event => {
      window.addEventListener(event, handleActivity, { passive: true });
    });

    return () => {
      clearTimers();
      ACTIVITY_EVENTS.forEach(event => {
        window.removeEventListener(event, handleActivity);
      });
    };
  }, [enabled, resetTimers, clearTimers]);
}
