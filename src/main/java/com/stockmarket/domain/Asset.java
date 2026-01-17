package com.stockmarket.domain;

public abstract class Asset {
    private final String symbol;
    private final String name;
    private double marketPrice;

    public Asset(String symbol, String name, double marketPrice) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol nie może być null ani pusty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa nie może być null ani pusta");
        }
        if (marketPrice <= 0) {
            throw new IllegalArgumentException("Cena rynkowa musi być większa od zera");
        }
        if (Double.isNaN(marketPrice) || Double.isInfinite(marketPrice)) {
            throw new IllegalArgumentException("Cena rynkowa musi być skończoną liczbą");
        }

        this.symbol = symbol.trim();
        this.name = name.trim();
        this.marketPrice = marketPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(double marketPrice) {
        if (marketPrice <= 0) {
            throw new IllegalArgumentException("Cena rynkowa musi być większa od zera");
        }
        if (Double.isNaN(marketPrice) || Double.isInfinite(marketPrice)) {
            throw new IllegalArgumentException("Cena rynkowa musi być skończoną liczbą");
        }
        this.marketPrice = marketPrice;
    }

    public abstract AssetType getAssetType();

    public abstract double calculateRealValue(int quantity);

    public abstract double getPurchaseCost(int quantity);

    public abstract Asset createCopy();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Asset asset = (Asset) obj;
        return symbol.equals(asset.symbol);
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();
    }
}

