package com.stockmarket.persistence;

import com.stockmarket.domain.Asset;
import com.stockmarket.domain.AssetType;
import com.stockmarket.domain.Commodity;
import com.stockmarket.domain.Currency;
import com.stockmarket.domain.PurchaseLot;
import com.stockmarket.domain.Share;
import com.stockmarket.logic.Portfolio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PortfolioPersistence {
    private static final String SEPARATOR = " | ";
    private static final String HEADER_PREFIX = "HEADER";
    private static final String ASSET_PREFIX = "ASSET";
    private static final String LOT_PREFIX = "LOT";
    private static final String CASH_KEY = "CASH";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public void savePortfolio(Portfolio portfolio, Path filePath) throws IOException {
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio nie może być null");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("Ścieżka pliku nie może być null");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(HEADER_PREFIX + SEPARATOR + CASH_KEY + SEPARATOR + portfolio.getCash());
            writer.newLine();

            List<String> symbols = portfolio.getAllAssetSymbols();
            for (int i = 0; i < symbols.size(); i++) {
                String symbol = symbols.get(i);
                Asset asset = portfolio.getAsset(symbol);
                if (asset != null) {
                    AssetType assetType = asset.getAssetType();

                    writer.write(ASSET_PREFIX + SEPARATOR + assetType.name() + SEPARATOR + symbol);
                    writer.newLine();

                    List<PurchaseLot> lots = portfolio.getPurchaseLots(symbol);
                    for (int j = 0; j < lots.size(); j++) {
                        PurchaseLot lot = lots.get(j);
                        writer.write(LOT_PREFIX + SEPARATOR + 
                            lot.getPurchaseDate().format(DATE_FORMATTER) + SEPARATOR + 
                            lot.getQuantity() + SEPARATOR + 
                            lot.getUnitPrice());
                        writer.newLine();
                    }
                }
            }
        }
    }

    public Portfolio loadPortfolio(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("Ścieżka pliku nie może być null");
        }
        if (!Files.exists(filePath)) {
            throw new IOException("Plik nie istnieje: " + filePath);
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line = reader.readLine();
            if (line == null) {
                throw new DataIntegrityException("Plik jest pusty");
            }

            double cash = parseHeader(line);
            Portfolio portfolio = new Portfolio(cash);

            String currentAssetLine = null;
            AssetType currentAssetType = null;
            String currentSymbol = null;
            List<PurchaseLot> currentLots = new ArrayList<>();
            int expectedQuantity = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s*\\|\\s*");
                if (parts.length < 2) {
                    throw new DataIntegrityException("Nieprawidłowy format linii: " + line);
                }

                String prefix = parts[0].trim();

                if (ASSET_PREFIX.equals(prefix)) {
                    if (currentAssetType != null && !currentLots.isEmpty()) {
                        double firstLotPrice = currentLots.get(0).getUnitPrice();
                        Asset asset = createAssetWithPrice(currentAssetType, currentSymbol, firstLotPrice);
                        validateAndAddAsset(portfolio, asset, currentLots, expectedQuantity);
                    }

                    if (parts.length != 3) {
                        throw new DataIntegrityException("Nieprawidłowy format linii ASSET: " + line);
                    }

                    currentAssetLine = line;
                    currentAssetType = parseAssetType(parts);
                    currentSymbol = parts[2].trim();
                    currentLots = new ArrayList<>();
                    expectedQuantity = 0;
                } else if (LOT_PREFIX.equals(prefix)) {
                    if (currentAssetType == null) {
                        throw new DataIntegrityException("LOT bez poprzedzającego ASSET: " + line);
                    }

                    if (parts.length != 4) {
                        throw new DataIntegrityException("Nieprawidłowy format linii LOT: " + line);
                    }

                    PurchaseLot lot = parseLot(parts);
                    currentLots.add(lot);
                    expectedQuantity += lot.getQuantity();
                } else {
                    throw new DataIntegrityException("Nieznany prefiks: " + prefix);
                }
            }

            if (currentAssetType != null && !currentLots.isEmpty()) {
                double firstLotPrice = currentLots.get(0).getUnitPrice();
                Asset asset = createAssetWithPrice(currentAssetType, currentSymbol, firstLotPrice);
                validateAndAddAsset(portfolio, asset, currentLots, expectedQuantity);
            }

            return portfolio;
        } catch (DateTimeParseException e) {
            throw new DataIntegrityException("Błąd parsowania daty: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new DataIntegrityException("Błąd parsowania liczby: " + e.getMessage());
        }
    }

    private double parseHeader(String line) {
        String[] parts = line.split("\\s*\\|\\s*");
        if (parts.length != 3) {
            throw new DataIntegrityException("Nieprawidłowy format HEADER: " + line);
        }
        if (!HEADER_PREFIX.equals(parts[0].trim())) {
            throw new DataIntegrityException("Oczekiwano HEADER, otrzymano: " + parts[0]);
        }
        if (!CASH_KEY.equals(parts[1].trim())) {
            throw new DataIntegrityException("Oczekiwano CASH, otrzymano: " + parts[1]);
        }

        try {
            double cash = Double.parseDouble(parts[2].trim());
            if (cash < 0) {
                throw new DataIntegrityException("Gotówka nie może być ujemna: " + cash);
            }
            return cash;
        } catch (NumberFormatException e) {
            throw new DataIntegrityException("Nieprawidłowa wartość gotówki: " + parts[2]);
        }
    }

    private AssetType parseAssetType(String[] parts) {
        String assetTypeStr = parts[1].trim();
        try {
            return AssetType.valueOf(assetTypeStr);
        } catch (IllegalArgumentException e) {
            throw new DataIntegrityException("Nieznany typ aktywa: " + assetTypeStr);
        }
    }

    private PurchaseLot parseLot(String[] parts) {
        String dateStr = parts[1].trim();
        String quantityStr = parts[2].trim();
        String priceStr = parts[3].trim();

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new DataIntegrityException("Nieprawidłowy format daty: " + dateStr);
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new DataIntegrityException("Ilość musi być większa od zera: " + quantity);
            }
        } catch (NumberFormatException e) {
            throw new DataIntegrityException("Nieprawidłowa ilość: " + quantityStr);
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                throw new DataIntegrityException("Cena musi być większa od zera: " + price);
            }
        } catch (NumberFormatException e) {
            throw new DataIntegrityException("Nieprawidłowa cena: " + priceStr);
        }

        return new PurchaseLot(date, price, quantity);
    }

    private void validateAndAddAsset(Portfolio portfolio, Asset asset, List<PurchaseLot> lots, int expectedQuantity) {
        if (lots.isEmpty()) {
            throw new DataIntegrityException("Asset bez partii zakupowych");
        }

        int actualQuantity = 0;
        for (int i = 0; i < lots.size(); i++) {
            actualQuantity += lots.get(i).getQuantity();
        }

        if (actualQuantity != expectedQuantity) {
            throw new DataIntegrityException("Niezgodność ilości: oczekiwano " + expectedQuantity + ", otrzymano " + actualQuantity);
        }

        for (int i = 0; i < lots.size(); i++) {
            PurchaseLot lot = lots.get(i);
            Asset assetCopy = asset.createCopy();
            assetCopy.setMarketPrice(lot.getUnitPrice());
            portfolio.addAsset(assetCopy, lot.getQuantity(), lot.getPurchaseDate());
        }
    }

    private Asset createAssetWithPrice(AssetType assetType, String symbol, double marketPrice) {
        switch (assetType) {
            case SHARE:
                return new Share(symbol, symbol, marketPrice);
            case COMMODITY:
                return new Commodity(symbol, symbol, marketPrice);
            case CURRENCY:
                double spread = marketPrice * 0.01;
                if (spread >= marketPrice) {
                    spread = marketPrice * 0.001;
                }
                return new Currency(symbol, symbol, marketPrice, spread);
            default:
                throw new DataIntegrityException("Nieobsługiwany typ aktywa: " + assetType);
        }
    }
}
