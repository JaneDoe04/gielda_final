package com.stockmarket.logic;

import com.stockmarket.domain.Share;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class PortfolioFIFOTest {
    private Portfolio portfolio;
    private Share shareXYZ;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio(100000.0);
        shareXYZ = new Share("XYZ", "XYZ Company", 100.0);
    }

    @Test
    void testFIFOSingleLotSale() {
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 1, 1));
        
        SaleResult result = portfolio.sellAsset("XYZ", 10, 150.0);
        
        assertEquals(1500.0, result.getTotalRevenue(), 0.01);
        assertEquals(500.0, result.getTotalProfit(), 0.01);
        assertEquals(0, portfolio.getAssetQuantity("XYZ"));
    }

    @Test
    void testFIFOMultiLotSale() {
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 1, 1));
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 2, 1));
        
        SaleResult result = portfolio.sellAsset("XYZ", 15, 150.0);
        
        assertEquals(2250.0, result.getTotalRevenue(), 0.01);
        double expectedProfit = 10 * (150.0 - 100.0) + 5 * (150.0 - 120.0);
        assertEquals(expectedProfit, result.getTotalProfit(), 0.01);
        assertEquals(5, portfolio.getAssetQuantity("XYZ"));
    }

    @Test
    void testFIFOPartialLotRemaining() {
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 1, 1));
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 2, 1));
        
        SaleResult result = portfolio.sellAsset("XYZ", 15, 150.0);
        
        assertEquals(5, portfolio.getAssetQuantity("XYZ"));
        
        var lots = portfolio.getPurchaseLots("XYZ");
        assertEquals(1, lots.size());
        assertEquals(5, lots.get(0).getQuantity());
        assertEquals(120.0, lots.get(0).getUnitPrice(), 0.01);
    }

    @Test
    void testFIFOCompleteLotRemoval() {
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 1, 1));
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 2, 1));
        
        portfolio.sellAsset("XYZ", 10, 150.0);
        
        var lots = portfolio.getPurchaseLots("XYZ");
        assertEquals(1, lots.size());
        assertEquals(10, lots.get(0).getQuantity());
        assertEquals(120.0, lots.get(0).getUnitPrice(), 0.01);
    }

    @Test
    void testFIFOInsufficientAssets() {
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 1, 1));
        
        assertThrows(InsufficientAssetsException.class, () -> {
            portfolio.sellAsset("XYZ", 15, 150.0);
        });
    }

    @Test
    void testFIFOProfitCalculation() {
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 1, 1));
        portfolio.addAsset(shareXYZ, 10, LocalDate.of(2023, 2, 1));
        
        SaleResult result = portfolio.sellAsset("XYZ", 15, 150.0);
        
        double lot1Profit = 10 * (150.0 - 100.0);
        double lot2Profit = 5 * (150.0 - 120.0);
        double expectedTotalProfit = lot1Profit + lot2Profit;
        
        assertEquals(expectedTotalProfit, result.getTotalProfit(), 0.01);
    }
}
