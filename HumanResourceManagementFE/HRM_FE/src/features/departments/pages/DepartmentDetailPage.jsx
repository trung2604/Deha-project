import { useNavigate, useParams } from "react-router-dom";
import { DepartmentDetailModal } from "../components/DepartmentDetailModal";

export function DepartmentDetailPage() {
  const navigate = useNavigate();
  const { departmentId } = useParams();

  return (
    <DepartmentDetailModal
      open={true}
      departmentId={departmentId}
      onClose={() => navigate("/departments")}
    />
  );
}

