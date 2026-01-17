package com.stockmarket.domain;

public class Commodity extends Asset {
    private static final double STORAGE_COST_PER_UNIT = 1.0;

    public Commodity(String symbol, String name, double marketPrice) {
        super(symbol, name, marketPrice);
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.COMMODITY;
    }

    @Override
    public double calculateRealValue(int quantity) {
        double baseValue = getMarketPrice() * quantity;
        double storageCost = quantity * STORAGE_COST_PER_UNIT;
        return baseValue - storageCost;
    }

    @Override
    public double getPurchaseCost(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ilość musi być większa od zera");
        }
        double baseCost = getMarketPrice() * quantity;
        double storageCost = quantity * STORAGE_COST_PER_UNIT;
        return baseCost + storageCost;
    }

    @Override
    public Asset createCopy() {
        return new Commodity(getSymbol(), getName(), getMarketPrice());
    }
}

