const API_BASE = "http://localhost:3000/api/v1";

const state = {
    token: localStorage.getItem("moneymap_access_token"),
    refreshToken: localStorage.getItem("moneymap_refresh_token"),
    user: JSON.parse(localStorage.getItem("moneymap_user") || "null"),
    categories: [],
    transactions: [],
    dashboard: null,
    trends: [],
    budgets: [],
    budgetSummary: null,
    goals: [],
    subscriptions: [],
    profile: null,
    weeklyReport: null,
    monthlyReport: null,
    chatSessionId: null,
    chatMessages: []
};

const elements = {
    authView: document.getElementById("authView"),
    appView: document.getElementById("appView"),
    loginTab: document.getElementById("loginTab"),
    signupTab: document.getElementById("signupTab"),
    loginForm: document.getElementById("loginForm"),
    signupForm: document.getElementById("signupForm"),
    authMessage: document.getElementById("authMessage"),
    pageTitle: document.getElementById("pageTitle"),
    userName: document.getElementById("userName"),
    userEmail: document.getElementById("userEmail"),
    userInitials: document.getElementById("userInitials"),
    dashboardGreeting: document.getElementById("dashboardGreeting"),
    dashboardName: document.getElementById("dashboardName"),
    balanceCardTitle: document.getElementById("balanceCardTitle"),
    balanceCardAmount: document.getElementById("balanceCardAmount"),
    balanceCardSpent: document.getElementById("balanceCardSpent"),
    balanceCardRightTitle: document.getElementById("balanceCardRightTitle"),
    balanceCardRightAmount: document.getElementById("balanceCardRightAmount"),
    floatingAssistantButton: document.getElementById("floatingAssistantButton"),
    recentTransactions: document.getElementById("recentTransactions"),
    transactionList: document.getElementById("transactionList"),
    budgetList: document.getElementById("budgetList"),
    transactionForm: document.getElementById("transactionForm"),
    transactionType: document.getElementById("transactionType"),
    transactionCategory: document.getElementById("transactionCategory"),
    budgetCategory: document.getElementById("budgetCategory"),
    budgetForm: document.getElementById("budgetForm"),
    budgetAmount: document.getElementById("budgetAmount"),
    budgetMonth: document.getElementById("budgetMonth"),
    budgetYear: document.getElementById("budgetYear"),
    budgetMessage: document.getElementById("budgetMessage"),
    budgetSummaryList: document.getElementById("budgetSummaryList"),
    weeklyBudgetList: document.getElementById("weeklyBudgetList"),
    transactionTags: document.getElementById("transactionTags"),
    editTransactionModal: document.getElementById("editTransactionModal"),
    editTransactionForm: document.getElementById("editTransactionForm"),
    editTransactionId: document.getElementById("editTransactionId"),
    editTransactionType: document.getElementById("editTransactionType"),
    editTransactionCategory: document.getElementById("editTransactionCategory"),
    editTransactionAmount: document.getElementById("editTransactionAmount"),
    editTransactionDescription: document.getElementById("editTransactionDescription"),
    editTransactionDate: document.getElementById("editTransactionDate"),
    editTransactionTags: document.getElementById("editTransactionTags"),
    editCancelBtn: document.getElementById("editCancelBtn"),
    editTransactionMessage: document.getElementById("editTransactionMessage"),
    transactionAmount: document.getElementById("transactionAmount"),
    transactionDescription: document.getElementById("transactionDescription"),
    transactionDate: document.getElementById("transactionDate"),
    transactionMessage: document.getElementById("transactionMessage"),
    categoryForm: document.getElementById("categoryForm"),
    categoryName: document.getElementById("categoryName"),
    categoryColor: document.getElementById("categoryColor"),
    categoryMessage: document.getElementById("categoryMessage"),
    categoryList: document.getElementById("categoryList"),
    goalForm: document.getElementById("goalForm"),
    goalName: document.getElementById("goalName"),
    goalTarget: document.getElementById("goalTarget"),
    goalCurrent: document.getElementById("goalCurrent"),
    goalDate: document.getElementById("goalDate"),
    goalColor: document.getElementById("goalColor"),
    goalMessage: document.getElementById("goalMessage"),
    goalList: document.getElementById("goalList"),
    subscriptionForm: document.getElementById("subscriptionForm"),
    subscriptionName: document.getElementById("subscriptionName"),
    subscriptionAmount: document.getElementById("subscriptionAmount"),
    subscriptionCycle: document.getElementById("subscriptionCycle"),
    subscriptionDate: document.getElementById("subscriptionDate"),
    subscriptionColor: document.getElementById("subscriptionColor"),
    subscriptionMessage: document.getElementById("subscriptionMessage"),
    subscriptionList: document.getElementById("subscriptionList"),
    trendChart: document.getElementById("trendChart"),
    breakdownList: document.getElementById("breakdownList"),
    weeklyReportList: document.getElementById("weeklyReportList"),
    monthlyReportList: document.getElementById("monthlyReportList"),
    settingsForm: document.getElementById("settingsForm"),
    settingsName: document.getElementById("settingsName"),
    settingsCurrency: document.getElementById("settingsCurrency"),
    settingsNotifications: document.getElementById("settingsNotifications"),
    settingsMessage: document.getElementById("settingsMessage"),
    profileForm: document.getElementById("profileForm"),
    profileInstitution: document.getElementById("profileInstitution"),
    profileCompany: document.getElementById("profileCompany"),
    profileJobTitle: document.getElementById("profileJobTitle"),
    profileMonthlyIncome: document.getElementById("profileMonthlyIncome"),
    profileFinancialGoal: document.getElementById("profileFinancialGoal"),
    profileMessage: document.getElementById("profileMessage"),
    chatMessages: document.getElementById("chatMessages"),
    chatForm: document.getElementById("chatForm"),
    chatInput: document.getElementById("chatInput"),
    chatMessage: document.getElementById("chatMessage"),
    // Forgot Password elements
    forgotPasswordForm: document.getElementById("forgotPasswordForm"),
    resetPasswordForm: document.getElementById("resetPasswordForm"),
    forgotEmail: document.getElementById("forgotEmail"),
    resetOtp: document.getElementById("resetOtp"),
    resetPassword: document.getElementById("resetPassword"),
    forgotPasswordBtn: document.getElementById("forgotPasswordBtn"),
    backToLoginFromForgot: document.getElementById("backToLoginFromForgot"),
    backToLoginFromReset: document.getElementById("backToLoginFromReset"),
    // Onboarding elements
    onboardingView: document.getElementById("onboardingView"),
    onboardingProgress: document.getElementById("onboardingProgress"),
    onboardStudentInst: document.getElementById("onboardStudentInst"),
    onboardStudentYear: document.getElementById("onboardStudentYear"),
    onboardStudentAllowance: document.getElementById("onboardStudentAllowance"),
    onboardProfCompany: document.getElementById("onboardProfCompany"),
    onboardProfJob: document.getElementById("onboardProfJob"),
    onboardProfIncome: document.getElementById("onboardProfIncome"),
    onboardHomeSize: document.getElementById("onboardHomeSize"),
    onboardHomeBudget: document.getElementById("onboardHomeBudget"),
    obCatGroceries: document.getElementById("obCatGroceries"),
    obCatUtilities: document.getElementById("obCatUtilities"),
    obCatEducation: document.getElementById("obCatEducation"),
    obCatHealthcare: document.getElementById("obCatHealthcare"),
    onboardPersonalIncome: document.getElementById("onboardPersonalIncome"),
    onboardPersonalGoal: document.getElementById("onboardPersonalGoal"),
    onboardAllowNotifications: document.getElementById("onboardAllowNotifications"),
    onboardNotNowNotifications: document.getElementById("onboardNotNowNotifications"),
    onboardBudgetList: document.getElementById("onboardBudgetList"),
    onboardFinishBtn: document.getElementById("onboardFinishBtn"),
    onboardBackBtn: document.getElementById("onboardBackBtn"),
    onboardNextBtn: document.getElementById("onboardNextBtn"),
    onboardingFooter: document.getElementById("onboardingFooter"),
    // Overspending warning modal elements
    overspendingWarningModal: document.getElementById("overspendingWarningModal"),
    warningCategoryName: document.getElementById("warningCategoryName"),
    warningExpenseAmount: document.getElementById("warningExpenseAmount"),
    warningBudgetRemaining: document.getElementById("warningBudgetRemaining"),
    warningCancelBtn: document.getElementById("warningCancelBtn"),
    warningProceedBtn: document.getElementById("warningProceedBtn"),
    // Alerts and Tips elements
    alertsList: document.getElementById("alertsList"),
    // Search and filter elements
    transactionSearch: document.getElementById("transactionSearch"),
    transactionTypeFilter: document.getElementById("transactionTypeFilter"),
    transactionCategoryFilter: document.getElementById("transactionCategoryFilter")
};

