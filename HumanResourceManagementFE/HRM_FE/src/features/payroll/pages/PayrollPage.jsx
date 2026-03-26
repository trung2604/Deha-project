import { useCallback, useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import payrollService from "../api/payrollService";
import salaryContractService from "../api/salaryContractService";
import officeService from "@/features/offices/api/officeService";
import UserService from "@/features/users/api/UserService";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole } from "@/utils/role";
import { getListData, getPageContent, getResponseMessage, isSuccessResponse } from "@/utils/apiResponse";
import { PayrollFilters } from "../components/PayrollFilters";
import { GeneratePayrollPanel } from "../components/GeneratePayrollPanel";
import { PayrollTable } from "../components/PayrollTable";
import { PayrollDetailDrawer } from "../components/PayrollDetailDrawer";
import { SalaryContractSection } from "../components/SalaryContractSection";
import { SalaryContractModal } from "../components/SalaryContractModal";

export function PayrollPage() {
  const { user } = useAuth();
  const isAdmin = isAdminRole(user?.role);
  const now = new Date();

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [contractsLoading, setContractsLoading] = useState(false);

  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [officeId, setOfficeId] = useState(isAdmin ? undefined : user?.officeId);

  const [offices, setOffices] = useState([]);
  const [users, setUsers] = useState([]);
  const [payrolls, setPayrolls] = useState([]);

  const [selectedPayroll, setSelectedPayroll] = useState(null);
  const [detailOpen, setDetailOpen] = useState(false);

  const [selectedUserId, setSelectedUserId] = useState(undefined);
  const [contracts, setContracts] = useState([]);
  const [contractModalOpen, setContractModalOpen] = useState(false);
  const [editingContract, setEditingContract] = useState(null);

  const effectiveOfficeId = useMemo(() => (isAdmin ? officeId : user?.officeId), [isAdmin, officeId, user?.officeId]);

  const loadFilters = useCallback(async () => {
    try {
      const officeRes = isAdmin
        ? await officeService.getOffices()
        : { data: user?.officeId ? [{ id: user.officeId, name: user.officeName }] : [] };
      if (!isAdmin && user?.officeId) {
        setOfficeId(user.officeId);
      }

      const userRes = await UserService.getUsers({
        page: 0,
        size: 2000,
        officeId: effectiveOfficeId || undefined,
      });

      setOffices(getListData(officeRes));
      setUsers(getPageContent(userRes));
    } catch {
      toast.error("Failed to load payroll filters");
    }
  }, [isAdmin, user?.officeId, user?.officeName, effectiveOfficeId]);

  const loadPayrolls = useCallback(async () => {
    setLoading(true);
    try {
      const res = await payrollService.getPayrolls({
        year,
        month,
        officeId: effectiveOfficeId || undefined,
      });
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Failed to load payrolls"));
      }
      setPayrolls(Array.isArray(res?.data) ? res.data : []);
    } catch {
      toast.error("Failed to load payrolls");
    } finally {
      setLoading(false);
    }
  }, [year, month, effectiveOfficeId]);

  const loadContracts = useCallback(async () => {
    if (!users.length) {
      setContracts([]);
      return;
    }

    setContractsLoading(true);
    try {
      if (selectedUserId) {
        const res = await salaryContractService.getByUser(selectedUserId);
        if (!isSuccessResponse(res)) {
          return toast.error(getResponseMessage(res, "Failed to load contracts"));
        }
        setContracts(Array.isArray(res?.data) ? res.data : []);
        return;
      }

      const settled = await Promise.allSettled(
        users.map((u) => salaryContractService.getByUser(u.id)),
      );

      const nextContracts = [];
      for (const item of settled) {
        if (item.status !== "fulfilled") continue;
        const res = item.value;
        if (!isSuccessResponse(res) || !Array.isArray(res?.data)) continue;
        nextContracts.push(...res.data);
      }
      setContracts(nextContracts);
    } catch {
      toast.error("Failed to load contracts");
    } finally {
      setContractsLoading(false);
    }
  }, [selectedUserId, users]);

  useEffect(() => {
    loadFilters();
  }, [loadFilters]);

  useEffect(() => {
    loadPayrolls();
  }, [loadPayrolls]);

  useEffect(() => {
    loadContracts();
  }, [loadContracts]);

  const handleGenerate = async (values) => {
    setSubmitting(true);
    try {
      const payload = {
        year: values.year,
        month: values.month,
        officeId: isAdmin ? values.officeId : (user?.officeId ?? undefined),
        userId: values.userId,
      };
      const res = await payrollService.generatePayroll(payload);
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Failed to generate payroll"));
      }
      toast.success(getResponseMessage(res, "Payroll generated successfully"));
      await loadPayrolls();
    } catch {
      toast.error("Failed to generate payroll");
    } finally {
      setSubmitting(false);
    }
  };

  const handleOpenDetail = async (row) => {
    if (!row?.id) return;
    try {
      const res = await payrollService.getPayrollById(row.id);
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Failed to load payroll detail"));
      }
      setSelectedPayroll(res?.data ?? null);
      setDetailOpen(true);
    } catch {
      toast.error("Failed to load payroll detail");
    }
  };

  const handleSubmitContract = async (payload) => {
    setSubmitting(true);
    try {
      const res = editingContract?.id
        ? await salaryContractService.update(editingContract.id, payload)
        : await salaryContractService.create(payload);
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Failed to save salary contract"));
      }
      toast.success(getResponseMessage(res, "Salary contract saved successfully"));
      setContractModalOpen(false);
      setEditingContract(null);
      await loadContracts();
    } catch {
      toast.error("Failed to save salary contract");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 style={{ fontFamily: "DM Sans, sans-serif", fontSize: "24px", fontWeight: "600", color: "#0A0A0A" }}>
          Payroll
        </h1>
      </div>

      <PayrollFilters
        year={year}
        month={month}
        officeId={officeId}
        offices={offices}
        showOfficeFilter={isAdmin}
        onYearChange={setYear}
        onMonthChange={setMonth}
        onOfficeChange={(value) => setOfficeId(value ?? undefined)}
        onReload={loadPayrolls}
      />

      <GeneratePayrollPanel
        submitting={submitting}
        offices={offices}
        users={users}
        showOfficeFilter={isAdmin}
        initialYear={year}
        initialMonth={month}
        initialOfficeId={officeId}
        onSubmit={handleGenerate}
      />

      <PayrollTable loading={loading} payrolls={payrolls} onViewDetail={handleOpenDetail} />

      <SalaryContractSection
        loading={contractsLoading}
        contracts={contracts}
        users={users}
        selectedUserId={selectedUserId}
        onUserChange={(value) => setSelectedUserId(value)}
        onCreate={() => {
          setEditingContract(null);
          setContractModalOpen(true);
        }}
        onEdit={(contract) => {
          setEditingContract(contract);
          setContractModalOpen(true);
        }}
      />

      <SalaryContractModal
        open={contractModalOpen}
        submitting={submitting}
        users={users}
        editingContract={editingContract}
        defaultUserId={selectedUserId}
        onClose={() => {
          setContractModalOpen(false);
          setEditingContract(null);
        }}
        onSubmit={handleSubmitContract}
      />

      <PayrollDetailDrawer
        open={detailOpen}
        payroll={selectedPayroll}
        onClose={() => {
          setDetailOpen(false);
          setSelectedPayroll(null);
        }}
      />
    </div>
  );
}

