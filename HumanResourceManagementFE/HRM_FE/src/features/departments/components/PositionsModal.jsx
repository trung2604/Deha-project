import { useEffect, useMemo, useState } from "react";
import { Edit, Plus, Save, Trash2, X } from "lucide-react";

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
    setNewName("");
    setEditingId(null);
    setEditingName("");
  }, [open, department?.id]);

  if (!open) return null;

  const handleCreate = async () => {
    const trimmed = newName.trim();
    if (!trimmed || !department?.id) return;
    await onCreate?.(trimmed);
    setNewName("");
  };

  return (
    <>
      <div className="fixed inset-0 bg-black/50 z-40" onClick={onClose} />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-[720px] rounded-xl z-50"
        style={{ backgroundColor: "#FFFFFF", boxShadow: "0 20px 40px rgba(0,0,0,0.2)" }}
      >
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

        <div className="p-6 space-y-5">
          <div className="rounded-xl p-4 border" style={{ borderColor: "#E8E8E8" }}>
            <div className="flex items-center justify-between mb-3">
              <div style={{ fontSize: "13px", fontWeight: 600, color: "#0A0A0A" }}>Add position</div>
            </div>
            <div className="flex items-center gap-3">
              <input
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                placeholder="e.g. Senior Developer"
                className="flex-1 h-9 px-3 rounded-lg border outline-none transition-all duration-150 focus:border-blue-500"
                style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
              />
              <button
                onClick={handleCreate}
                disabled={submitting || !newName.trim() || !department?.id}
                className="flex items-center gap-2 px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90 disabled:opacity-60"
                style={{
                  backgroundColor: "#1677FF",
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

          <div>
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

            <div className="space-y-2">
              {deptPositions.map((p) => {
                const isEditing = editingId === p.id;
                return (
                  <div
                    key={p.id}
                    className="flex items-center gap-3 rounded-lg px-3 py-2 border"
                    style={{ borderColor: "#E8E8E8" }}
                  >
                    {isEditing ? (
                      <>
                        <input
                          value={editingName}
                          onChange={(e) => setEditingName(e.target.value)}
                          className="flex-1 h-9 px-3 rounded-lg border outline-none transition-all duration-150 focus:border-blue-500"
                          style={{ borderColor: "#E8E8E8", fontSize: "14px" }}
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
                          <div className="truncate" style={{ color: "#0A0A0A", fontSize: "14px", fontWeight: 500 }}>
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
                <div className="rounded-lg p-4 border" style={{ borderColor: "#E8E8E8", color: "#8C8C8C" }}>
                  No positions yet. Add the first position above.
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