function money(value) {
    let curr = state.user?.currency || "INR";
    if (curr === "USD") curr = "INR";
    if (curr.length > 3 || curr === "RUPPEE") curr = "INR";
    try {
        return new Intl.NumberFormat(curr === "INR" ? "en-IN" : "en-US", {
            style: "currency",
            currency: curr
        }).format(Number(value || 0));
    } catch (e) {
        return "₹" + Number(value || 0).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
}

function dateLabel(value) {
    if (!value) return "No date";
    return new Intl.DateTimeFormat("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric"
    }).format(new Date(value));
}

function initials(name) {
    return String(name || "MoneyMap")
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("");
}

function setMessage(element, text, type = "error") {
    element.textContent = text || "";
    element.classList.toggle("success", type === "success");
}

async function request(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers
    });

    let body = null;
    try {
        body = await response.json();
    } catch {
        body = null;
    }

    if (!response.ok) {
        const message = body?.message || body?.error || `Request failed with ${response.status}`;
        throw new Error(Array.isArray(message) ? message.join(", ") : message);
    }

    return body?.data ?? body;
}

function saveSession(payload) {
    state.token = payload.accessToken;
    state.refreshToken = payload.refreshToken;
    state.user = payload.user;
    localStorage.setItem("moneymap_access_token", payload.accessToken);
    localStorage.setItem("moneymap_refresh_token", payload.refreshToken);
    localStorage.setItem("moneymap_user", JSON.stringify(payload.user));
}

function clearSession() {
    state.token = null;
    state.refreshToken = null;
    state.user = null;
    localStorage.removeItem("moneymap_access_token");
    localStorage.removeItem("moneymap_refresh_token");
    localStorage.removeItem("moneymap_user");
}

function showApp() {
    elements.authView.classList.add("hidden");
    elements.appView.classList.remove("hidden");
    elements.floatingAssistantButton.classList.remove("hidden");
    elements.userName.textContent = state.user?.name || "MoneyMap user";
    elements.userEmail.textContent = state.user?.email || "";
    elements.userInitials.textContent = initials(state.user?.name);
}

function showAuth() {
    elements.appView.classList.add("hidden");
    elements.authView.classList.remove("hidden");
    elements.floatingAssistantButton.classList.add("hidden");
}

function switchAuth(mode) {
    elements.loginForm.classList.toggle("hidden", mode !== "login");
    elements.signupForm.classList.toggle("hidden", mode !== "signup");
    elements.forgotPasswordForm.classList.toggle("hidden", mode !== "forgot");
    elements.resetPasswordForm.classList.toggle("hidden", mode !== "reset");

    const isMainAuth = mode === "login" || mode === "signup";
    document.querySelector(".auth-tabs").classList.toggle("hidden", !isMainAuth);
    if (isMainAuth) {
        elements.signupTab.classList.toggle("active", mode === "signup");
        elements.loginTab.classList.toggle("active", mode === "login");
    }

    const googleContainer = document.getElementById("googleAuthContainer");
    if (googleContainer) {
        googleContainer.classList.toggle("hidden", !isMainAuth);
    }

    setMessage(elements.authMessage, "");
}

async function loadAppData() {
    const [
        dashboard,
        categories,
        transactions,
        trends,
        monthly,
        weekly,
        budgets,
        budgetSummary,
        goals,
        subscriptions,
        profile,
        allExpenses
    ] = await Promise.all([
        request("/reports/dashboard"),
        request("/categories"),
        request("/transactions?limit=20"),
        request("/reports/trends"),
        request("/reports/monthly"),
        request("/reports/weekly"),
        request("/budgets"),
        request("/budgets/summary"),
        request("/savings-goals"),
        request("/subscriptions"),
        request("/users/profile"),
        request("/transactions?type=EXPENSE&limit=1000")
    ]);

    state.dashboard = dashboard;
    state.categories = categories || [];
    state.transactions = transactions?.transactions || [];
    state.trends = trends || [];
    state.monthlyReport = monthly;
    state.weeklyReport = weekly;
    state.budgets = budgets || [];
    state.budgetSummary = budgetSummary;
    state.goals = goals || [];
    state.subscriptions = subscriptions || [];
    state.profile = profile;
    state.allExpenses = allExpenses?.transactions || [];

    renderDashboard();
    renderCategories();
    renderTransactions();
    renderBudgetSummary();
    renderWeeklyBudgetBreakdown();
    renderGoals();
    renderSubscriptions();
    renderProfile();
    renderTrends(monthly?.breakdown || []);
    renderReports();
    renderAlerts();
}

function checkOnboarding() {
    const profileCompleted = state.profile?.profile?.onboardingCompleted;
    if (profileCompleted === false) {
        elements.appView.classList.add("hidden");
        elements.authView.classList.add("hidden");
        elements.onboardingView.classList.remove("hidden");
        elements.floatingAssistantButton.classList.add("hidden");
        startOnboarding();
        return false;
    } else {
        elements.onboardingView.classList.add("hidden");
        return true;
    }
}

let onboardingState = {
    step: 1,
    role: "",
    notifications: false
};

function startOnboarding() {
    onboardingState.step = 1;
    onboardingState.role = "";
    onboardingState.notifications = false;
    
    // Reset cards selection
    document.querySelectorAll(".role-option-card").forEach(c => {
        c.classList.remove("selected");
    });
    elements.onboardNextBtn.disabled = true;
    elements.onboardBackBtn.disabled = true;
    
    showOnboardStep(1);
}

function showOnboardStep(step) {
    onboardingState.step = step;
    
    // Hide all step panels
    document.querySelectorAll(".onboarding-step").forEach(s => {
        s.classList.add("hidden");
    });
    
    // Show current step panel
    const stepEl = document.getElementById("onboardStep" + step);
    if (stepEl) stepEl.classList.remove("hidden");
    
    // Update progress bar width
    elements.onboardingProgress.style.width = (step * 20) + "%";
    
    // Step configuration
    if (step === 1) {
        elements.onboardingFooter.classList.remove("hidden");
        elements.onboardBackBtn.disabled = true;
        elements.onboardNextBtn.disabled = !onboardingState.role;
    } else if (step === 2) {
        elements.onboardingFooter.classList.remove("hidden");
        elements.onboardBackBtn.disabled = false;
        elements.onboardNextBtn.disabled = false;
        
        // Show fields based on role
        document.getElementById("roleFieldsStudent").classList.add("hidden");
        document.getElementById("roleFieldsProfessional").classList.add("hidden");
        document.getElementById("roleFieldsHomemaker").classList.add("hidden");
        document.getElementById("roleFieldsPersonal").classList.add("hidden");
        
        if (onboardingState.role === "STUDENT") {
            document.getElementById("roleFieldsStudent").classList.remove("hidden");
        } else if (onboardingState.role === "PROFESSIONAL") {
            document.getElementById("roleFieldsProfessional").classList.remove("hidden");
        } else if (onboardingState.role === "HOMEMAKER") {
            document.getElementById("roleFieldsHomemaker").classList.remove("hidden");
        } else {
            document.getElementById("roleFieldsPersonal").classList.remove("hidden");
        }
    } else if (step === 3) {
        // Hide standard footer
        elements.onboardingFooter.classList.add("hidden");
    } else if (step === 4) {
        elements.onboardingFooter.classList.remove("hidden");
        elements.onboardBackBtn.disabled = false;
        elements.onboardNextBtn.disabled = false;
        
        // Dynamically build starting budget list
        renderOnboardBudgets();
    } else if (step === 5) {
        // Hide standard footer
        elements.onboardingFooter.classList.add("hidden");
    }
}

