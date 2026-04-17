import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { API_ORIGIN } from "@/utils/axios";

function wsEndpointFromApiBase() {
  return API_ORIGIN + "/ws";
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

