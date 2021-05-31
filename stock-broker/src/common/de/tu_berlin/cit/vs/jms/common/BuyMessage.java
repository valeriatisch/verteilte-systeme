package de.tu_berlin.cit.vs.jms.common;


public class BuyMessage extends BrokerMessage {
    private String stockName;
    private int amount;
    
    public BuyMessage(String stockName, int amount) {
        super(Type.STOCK_BUY);
        this.stockName = stockName;
        this.amount = amount;
    }

    public String getStockName() {
        return stockName;
    }

    public int getAmount() {
        return amount;
    }
}