function renderOnboardBudgets() {
    const list = elements.onboardBudgetList;
    if (!state.categories.length) {
        list.innerHTML = `<div class="empty-state">No categories available to set budget.</div>`;
        return;
    }
    
    let budgetCategories = [];
    if (onboardingState.role === "HOMEMAKER") {
        const selectedNames = [];
        if (elements.obCatGroceries.checked) selectedNames.push("groceries");
        if (elements.obCatUtilities.checked) selectedNames.push("utilities");
        if (elements.obCatEducation.checked) selectedNames.push("education");
        if (elements.obCatHealthcare.checked) selectedNames.push("healthcare");
        
        budgetCategories = state.categories.filter(c => {
            const nameLower = c.name.toLowerCase();
            return selectedNames.includes(nameLower);
        });
    } else {
        budgetCategories = state.categories.filter(c => {
            const nameLower = c.name.toLowerCase();
            return nameLower !== "income" && nameLower !== "salary";
        }).slice(0, 4);
    }
    
    list.innerHTML = budgetCategories.map(cat => {
        let defaultLimit = 0;
        const nameLower = cat.name.toLowerCase();
        if (nameLower === "food" || nameLower === "groceries") defaultLimit = 3000;
        else if (nameLower === "transport" || nameLower === "transportation" || nameLower === "utilities") defaultLimit = 1500;
        else if (nameLower === "shopping" || nameLower === "healthcare") defaultLimit = 2000;
        else if (nameLower === "education") defaultLimit = 5000;
        
        return `
            <div style="display:flex; align-items:center; justify-content:space-between; background:var(--surface-soft); padding:14px 18px; border-radius:16px; border:1px solid var(--border);">
                <div style="display:flex; align-items:center; gap:12px;">
                    <span style="width:10px; height:10px; border-radius:50%; background:${cat.color}"></span>
                    <strong>${cat.name}</strong>
                </div>
                <div style="width: 140px;">
                    <input class="onboard-budget-input" type="number" min="0" data-cat-id="${cat.id}" value="${defaultLimit || ''}" placeholder="0" style="height: 38px; border-radius: 10px;">
                </div>
            </div>
        `;
    }).join("");
}

async function saveOnboardingData() {
    elements.onboardFinishBtn.textContent = "Saving...";
    elements.onboardFinishBtn.disabled = true;
    
    try {
        const role = onboardingState.role;
        const profilePayload = {
            onboardingCompleted: true
        };
        
        if (role === "STUDENT") {
            profilePayload.institution = elements.onboardStudentInst.value.trim() || undefined;
            profilePayload.yearOfStudy = elements.onboardStudentYear.value;
            profilePayload.monthlyAllowance = elements.onboardStudentAllowance.value ? Number(elements.onboardStudentAllowance.value) : undefined;
        } else if (role === "PROFESSIONAL") {
            profilePayload.companyName = elements.onboardProfCompany.value.trim() || undefined;
            profilePayload.jobTitle = elements.onboardProfJob.value.trim() || undefined;
            profilePayload.monthlyIncome = elements.onboardProfIncome.value ? Number(elements.onboardProfIncome.value) : undefined;
        } else if (role === "HOMEMAKER") {
            profilePayload.householdSize = elements.onboardHomeSize.value ? Number(elements.onboardHomeSize.value) : undefined;
            profilePayload.monthlyBudget = elements.onboardHomeBudget.value ? Number(elements.onboardHomeBudget.value) : undefined;
            
            const catsSelected = [];
            if (elements.obCatGroceries.checked) catsSelected.push("Groceries");
            if (elements.obCatUtilities.checked) catsSelected.push("Utilities");
            if (elements.obCatEducation.checked) catsSelected.push("Education");
            if (elements.obCatHealthcare.checked) catsSelected.push("Healthcare");
            profilePayload.primaryCategories = catsSelected;
        } else {
            profilePayload.monthlyIncome = elements.onboardPersonalIncome.value ? Number(elements.onboardPersonalIncome.value) : undefined;
            profilePayload.financialGoal = elements.onboardPersonalGoal.value.trim() || undefined;
        }
        
        // 1. Update user settings (updates role too!)
        await request("/users/settings", {
            method: "PATCH",
            body: JSON.stringify({
                role: role,
                notificationsEnabled: onboardingState.notifications,
                budgetAlerts: onboardingState.notifications,
                weeklyReport: onboardingState.notifications,
                monthlyReport: onboardingState.notifications
            })
        });
        
        // 2. Update user profile details
        await request("/users/profile", {
            method: "PATCH",
            body: JSON.stringify(profilePayload)
        });
        
        // 3. Post budgets
        const budgetInputs = document.querySelectorAll(".onboard-budget-input");
        const month = new Date().getMonth() + 1;
        const year = new Date().getFullYear();
        
        for (const input of budgetInputs) {
            const amount = Number(input.value);
            if (amount > 0) {
                try {
                    await request("/budgets", {
                        method: "POST",
                        body: JSON.stringify({
                            categoryId: input.dataset.catId,
                            amount: amount,
                            month: month,
                            year: year
                        })
                    });
                } catch (e) {
                    console.error("Failed to save onboarding budget", e);
                }
            }
        }
        
        // Reload user data and show dashboard
        state.user.role = role;
        localStorage.setItem("moneymap_user", JSON.stringify(state.user));
        
        elements.onboardingView.classList.add("hidden");
        showApp();
        await loadAppData();
        switchView("dashboard");
    } catch (e) {
        console.error("Failed to complete onboarding", e);
        alert("Error saving onboarding details: " + e.message);
        elements.onboardFinishBtn.textContent = "Go to Dashboard";
        elements.onboardFinishBtn.disabled = false;
    }
}

function renderAlerts() {
    const list = elements.alertsList;
    if (!list) return;
    
    const alerts = state.dashboard?.alerts || [];
    list.innerHTML = alerts.map(alert => `
        <article class="alert-item ${alert.isCritical ? 'critical' : 'warning'}">
            <div class="alert-badge">
                ${alert.isCritical ? '⚠️' : '🔔'}
            </div>
            <div class="alert-content">
                <div class="alert-header">
                    <strong>${alert.title}</strong>
                    <span>${alert.time}</span>
                </div>
                <p class="alert-msg">${alert.message}</p>
                ${alert.isCritical ? `<div style="margin-top: 10px;"><button class="text-button" type="button" data-view-link="budgets" style="padding:0; font-size:13px; font-weight:700;">Adjust Budget</button></div>` : ''}
            </div>
        </article>
    `).join("");

    // Hook up view links inside dynamic list
    list.querySelectorAll("[data-view-link]").forEach((button) => {
        button.addEventListener("click", () => switchView(button.dataset.viewLink));
    });
}

function getGreeting() {
    const hour = new Date().getHours();
    if (hour < 12) return "Good Morning";
    if (hour < 17) return "Good Afternoon";
    return "Good Evening";
}

