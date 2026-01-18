package com.stockmarket.logic;

import com.stockmarket.domain.Asset;
import com.stockmarket.domain.AssetType;
import com.stockmarket.domain.Order;
import com.stockmarket.domain.OrderType;
import com.stockmarket.domain.PurchaseLot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Portfolio {
    private double cash;
    private final Map<String, AssetHolding> holdings;
    private final PriorityQueue<Order> pendingOrders;

    private static class AssetHolding {
        private final Asset asset;
        private final List<PurchaseLot> purchaseLots;

        public AssetHolding(Asset asset) {
            this.asset = asset;
            this.purchaseLots = new ArrayList<>();
        }

        public Asset getAsset() {
            return asset;
        }

        public List<PurchaseLot> getPurchaseLots() {
            return purchaseLots;
        }

        public int getTotalQuantity() {
            int total = 0;
            for (int i = 0; i < purchaseLots.size(); i++) {
                total += purchaseLots.get(i).getQuantity();
            }
            return total;
        }
    }

    public Portfolio(double initialCash) {
        if (initialCash < 0) {
            throw new IllegalArgumentException("Początkowa gotówka nie może być ujemna");
        }
        if (Double.isNaN(initialCash) || Double.isInfinite(initialCash)) {
            throw new IllegalArgumentException("Gotówka musi być skończoną liczbą");
        }

        this.cash = initialCash;
        this.holdings = new HashMap<>();
        this.pendingOrders = new PriorityQueue<>(new OrderComparator());
    }

    public void addAsset(Asset asset, int quantity, LocalDate purchaseDate) {
        if (asset == null || quantity <= 0) {
            throw new IllegalArgumentException("Asset nie może być null, a ilość musi być większa od zera");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Data zakupu nie może być null");
        }

        double purchaseCost = asset.getPurchaseCost(quantity);
        if (cash < purchaseCost) {
            throw new InsufficientFundsException("Niewystarczająca gotówka. Wymagane: " + purchaseCost + ", dostępne: " + cash);
        }

        String symbol = asset.getSymbol();
        AssetHolding holding = holdings.get(symbol);
        
        if (holding == null) {
            holding = new AssetHolding(asset.createCopy());
            holdings.put(symbol, holding);
        }

        PurchaseLot lot = new PurchaseLot(purchaseDate, asset.getMarketPrice(), quantity);
        holding.getPurchaseLots().add(lot);
        cash -= purchaseCost;
    }

    public SaleResult sellAsset(String symbol, int quantity, double salePrice) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol nie może być null ani pusty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ilość musi być większa od zera");
        }
        if (salePrice <= 0) {
            throw new IllegalArgumentException("Cena sprzedaży musi być większa od zera");
        }

        AssetHolding holding = holdings.get(symbol);
        if (holding == null) {
            throw new InsufficientAssetsException("Brak aktywa o symbolu: " + symbol);
        }

        int totalQuantity = holding.getTotalQuantity();
        if (totalQuantity < quantity) {
            throw new InsufficientAssetsException("Niewystarczająca ilość aktywów. Wymagane: " + quantity + ", dostępne: " + totalQuantity);
        }

        double totalRevenue = salePrice * quantity;
        double totalProfit = 0.0;
        int remainingToSell = quantity;

        List<PurchaseLot> lots = holding.getPurchaseLots();
        List<PurchaseLot> lotsToRemove = new ArrayList<>();

        for (int i = 0; i < lots.size() && remainingToSell > 0; i++) {
            PurchaseLot lot = lots.get(i);
            int lotQuantity = lot.getQuantity();
            
            if (lotQuantity <= remainingToSell) {
                double lotProfit = lotQuantity * (salePrice - lot.getUnitPrice());
                totalProfit += lotProfit;
                remainingToSell -= lotQuantity;
                lotsToRemove.add(lot);
            } else {
                double lotProfit = remainingToSell * (salePrice - lot.getUnitPrice());
                totalProfit += lotProfit;
                lot.setQuantity(lotQuantity - remainingToSell);
                remainingToSell = 0;
            }
        }

        for (int i = 0; i < lotsToRemove.size(); i++) {
            lots.remove(lotsToRemove.get(i));
        }

        if (lots.isEmpty()) {
            holdings.remove(symbol);
        }

        cash += totalRevenue;
        return new SaleResult(totalRevenue, totalProfit);
    }

    public void addOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Zlecenie nie może być null");
        }

        pendingOrders.offer(order);
    }

    public Order peekNextOrder() {
        if (pendingOrders.isEmpty()) {
            return null;
        }
        return pendingOrders.peek();
    }

    public Order pollNextOrder() {
        if (pendingOrders.isEmpty()) {
            return null;
        }
        return pendingOrders.poll();
    }

    public double auditPortfolio() {
        double totalValue = cash;
        
        for (AssetHolding holding : holdings.values()) {
            Asset asset = holding.getAsset();
            int totalQuantity = holding.getTotalQuantity();
            totalValue += asset.calculateRealValue(totalQuantity);
        }
        
        return totalValue;
    }

    public double getCash() {
        return cash;
    }

    public int getHoldingsCount() {
        return holdings.size();
    }

    public int getAssetQuantity(String symbol) {
        if (symbol == null) {
            return 0;
        }
        AssetHolding holding = holdings.get(symbol);
        if (holding == null) {
            return 0;
        }
        return holding.getTotalQuantity();
    }

    public Asset getAsset(String symbol) {
        if (symbol == null) {
            return null;
        }
        AssetHolding holding = holdings.get(symbol);
        if (holding == null) {
            return null;
        }
        return holding.getAsset();
    }

    public List<PurchaseLot> getPurchaseLots(String symbol) {
        if (symbol == null) {
            return new ArrayList<>();
        }
        AssetHolding holding = holdings.get(symbol);
        if (holding == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(holding.getPurchaseLots());
    }

    public List<String> getAllAssetSymbols() {
        return new ArrayList<>(holdings.keySet());
    }

    private static class OrderComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            double att1 = o1.calculateAttractiveness();
            double att2 = o2.calculateAttractiveness();
            return Double.compare(att2, att1);
        }
    }
}
