-- Normalize legacy PENDING values to explicit approval stages.
-- Keep enum/value compatibility while making queue behavior deterministic.

UPDATE ot_requests r
SET status = CASE
    WHEN u.role = 'ROLE_MANAGER_DEPARTMENT' THEN 'PENDING_OFFICE'
    ELSE 'PENDING_DEPARTMENT'
END
FROM users u
WHERE r.user_id = u.id
  AND r.status = 'PENDING';

UPDATE ot_reports rep
SET status = CASE
    WHEN u.role = 'ROLE_MANAGER_DEPARTMENT' THEN 'PENDING_OFFICE'
    ELSE 'PENDING_DEPARTMENT'
END
FROM attendance_logs a
JOIN users u ON u.id = a.user_id
WHERE rep.attendance_log_id = a.id
  AND rep.status = 'PENDING';