function renderDashboard() {
    const data = state.dashboard || {};
    const role = state.profile?.role || state.user?.role || "PERSONAL";
    
    // Greeting
    elements.dashboardGreeting.textContent = getGreeting();
    if (role === "STUDENT") {
        elements.dashboardName.textContent = (state.user?.name || "User") + " 👋";
    } else {
        elements.dashboardName.textContent = state.user?.name || "MoneyMap User";
    }

    const totalBudget = (data.budgets || []).reduce((acc, curr) => acc + Number(curr.limit || 0), 0);
    const monthlySpent = Number(data.monthlySpent || 0);
    const monthlyIncome = Number(data.monthlyIncome || 0);
    const netSavings = Number(data.netSavings || 0);
    const profSalary = Number(state.profile?.profile?.monthlyIncome || 0);

    // Dynamic Balance Card
    if (role === "STUDENT") {
        const allowance = Number(state.profile?.profile?.monthlyAllowance || 5000);
        elements.balanceCardTitle.textContent = "Student Allowance";
        elements.balanceCardAmount.textContent = money(allowance + monthlyIncome);
        elements.balanceCardSpent.textContent = money(monthlySpent);
        elements.balanceCardRightTitle.textContent = "Saved";
        elements.balanceCardRightAmount.textContent = money(Math.max(0, allowance + monthlyIncome - monthlySpent));
    } else if (role === "PROFESSIONAL") {
        elements.balanceCardTitle.textContent = "Available Balance";
        elements.balanceCardAmount.textContent = money(Math.max(0, profSalary + monthlyIncome - monthlySpent));
        elements.balanceCardSpent.textContent = money(monthlySpent);
        elements.balanceCardRightTitle.textContent = "Salary";
        elements.balanceCardRightAmount.textContent = money(profSalary);
    } else if (role === "HOMEMAKER") {
        const homemakerBudget = Number(state.profile?.profile?.monthlyBudget || 25000);
        elements.balanceCardTitle.textContent = "Household Budget";
        elements.balanceCardAmount.textContent = money(homemakerBudget);
        elements.balanceCardSpent.textContent = money(monthlySpent);
        elements.balanceCardRightTitle.textContent = "Remaining";
        elements.balanceCardRightAmount.textContent = money(Math.max(0, homemakerBudget + monthlyIncome - monthlySpent));
    } else {
        // PERSONAL / General
        const hasBudget = totalBudget > 0;
        const headlineAmount = hasBudget ? totalBudget : monthlyIncome;
        const remaining = hasBudget ? (totalBudget + monthlyIncome - monthlySpent) : netSavings;

        elements.balanceCardTitle.textContent = hasBudget ? "Monthly Budget" : "This Month";
        elements.balanceCardAmount.textContent = money(headlineAmount);
        elements.balanceCardSpent.textContent = money(monthlySpent);
        elements.balanceCardRightTitle.textContent = hasBudget ? "Remaining" : "Saved";
        elements.balanceCardRightAmount.textContent = money(Math.max(0, remaining));
    }

    // Dynamic Tools Section
    const toolsGrid = document.getElementById("toolsGrid");
    const toolsSectionTitle = document.getElementById("toolsSectionTitle");
    if (toolsGrid && toolsSectionTitle) {
        if (role === "STUDENT") {
            toolsSectionTitle.textContent = "Quick Actions";
            toolsGrid.style.gridTemplateColumns = "1fr 1fr";
            toolsGrid.innerHTML = `
                <article class="tool-card blue" data-view-link="transactions">
                    <div class="tool-icon">💸</div>
                    <strong>Add Expense</strong>
                </article>
                <article class="tool-card pink" data-view-link="budgets" style="background:#ede9fe;">
                    <div class="tool-icon" style="background:#ddd6fe; color:#8b5cf6;">📊</div>
                    <strong>Budget Limits</strong>
                </article>
            `;
        } else if (role === "PROFESSIONAL") {
            toolsSectionTitle.textContent = "Professional Dashboard Stats";
            toolsGrid.style.gridTemplateColumns = "1fr 1fr";
            // Render Stats Row inside toolsGrid
            const budgetLimitVal = totalBudget || profSalary || 1;
            const pct = Math.min(100, Math.round((monthlySpent / budgetLimitVal) * 100));
            const savedAmount = monthlyIncome > 0 ? netSavings : (profSalary - monthlySpent);
            toolsGrid.innerHTML = `
                <div class="stats-card">
                    <div class="stats-icon-box" style="background:#dcfce7; color:#10b981;">📈</div>
                    <div class="stats-info">
                        <span>Saved This Month</span>
                        <strong>${money(Math.max(0, savedAmount))}</strong>
                    </div>
                </div>
                <div class="stats-card">
                    <div class="stats-icon-box" style="background:#f3e8ff; color:#a855f7;">📊</div>
                    <div class="stats-info">
                        <span>Budget Used</span>
                        <strong>${pct}%</strong>
                    </div>
                </div>
            `;
        } else {
            // HOMEMAKER and PERSONAL/Default
            toolsSectionTitle.textContent = role === "HOMEMAKER" ? "Household Tools" : "Tools";
            toolsGrid.style.gridTemplateColumns = "repeat(3, 1fr)";
            toolsGrid.innerHTML = `
                <article class="tool-card blue" data-view-link="transactions">
                    <div class="tool-icon">💸</div>
                    <strong>Add Expense</strong>
                </article>
                <article class="tool-card green" data-view-link="budgets">
                    <div class="tool-icon">🛒</div>
                    <strong>Grocery List</strong>
                </article>
                <article class="tool-card pink" data-view-link="goals">
                    <div class="tool-icon">👨‍👩‍👧‍👦</div>
                    <strong>Family Allowances</strong>
                </article>
            `;
        }

        // Re-bind view links inside dynamically rendered toolsGrid
        toolsGrid.querySelectorAll("[data-view-link]").forEach((button) => {
            button.addEventListener("click", () => switchView(button.dataset.viewLink));
        });
    }

    renderTransactionList(elements.recentTransactions, data.recentTransactions || [], true);
    renderBudgets(data.budgets || []);
}

function renderTransactionList(container, transactions, compact = false) {
    if (!transactions.length) {
        container.innerHTML = `<div class="empty-state">No transactions yet. Add your first income or expense.</div>`;
        return;
    }

    container.innerHTML = transactions.map((transaction) => {
        const isIncome = transaction.type === "INCOME";
        const color = transaction.color || transaction.category?.color || (isIncome ? "#059669" : "#2563eb");
        const category = transaction.category?.name || transaction.category || "General";
        const description = transaction.description || category;
        const amountClass = isIncome ? "income" : "expense";
        const sign = isIncome ? "+" : "-";
        const when = dateLabel(transaction.transactionDate);

        return `
            <article class="transaction-item">
                <span class="transaction-icon" style="background:${color}">${category[0] || "T"}</span>
                <div>
                    <strong>${description}</strong>
                    <small>${category}${compact ? "" : ` · ${when}`}</small>
                    ${compact ? "" : `
                        <div class="item-actions" style="display: flex; gap: 8px;">
                            <button class="edit-button" type="button" data-tx-edit="${transaction.id}" style="background: #e2e8f0; color: #475569; border: none; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.2s;">Edit</button>
                            <button class="danger-button" type="button" data-tx-delete="${transaction.id}">Delete</button>
                        </div>
                    `}
                </div>
                <span class="amount ${amountClass}">${sign}${money(transaction.amount)}</span>
            </article>
        `;
    }).join("");
}

function renderTransactions() {
    let filtered = [...(state.transactions || [])];

    // Filter by Search Query
    const searchVal = elements.transactionSearch?.value.trim().toLowerCase();
    if (searchVal) {
        filtered = filtered.filter(t => 
            (t.description || "").toLowerCase().includes(searchVal) ||
            (t.category?.name || t.category || "").toLowerCase().includes(searchVal)
        );
    }

    // Filter by Type
    const typeVal = elements.transactionTypeFilter?.value;
    if (typeVal && typeVal !== "ALL") {
        filtered = filtered.filter(t => t.type === typeVal);
    }

    // Filter by Category
    const categoryVal = elements.transactionCategoryFilter?.value;
    if (categoryVal && categoryVal !== "ALL") {
        filtered = filtered.filter(t => {
            const catId = t.categoryId || t.category?.id;
            return catId === categoryVal;
        });
    }

    renderTransactionList(elements.transactionList, filtered);
}

function renderBudgetSummary() {
    const summary = state.budgetSummary;
    const budgets = state.budgets || [];

    if (!summary || !budgets.length) {
        elements.budgetSummaryList.innerHTML = `<div class="empty-state">No budgets set for this month yet.</div>`;
        return;
    }

    const utilization = Math.round(summary.overallUtilization || 0);
    elements.budgetSummaryList.innerHTML = `
        <article class="budget-item">
            <div class="budget-head">
                <span>Total budgeted</span>
                <span>${money(summary.totalBudgeted)}</span>
            </div>
            <small>Spent ${money(summary.totalSpent)} · Remaining ${money(summary.remainingBudget)}</small>
            <div class="progress-track">
                <div class="progress-fill" style="width:${Math.min(100, utilization)}%"></div>
            </div>
        </article>
        ${budgets.map((budget) => {
            const percent = Math.min(100, Math.round(budget.utilizationPercentage || 0));
            return `
                <article class="budget-item">
                    <div class="budget-head">
                        <span>${budget.categoryName}</span>
                        <span>${percent}%</span>
                    </div>
                    <div class="progress-track">
                        <div class="progress-fill" style="width:${percent}%;background:${budget.color || "#2563eb"}"></div>
                    </div>
                    <small>${money(budget.spent)} of ${money(budget.limit)}</small>
                </article>
            `;
        }).join("")}
    `;
}

