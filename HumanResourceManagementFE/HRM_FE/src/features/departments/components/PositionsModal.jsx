import { useEffect, useMemo, useState } from "react";
import { Edit, Plus, Save, Trash2, X } from "lucide-react";
import { Input, Spin } from "antd";

export function PositionsModal({
  open,
  department,
  positions,
  onClose,
  onCreate,
  onUpdate,
  onDelete,
  submitting,
}) {
  const deptName = department?.name ?? "";

  const deptPositions = useMemo(() => positions ?? [], [positions]);

  const [newName, setNewName] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [editingName, setEditingName] = useState("");

  useEffect(() => {
    if (!open) return;
    // Defer state reset to avoid React/lint warnings about cascading renders.
    const t = setTimeout(() => {
      setNewName("");
      setEditingId(null);
      setEditingName("");
    }, 0);
    return () => clearTimeout(t);
  }, [open, department?.id]);

  const [mounted, setMounted] = useState(open);
  const [closing, setClosing] = useState(false);

  useEffect(() => {
    if (open) {
      // Defer to avoid React/lint warnings about cascading renders.
      const t = setTimeout(() => {
        setMounted(true);
        setClosing(false);
      }, 0);
      return () => clearTimeout(t);
    }

    if (!mounted) return;
    const tClose = setTimeout(() => {
      setClosing(true);
    }, 0);
    const tUnmount = setTimeout(() => {
      setMounted(false);
      setClosing(false);
    }, 200);
    return () => {
      clearTimeout(tClose);
      clearTimeout(tUnmount);
    };
  }, [open, mounted]);

  if (!mounted) return null;

  const fadeStyle = {
    opacity: closing ? 0 : 1,
    transform: closing ? "translateY(10px) scale(0.99)" : "translateY(0px) scale(1)",
    transition: "opacity 180ms ease, transform 180ms ease",
  };

  const handleCreate = async () => {
    const trimmed = newName.trim();
    if (!trimmed || !department?.id) return;
    await onCreate?.(trimmed);
    setNewName("");
  };

  return (
    <>
      <div
        className="fixed inset-0 bg-black/50 z-40"
        onClick={onClose}
        style={{
          opacity: closing ? 0 : 1,
          transition: "opacity 180ms ease",
          pointerEvents: closing ? "none" : "auto",
        }}
      />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[860px] rounded-2xl z-50 flex flex-col overflow-hidden"
        style={{
          backgroundColor: "#FFFFFF",
          boxShadow: "0 20px 40px rgba(0,0,0,0.2)",
          height: "620px",
          maxHeight: "calc(100vh - 40px)",
          maxWidth: "calc(100vw - 40px)",
        }}
        // keep interactions smooth while closing
        aria-hidden={closing}
      >
        <div style={fadeStyle} className="relative h-full w-full flex flex-col min-h-0">
          {submitting && (
            <div
              className="absolute inset-0 z-50"
              style={{
                backgroundColor: "rgba(255,255,255,0.7)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: 12,
              }}
            >
              <Spin />
            </div>
          )}

          <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: "#E8E8E8" }}>
            <div>
              <h3
                style={{
                  fontFamily: "DM Sans, sans-serif",
                  fontSize: "16px",
                  fontWeight: "600",
                  color: "#0A0A0A",
                }}
              >
                Positions
              </h3>
              <div style={{ color: "#8C8C8C", fontSize: "13px", marginTop: "2px" }}>{deptName}</div>
            </div>
            <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded transition-colors">
              <X className="w-5 h-5" style={{ color: "#595959" }} />
            </button>
          </div>

          <div className="p-6 space-y-4 flex-1 flex flex-col overflow-hidden min-h-0">
            <div className="rounded-xl p-4 border" style={{ borderColor: "#E8E8E8" }}>
              <div className="flex items-center justify-between mb-3">
                <div style={{ fontSize: "13px", fontWeight: 600, color: "#0A0A0A" }}>Add position</div>
              </div>
              <div className="flex items-center gap-3">
                <Input
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="e.g. Senior Developer"
                  className="flex-1"
                  size="middle"
                />
                <button
                  onClick={handleCreate}
                  disabled={submitting || !newName.trim() || !department?.id}
                  className="flex items-center gap-2 px-5 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
                  style={{
                    background: "linear-gradient(135deg, #69B1FF 0%, #4096FF 100%)",
                    color: "#FFFFFF",
                    fontSize: "14px",
                    fontWeight: "500",
                  }}
                >
                  <Plus className="w-4 h-4" />
                  Add
                </button>
              </div>
            </div>

            <div className="flex flex-col min-h-0 flex-1">
              <div className="flex items-center justify-between mb-3">
                <div style={{ fontSize: "13px", fontWeight: 600, color: "#0A0A0A" }}>
                  Current positions
                  <span
                    className="ml-2 px-2 py-0.5 rounded-full"
                    style={{ backgroundColor: "rgba(22, 119, 255, 0.1)", color: "#1677FF", fontSize: "12px" }}
                  >
                    {deptPositions.length}
                  </span>
                </div>
              </div>

              <div
                className="flex-1 min-h-0 overflow-hidden rounded-xl border flex flex-col"
                style={{ borderColor: "#E8E8E8", backgroundColor: "#FFFFFF" }}
              >
                <div className="flex-1 min-h-0 overflow-y-auto pr-1">
                  {deptPositions.map((p) => {
                    const isEditing = editingId === p.id;
                    return (
                      <div
                        key={p.id}
                        className="flex items-center gap-3 px-4 py-3 border-b last:border-b-0"
                        style={{ borderColor: "#F0F0F0" }}
                      >
                        {isEditing ? (
                          <>
                            <Input
                              value={editingName}
                              onChange={(e) => setEditingName(e.target.value)}
                              className="flex-1"
                              size="middle"
                            />
                            <button
                              onClick={() => {
                                const trimmed = editingName.trim();
                                if (!trimmed) return;
                                Promise.resolve(onUpdate?.(p.id, trimmed)).then(() => {
                                  setEditingId(null);
                                  setEditingName("");
                                });
                              }}
                              disabled={submitting || !editingName.trim()}
                              className="p-2 rounded-lg transition-colors duration-150 hover:bg-blue-50 disabled:opacity-60"
                              style={{ color: "#1677FF" }}
                              title="Save"
                            >
                              <Save className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => {
                                setEditingId(null);
                                setEditingName("");
                              }}
                              disabled={submitting}
                              className="p-2 rounded-lg transition-colors duration-150 hover:bg-gray-50 disabled:opacity-60"
                              style={{ color: "#595959" }}
                              title="Cancel"
                            >
                              <X className="w-4 h-4" />
                            </button>
                          </>
                        ) : (
                          <>
                            <div className="flex-1 min-w-0">
                              <div
                                className="truncate"
                                style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: 500 }}
                              >
                                {p.name}
                              </div>
                              <div style={{ color: "#8C8C8C", fontSize: "12px" }}>{p.id}</div>
                            </div>
                            <button
                              onClick={() => {
                                setEditingId(p.id);
                                setEditingName(p.name ?? "");
                              }}
                              disabled={submitting}
                              className="p-2 rounded-lg transition-colors duration-150 hover:bg-blue-50 disabled:opacity-60"
                              style={{ color: "#1677FF" }}
                              title="Edit"
                            >
                              <Edit className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => onDelete?.(p.id)}
                              disabled={submitting}
                              className="p-2 rounded-lg transition-colors duration-150 hover:bg-red-50 disabled:opacity-60"
                              style={{ color: "#FF4D4F" }}
                              title="Delete"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </>
                        )}
                      </div>
                    );
                  })}

                  {deptPositions.length === 0 && (
                    <div className="m-3 rounded-lg p-4 border" style={{ borderColor: "#E8E8E8", color: "#8C8C8C" }}>
                      No positions yet. Add the first position above.
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

