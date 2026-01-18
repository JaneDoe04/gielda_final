package com.stockmarket.domain;

public class Order {
    private final String assetSymbol;
    private final OrderType orderType;
    private final int quantity;
    private final double limitPrice;
    private final double marketPrice;

    public Order(String assetSymbol, OrderType orderType, int quantity, double limitPrice, double marketPrice) {
        if (assetSymbol == null || assetSymbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol aktywa nie może być null ani pusty");
        }
        if (orderType == null) {
            throw new IllegalArgumentException("Typ zlecenia nie może być null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ilość musi być większa od zera");
        }
        if (limitPrice <= 0) {
            throw new IllegalArgumentException("Cena limitu musi być większa od zera");
        }
        if (Double.isNaN(limitPrice) || Double.isInfinite(limitPrice)) {
            throw new IllegalArgumentException("Cena limitu musi być skończoną liczbą");
        }
        if (marketPrice <= 0) {
            throw new IllegalArgumentException("Cena rynkowa musi być większa od zera");
        }
        if (Double.isNaN(marketPrice) || Double.isInfinite(marketPrice)) {
            throw new IllegalArgumentException("Cena rynkowa musi być skończoną liczbą");
        }

        this.assetSymbol = assetSymbol.trim();
        this.orderType = orderType;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.marketPrice = marketPrice;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public double calculateAttractiveness() {
        if (orderType == OrderType.BUY) {
            return limitPrice;
        } else {
            return -limitPrice;
        }
    }
}