function renderWeeklyBudgetBreakdown() {
    const totalMonthly = state.budgetSummary?.totalBudgeted || 0;
    const expenses = state.allExpenses || [];

    const maxDays = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate();
    const currentDay = new Date().getDate();
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();

    const currentMonthExpenses = expenses.filter(t => {
        const d = new Date(t.transactionDate);
        return d.getMonth() === currentMonth && d.getFullYear() === currentYear;
    });

    const getDayFromDate = (dateStr) => {
        return new Date(dateStr).getDate();
    };

    const week1Spent = currentMonthExpenses.filter(t => getDayFromDate(t.transactionDate) >= 1 && getDayFromDate(t.transactionDate) <= 7).reduce((sum, t) => sum + Number(t.amount), 0);
    const week2Spent = currentMonthExpenses.filter(t => getDayFromDate(t.transactionDate) >= 8 && getDayFromDate(t.transactionDate) <= 14).reduce((sum, t) => sum + Number(t.amount), 0);
    const week3Spent = currentMonthExpenses.filter(t => getDayFromDate(t.transactionDate) >= 15 && getDayFromDate(t.transactionDate) <= 21).reduce((sum, t) => sum + Number(t.amount), 0);
    const week4Spent = currentMonthExpenses.filter(t => getDayFromDate(t.transactionDate) >= 22 && getDayFromDate(t.transactionDate) <= 28).reduce((sum, t) => sum + Number(t.amount), 0);
    const week5Spent = currentMonthExpenses.filter(t => getDayFromDate(t.transactionDate) >= 29).reduce((sum, t) => sum + Number(t.amount), 0);

    const week1Limit = totalMonthly * (7 / maxDays);
    const week2Limit = totalMonthly * (7 / maxDays);
    const week3Limit = totalMonthly * (7 / maxDays);
    const week4Limit = totalMonthly * (7 / maxDays);
    const week5Limit = maxDays > 28 ? totalMonthly * ((maxDays - 28) / maxDays) : 0;

    const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    const monthName = monthNames[currentMonth];

    const weeks = [
        { name: `Week 1 (${monthName} 1 - ${monthName} 7)`, spent: week1Spent, limit: week1Limit, active: currentDay >= 1 && currentDay <= 7, color: "#10b981" },
        { name: `Week 2 (${monthName} 8 - ${monthName} 14)`, spent: week2Spent, limit: week2Limit, active: currentDay >= 8 && currentDay <= 14, color: "#3b82f6" },
        { name: `Week 3 (${monthName} 15 - ${monthName} 21)`, spent: week3Spent, limit: week3Limit, active: currentDay >= 15 && currentDay <= 21, color: "#8b5cf6" },
        { name: `Week 4 (${monthName} 22 - ${monthName} 28)`, spent: week4Spent, limit: week4Limit, active: currentDay >= 22 && currentDay <= 28, color: "#ec4899" }
    ];

    if (maxDays > 28) {
        weeks.push({ name: `Week 5 (${monthName} 29 - ${monthName} ${maxDays})`, spent: week5Spent, limit: week5Limit, active: currentDay >= 29, color: "#f59e0b" });
    }

    if (!totalMonthly) {
        elements.weeklyBudgetList.innerHTML = `<div class="empty-state">Please set a monthly budget first to view weekly breakdowns.</div>`;
        return;
    }

    elements.weeklyBudgetList.innerHTML = weeks.map(w => {
        const percent = w.limit > 0 ? Math.min(100, Math.round((w.spent / w.limit) * 100)) : 0;
        return `
            <article class="budget-item" style="${w.active ? 'background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 12px; margin-bottom: 12px;' : 'margin-bottom: 12px;'}">
                <div class="budget-head">
                    <span style="font-weight: ${w.active ? 'bold' : 'normal'}; color: ${w.active ? '#1e40af' : 'inherit'}">
                        ${w.name} ${w.active ? '<span style="margin-left: 8px; font-size: 11px; font-weight: bold; background: #dbeafe; color: #1e40af; padding: 2px 8px; border-radius: 8px;">Active</span>' : ''}
                    </span>
                    <span style="font-weight: ${w.active ? 'bold' : 'normal'}; color: ${w.active ? '#1e40af' : 'inherit'}">${percent}%</span>
                </div>
                <div class="progress-track" style="background: ${w.active ? '#dbeafe' : '#e2e8f0'}">
                    <div class="progress-fill" style="width:${percent}%;background:${w.color}"></div>
                </div>
                <small style="color: ${w.active ? '#1e40af' : '#64748b'}">${money(w.spent)} of ${money(w.limit)}</small>
            </article>
        `;
    }).join("");
}

function renderBudgets(budgets) {
    if (!budgets.length) {
        elements.budgetList.innerHTML = `<div class="empty-state">No budgets yet. Your category spending will appear here.</div>`;
        return;
    }

    elements.budgetList.innerHTML = budgets.map((budget) => {
        const percent = Math.min(100, Math.round(budget.utilizationPercentage || 0));
        return `
            <article class="budget-item">
                <div class="budget-head">
                    <span>${budget.categoryName}</span>
                    <span>${percent}%</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill" style="width:${percent}%;background:${budget.color || "#2563eb"}"></div>
                </div>
                <small>${money(budget.spent)} of ${money(budget.limit)}</small>
            </article>
        `;
    }).join("");
}

function renderCategories() {
    if (!state.categories.length) {
        elements.categoryList.innerHTML = `<div class="empty-state">No categories found. Create one to start adding transactions.</div>`;
        elements.transactionCategory.innerHTML = `<option value="">Create a category first</option>`;
        elements.budgetCategory.innerHTML = `<option value="">Create a category first</option>`;
        if (elements.transactionCategoryFilter) {
            elements.transactionCategoryFilter.innerHTML = `<option value="ALL">All Categories</option>`;
        }
        return;
    }

    elements.categoryList.innerHTML = state.categories.map((category) => `
        <article class="category-item">
            <span class="color-dot" style="background:${category.color}"></span>
            ${category.name}
        </article>
    `).join("");

    elements.transactionCategory.innerHTML = state.categories.map((category) => `
        <option value="${category.id}">${category.name}</option>
    `).join("");

    elements.budgetCategory.innerHTML = state.categories.map((category) => `
        <option value="${category.id}">${category.name}</option>
    `).join("");

    if (elements.editTransactionCategory) {
        elements.editTransactionCategory.innerHTML = state.categories.map((category) => `
            <option value="${category.id}">${category.name}</option>
        `).join("");
    }

    if (elements.transactionCategoryFilter) {
        elements.transactionCategoryFilter.innerHTML = `
            <option value="ALL">All Categories</option>
            ${state.categories.map((category) => `
                <option value="${category.id}">${category.name}</option>
            `).join("")}
        `;
    }
}

function renderGoals() {
    if (!state.goals.length) {
        elements.goalList.innerHTML = `<div class="empty-state">No savings goals yet. Add a goal to track progress.</div>`;
        return;
    }

    elements.goalList.innerHTML = state.goals.map((goal) => {
        const percent = Math.min(100, Math.round(goal.progressPercentage || 0));
        return `
            <article class="budget-item">
                <div class="budget-head">
                    <span>${escapeHtml(goal.name)}</span>
                    <span>${percent}%</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill" style="width:${percent}%;background:${goal.color || "#059669"}"></div>
                </div>
                <small>${money(goal.currentAmount)} of ${money(goal.targetAmount)}${goal.targetDate ? ` · ${dateLabel(goal.targetDate)}` : ""}</small>
                <div class="item-actions">
                    <button class="small-button" type="button" data-goal-add="${goal.id}">Add ${money(50)}</button>
                    <button class="danger-button" type="button" data-goal-delete="${goal.id}">Delete</button>
                </div>
            </article>
        `;
    }).join("");
}

function renderSubscriptions() {
    if (!state.subscriptions.length) {
        elements.subscriptionList.innerHTML = `<div class="empty-state">No subscriptions yet. Add recurring bills to track upcoming payments.</div>`;
        return;
    }

    elements.subscriptionList.innerHTML = state.subscriptions.map((sub) => `
        <article class="budget-item">
            <div class="budget-head">
                <span>${escapeHtml(sub.name)}</span>
                <span>${money(sub.amount)}</span>
            </div>
            <small>${sub.billingCycle} · Next billing ${dateLabel(sub.nextBillingDate)} · ${sub.isActive ? "Active" : "Paused"}</small>
            <div class="item-actions">
                <button class="small-button" type="button" data-sub-toggle="${sub.id}" data-active="${sub.isActive}">${sub.isActive ? "Pause" : "Activate"}</button>
                <button class="danger-button" type="button" data-sub-delete="${sub.id}">Delete</button>
            </div>
        </article>
    `).join("");
}

