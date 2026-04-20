interface Window {
  __ACTIVITY_MONITOR_URL__?: string;
  ActivityTracker?: {
    sessionId: string;
    send: (type: string, payload?: Record<string, unknown>) => void;
    track: (type: string, data?: Record<string, unknown>) => void;
  };
}
