public class Stock {

    private String symbol;
    private double price;
    private String change;
    private double[] chart;

    public Stock(String symbol, double price, String change, double[] chart) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.chart = chart;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public String getChange() {
        return change;
    }

    public double[] getChart() {
        return chart;
    }
}