function renderProfile() {
    if (!state.profile) return;

    const profile = state.profile.profile || {};
    elements.settingsName.value = state.profile.name || "";
    elements.settingsCurrency.value = state.profile.currency || "INR";
    elements.settingsNotifications.checked = Boolean(profile.notificationsEnabled);
    elements.profileInstitution.value = profile.institution || "";
    elements.profileCompany.value = profile.companyName || "";
    elements.profileJobTitle.value = profile.jobTitle || "";
    elements.profileMonthlyIncome.value = profile.monthlyIncome ? Number(profile.monthlyIncome) : "";
    elements.profileFinancialGoal.value = profile.financialGoal || "";
}

function renderReports() {
    const weekly = state.weeklyReport;
    const monthly = state.monthlyReport;

    if (!weekly) {
        elements.weeklyReportList.innerHTML = `<div class="empty-state">Weekly report unavailable.</div>`;
    } else {
        elements.weeklyReportList.innerHTML = `
            <article class="budget-item">
                <div class="budget-head"><span>Total spent</span><span>${money(weekly.totalSpent)}</span></div>
                <small>Daily average ${money(weekly.averageDailySpent)}</small>
            </article>
            ${(weekly.breakdown || []).slice(0, 4).map((item) => `
                <article class="budget-item">
                    <div class="budget-head"><span>${item.category}</span><span>${Math.round(item.percentage)}%</span></div>
                    <small>${money(item.amount)}</small>
                </article>
            `).join("")}
        `;
    }

    if (!monthly) {
        elements.monthlyReportList.innerHTML = `<div class="empty-state">Monthly report unavailable.</div>`;
    } else {
        elements.monthlyReportList.innerHTML = `
            <article class="budget-item">
                <div class="budget-head"><span>This month</span><span>${money(monthly.totalSpent)}</span></div>
                <small>Previous month ${money(monthly.previousMonthSpent)} · Change ${Math.round(monthly.percentageChange || 0)}%</small>
            </article>
        `;
    }
}

function renderTrends(breakdown) {
    if (!state.trends.length) {
        elements.trendChart.innerHTML = `<div class="empty-state">Trend data will appear after transactions are recorded.</div>`;
    } else {
        const max = Math.max(...state.trends.map((item) => item.income || item.expenses || 1), 1);
        elements.trendChart.innerHTML = state.trends.map((item) => {
            const height = Math.max(18, Math.round(((item.expenses || item.income || 0) / max) * 220));
            return `
                <div class="trend-bar">
                    <span style="height:${height}px"></span>
                    <small>${item.monthName}</small>
                </div>
            `;
        }).join("");
    }

    if (!breakdown.length) {
        elements.breakdownList.innerHTML = `<div class="empty-state">No monthly spending breakdown yet.</div>`;
        return;
    }

    elements.breakdownList.innerHTML = breakdown.map((item) => `
        <article class="budget-item">
            <div class="budget-head">
                <span>${item.category}</span>
                <span>${Math.round(item.percentage)}%</span>
            </div>
            <div class="progress-track">
                <div class="progress-fill" style="width:${Math.min(100, item.percentage)}%;background:${item.color || "#2563eb"}"></div>
            </div>
            <small>${money(item.amount)}</small>
        </article>
    `).join("");
}

function renderChat() {
    if (!state.chatMessages.length) {
        elements.chatMessages.innerHTML = `
            <div class="empty-state">
                Ask the assistant about your spending, budget limits, savings goals, or simple ways to improve your month.
            </div>
        `;
        return;
    }

    elements.chatMessages.innerHTML = state.chatMessages.map((message) => `
        <div class="chat-bubble ${message.isUser ? "user" : "assistant"}">${escapeHtml(message.content)}</div>
    `).join("");
    elements.chatMessages.scrollTop = elements.chatMessages.scrollHeight;
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function sendAssistantMessage(content) {
    const text = content.trim();
    if (!text) return;

    setMessage(elements.chatMessage, "Thinking...", "success");
    state.chatMessages.push({ isUser: true, content: text });
    renderChat();
    elements.chatInput.value = "";

    try {
        const response = await request("/chatbot/message", {
            method: "POST",
            body: JSON.stringify({
                sessionId: state.chatSessionId || undefined,
                content: text
            })
        });

        state.chatSessionId = response.sessionId;
        state.chatMessages.push({
            isUser: false,
            content: response.message?.content || "I could not generate a response right now."
        });
        setMessage(elements.chatMessage, "");
        renderChat();
    } catch (error) {
        state.chatMessages.push({
            isUser: false,
            content: "I could not reach the assistant service. Check that the backend is running."
        });
        setMessage(elements.chatMessage, error.message);
        renderChat();
    }
}

function switchView(viewName) {
    document.querySelectorAll(".view-section").forEach((section) => {
        section.classList.add("hidden");
    });
    document.getElementById(`${viewName}View`).classList.remove("hidden");

    document.querySelectorAll(".nav-item").forEach((item) => {
        item.classList.toggle("active", item.dataset.view === viewName);
    });

    elements.pageTitle.textContent = viewName[0].toUpperCase() + viewName.slice(1);
}

async function ensureStarterCategories() {
    if (state.categories.length) return;

    const starters = [
        { name: "Food", color: "#f97316", icon: "food" },
        { name: "Transport", color: "#2563eb", icon: "car" },
        { name: "Salary", color: "#059669", icon: "salary" },
        { name: "Shopping", color: "#db2777", icon: "shopping" }
    ];

    for (const category of starters) {
        try {
            await request("/categories", {
                method: "POST",
                body: JSON.stringify(category)
            });
        } catch {
            // Existing categories are fine; reload below.
        }
    }

    state.categories = await request("/categories");
}

elements.loginTab.addEventListener("click", () => switchAuth("login"));
elements.signupTab.addEventListener("click", () => switchAuth("signup"));
elements.forgotPasswordBtn.addEventListener("click", () => switchAuth("forgot"));
elements.backToLoginFromForgot.addEventListener("click", () => switchAuth("login"));
elements.backToLoginFromReset.addEventListener("click", () => switchAuth("login"));

elements.forgotPasswordForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.authMessage, "Sending OTP...", "success");
    try {
        await request("/auth/forgot-password", {
            method: "POST",
            body: JSON.stringify({ email: elements.forgotEmail.value.trim() })
        });
        setMessage(elements.authMessage, "OTP sent! Check your inbox.", "success");
        switchAuth("reset");
    } catch (error) {
        setMessage(elements.authMessage, error.message);
    }
});

elements.resetPasswordForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.authMessage, "Resetting password...", "success");
    try {
        await request("/auth/reset-password", {
            method: "POST",
            body: JSON.stringify({
                email: elements.forgotEmail.value.trim(),
                otp: elements.resetOtp.value.trim(),
                newPassword: elements.resetPassword.value
            })
        });
        setMessage(elements.authMessage, "Password reset successful! You can now login.", "success");
        switchAuth("login");
    } catch (error) {
        setMessage(elements.authMessage, error.message);
    }
});

elements.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.authMessage, "Logging in...", "success");

    try {
        const payload = await request("/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email: document.getElementById("loginEmail").value.trim(),
                password: document.getElementById("loginPassword").value
            })
        });
        saveSession(payload);
        showApp();
        await loadAppData();
        await ensureStarterCategories();
        renderCategories();
        checkOnboarding();
    } catch (error) {
        setMessage(elements.authMessage, error.message);
    }
});

elements.signupForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.authMessage, "Creating account...", "success");

    try {
        const email = document.getElementById("signupEmail").value.trim();
        const password = document.getElementById("signupPassword").value;
        await request("/auth/signup", {
            method: "POST",
            body: JSON.stringify({
                name: document.getElementById("signupName").value.trim(),
                email,
                password,
                role: "PERSONAL",
                currency: "INR"
            })
        });

        const payload = await request("/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        saveSession(payload);
        showApp();
        await loadAppData();
        await ensureStarterCategories();
        renderCategories();
        checkOnboarding();
    } catch (error) {
        setMessage(elements.authMessage, error.message);
    }
});

