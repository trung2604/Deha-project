import { useEffect, useMemo, useState } from "react";
import { Bell, CheckCheck, CheckCircle2, Info, MessageSquareText } from "lucide-react";
import { Button } from "antd";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";
import { getResponseMessage } from "@/utils/apiResponse";
import { useNotifications } from "@/features/notifications/context/NotificationContext";

const PAGE_SIZE = 20;

function formatTime(value) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "--";
  return date.toLocaleString();
}

function iconForType(type) {
  if (type === "NEW_MESSAGE") return MessageSquareText;
  if (type === "SYSTEM") return Info;
  return Bell;
}

export function NotificationsPage() {
  const navigate = useNavigate();
  const {
    notifications,
    unreadCount,
    isNotificationsLoading,
    refreshNotifications,
    loadMoreNotifications,
    markNotificationRead,
    markAllNotificationsRead,
  } = useNotifications();
  const [page, setPage] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);
  const [markingAll, setMarkingAll] = useState(false);

  useEffect(() => {
    refreshNotifications();
  }, [refreshNotifications]);

  const items = useMemo(() => notifications, [notifications]);

  const loadMore = async () => {
    setLoadingMore(true);
    const nextPage = page + 1;
    const result = await loadMoreNotifications({ page: nextPage, size: PAGE_SIZE });
    if (!result.ok) {
      toast.error(getResponseMessage(result.response, "Failed to load notifications"));
      setLoadingMore(false);
      return;
    }
    setPage(nextPage);
    setLoadingMore(false);
  };

  const handleMarkAsRead = async (id) => {
    const result = await markNotificationRead(id);
    if (!result.ok) {
      toast.error(getResponseMessage(result.response, "Failed to mark as read"));
    }
  };

  const handleMarkAllAsRead = async () => {
	setMarkingAll(true);
	const result = await markAllNotificationsRead();
	if (!result.ok) {
	  toast.error(getResponseMessage(result.response, "Failed to mark all as read"));
	  setMarkingAll(false);
	  return;
	}
	toast.success(getResponseMessage(result.response, "All notifications marked as read"));
	setMarkingAll(false);
  };

  const handleNotificationClick = async (item) => {
	if (!item) return;

	if (!item.read) {
	  await handleMarkAsRead(item.id);
	}

	if (item.type === "NEW_MESSAGE") {
	  navigate(item.referenceId ? `/chat?roomId=${item.referenceId}` : "/chat");
	  return;
	}

	if (item.referenceId) {
	  navigate(`/notifications?referenceId=${item.referenceId}`);
	}
  };

  return (
	<div className="space-y-4">
	  <div className="glass-surface page-surface p-5 md:p-6 soft-ring">
		<div className="flex flex-wrap items-center justify-between gap-3">
		  <div>
			<h1 style={{ fontSize: "24px", fontWeight: 700, color: "#0A0A0A", margin: 0 }}>Notifications</h1>
			<p style={{ marginTop: 6, marginBottom: 0, color: "#595959", fontSize: "14px" }}>
			  {unreadCount} unread notification(s)
			</p>
		  </div>

		  <Button
			icon={<CheckCheck className="w-4 h-4" />}
			onClick={handleMarkAllAsRead}
			loading={markingAll}
			disabled={!items.length || unreadCount === 0}
			style={{ borderRadius: 10 }}
		  >
			Mark all as read
		  </Button>
		</div>
	  </div>

	  <div className="glass-surface page-surface p-4 soft-ring">
		{isNotificationsLoading && !items.length ? (
		  <p style={{ margin: 0, color: "#8C8C8C" }}>Loading notifications...</p>
		) : items.length === 0 ? (
		  <div className="py-10 text-center">
			<Bell className="w-10 h-10 mx-auto" style={{ color: "#D9D9D9" }} />
			<p style={{ marginTop: 12, marginBottom: 0, color: "#595959" }}>No notifications yet.</p>
		  </div>
		) : (
		  <div className="space-y-2">
			{items.map((item) => {
			  const Icon = iconForType(item.type);
			  return (
				<div
				  key={item.id}
				  onClick={() => handleNotificationClick(item)}
				  className="rounded-xl p-4 transition-colors"
				  style={{
					border: "1px solid #E8E8E8",
					backgroundColor: item.read ? "#FFFFFF" : "rgba(22, 119, 255, 0.04)",
					cursor: "pointer",
				  }}
				>
				  <div className="flex items-start justify-between gap-3">
					<div className="flex items-start gap-3 min-w-0">
					  <div
						className="w-9 h-9 rounded-full flex items-center justify-center shrink-0"
						style={{ backgroundColor: "rgba(22,119,255,0.12)", color: "#1677FF" }}
					  >
						<Icon className="w-4 h-4" />
					  </div>
					  <div className="min-w-0">
						<div className="flex items-center gap-2">
						  <h3 style={{ margin: 0, fontSize: "14px", fontWeight: item.read ? 500 : 700, color: "#0A0A0A" }}>
							{item.title}
						  </h3>
						  {!item.read ? <CheckCircle2 className="w-4 h-4" style={{ color: "#1677FF" }} /> : null}
						</div>
						<p style={{ marginTop: 6, marginBottom: 0, color: "#595959", fontSize: "13px" }}>{item.body}</p>
						<p style={{ marginTop: 6, marginBottom: 0, color: "#8C8C8C", fontSize: "12px" }}>
						  {formatTime(item.createdAt)}
						</p>
					  </div>
					</div>

					{!item.read ? (
					  <Button
						size="small"
						onClick={(event) => {
						  event.stopPropagation();
						  handleMarkAsRead(item.id);
						}}
						style={{ borderRadius: 8 }}
					  >
						Mark read
					  </Button>
					) : null}
				  </div>
				</div>
			  );
			})}

			<div className="pt-2 text-center">
			  <Button onClick={loadMore} loading={loadingMore} style={{ borderRadius: 10 }}>
				Load more
			  </Button>
			</div>
		  </div>
		)}
	  </div>
	</div>
  );
}


