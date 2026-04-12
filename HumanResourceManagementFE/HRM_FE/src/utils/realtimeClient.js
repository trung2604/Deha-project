import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

function wsEndpointFromApiBase() {
  const apiBase = import.meta.env.VITE_API_URL || "http://localhost:8081/api";
  return apiBase.replace(/\/api\/?$/, "") + "/ws";
}

export function createRealtimeClient({ token, onConnect, onDisconnect, onError } = {}) {
  if (!token) {
    throw new Error("Missing auth token for realtime connection");
  }

  const client = new Client({
    webSocketFactory: () => new SockJS(wsEndpointFromApiBase()),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    debug: () => {},
  });

  client.onConnect = (frame) => {
    if (typeof onConnect === "function") {
      onConnect(client, frame);
    }
  };

  client.onWebSocketClose = () => {
    if (typeof onDisconnect === "function") {
      onDisconnect();
    }
  };

  client.onStompError = (frame) => {
    if (typeof onError === "function") {
      onError(frame);
    }
  };

  return client;
}