let pendingTransactionPayload = null;

async function executeTransactionSave(payload) {
    try {
        await request("/transactions", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        elements.transactionForm.reset();
        elements.transactionDate.valueAsDate = new Date();
        setMessage(elements.transactionMessage, "Transaction saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.transactionMessage, error.message);
    }
}

elements.transactionForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.transactionMessage, "Saving transaction...", "success");

    try {
        if (!elements.transactionCategory.value) {
            throw new Error("Create a category before adding a transaction.");
        }

        const type = elements.transactionType.value;
        const amount = Number(elements.transactionAmount.value);
        const categoryId = elements.transactionCategory.value;
        const description = elements.transactionDescription.value.trim();
        const dateVal = elements.transactionDate.value;

        // Overspending Check:
        if (type === "EXPENSE") {
            const budget = state.budgets.find(b => b.categoryId === categoryId);
            if (budget) {
                const spent = Number(budget.spent || 0);
                const limit = Number(budget.limit || 0);
                if (limit > 0 && (spent + amount) > limit) {
                    // Show overspending warning modal
                    const categoryObj = state.categories.find(c => c.id === categoryId);
                    elements.warningCategoryName.textContent = categoryObj?.name || "Category";
                    elements.warningExpenseAmount.textContent = money(amount);
                    elements.warningBudgetRemaining.textContent = money(limit - spent);
                    
                    const tags = (elements.transactionTags?.value || "").split(",").map(t => t.trim()).filter(Boolean);
                    pendingTransactionPayload = {
                        type, categoryId, amount, description, transactionDate: dateVal, tags
                    };
                    
                    elements.overspendingWarningModal.classList.remove("hidden");
                    setMessage(elements.transactionMessage, "");
                    return;
                }
            }
        }

        const tags = (elements.transactionTags?.value || "").split(",").map(t => t.trim()).filter(Boolean);
        await executeTransactionSave({
            type, categoryId, amount, description, transactionDate: dateVal, tags
        });
    } catch (error) {
        setMessage(elements.transactionMessage, error.message);
    }
});

// Modal warning handlers
elements.warningCancelBtn.addEventListener("click", () => {
    elements.overspendingWarningModal.classList.add("hidden");
    pendingTransactionPayload = null;
});

elements.warningProceedBtn.addEventListener("click", async () => {
    elements.overspendingWarningModal.classList.add("hidden");
    if (pendingTransactionPayload) {
        await executeTransactionSave(pendingTransactionPayload);
        pendingTransactionPayload = null;
    }
});

elements.categoryForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.categoryMessage, "Adding category...", "success");

    try {
        await request("/categories", {
            method: "POST",
            body: JSON.stringify({
                name: elements.categoryName.value.trim(),
                color: elements.categoryColor.value,
                icon: elements.categoryName.value.trim().toLowerCase() || "category"
            })
        });

        elements.categoryForm.reset();
        elements.categoryColor.value = "#2563eb";
        setMessage(elements.categoryMessage, "Category added.", "success");
        state.categories = await request("/categories");
        renderCategories();
    } catch (error) {
        setMessage(elements.categoryMessage, error.message);
    }
});

elements.budgetForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.budgetMessage, "Saving budget...", "success");

    try {
        if (!elements.budgetCategory.value) {
            throw new Error("Create a category before setting a budget.");
        }

        await request("/budgets", {
            method: "POST",
            body: JSON.stringify({
                categoryId: elements.budgetCategory.value,
                amount: Number(elements.budgetAmount.value),
                month: Number(elements.budgetMonth.value),
                year: Number(elements.budgetYear.value)
            })
        });

        elements.budgetAmount.value = "";
        setMessage(elements.budgetMessage, "Budget saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.budgetMessage, error.message);
    }
});

elements.goalForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.goalMessage, "Saving goal...", "success");

    try {
        await request("/savings-goals", {
            method: "POST",
            body: JSON.stringify({
                name: elements.goalName.value.trim(),
                targetAmount: Number(elements.goalTarget.value),
                currentAmount: Number(elements.goalCurrent.value || 0),
                targetDate: elements.goalDate.value || undefined,
                icon: "flag",
                color: elements.goalColor.value
            })
        });

        elements.goalForm.reset();
        elements.goalCurrent.value = "0";
        elements.goalColor.value = "#059669";
        setMessage(elements.goalMessage, "Goal saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.goalMessage, error.message);
    }
});

elements.subscriptionForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.subscriptionMessage, "Saving subscription...", "success");

    try {
        await request("/subscriptions", {
            method: "POST",
            body: JSON.stringify({
                name: elements.subscriptionName.value.trim(),
                amount: Number(elements.subscriptionAmount.value),
                billingCycle: elements.subscriptionCycle.value,
                nextBillingDate: elements.subscriptionDate.value,
                color: elements.subscriptionColor.value,
                isActive: true
            })
        });

        elements.subscriptionForm.reset();
        elements.subscriptionCycle.value = "MONTHLY";
        elements.subscriptionColor.value = "#db2777";
        elements.subscriptionDate.valueAsDate = new Date();
        setMessage(elements.subscriptionMessage, "Subscription saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.subscriptionMessage, error.message);
    }
});

elements.settingsForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.settingsMessage, "Saving settings...", "success");

    try {
        state.profile = await request("/users/settings", {
            method: "PATCH",
            body: JSON.stringify({
                name: elements.settingsName.value.trim(),
                currency: elements.settingsCurrency.value,
                notificationsEnabled: elements.settingsNotifications.checked,
                budgetAlerts: elements.settingsNotifications.checked,
                goalReminders: elements.settingsNotifications.checked,
                subscriptionReminders: elements.settingsNotifications.checked,
                weeklyReport: elements.settingsNotifications.checked,
                monthlyReport: elements.settingsNotifications.checked
            })
        });

        state.user = {
            ...state.user,
            name: state.profile.name,
            currency: state.profile.currency
        };
        localStorage.setItem("moneymap_user", JSON.stringify(state.user));
        showApp();
        setMessage(elements.settingsMessage, "Settings saved.", "success");
    } catch (error) {
        setMessage(elements.settingsMessage, error.message);
    }
});

elements.profileForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.profileMessage, "Saving profile...", "success");

    try {
        await request("/users/profile", {
            method: "PATCH",
            body: JSON.stringify({
                institution: elements.profileInstitution.value.trim() || undefined,
                companyName: elements.profileCompany.value.trim() || undefined,
                jobTitle: elements.profileJobTitle.value.trim() || undefined,
                monthlyIncome: elements.profileMonthlyIncome.value ? Number(elements.profileMonthlyIncome.value) : undefined,
                financialGoal: elements.profileFinancialGoal.value.trim() || undefined,
                onboardingCompleted: true
            })
        });

        state.profile = await request("/users/profile");
        renderProfile();
        setMessage(elements.profileMessage, "Profile saved.", "success");
    } catch (error) {
        setMessage(elements.profileMessage, error.message);
    }
});

document.addEventListener("click", async (event) => {
    const txDelete = event.target.closest("[data-tx-delete]");
    const txEdit = event.target.closest("[data-tx-edit]");
    const goalAdd = event.target.closest("[data-goal-add]");
    const goalDelete = event.target.closest("[data-goal-delete]");
    const subToggle = event.target.closest("[data-sub-toggle]");
    const subDelete = event.target.closest("[data-sub-delete]");

    try {
        if (txDelete) {
            await request(`/transactions/${txDelete.dataset.txDelete}`, { method: "DELETE" });
            await loadAppData();
        }

        if (txEdit) {
            const txId = txEdit.dataset.txEdit;
            const tx = state.transactions.find(t => t.id === txId) || state.allExpenses.find(t => t.id === txId);
            if (tx) {
                elements.editTransactionId.value = tx.id;
                elements.editTransactionType.value = tx.type;
                elements.editTransactionCategory.value = tx.categoryId || (tx.category?.id) || "";
                elements.editTransactionAmount.value = tx.amount;
                elements.editTransactionDescription.value = tx.description || "";
                elements.editTransactionDate.value = tx.transactionDate.split("T")[0];
                elements.editTransactionTags.value = (tx.tags || []).join(", ");
                
                elements.editTransactionModal.classList.remove("hidden");
            }
        }

        if (goalAdd) {
            const goal = state.goals.find((item) => item.id === goalAdd.dataset.goalAdd);
            if (goal) {
                await request(`/savings-goals/${goal.id}`, {
                    method: "PATCH",
                    body: JSON.stringify({
                        currentAmount: Number(goal.currentAmount || 0) + 50
                    })
                });
                await loadAppData();
            }
        }

        if (goalDelete) {
            await request(`/savings-goals/${goalDelete.dataset.goalDelete}`, { method: "DELETE" });
            await loadAppData();
        }

        if (subToggle) {
            await request(`/subscriptions/${subToggle.dataset.subToggle}`, {
                method: "PATCH",
                body: JSON.stringify({ isActive: subToggle.dataset.active !== "true" })
            });
            await loadAppData();
        }

        if (subDelete) {
            await request(`/subscriptions/${subDelete.dataset.subDelete}`, { method: "DELETE" });
            await loadAppData();
        }
    } catch (error) {
        console.error(error);
    }
});

