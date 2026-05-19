let currentUser = null;
let balance = 0;
let allStocks = [];
let selectedStock = null;
let portfolio = [];
let bigChartInstance = null;

const pages = {};

// ---------------- LOGIN ----------------
pages["login"] = `
    <h1>Login</h1>
    
    <input id="email" type="email" placeholder="Email">
    <input id="password" type="password" placeholder="Password">
    <button onclick="login()">Login</button>

    <div class="hint-row">
        <span>Don't have an account?</span>
        <button class="secondary-button" onclick="showPage('register')">Register</button>
    </div>
    <div id="message"></div>
`;

// ---------------- REGISTER ----------------
pages["register"] = `
    <h1>Register</h1>

    <input id="email" type="email" placeholder="Email">
    <input id="password" type="password" placeholder="Password">
    <button onclick="register()">Create Account</button>

    <div class="hint-row">
        <span>Already have an account?</span>
 <button class="secondary-button" onclick="showPage('login')">Login</button>
    </div>
    <div id="message"></div>
`;

// ---------------- DASHBOARD ----------------
pages["dashboard"] = `
    <div id="top-bar">
        <h1>Dashboard</h1>
        <div>
            Balance: $<span id="balance">0.00</span>
            <button onclick="logout()">Logout</button>
        </div>
    </div>

    <h3>Portfolio</h3>
    <ul id="portfolio"></ul>

    <input id="addBalanceInput" type="number" placeholder="Deposit amount">
    <button onclick="addBalance()"> Add Balance </button>

    <button onclick="showPage('stocks')"> Go to Stocks </button>
`;

// ---------------- STOCKS ----------------
pages["stocks"] = `
    <div id="top-bar">
        <h1>Stocks</h1>
        <div>
            Balance: $<span id="balance">0.00</span>
            <button onclick="logout()">Logout</button>
        </div>
    </div>

    <input id="search" placeholder="Search..." oninput="filterStocks()">

    <table id="stocks-table">
        <thead>
            <tr>
                <th>Symbol</th>
                <th>Price</th>
                <th>Change</th>
                <th>Chart</th>
            </tr>
        </thead>
        <tbody id="stocks-body"></tbody>
    </table>

    <button onclick="showPage('dashboard')">Back</button>
`;

// ---------------- STOCK DETAIL ----------------
pages["stockDetail"] = `
    <div id="top-bar">
        <h1 id="stock-title"></h1>

        <div>
            Balance: $<span id="balance">0.00</span>
            <button onclick="logout()">Logout</button>
        </div>
    </div>

    <canvas id="bigChart" height="200"></canvas>

    <h3>Trade</h3>

    <input id="amount" type="number" placeholder="Quantity">
    <button onclick="buySelectedStock()"> Buy </button>
    <button onclick="sellSelectedStock()"> Sell </button>
    
    <button onclick="showPage('stocks')"> Back </button>
`;

// ---------------- PAGE SWITCH ----------------
function showPage(page, push = true) {

    if (bigChartInstance) {
        bigChartInstance.destroy();
        bigChartInstance = null;
    }

    document.getElementById("app-container").innerHTML = pages[page];

    if (push) {
        history.pushState({ page }, "", `#${page}`);
    }

    requestAnimationFrame(() => {

        if (page === "dashboard") {
            renderPortfolio();
            loadPortfolio();
            refreshBalance();
        }

        if (page === "stocks") {
            loadStocks();
            refreshBalance();
        }

        if (page === "stockDetail") {
            refreshBalance();
        }
    });
}

// ---------------- LOGIN ----------------
async function login() {

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const res = await fetch("/login", {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body:JSON.stringify({email,password})
    });

    const data = await res.json();

    if(data.success){

        currentUser = data.userId;
        balance = data.balance;
        await loadPortfolio();

        showPage("dashboard");

    } else {
        document.getElementById("message").innerText = data.message;
    }
}

// ---------------- REGISTER ----------------
async function register(){

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const res = await fetch("/register", {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body:JSON.stringify({email,password})
    });

    const text = await res.text();
    document.getElementById("message").innerText = text;
}

// ---------------- PORTFOLIO ----------------
async function loadPortfolio(){

    const res =
        await fetch("/portfolio?userId=" + currentUser);

    portfolio = await res.json();

    renderPortfolio();
}

function renderPortfolio(){

    const el = document.getElementById("portfolio");

    if(!el) return;

    if(portfolio.length === 0){
        el.innerHTML = "<li>No stocks</li>";
        return;
    }

    el.innerHTML = portfolio.map(p => `
        <li>
            ${p.symbol}
            —
            ${p.quantity} shares
            —
            $${p.avgPrice.toFixed(2)}
        </li>
    `).join("");
}

// ---------------- BALANCE ----------------
async function refreshBalance(){

    const res = await fetch("/balance?userId=" + currentUser);
    const data = await res.json();

    balance = data.balance;

    const el = document.getElementById("balance");
    if(el) el.innerText = balance.toFixed(2)
}

