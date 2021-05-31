package de.tu_berlin.cit.vs.jms.common;


public class SellMessage extends BrokerMessage {
    private String stockName;
    private int amount;
    
    public SellMessage(String stockName, int amount) {
        super(Type.STOCK_SELL);
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