document.querySelectorAll(".nav-item").forEach((button) => {
    button.addEventListener("click", () => switchView(button.dataset.view));
});

document.querySelectorAll("[data-view-link]").forEach((button) => {
    button.addEventListener("click", () => switchView(button.dataset.viewLink));
});

elements.floatingAssistantButton.addEventListener("click", () => {
    switchView("assistant");
});

document.getElementById("refreshButton").addEventListener("click", loadAppData);

// Search and filter listeners
elements.transactionSearch.addEventListener("input", renderTransactions);
elements.transactionTypeFilter.addEventListener("change", renderTransactions);
elements.transactionCategoryFilter.addEventListener("change", renderTransactions);

elements.chatForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await sendAssistantMessage(elements.chatInput.value);
});

document.getElementById("newChatButton").addEventListener("click", () => {
    state.chatSessionId = null;
    state.chatMessages = [];
    setMessage(elements.chatMessage, "");
    renderChat();
});

document.querySelectorAll(".prompt-chip").forEach((button) => {
    button.addEventListener("click", async () => {
        switchView("assistant");
        await sendAssistantMessage(button.textContent);
    });
});

document.addEventListener("click", (event) => {
    const toggle = event.target.closest(".password-toggle");
    if (!toggle) return;

    const inputId = toggle.dataset.togglePassword;
    const input = document.getElementById(inputId);
    if (!input) return;

    const isHidden = input.type === "password";
    input.type = isHidden ? "text" : "password";
    toggle.setAttribute("aria-label", isHidden ? "Hide password" : "Show password");

    toggle.querySelector(".eye-icon").classList.toggle("hidden", isHidden);
    toggle.querySelector(".eye-off-icon").classList.toggle("hidden", !isHidden);
});

document.getElementById("logoutButton").addEventListener("click", () => {
    clearSession();
    state.chatSessionId = null;
    state.chatMessages = [];
    showAuth();
});

elements.transactionDate.valueAsDate = new Date();
elements.subscriptionDate.valueAsDate = new Date();
elements.budgetMonth.value = String(new Date().getMonth() + 1);
elements.budgetYear.value = String(new Date().getFullYear());
renderChat();

// Onboarding event listeners
document.querySelectorAll(".role-option-card").forEach(card => {
    card.addEventListener("click", () => {
        document.querySelectorAll(".role-option-card").forEach(c => c.classList.remove("selected"));
        card.classList.add("selected");
        onboardingState.role = card.dataset.role;
        elements.onboardNextBtn.disabled = false;
    });
});

elements.onboardBackBtn.addEventListener("click", () => {
    if (onboardingState.step > 1) {
        showOnboardStep(onboardingState.step - 1);
    }
});

async function prepareHomemakerCategories() {
    const catsSelected = [];
    if (elements.obCatGroceries.checked) catsSelected.push({ name: "Groceries", color: "#10b981", icon: "groceries" });
    if (elements.obCatUtilities.checked) catsSelected.push({ name: "Utilities", color: "#3b82f6", icon: "bolt" });
    if (elements.obCatEducation.checked) catsSelected.push({ name: "Education", color: "#8b5cf6", icon: "book" });
    if (elements.obCatHealthcare.checked) catsSelected.push({ name: "Healthcare", color: "#ef4444", icon: "heart" });

    for (const cat of catsSelected) {
        const exists = state.categories.some(c => c.name.toLowerCase() === cat.name.toLowerCase());
        if (!exists) {
            try {
                await request("/categories", {
                    method: "POST",
                    body: JSON.stringify(cat)
                });
            } catch (e) {
                console.error("Failed to create homemaker category: " + cat.name, e);
            }
        }
    }
    state.categories = await request("/categories");
}

elements.onboardNextBtn.addEventListener("click", async () => {
    if (onboardingState.step === 2 && onboardingState.role === "HOMEMAKER") {
        elements.onboardNextBtn.disabled = true;
        const originalText = elements.onboardNextBtn.textContent;
        elements.onboardNextBtn.textContent = "Loading...";
        try {
            await prepareHomemakerCategories();
        } catch (e) {
            console.error("Failed to prepare homemaker categories", e);
        } finally {
            elements.onboardNextBtn.disabled = false;
            elements.onboardNextBtn.textContent = originalText;
        }
    }

    if (onboardingState.step === 5) {
        saveOnboardingData();
    } else {
        showOnboardStep(onboardingState.step + 1);
    }
});

elements.onboardAllowNotifications.addEventListener("click", () => {
    onboardingState.notifications = true;
    showOnboardStep(4);
});

elements.onboardNotNowNotifications.addEventListener("click", () => {
    onboardingState.notifications = false;
    showOnboardStep(4);
});

elements.onboardFinishBtn.addEventListener("click", () => {
    saveOnboardingData();
});

// Google Sign-In Initialization
function initGoogleAuth() {
    if (typeof google === "undefined") {
        setTimeout(initGoogleAuth, 100);
        return;
    }

    google.accounts.id.initialize({
        client_id: "794079844588-lbk0rg3rrdk1r8i3dpmg8ou314vpp8vd.apps.googleusercontent.com",
        callback: handleCredentialResponse
    });

    const googleBtn = document.getElementById("googleBtn");
    if (googleBtn) {
        google.accounts.id.renderButton(googleBtn, {
            type: "standard",
            theme: "outline",
            size: "large",
            text: "continue_with",
            shape: "rectangular",
            logo_alignment: "left",
            width: 320
        });
    }
}

// Google Sign-In Callback
async function handleCredentialResponse(response) {
    setMessage(elements.authMessage, "Logging in with Google...", "success");

    try {
        const payload = await request("/auth/google", {
            method: "POST",
            body: JSON.stringify({
                idToken: response.credential
            })
        });
        saveSession(payload);
        showApp();
        await loadAppData();
        await ensureStarterCategories();
        renderCategories();
        checkOnboarding();
    } catch (error) {
        setMessage(elements.authMessage, error.message);
    }
}

elements.editCancelBtn.addEventListener("click", () => {
    elements.editTransactionModal.classList.add("hidden");
    setMessage(elements.editTransactionMessage, "");
});

elements.editTransactionForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.editTransactionMessage, "Saving changes...", "success");

    try {
        const id = elements.editTransactionId.value;
        const type = elements.editTransactionType.value;
        const categoryId = elements.editTransactionCategory.value;
        const amount = Number(elements.editTransactionAmount.value);
        const description = elements.editTransactionDescription.value.trim();
        const transactionDate = elements.editTransactionDate.value;
        const tags = (elements.editTransactionTags.value || "").split(",").map(t => t.trim()).filter(Boolean);

        await request(`/transactions/${id}`, {
            method: "PATCH",
            body: JSON.stringify({
                type,
                categoryId,
                amount,
                description,
                transactionDate,
                tags
            })
        });

        elements.editTransactionModal.classList.add("hidden");
        setMessage(elements.editTransactionMessage, "");
        await loadAppData();
    } catch (error) {
        setMessage(elements.editTransactionMessage, error.message);
    }
});

// Initialize Google Auth on script load
initGoogleAuth();

if (state.token && state.user) {
    showApp();
    loadAppData()
        .then(ensureStarterCategories)
        .then(renderCategories)
        .then(checkOnboarding)
        .catch((error) => {
            clearSession();
            showAuth();
            setMessage(elements.authMessage, error.message);
        });
} else {
    showAuth();
}
