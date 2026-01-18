package com.stockmarket.domain;

public class Share extends Asset {
    private static final double HANDLING_FEE = 5.0;

    public Share(String symbol, String companyName, double marketPrice) {
        super(symbol, companyName, marketPrice);
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.SHARE;
    }

    @Override
    public double calculateRealValue(int quantity) {
        double baseValue = getMarketPrice() * quantity;
        return baseValue - HANDLING_FEE;
    }

    @Override
    public double getPurchaseCost(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ilość musi być większa od zera");
        }
        return getMarketPrice() * quantity + HANDLING_FEE;
    }

    @Override
    public Asset createCopy() {
        return new Share(getSymbol(), getName(), getMarketPrice());
    }
}

