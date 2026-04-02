import { useCallback, useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { DollarSign } from "lucide-react";
import payrollService from "../api/payrollService";
import salaryContractService from "../api/salaryContractService";
import officeService from "@/features/offices/api/officeService";
import userService from "@/features/users/api/UserService";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isAdminRole } from "@/utils/role";
import {
  getListData,
  getOptimisticConflictMessage,
  getPageContent,
  getResponseMessage,
  isOptimisticConflictResponse,
  isSuccessResponse,
} from "@/utils/apiResponse";
import { PayrollFilters } from "../components/PayrollFilters";
import { GeneratePayrollPanel } from "../components/GeneratePayrollPanel";
import { PayrollTable } from "../components/PayrollTable";
import { PayrollDetailDrawer } from "../components/PayrollDetailDrawer";
import { SalaryContractSection } from "../components/SalaryContractSection";
import { SalaryContractModal } from "../components/SalaryContractModal";

function PayrollPageHeader() {
  return (
    <div className="mb-8">
      <div className="flex items-center gap-3 mb-2">
        <div
          className="p-2 rounded-lg"
          style={{
            backgroundColor: "rgba(82, 196, 26, 0.1)",
            color: "#52c41a",
          }}
        >
          <DollarSign className="w-6 h-6" />
        </div>
        <h1
          style={{
            fontFamily: "DM Sans, sans-serif",
            fontSize: "28px",
            fontWeight: "700",
            color: "#0A0A0A",
            margin: 0,
          }}
        >
          Payroll Management
        </h1>
      </div>
      <p
        style={{
          fontSize: "14px",
          color: "#595959",
          margin: 0,
          paddingLeft: "44px",
        }}
      >
        Generate, view, and manage payroll records and salary contracts
      </p>
    </div>
  );
}

