package com.stockmarket.domain;

import java.time.LocalDate;

public class PurchaseLot {
    private final LocalDate purchaseDate;
    private final double unitPrice;
    private int quantity;

    public PurchaseLot(LocalDate purchaseDate, double unitPrice, int quantity) {
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Data zakupu nie może być null");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Cena jednostkowa musi być większa od zera");
        }
        if (Double.isNaN(unitPrice) || Double.isInfinite(unitPrice)) {
            throw new IllegalArgumentException("Cena jednostkowa musi być skończoną liczbą");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ilość musi być większa od zera");
        }

        this.purchaseDate = purchaseDate;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Ilość nie może być ujemna");
        }
        this.quantity = quantity;
    }

    public double getTotalValue() {
        return unitPrice * quantity;
    }
}
