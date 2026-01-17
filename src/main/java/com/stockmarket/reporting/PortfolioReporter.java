package com.stockmarket.reporting;

import com.stockmarket.domain.Asset;
import com.stockmarket.domain.AssetType;
import com.stockmarket.logic.Portfolio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PortfolioReporter {
    private final Portfolio portfolio;

    public PortfolioReporter(Portfolio portfolio) {
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio nie może być null");
        }
        this.portfolio = portfolio;
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPORT PORTFELA ===\n");
        report.append("Gotówka: ").append(String.format("%.2f", portfolio.getCash())).append("\n");
        report.append("Wartość całkowita: ").append(String.format("%.2f", portfolio.auditPortfolio())).append("\n\n");

        List<AssetReportEntry> entries = new ArrayList<>();
        List<String> symbols = portfolio.getAllAssetSymbols();

        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            Asset asset = portfolio.getAsset(symbol);
            if (asset != null) {
                int totalQuantity = portfolio.getAssetQuantity(symbol);
                double marketValue = asset.calculateRealValue(totalQuantity);
                entries.add(new AssetReportEntry(asset, marketValue));
            }
        }

        entries.sort(new AssetReportComparator());

        report.append("Aktywa (posortowane: Typ -> Wartość malejąco):\n");
        report.append("----------------------------------------\n");

        for (int i = 0; i < entries.size(); i++) {
            AssetReportEntry entry = entries.get(i);
            Asset asset = entry.getAsset();
            double marketValue = entry.getMarketValue();

            report.append(String.format("%s (%s): %s - Wartość: %.2f\n",
                asset.getSymbol(),
                asset.getAssetType().name(),
                asset.getName(),
                marketValue));
        }

        return report.toString();
    }

    private static class AssetReportEntry {
        private final Asset asset;
        private final double marketValue;

        public AssetReportEntry(Asset asset, double marketValue) {
            this.asset = asset;
            this.marketValue = marketValue;
        }

        public Asset getAsset() {
            return asset;
        }

        public double getMarketValue() {
            return marketValue;
        }
    }

    private static class AssetReportComparator implements Comparator<AssetReportEntry> {
        @Override
        public int compare(AssetReportEntry e1, AssetReportEntry e2) {
            AssetType type1 = e1.getAsset().getAssetType();
            AssetType type2 = e2.getAsset().getAssetType();

            int typeComparison = type1.compareTo(type2);
            if (typeComparison != 0) {
                return typeComparison;
            }

            return Double.compare(e2.getMarketValue(), e1.getMarketValue());
        }
    }
}
