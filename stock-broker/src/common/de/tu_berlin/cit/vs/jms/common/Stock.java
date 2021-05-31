package de.tu_berlin.cit.vs.jms.common;

import java.io.Serializable;


public class Stock implements Serializable {
    private String name;
    private int stockCount;
    private int availableCount;
    private double price;
    
    public Stock(String name, int stockCount, double startingPrice) {
        this.stockCount = stockCount;
        this.availableCount = stockCount;
        this.name = name;
        this.price = startingPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStockCount() {
        return stockCount;
    }

    public void setStockCount(int stockCount) {
        this.stockCount = stockCount;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    @Override
    public String toString() {
        return "" + getName() +
                " -- price: " + getPrice() +
                " -- available: " + getAvailableCount() +
                " -- sum: " + getStockCount();
    }
}