export function PayrollPage() {
  const { user } = useAuth();
  const isAdmin = isAdminRole(user?.role);
  const now = new Date();

  const [isPayrollListLoading, setIsPayrollListLoading] = useState(true);
  const [isSubmittingPayrollAction, setIsSubmittingPayrollAction] = useState(false);
  const [isSalaryContractsLoading, setIsSalaryContractsLoading] = useState(false);

  const [selectedYear, setSelectedYear] = useState(now.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(now.getMonth() + 1);
  const [selectedOfficeId, setSelectedOfficeId] = useState(isAdmin ? undefined : user?.officeId);

  const [officeOptions, setOfficeOptions] = useState([]);
  const [userOptions, setUserOptions] = useState([]);
  const [payrollRows, setPayrollRows] = useState([]);

  const [selectedPayroll, setSelectedPayroll] = useState(null);
  const [isPayrollDetailDrawerOpen, setIsPayrollDetailDrawerOpen] = useState(false);

  const [selectedContractUserId, setSelectedContractUserId] = useState(undefined);
  const [salaryContracts, setSalaryContracts] = useState([]);
  const [isSalaryContractModalOpen, setIsSalaryContractModalOpen] = useState(false);
  const [editingSalaryContract, setEditingSalaryContract] = useState(null);

  const effectiveScopeOfficeId = useMemo(
    () => (isAdmin ? selectedOfficeId : user?.officeId),
    [isAdmin, selectedOfficeId, user?.officeId],
  );

  const loadFilterOptions = useCallback(async () => {
    try {
      const officeRes = isAdmin
        ? await officeService.getOffices()
        : { data: user?.officeId ? [{ id: user.officeId, name: user.officeName }] : [] };
      if (!isAdmin && user?.officeId) {
        setSelectedOfficeId(user.officeId);
      }

      const userListRes = await userService.getUsers({
        page: 0,
        size: 2000,
        officeId: effectiveScopeOfficeId || undefined,
      });

      setOfficeOptions(getListData(officeRes));
      setUserOptions(getPageContent(userListRes));
    } catch {
      toast.error("Failed to load payroll filters");
    }
  }, [isAdmin, user?.officeId, user?.officeName, effectiveScopeOfficeId]);

  const loadPayrollList = useCallback(async () => {
    setIsPayrollListLoading(true);
    try {
      const res = await payrollService.listPayrolls({
        year: selectedYear,
        month: selectedMonth,
        officeId: effectiveScopeOfficeId || undefined,
      });
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Failed to load payrolls"));
      }
      setPayrollRows(Array.isArray(res?.data) ? res.data : []);
    } catch {
      toast.error("Failed to load payrolls");
    } finally {
      setIsPayrollListLoading(false);
    }
  }, [selectedYear, selectedMonth, effectiveScopeOfficeId]);

  const loadSalaryContracts = useCallback(async () => {
    if (!userOptions.length) {
      setSalaryContracts([]);
      return;
    }

    setIsSalaryContractsLoading(true);
    try {
      if (selectedContractUserId) {
        const res = await salaryContractService.getByUser(selectedContractUserId);
        if (!isSuccessResponse(res)) {
          return toast.error(getResponseMessage(res, "Failed to load contracts"));
        }
        setSalaryContracts(Array.isArray(res?.data) ? res.data : []);
        return;
      }

      const settled = await Promise.allSettled(
        userOptions.map((userOption) => salaryContractService.getByUser(userOption.id)),
      );

      const aggregatedContracts = [];
      for (const settledResult of settled) {
        if (settledResult.status !== "fulfilled") continue;
        const response = settledResult.value;
        if (!isSuccessResponse(response) || !Array.isArray(response?.data)) continue;
        aggregatedContracts.push(...response.data);
      }
      setSalaryContracts(aggregatedContracts);
    } catch {
      toast.error("Failed to load contracts");
    } finally {
      setIsSalaryContractsLoading(false);
    }
  }, [selectedContractUserId, userOptions]);

  useEffect(() => {
    loadFilterOptions();
  }, [loadFilterOptions]);

  useEffect(() => {
    loadPayrollList();
  }, [loadPayrollList]);

  useEffect(() => {
    loadSalaryContracts();
  }, [loadSalaryContracts]);

  const handleGeneratePayroll = async (values) => {
    setIsSubmittingPayrollAction(true);
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
      await loadPayrollList();
    } catch {
      toast.error("Failed to generate payroll");
    } finally {
      setIsSubmittingPayrollAction(false);
    }
  };

  const handleOpenPayrollDetail = async (payrollRow) => {
    if (!payrollRow?.id) return;
    try {
      const res = await payrollService.getPayrollDetailById(payrollRow.id);
      if (!isSuccessResponse(res)) {
        return toast.error(getResponseMessage(res, "Failed to load payroll detail"));
      }
      setSelectedPayroll(res?.data ?? null);
      setIsPayrollDetailDrawerOpen(true);
    } catch {
      toast.error("Failed to load payroll detail");
    }
  };

  const handleSubmitSalaryContract = async (payload) => {
    if (editingSalaryContract?.id && editingSalaryContract?.version == null) {
      toast.error(getOptimisticConflictMessage());
      return;
    }

    const normalizedPayload = editingSalaryContract?.id
      ? { ...payload, expectedVersion: editingSalaryContract.version }
      : payload;

    setIsSubmittingPayrollAction(true);
    try {
      const res = editingSalaryContract?.id
        ? await salaryContractService.update(editingSalaryContract.id, normalizedPayload)
        : await salaryContractService.create(normalizedPayload);
      if (!isSuccessResponse(res)) {
        return toast.error(
          isOptimisticConflictResponse(res)
            ? getOptimisticConflictMessage(res)
            : getResponseMessage(res, "Failed to save salary contract"),
        );
      }
      toast.success(getResponseMessage(res, "Salary contract saved successfully"));
      setIsSalaryContractModalOpen(false);
      setEditingSalaryContract(null);
      await loadSalaryContracts();
    } catch {
      toast.error("Failed to save salary contract");
    } finally {
      setIsSubmittingPayrollAction(false);
    }
  };

  return (
    <div className="space-y-6">
      <PayrollPageHeader />

      <PayrollFilters
        year={selectedYear}
        month={selectedMonth}
        officeId={selectedOfficeId}
        offices={officeOptions}
        showOfficeFilter={isAdmin}
        onYearChange={setSelectedYear}
        onMonthChange={setSelectedMonth}
        onOfficeChange={(value) => setSelectedOfficeId(value ?? undefined)}
        onReload={loadPayrollList}
      />

      <GeneratePayrollPanel
        submitting={isSubmittingPayrollAction}
        offices={officeOptions}
        users={userOptions}
        showOfficeFilter={isAdmin}
        initialYear={selectedYear}
        initialMonth={selectedMonth}
        initialOfficeId={selectedOfficeId}
        onSubmit={handleGeneratePayroll}
      />

      <PayrollTable loading={isPayrollListLoading} payrolls={payrollRows} onViewDetail={handleOpenPayrollDetail} />

      <SalaryContractSection
        loading={isSalaryContractsLoading}
        contracts={salaryContracts}
        users={userOptions}
        selectedUserId={selectedContractUserId}
        onUserChange={(value) => setSelectedContractUserId(value)}
        onCreate={() => {
          setEditingSalaryContract(null);
          setIsSalaryContractModalOpen(true);
        }}
        onEdit={(contract) => {
          setEditingSalaryContract(contract);
          setIsSalaryContractModalOpen(true);
        }}
      />

      <SalaryContractModal
        open={isSalaryContractModalOpen}
        submitting={isSubmittingPayrollAction}
        users={userOptions}
        editingContract={editingSalaryContract}
        defaultUserId={selectedContractUserId}
        onClose={() => {
          setIsSalaryContractModalOpen(false);
          setEditingSalaryContract(null);
        }}
        onSubmit={handleSubmitSalaryContract}
      />

      <PayrollDetailDrawer
        open={isPayrollDetailDrawerOpen}
        payroll={selectedPayroll}
        onClose={() => {
          setIsPayrollDetailDrawerOpen(false);
          setSelectedPayroll(null);
        }}
      />
    </div>
  );
}

