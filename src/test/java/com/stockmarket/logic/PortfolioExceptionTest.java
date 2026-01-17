package com.stockmarket.logic;

import com.stockmarket.domain.Share;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class PortfolioExceptionTest {
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio(1000.0);
    }

    @Test
    void testInsufficientFundsException() {
        Share expensiveShare = new Share("EXP", "Expensive", 1000.0);
        
        assertThrows(InsufficientFundsException.class, () -> {
            portfolio.addAsset(expensiveShare, 1, LocalDate.now());
        });
    }

    @Test
    void testInsufficientAssetsException() {
        Share share = new Share("AAPL", "Apple", 100.0);
        portfolio.addAsset(share, 10, LocalDate.now());
        
        assertThrows(InsufficientAssetsException.class, () -> {
            portfolio.sellAsset("AAPL", 15, 150.0);
        });
    }

    @Test
    void testInsufficientAssetsExceptionForNonExistentAsset() {
        assertThrows(InsufficientAssetsException.class, () -> {
            portfolio.sellAsset("NONEXISTENT", 10, 150.0);
        });
    }

    @Test
    void testInsufficientFundsExceptionMessage() {
        Share expensiveShare = new Share("EXP", "Expensive", 1000.0);
        
        InsufficientFundsException exception = assertThrows(
            InsufficientFundsException.class,
            () -> portfolio.addAsset(expensiveShare, 1, LocalDate.now())
        );
        
        assertTrue(exception.getMessage().contains("Niewystarczająca gotówka"));
    }
}
