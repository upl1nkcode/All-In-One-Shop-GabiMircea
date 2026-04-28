/**
 * useWebSocket — lightweight STOMP-over-SockJS hook
 *
 * Connects to the Spring Boot WebSocket endpoint at /ws using the
 * SockJS protocol via a plain WebSocket (the /websocket transport).
 * Subscribes to /topic/products and returns incoming messages.
 *
 * No external dependencies required — uses the browser-native WebSocket.
 */
import { useEffect, useRef, useState, useCallback } from 'react';
import { API_BASE_URL } from './client';

// The backend serves SockJS at /ws — we use the raw WS transport
function buildWsUrl(): string {
  const base = API_BASE_URL.replace('/api', '');
  const httpUrl = `${base}/ws/websocket`;
  return httpUrl.replace(/^http/, 'ws');
}

// Minimal STOMP frame encoder / decoder
function encodeStompFrame(command: string, headers: Record<string, string> = {}, body = ''): string {
  let frame = command + '\n';
  for (const [k, v] of Object.entries(headers)) {
    frame += `${k}:${v}\n`;
  }
  frame += '\n' + body + '\0';
  return frame;
}

interface StompFrame {
  command: string;
  headers: Record<string, string>;
  body: string;
}

function decodeStompFrame(raw: string): StompFrame | null {
  const endOfHeaders = raw.indexOf('\n\n');
  if (endOfHeaders === -1) return null;
  const headerPart = raw.substring(0, endOfHeaders);
  const body = raw.substring(endOfHeaders + 2).replace(/\0$/, '');
  const lines = headerPart.split('\n');
  const command = lines[0];
  const headers: Record<string, string> = {};
  for (let i = 1; i < lines.length; i++) {
    const colon = lines[i].indexOf(':');
    if (colon > 0) {
      headers[lines[i].substring(0, colon)] = lines[i].substring(colon + 1);
    }
  }
  return { command, headers, body };
}

export interface WebSocketMessage {
  type: string;
  count?: number;
  products?: unknown[];
  totalProducts?: number;
  timestamp?: number;
}

export function useWebSocket(shouldConnect: boolean) {
  const wsRef = useRef<WebSocket | null>(null);
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState<WebSocketMessage[]>([]);
  const [lastMessage, setLastMessage] = useState<WebSocketMessage | null>(null);

  const connect = useCallback(() => {
    if (wsRef.current) return;

    const url = buildWsUrl();
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => {
      // STOMP CONNECT
      ws.send(encodeStompFrame('CONNECT', { 'accept-version': '1.1,1.0', 'heart-beat': '10000,10000' }));
    };

    ws.onmessage = (event) => {
      const frame = decodeStompFrame(event.data as string);
      if (!frame) return;

      if (frame.command === 'CONNECTED') {
        setConnected(true);
        // Subscribe to /topic/products
        ws.send(
          encodeStompFrame('SUBSCRIBE', {
            id: 'sub-0',
            destination: '/topic/products',
          }),
        );
      }

      if (frame.command === 'MESSAGE') {
        try {
          const msg = JSON.parse(frame.body) as WebSocketMessage;
          setLastMessage(msg);
          setMessages(prev => [...prev.slice(-200), msg]); // Keep last 200
        } catch {
          // Not JSON — ignore
        }
      }
    };

    ws.onerror = () => {
      setConnected(false);
    };

    ws.onclose = () => {
      setConnected(false);
      wsRef.current = null;
    };
  }, []);

  const disconnect = useCallback(() => {
    const ws = wsRef.current;
    if (ws) {
      try {
        ws.send(encodeStompFrame('DISCONNECT'));
      } catch { /* already closed */ }
      ws.close();
      wsRef.current = null;
    }
    setConnected(false);
  }, []);

  const clearMessages = useCallback(() => {
    setMessages([]);
    setLastMessage(null);
  }, []);

  useEffect(() => {
    if (shouldConnect) {
      connect();
    } else {
      disconnect();
    }
    return () => disconnect();
  }, [shouldConnect, connect, disconnect]);

  return { connected, messages, lastMessage, clearMessages };
}
