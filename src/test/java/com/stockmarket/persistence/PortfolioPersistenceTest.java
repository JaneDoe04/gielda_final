package com.stockmarket.persistence;

import com.stockmarket.domain.Share;
import com.stockmarket.logic.Portfolio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

class PortfolioPersistenceTest {
    private PortfolioPersistence persistence;
    private Portfolio portfolio;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        persistence = new PortfolioPersistence();
        portfolio = new Portfolio(10500.50);
    }

    @Test
    void testSaveAndLoadPortfolio() throws Exception {
        Share share = new Share("AAPL", "Apple Inc.", 150.0);
        portfolio.addAsset(share, 10, LocalDate.of(2023, 5, 10));
        portfolio.addAsset(share, 5, LocalDate.of(2023, 6, 12));

        Path filePath = tempDir.resolve("portfolio.txt");
        persistence.savePortfolio(portfolio, filePath);

        Portfolio loaded = persistence.loadPortfolio(filePath);

        assertEquals(10500.50, loaded.getCash(), 0.01);
        assertEquals(15, loaded.getAssetQuantity("AAPL"));
    }

    @Test
    void testLoadPortfolioWithCorruptedData() throws Exception {
        Path filePath = tempDir.resolve("corrupted.txt");
        Files.write(filePath, "HEADER | CASH | invalid".getBytes());

        assertThrows(DataIntegrityException.class, () -> {
            persistence.loadPortfolio(filePath);
        });
    }

    @Test
    void testLoadPortfolioWithInvalidFormat() throws Exception {
        Path filePath = tempDir.resolve("invalid.txt");
        Files.write(filePath, "INVALID_LINE".getBytes());

        assertThrows(DataIntegrityException.class, () -> {
            persistence.loadPortfolio(filePath);
        });
    }

    @Test
    void testLoadPortfolioWithMissingLots() throws Exception {
        Path filePath = tempDir.resolve("missing_lots.txt");
        String content = "HEADER | CASH | 10000.0\n" +
                        "ASSET | SHARE | AAPL\n";
        Files.write(filePath, content.getBytes());

        assertThrows(DataIntegrityException.class, () -> {
            persistence.loadPortfolio(filePath);
        });
    }

    @Test
    void testLoadPortfolioWithInvalidDate() throws Exception {
        Path filePath = tempDir.resolve("invalid_date.txt");
        String content = "HEADER | CASH | 10000.0\n" +
                        "ASSET | SHARE | AAPL\n" +
                        "LOT | invalid-date | 10 | 150.00\n";
        Files.write(filePath, content.getBytes());

        assertThrows(DataIntegrityException.class, () -> {
            persistence.loadPortfolio(filePath);
        });
    }

    @Test
    void testSaveAndLoadMultipleAssets() throws Exception {
        Share share1 = new Share("AAPL", "Apple Inc.", 150.0);
        Share share2 = new Share("GOOGL", "Google Inc.", 200.0);
        
        portfolio.addAsset(share1, 10, LocalDate.of(2023, 5, 10));
        portfolio.addAsset(share2, 5, LocalDate.of(2023, 6, 12));

        Path filePath = tempDir.resolve("multiple.txt");
        persistence.savePortfolio(portfolio, filePath);

        Portfolio loaded = persistence.loadPortfolio(filePath);

        assertEquals(10, loaded.getAssetQuantity("AAPL"));
        assertEquals(5, loaded.getAssetQuantity("GOOGL"));
    }
}
