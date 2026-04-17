import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Button, Input } from "antd";
import { Building2, Hash, MessageSquare, RefreshCw, Send, Users } from "lucide-react";
import { toast } from "sonner";
import { useLocation } from "react-router-dom";
import chatService from "@/features/chat/api/chatService.js";
import { getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";
import { createRealtimeClient } from "@/utils/realtimeClient";

const MESSAGE_PAGE_SIZE = 30;

function roomLabel(room) {
  if (!room) return "";
  if (room?.name) return room.name;
  if (String(room?.type || "").toUpperCase() === "GENERAL") return "General";
  if (room?.department?.name) return `${room.department.name} Department`;
  return "Department room";
}

function roomTypeLabel(room) {
  const type = String(room?.type || "").toUpperCase();
  return type === "GENERAL" ? "General" : "Department";
}

function roomTypeIcon(room) {
  const type = String(room?.type || "").toUpperCase();
  return type === "GENERAL" ? Hash : Building2;
}

function roomTopic(room) {
  if (!room?.id) return "";
  return `/topic/room.${room.id}`;
}

function formatTime(value) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "--";
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function normalizeMessage(message) {
  const fallbackSentAt = new Date().toISOString();
  return {
    id: message?.id,
    roomId: message?.roomId,
    senderId: message?.senderId,
    senderName: message?.senderName || "Unknown",
    content: message?.content || "",
    sentAt: message?.sentAt || fallbackSentAt,
  };
}

export function ChatPage() {
  const location = useLocation();
  const [rooms, setRooms] = useState([]);
  const [selectedRoomId, setSelectedRoomId] = useState("");
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState("");
  const [loadingRooms, setLoadingRooms] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [wsConnected, setWsConnected] = useState(false);
  const [typingUsers, setTypingUsers] = useState([]);

  const clientRef = useRef(null);
  const messageSubscriptionRef = useRef(null);
  const typingSubscriptionRef = useRef(null);
  const typingLastSentAtRef = useRef(0);
  const typingTimeoutMapRef = useRef(new Map());
  const typingStopTimerRef = useRef(null);
  const messagesBottomRef = useRef(null);

  const selectedRoom = useMemo(
    () => rooms.find((room) => String(room.id) === String(selectedRoomId)) || null,
    [rooms, selectedRoomId],
  );

  const currentUserId = useMemo(() => {
    try {
      const raw = localStorage.getItem("user_info");
      const parsed = raw ? JSON.parse(raw) : null;
      return parsed?.id ? String(parsed.id) : "";
    } catch {
      return "";
    }
  }, []);

  const groupedRooms = useMemo(() => {
    const general = [];
    const departments = [];

    rooms.forEach((room) => {
      if (String(room?.type || "").toUpperCase() === "GENERAL") {
        general.push(room);
      } else {
        departments.push(room);
      }
    });

    return { general, departments };
  }, [rooms]);

  const preferredRoomId = useMemo(() => {
    const search = new URLSearchParams(location.search);
    return search.get("roomId") || "";
  }, [location.search]);

  const loadRooms = useCallback(async () => {
    setLoadingRooms(true);
    try {
      const res = await chatService.getMyRooms();
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load chat rooms"));
        return;
      }
      const nextRooms = Array.isArray(res?.data) ? res.data : [];
      setRooms(nextRooms);
      if (!selectedRoomId && nextRooms.length > 0) {
        const preferredRoom = nextRooms.find((room) => String(room.id) === String(preferredRoomId));
        setSelectedRoomId(String(preferredRoom?.id || nextRooms[0].id));
      }
    } catch {
      toast.error("Failed to load chat rooms");
    } finally {
      setLoadingRooms(false);
    }
  }, [preferredRoomId, selectedRoomId]);

  const loadMessages = useCallback(async (roomId) => {
    if (!roomId) {
      setMessages([]);
      return;
    }

    setLoadingMessages(true);
    try {
      const res = await chatService.getHistory(roomId, { page: 0, size: MESSAGE_PAGE_SIZE });
      if (!isSuccessResponse(res)) {
        toast.error(getResponseMessage(res, "Failed to load chat history"));
        return;
      }

      const history = Array.isArray(res?.data) ? res.data.map(normalizeMessage) : [];
      setMessages(history.slice().reverse());
    } catch {
      toast.error("Failed to load chat history");
    } finally {
      setLoadingMessages(false);
    }
  }, []);

  useEffect(() => {
    loadRooms();
  }, [loadRooms]);

  useEffect(() => {
    loadMessages(selectedRoomId);
  }, [selectedRoomId, loadMessages]);

  useEffect(() => {
    messagesBottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  useEffect(() => {
    const token = localStorage.getItem("auth_token");
    if (!token) return undefined;
    const typingTimeoutMap = typingTimeoutMapRef.current;

    const client = createRealtimeClient({
      token,
      onConnect: () => {
        setWsConnected(true);
      },
      onDisconnect: () => {
        setWsConnected(false);
      },
      onError: () => {
        toast.error("Chat connection error");
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      messageSubscriptionRef.current?.unsubscribe();
      messageSubscriptionRef.current = null;
      typingSubscriptionRef.current?.unsubscribe();
      typingSubscriptionRef.current = null;
      typingTimeoutMap.forEach((timerId) => clearTimeout(timerId));
      typingTimeoutMap.clear();
      clientRef.current = null;
      setWsConnected(false);
      if (typingStopTimerRef.current) {
        clearTimeout(typingStopTimerRef.current);
        typingStopTimerRef.current = null;
      }
      client.deactivate();
    };
  }, []);

  useEffect(() => {
    const client = clientRef.current;
    if (!client || !wsConnected || !selectedRoom) return undefined;

    const topic = roomTopic(selectedRoom);
    if (!topic) return undefined;

    messageSubscriptionRef.current?.unsubscribe();
    messageSubscriptionRef.current = client.subscribe(topic, (frame) => {
      try {
        const incoming = normalizeMessage(JSON.parse(frame.body || "{}"));
        if (String(incoming.roomId) !== String(selectedRoom.id)) return;

        setMessages((prev) => {
          if (prev.some((msg) => String(msg.id) === String(incoming.id))) return prev;
          return [...prev, incoming];
        });
      } catch {
        // ignore malformed message payload
      }
    });

    return () => {
      messageSubscriptionRef.current?.unsubscribe();
      messageSubscriptionRef.current = null;
    };
  }, [selectedRoom, wsConnected]);

  useEffect(() => {
    const client = clientRef.current;
    if (!client || !wsConnected || !selectedRoom) return undefined;
    const typingTimeoutMap = typingTimeoutMapRef.current;

    const topic = `${roomTopic(selectedRoom)}.typing`;
    if (!topic) return undefined;

    setTypingUsers([]);
    typingSubscriptionRef.current?.unsubscribe();
    typingSubscriptionRef.current = client.subscribe(topic, (frame) => {
      try {
        const event = JSON.parse(frame.body || "{}");
        const eventRoomId = event?.roomId ? String(event.roomId) : "";
        const eventUserId = event?.userId ? String(event.userId) : "";
        const eventUserName = event?.userName || "Someone";
        const isTyping = Boolean(event?.typing);

        if (eventRoomId !== String(selectedRoom.id)) return;
        if (currentUserId && eventUserId === currentUserId) return;

        if (!eventUserId) return;

        if (!isTyping) {
          const existing = typingTimeoutMap.get(eventUserId);
          if (existing) clearTimeout(existing);
          typingTimeoutMap.delete(eventUserId);
          setTypingUsers((prev) => prev.filter((user) => user.id !== eventUserId));
          return;
        }

        setTypingUsers((prev) => {
          const existing = prev.some((user) => user.id === eventUserId);
          if (existing) {
            return prev.map((user) => (user.id === eventUserId ? { ...user, name: eventUserName } : user));
          }
          return [...prev, { id: eventUserId, name: eventUserName }];
        });
        const existing = typingTimeoutMap.get(eventUserId);
        if (existing) clearTimeout(existing);
        const timeoutId = setTimeout(() => {
          typingTimeoutMap.delete(eventUserId);
          setTypingUsers((prev) => prev.filter((user) => user.id !== eventUserId));
        }, 2500);
        typingTimeoutMap.set(eventUserId, timeoutId);
      } catch {
        // ignore malformed typing payload
      }
    });

    return () => {
      typingSubscriptionRef.current?.unsubscribe();
      typingSubscriptionRef.current = null;
      typingTimeoutMap.forEach((timerId) => clearTimeout(timerId));
      typingTimeoutMap.clear();
      setTypingUsers([]);
    };
  }, [selectedRoom, wsConnected]);

  const publishTyping = useCallback((typing) => {
    if (!selectedRoom) return;
    const client = clientRef.current;
    if (!client || !client.connected) return;
    client.publish({
      destination: "/app/chat.typing",
      body: JSON.stringify({ roomId: selectedRoom.id, typing }),
    });
  }, [selectedRoom]);

  const scheduleTypingStop = useCallback(() => {
    if (typingStopTimerRef.current) {
      clearTimeout(typingStopTimerRef.current);
    }
    typingStopTimerRef.current = setTimeout(() => {
      publishTyping(false);
    }, 1500);
  }, [publishTyping]);

  const handleSend = async () => {
    const content = messageText.trim();
    if (!content || !selectedRoom) return;

    const client = clientRef.current;
    if (!client || !client.connected) {
      toast.error("Chat is connecting, please try again");
      return;
    }

    setIsSending(true);
    try {
      client.publish({
        destination: "/app/chat.send",
        body: JSON.stringify({ roomId: selectedRoom.id, content }),
      });
      publishTyping(false);
      if (typingStopTimerRef.current) {
        clearTimeout(typingStopTimerRef.current);
        typingStopTimerRef.current = null;
      }
      setMessageText("");
    } catch {
      toast.error("Failed to send message");
    } finally {
      setIsSending(false);
    }
  };

  const handleInputChange = (event) => {
    const nextValue = event.target.value;
    setMessageText(nextValue);

    if (!nextValue.trim()) {
      publishTyping(false);
      if (typingStopTimerRef.current) {
        clearTimeout(typingStopTimerRef.current);
        typingStopTimerRef.current = null;
      }
      return;
    }

    if (!selectedRoom || !wsConnected || !nextValue.trim()) return;

    scheduleTypingStop();

    const now = Date.now();
    if (now - typingLastSentAtRef.current < 1200) return;
    typingLastSentAtRef.current = now;
    publishTyping(true);
  };

  const typingNames = typingUsers.map((user) => user.name).filter(Boolean);
  const typingText =
    typingNames.length === 0
      ? ""
      : typingNames.length === 1
        ? `${typingNames[0]} is typing`
        : typingNames.length === 2
          ? `${typingNames[0]} and ${typingNames[1]} are typing`
          : `${typingNames[0]}, ${typingNames[1]} and ${typingNames.length - 2} others are typing`;
  const typingPreviewUsers = typingUsers.slice(0, 2);

  const renderRoomButton = (room) => {
    const active = String(room.id) === String(selectedRoomId);
    const RoomIcon = roomTypeIcon(room);

    return (
      <button
        key={room.id}
        type="button"
        onClick={() => {
          publishTyping(false);
          setMessageText("");
          if (typingStopTimerRef.current) {
            clearTimeout(typingStopTimerRef.current);
            typingStopTimerRef.current = null;
          }
          setSelectedRoomId(String(room.id));
        }}
        className="w-full text-left rounded-xl p-3 transition-all"
        style={{
          border: active ? "1px solid #1677FF" : "1px solid #E8E8E8",
          backgroundColor: active ? "rgba(22, 119, 255, 0.10)" : "#FFFFFF",
          boxShadow: active ? "0 6px 18px rgba(22, 119, 255, 0.12)" : "none",
        }}
      >
        <div className="flex items-start gap-2">
          <span className="mt-0.5" style={{ color: "#1677FF" }}>
            <RoomIcon className="w-4 h-4" />
          </span>
          <div className="min-w-0">
            <div style={{ fontSize: 13, fontWeight: 600, color: "#0A0A0A" }}>{roomLabel(room)}</div>
            <p style={{ margin: "4px 0 0", fontSize: 12, color: "#8C8C8C" }}>
              {roomTypeLabel(room)}
              {room?.department?.name && String(room?.type || "").toUpperCase() !== "GENERAL"
                ? ` • ${room.department.name}`
                : ""}
            </p>
          </div>
        </div>
      </button>
    );
  };

  return (
    <div className="grid grid-cols-1 xl:grid-cols-[280px_1fr] gap-4 h-[calc(100vh-9rem)]">
      <div className="glass-surface page-surface soft-ring p-3 overflow-hidden">
        <div className="flex items-center justify-between mb-3">
          <h2 style={{ margin: 0, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>Rooms</h2>
          <Button size="small" icon={<RefreshCw className="w-4 h-4" />} onClick={loadRooms} />
        </div>

        <div className="space-y-2 overflow-y-auto max-h-[calc(100vh-15rem)]">
          {loadingRooms ? <p style={{ color: "#8C8C8C", margin: 0 }}>Loading rooms...</p> : null}
          {!loadingRooms && rooms.length === 0 ? (
            <p style={{ color: "#8C8C8C", margin: 0 }}>No chat rooms found.</p>
          ) : null}

          {groupedRooms.general.length > 0 ? (
            <div className="space-y-2">
              <p style={{ margin: "4px 2px", fontSize: 11, fontWeight: 700, color: "#8C8C8C", textTransform: "uppercase" }}>
                General
              </p>
              {groupedRooms.general.map(renderRoomButton)}
            </div>
          ) : null}

          {groupedRooms.departments.length > 0 ? (
            <div className="space-y-2 pt-1">
              <p style={{ margin: "4px 2px", fontSize: 11, fontWeight: 700, color: "#8C8C8C", textTransform: "uppercase" }}>
                Department Rooms
              </p>
              {groupedRooms.departments.map(renderRoomButton)}
            </div>
          ) : null}
        </div>
      </div>

      <div className="glass-surface page-surface soft-ring p-3 flex flex-col min-h-0">
        <div className="border-b pb-3 mb-3" style={{ borderColor: "#E8E8E8" }}>
          <div className="flex items-start justify-between gap-3">
            <div>
              <h2 style={{ margin: 0, fontSize: 16, fontWeight: 700, color: "#0A0A0A" }}>
                {selectedRoom ? roomLabel(selectedRoom) : "Select a room"}
              </h2>
              {selectedRoom ? (
                <p style={{ margin: "4px 0 0", fontSize: 12, color: "#8C8C8C" }}>
                  {roomTypeLabel(selectedRoom)} chat room
                </p>
              ) : null}
            </div>
            <span
              className="inline-flex items-center gap-1 rounded-full px-2.5 py-1"
              style={{
                fontSize: 11,
                fontWeight: 600,
                color: wsConnected ? "#237804" : "#AD2102",
                background: wsConnected ? "#F6FFED" : "#FFF2F0",
                border: wsConnected ? "1px solid #B7EB8F" : "1px solid #FFCCC7",
              }}
            >
              <Users className="w-3.5 h-3.5" />
              {wsConnected ? "Realtime connected" : "Realtime connecting"}
            </span>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-1 space-y-2">
          {loadingMessages ? <p style={{ color: "#8C8C8C", margin: 0 }}>Loading messages...</p> : null}
          {!loadingMessages && !messages.length ? (
            <div className="h-full flex items-center justify-center text-center">
              <div>
                <MessageSquare className="w-8 h-8 mx-auto" style={{ color: "#BFBFBF" }} />
                <p style={{ color: "#8C8C8C", margin: "8px 0 0" }}>No messages yet. Start the conversation.</p>
              </div>
            </div>
          ) : null}

          {messages.map((message) => {
            const isMine = currentUserId && String(message.senderId) === currentUserId;

            return (
              <div
                key={message.id || `${message.senderId}-${message.sentAt}`}
                className={`flex ${isMine ? "justify-end" : "justify-start"}`}
              >
                <div
                  className="max-w-[84%] rounded-2xl px-3 py-2"
                  style={{
                    background: isMine ? "#1677FF" : "#FFFFFF",
                    border: isMine ? "1px solid #1677FF" : "1px solid #E8E8E8",
                  }}
                >
                  <div className="flex items-center justify-between gap-3">
                    <strong style={{ fontSize: 12, color: isMine ? "#E6F4FF" : "#0A0A0A" }}>
                      {isMine ? "You" : message.senderName}
                    </strong>
                    <span style={{ fontSize: 11, color: isMine ? "#BAE0FF" : "#8C8C8C" }}>
                      {formatTime(message.sentAt)}
                    </span>
                  </div>
                  <p
                    style={{
                      margin: "4px 0 0",
                      color: isMine ? "#FFFFFF" : "#595959",
                      fontSize: 13,
                      whiteSpace: "pre-wrap",
                    }}
                  >
                    {message.content}
                  </p>
                </div>
              </div>
            );
          })}

          {typingText ? (
            <div className="flex justify-start">
              <div className="max-w-[84%]">
                <div className="mb-1 inline-flex -space-x-1.5">
                  {typingPreviewUsers.map((user) => (
                    <span
                      key={user.id}
                      className="inline-flex h-5 w-5 items-center justify-center rounded-full text-[10px] font-semibold"
                      style={{ background: "#D9D9D9", color: "#595959", border: "1px solid #FFFFFF" }}
                    >
                      {String(user.name || "?").trim().charAt(0).toUpperCase() || "?"}
                    </span>
                  ))}
                </div>
                <div
                  className="inline-flex items-center gap-1 rounded-2xl px-3 py-2"
                  style={{ background: "#F5F5F5", border: "1px solid #E8E8E8" }}
                >
                  <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: "#8C8C8C" }} />
                  <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: "#8C8C8C", animationDelay: "120ms" }} />
                  <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: "#8C8C8C", animationDelay: "240ms" }} />
                </div>
                <p style={{ margin: "6px 2px 0", fontSize: 11, color: "#8C8C8C" }}>{typingText}...</p>
              </div>
            </div>
          ) : null}

          <div ref={messagesBottomRef} />
        </div>

        <div className="pt-3 mt-3 border-t" style={{ borderColor: "#E8E8E8" }}>
          <div className="flex gap-2">
            <Input
              placeholder={selectedRoom ? "Type a message..." : "Select a room first"}
              value={messageText}
              disabled={!selectedRoom || !wsConnected}
              onChange={handleInputChange}
              onBlur={() => {
                publishTyping(false);
              }}
              onPressEnter={handleSend}
            />
            <Button
              type="primary"
              icon={<Send className="w-4 h-4" />}
              onClick={handleSend}
              loading={isSending}
              disabled={!selectedRoom || !messageText.trim() || !wsConnected}
            >
              Send
            </Button>
          </div>
          {/* Typing indicator is rendered in the message stream to match Messenger-style layout. */}
        </div>
      </div>
    </div>
  );
}