async function addBalance(){

    const amount = parseFloat(document.getElementById("addBalanceInput").value);

    if(isNaN(amount) || amount <= 0){
        alert("Invalid amount");
        return;
    }

    const res = await fetch("/addBalance", {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body:JSON.stringify({
            userId: currentUser,
            amount: amount
        })
    });

    const data = await res.json();

    if(data.success){
        await refreshBalance();
        document.getElementById("addBalanceInput").value = "";
    }
}

// ---------------- STOCKS ----------------
async function loadStocks(){

    const res = await fetch("/stocks/list");
    allStocks = await res.json();

    renderStocks(allStocks);
}

function renderStocks(stocks){

    const tbody = document.getElementById("stocks-body");
    tbody.innerHTML = "";

    stocks.forEach((s,index) => {

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>
                <a onclick="openStock('${s.symbol}')">${s.symbol}</a>
            </td>
            <td>${s.price}</td>
            <td>${s.change}</td>
            <td>
                <canvas
                    id="chart-${index}"
                    width="150"
                    height="60">
                </canvas>
            </td>
        `;

        tbody.appendChild(row);

        const ctx = document.getElementById(`chart-${index}`).getContext("2d");

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: s.chart.map((_,i)=>i),

                datasets: [{
                    data: s.chart,

                    borderColor:
                        s.change.includes("-")
                            ? "#ff4d4d"
                            : "#00c853",

                    borderWidth: 2,

                    pointRadius: 0,

                    tension: 0.4,

                    fill: false
                }]
            },

            options: {
                responsive:false,

                plugins:{
                    legend:{
                        display:false
                    }
                },

                scales:{
                    x:{ display:false },
                    y:{ display:false }
                }
            }
        });
    });
}

function filterStocks(){

    const q = document.getElementById("search").value.toLowerCase();
    renderStocks(allStocks.filter(s => s.symbol.toLowerCase().includes(q)));
}

// ---------------- STOCK DETAIL ----------------
async function openStock(symbol){

    selectedStock = symbol;
    showPage("stockDetail");
    await refreshBalance();
    await waitForElement("bigChart");
    loadStockChart(symbol);
}

async function loadStockChart(symbol){

    const res = await fetch("/stocks/list");
    const data = await res.json();
    const stock = data.find(s => s.symbol === symbol);
    document.getElementById("stock-title").innerText = symbol;
    const canvas = document.getElementById("bigChart");

    if (!canvas) return;

    const ctx = canvas.getContext("2d");

    if (bigChartInstance) bigChartInstance.destroy()

    bigChartInstance = new Chart(ctx, {
        type: "line",
        data: {
            labels: stock.chart.map((_,i)=>i),
            datasets: [{
                data: stock.chart,
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.4,
                fill: false
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                x: { display: false },
                y: { display: true }
            }
        }
    });
}

// ---------------- BUY ----------------
async function buySelectedStock(){

    const quantity = Number(document.getElementById("amount").value);

    if (!Number.isInteger(quantity) || quantity <= 0) {
        alert("Please enter a valid positive quantity");
        return;
    }

    const stock = allStocks.find(s => s.symbol === selectedStock);

    if(!stock){
        alert("Stock not loaded");
        return;
    }

    const price = Number(stock.price);

    const res = await fetch("/buy", {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body:JSON.stringify({
            userId: currentUser,
            symbol: selectedStock,
            quantity,
            price
        })
    });

    const data = await res.json();

    if(data.success){
        alert("Bought!");
        await refreshBalance();
        await loadPortfolio();

    } else {
        alert(data.message);
    }
}

// ---------------- SELL ----------------
async function sellSelectedStock(){

    const quantity = Number(document.getElementById("amount").value);

    if (!Number.isInteger(quantity) || quantity <= 0) {
        alert("Please enter a valid positive quantity");
        return;
    }

    const stock = allStocks.find(s => s.symbol === selectedStock);

    if(!stock){
        alert("Stock not loaded");
        return;
    }

    const price = Number(stock.price);

    const res = await fetch("/sell", {
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body:JSON.stringify({
            userId: currentUser,
            symbol: selectedStock,
            quantity,
            price
        })
    });

    const data = await res.json();

    if(data.success){
        alert("Sold!");
        await refreshBalance();
        await loadPortfolio();

    } else {
        alert(data.message);
    }
}

async function waitForElement(id) {

    while (!document.getElementById(id)) {

        await new Promise(r =>
            requestAnimationFrame(r)
        );
    }
}

// ---------------- LOGOUT ----------------
function logout(){

    currentUser = null;
    balance = 0;
    portfolio = [];
    showPage("login");
}

// ---------------- HISTORY ----------------
window.addEventListener("popstate", (event) => {

    const page = event.state?.page || "login";
    showPage(page, false);
});

// ---------------- START ----------------
showPage("login");
history.replaceState({ page: "login" }, "", "#login");