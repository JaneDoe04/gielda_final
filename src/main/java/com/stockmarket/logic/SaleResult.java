package com.stockmarket.logic;

public class SaleResult {
    private final double totalRevenue;
    private final double totalProfit;

    public SaleResult(double totalRevenue, double totalProfit) {
        this.totalRevenue = totalRevenue;
        this.totalProfit = totalProfit;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }
}
