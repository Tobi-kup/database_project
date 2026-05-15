import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.util.HashMap;

public class SPAHandler implements HttpHandler {

    private static double userBalance = 0.0;

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {

        String html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>Stock Investment Simulator</title>
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
            <style>
                body { font-family:'Segoe UI', sans-serif; background:linear-gradient(135deg,#667eea,#764ba2);
                       height:100vh; margin:0; display:flex; justify-content:center; align-items:center;}
                .container { background:#fff; padding:50px 40px; border-radius:20px; box-shadow:0 10px 30px rgba(0,0,0,0.2);
                             width:100%; max-width:400px; text-align:center; overflow-y:auto; max-height:90vh;}
                h1 { color:#333; margin-bottom:20px; font-size:28px;}
                input { width:100%; padding:12px 15px; margin:10px 0; border-radius:10px; border:1px solid #ccc; font-size:16px; display:block;}
                input:focus { border-color:#667eea; outline:none; box-shadow:0 0 5px rgba(102,126,234,0.5);}
                button { width:80%; padding:8px 12px; border:none; border-radius:10px; background:#667eea; color:white; font-size:14px; cursor:pointer;}
                button:hover{ background:#5563c1;}
                .secondary-button{width:auto;background:#f4f4f4;color:#333;border:1px solid #ccc;}
                .secondary-button:hover{background:#e0e0e0;}
                .hint-row { margin-top:10px; display:flex; justify-content:center; align-items:center; gap:8px; font-size:14px; color:#555;}
                #top-bar { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
                #message { color:green; margin-top:10px; }
                #stocks-table { width:100%; border-collapse:collapse; }
                #stocks-table th, #stocks-table td { border:1px solid #ccc; padding:10px; text-align:center; }
                #stocks-table th { background:#667eea; color:white; }
            </style>
        </head>
        <body>
        <div class="container" id="app-container"></div>

        <script>
            let currentUser = null;
            let portfolio = [];
            let balance = 0;

            const pages = {};

            // --- Login ---
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

            // --- Register ---
            pages["register"] = `
                <h1>Register</h1>
                <input id="email" type="email" placeholder="Email">
                <input id="password" type="password" placeholder="Password">
                <button onclick="register()">Submit</button>
                <div class="hint-row">
                    <span>Already have an account?</span>
                    <button class="secondary-button" onclick="showPage('login')">Login</button>
                </div>
                <div id="message"></div>
            `;

            // --- Dashboard ---
            pages["dashboard"] = `
                <div id="top-bar">
                    <h1>Dashboard</h1>
                    <div style="display:flex; align-items:center; justify-content:flex-end; gap:10px;">
                        <button class="secondary-button" onclick="logout()">Logout</button>
                        <span>Kontostand: $<span id="balance">0.00</span></span>
                    </div>
                </div>
                <ul id="portfolio-list"></ul>
                <input id="addBalanceInput" type="number" placeholder="Betrag aufladen" style="margin-top:10px; padding:5px 10px;">
                <button onclick="addBalance()">Guthaben aufladen</button>
                <button onclick="showPage('stocks')">Stocks Market</button>
            `;

            // --- Stocks Market ---
            pages["stocks"] = `
                <div id="top-bar">
                    <h1>Stocks Market</h1>
                    <div style="display:flex; align-items:center; justify-content:flex-end; gap:10px;">
                        <button class="secondary-button" onclick="logout()">Logout</button>
                        <span>Kontostand: $<span id="balance">0.00</span></span>
                    </div>
                </div>
                <input id="search" type="text" placeholder="Suche nach Aktie..." oninput="filterStocks()">
                <table id="stocks-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Preis</th>
                            <th>%Change</th>
                            <th>Chart</th>
                        </tr>
                    </thead>
                    <tbody id="stocks-body"></tbody>
                </table>
                <button class="secondary-button" onclick="showPage('dashboard')">Back</button>
            `;

            pages["buySell"] = `<div id="buy-sell-container"></div>`;

            function showPage(page){
                document.getElementById('app-container').innerHTML = pages[page];
                if(page==="dashboard") renderPortfolio();
                if(page==="stocks") loadStocks();
            }

            function renderPortfolio(){
                const list = document.getElementById('portfolio-list');
                list.innerHTML = portfolio.length ? portfolio.map(s=>`<li>${s}</li>`).join('') : '<li>No stocks yet</li>';
                document.getElementById('balance').innerText = balance.toFixed(2);
            }

            // --- Login ---
            async function login(){
                const email=document.getElementById('email').value;
                const password=document.getElementById('password').value;
                const res = await fetch('/login',{method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({email,password})});
                const text = await res.text();
                document.getElementById('message').innerText = text;
                if(text.includes('Login successful')){
                    currentUser = 1;
                    const match = text.match(/Balance: ([0-9.]+)/);
                    if(match) balance = parseFloat(match[1]);
                    showPage('dashboard');
                }
            }

            // --- Register ---
            async function register(){
                const email=document.getElementById('email').value;
                const password=document.getElementById('password').value;
                const res = await fetch('/register',{method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({email,password})});
                const text = await res.text();
                document.getElementById('message').innerText = text;
            }

            // --- Guthaben aufladen ---
            async function addBalance(){
                const amount = parseFloat(document.getElementById('addBalanceInput').value);
                if(isNaN(amount) || amount<=0) return alert("Bitte gültigen Betrag eingeben");
                const res = await fetch('/addBalance',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({userId: currentUser, amount})});
                const text = await res.text();
                if(text.includes("success")){
                    balance += amount;
                    document.getElementById('balance').innerText = balance.toFixed(2);
                    document.getElementById('addBalanceInput').value = '';
                } else alert("Fehler beim Aufladen");
            }

            function logout(){portfolio=[];currentUser=null;balance=0; showPage('login');}

            // --- Stocks Table ---
            let allStocks = [];
            async function loadStocks(){
                const res = await fetch("/stocks/list");
                allStocks = await res.json();
                renderStockTable(allStocks);
            }

            function renderStockTable(stocks){
                const tbody = document.getElementById('stocks-body');
                tbody.innerHTML = '';
                stocks.forEach(stock=>{
                    const row = document.createElement('tr');
                    const chartId = 'chart-'+stock.symbol;
                    row.innerHTML = `<td>${stock.symbol}</td>
                                     <td>${stock.price}</td>
                                     <td>${stock.change}</td>
                                     <td><canvas id="${chartId}" width="200" height="100"></canvas></td>`;
                    row.onclick = ()=>showBuySell(stock.symbol);
                    tbody.appendChild(row);

                    const ctx = document.getElementById(chartId).getContext('2d');
                    new Chart(ctx,{type:'line',data:{labels:['Mon','Tue','Wed','Thu','Fri'],datasets:[{label:stock.symbol,data:[10,12,11,15,14],borderColor:'blue',fill:false}] }});
                });
            }

            function filterStocks(){
                const query = document.getElementById('search').value.toLowerCase();
                const filtered = allStocks.filter(s=>s.symbol.toLowerCase().includes(query));
                renderStockTable(filtered);
            }

            function showBuySell(symbol){
                document.getElementById('app-container').innerHTML = `
                    <h1>${symbol}</h1>
                    <input id="amount" type="number" placeholder="Shares">
                    <button onclick="buyStock('${symbol}')">Buy</button>
                    <button onclick="sellStock('${symbol}')">Sell</button>
                    <button onclick="showPage('stocks')">Back</button>`;
            }

            function buyStock(symbol){
                const amt = Number(document.getElementById('amount').value);
                portfolio.push(symbol);
                balance -= amt*10;
                alert('Bought '+amt+' '+symbol);
                showPage('stocks');
            }

            function sellStock(symbol){
                const amt = Number(document.getElementById('amount').value);
                portfolio = portfolio.filter(s=>s!==symbol);
                balance += amt*10;
                alert('Sold '+amt+' '+symbol);
                showPage('stocks');
            }

            showPage('login');
        </script>
        </body>
        </html>
        """;

        send(exchange, html);
    }

    public static HashMap<String,String> parseJson(String json){
        HashMap<String,String> map = new HashMap<>();
        json=json.replace("{","").replace("}","").replace("\"","");
        String[] pairs=json.split(",");
        for(String pair:pairs){
            String[] kv=pair.split(":");
            if(kv.length==2) map.put(kv[0].trim(),kv[1].trim());
        }
        return map;
    }

    public static void send(HttpExchange exchange, String response) throws java.io.IOException{
        exchange.sendResponseHeaders(200,response.getBytes().length);
        OutputStream os=exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}