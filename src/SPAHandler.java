import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class SPAHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>Stock Investment Simulator</title>
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

            <style>
                body {
                    font-family:'Segoe UI', sans-serif;
                    background:linear-gradient(135deg,#667eea,#764ba2);
                    height:100vh;
                    margin:0;
                    display:flex;
                    justify-content:center;
                    align-items:center;
                }

                .container {
                    background:#fff;
                    padding:50px 40px;
                    border-radius:20px;
                    box-shadow:0 10px 30px rgba(0,0,0,0.2);
                    width:100%;
                    max-width:400px;
                    text-align:center;
                    overflow-y:auto;
                    max-height:90vh;
                }

                h1 { color:#333; margin-bottom:20px; font-size:28px;}

                input {
                    width:100%;
                    padding:12px;
                    margin:10px 0;
                    border-radius:10px;
                    border:1px solid #ccc;
                    font-size:16px;
                    display:block;
                    box-sizing:border-box;
                
                }

                button {
                    width:80%;
                    padding:10px;
                    border:none;
                    border-radius:10px;
                    background:#667eea;
                    color:white;
                    cursor:pointer;
                    margin-top:5px;
                }

                button:hover { background:#5563c1; }

                .secondary-button {
                    width:auto;
                    background:#f4f4f4;
                    color:#333;
                    border:1px solid #ccc;
                }
                
                .secondary-button:hover{ background:#e0e0e0; }
                
                hint-row { 
                    margin-top:10px; 
                    display:flex; 
                    justify-content:center; 
                    align-items:center; 
                    gap:8px; 
                    font-size:14px; 
                    color:#555;}
                
                
                #top-bar {
                    display:flex;
                    justify-content:space-between;
                    align-items:center;
                }

                #stocks-table {
                    width:100%;
                    border-collapse:collapse;
                }

                #stocks-table th, #stocks-table td {
                    border:1px solid #ccc;
                    padding:8px;
                }

                #stocks-table th {
                    background:#667eea;
                    color:white;
                }
            </style>
        </head>

        <body>
        <div class="container" id="app-container"></div>

        <script>

        let currentUser = null;
        let balance = 0;
        let allStocks = [];
        let selectedStock = null;
        let portfolio = [];
        let bigChartInstance = null;

        const pages = {};
        
        async function loadPortfolio(){
            const res = await fetch("/portfolio?userId=" + currentUser);
            const data = await res.json();
            portfolio = data;
            renderPortfolio();
        }

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
            <button onclick="addBalance()">Add Balance</button>

            <button onclick="showPage('stocks')">Go to Stocks</button>
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
        
            <button onclick="buySelectedStock()">Buy</button>
            <button onclick="sellSelectedStock()">Sell</button>
        
            <button onclick="showPage('stocks')">Back</button>
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
                    refreshBalance(); }
                    
                if (page === "stocks") {
                    loadStocks();
                    refreshBalance(); }
                    
               if (page === "stockDetail") {
                    refreshBalance();}
            });
        }

        // ---------------- LOGIN ----------------
        async function login(){

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

        // ---------------- BALANCE ----------------
        async function refreshBalance(){

            const res = await fetch("/balance?userId=" + currentUser);
            const data = await res.json();

            balance = data.balance;

            const el = document.getElementById("balance");
            if(el) el.innerText = balance.toFixed(2);
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
        
            const tbody =
                document.getElementById("stocks-body");
        
            tbody.innerHTML = "";
        
            stocks.forEach((s,index) => {
        
                const row =
                    document.createElement("tr");
        
                row.innerHTML = `
                    <td>
                        <a onclick="openStock('${s.symbol}')">
                            ${s.symbol}
                        </a>
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
        
                const ctx = document
                    .getElementById(`chart-${index}`)
                    .getContext("2d");
        
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
                            x:{
                                display:false
                            },
                            y:{
                                display:false
                            }
                        }
                    }
                });
            });
        }

        function filterStocks(){

            const q = document.getElementById("search").value.toLowerCase();

            renderStocks(allStocks.filter(s =>
                s.symbol.toLowerCase().includes(q)
            ));
        }
        
        // ---------------- BUY & SELL ----------------
        async function sellSelectedStock(){
        
            const quantity =
                Number(document.getElementById("amount").value);
        
            const stock =
                allStocks.find(s => s.symbol === selectedStock);
        
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
        
        async function buySelectedStock(){
        
                      const quantity =
                          Number(document.getElementById("amount").value);
        
                      const stock =
                          allStocks.find(s => s.symbol === selectedStock);
        
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
                
        // ---------------- OPEN STOCK ----------------
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
             const canvas = document.getElementById("bigChart");
        
             if (!canvas) return;
        
             const ctx = canvas.getContext("2d");
        
             if (bigChartInstance) {
                 bigChartInstance.destroy();
             }
        
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
                     plugins: { legend: { display: false } },
                     scales: {
                         x: { display: false },
                         y: { display: true }
                     }
                 }
             });
         }
        
        async function waitForElement(id) {
            while (!document.getElementById(id)) {
                await new Promise(r => requestAnimationFrame(r));
            }
        }
                
        // ---------------- PORTFOLIO ----------------
        function renderPortfolio(){
        
            const el = document.getElementById("portfolio");
        
            if(!el) return;
        
            if(portfolio.length === 0){
                el.innerHTML = "<li>No stocks</li>";
                return;
            }
        
            el.innerHTML = portfolio.map(p => `
                <li>
                    ${p.symbol} —
                    ${p.quantity} shares —
                    avg $${p.avgPrice.toFixed(2)}
                </li>
            `).join("");
        }

        // ---------------- LOGOUT ----------------
        function logout(){
            currentUser = null;
            balance = 0;
            portfolio = [];
            showPage("login");
        }

        showPage("login");
        history.replaceState({ page: "login" }, "", "#login");
        
        window.addEventListener("popstate", (event) => {
            const page = event.state?.page || "login";
            showPage(page, false);
        
        });
        

        </script>
        </body>
        </html>
        """;


        send(exchange, html);
    }

    // ---------------- JSON PARSER ----------------
    public static HashMap<String,String> parseJson(String json){

        HashMap<String,String> map = new HashMap<>();

        json = json.replace("{","")
                .replace("}","")
                .replace("\"","");

        String[] pairs = json.split(",");

        for(String pair : pairs){
            String[] kv = pair.split(":");
            if(kv.length == 2){
                map.put(kv[0].trim(), kv[1].trim());
            }
        }

        return map;
    }

    // ---------------- RESPONSE HELPER ----------------
    public static void send(HttpExchange exchange, String response) throws IOException {

        exchange.getResponseHeaders().set("Content-Type","text/html");

        exchange.sendResponseHeaders(200, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
}