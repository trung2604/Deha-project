import { Trash2 } from 'lucide-react';

export function DeleteUserModal({ userName, onClose, onConfirm }) {
  return (
    <>
      <div className="fixed inset-0 bg-black/50 z-40" onClick={onClose} />
      <div
        className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-[420px] rounded-xl p-6 z-50"
        style={{ backgroundColor: '#FFFFFF', boxShadow: '0 20px 40px rgba(0,0,0,0.2)' }}
      >
        <div className="flex flex-col items-center text-center">
          <div
            className="w-16 h-16 rounded-full flex items-center justify-center mb-4"
            style={{ backgroundColor: 'rgba(250, 140, 22, 0.1)' }}
          >
            <Trash2 className="w-8 h-8" style={{ color: '#FA8C16' }} />
          </div>
          <h3
            className="mb-2"
            style={{
              fontFamily: 'DM Sans, sans-serif',
              fontSize: '18px',
              fontWeight: '600',
              color: '#0A0A0A',
            }}
          >
            Delete User?
          </h3>
          <p className="mb-6" style={{ color: '#595959', fontSize: '14px' }}>
            Are you sure you want to delete <strong>{userName}</strong>? This action cannot be undone.
          </p>
          <div className="flex items-center gap-2 w-full">
            <button
              onClick={onClose}
              className="flex-1 px-4 h-9 rounded-lg transition-colors duration-150 hover:bg-gray-100"
              style={{ color: '#0A0A0A', fontSize: '14px', fontWeight: '500' }}
            >
              Cancel
            </button>
            <button
              onClick={onConfirm}
              className="flex-1 px-4 h-9 rounded-lg transition-all duration-150 hover:opacity-90"
              style={{ backgroundColor: '#FF4D4F', color: '#FFFFFF', fontSize: '14px', fontWeight: '500' }}
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </>
  );
}

