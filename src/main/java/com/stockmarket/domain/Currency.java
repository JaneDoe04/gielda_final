package com.stockmarket.domain;

public class Currency extends Asset {
    private final double spread;

    public Currency(String symbol, String name, double marketPrice, double spread) {
        super(symbol, name, marketPrice);
        if (spread < 0) {
            throw new IllegalArgumentException("Spread nie może być ujemny");
        }
        if (spread >= marketPrice) {
            throw new IllegalArgumentException("Spread nie może być większy lub równy cenie rynkowej");
        }
        this.spread = spread;
    }

    public double getSpread() {
        return spread;
    }

    private double getBidPrice() {
        return getMarketPrice() - spread;
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.CURRENCY;
    }

    @Override
    public double calculateRealValue(int quantity) {
        return getBidPrice() * quantity;
    }

    @Override
    public double getPurchaseCost(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ilość musi być większa od zera");
        }
        return getMarketPrice() * quantity;
    }

    @Override
    public Asset createCopy() {
        return new Currency(getSymbol(), getName(), getMarketPrice(), spread);
    }
}

