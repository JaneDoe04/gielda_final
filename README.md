# Stock Market Simulator - Silnik Transakcyjny Rynku Kapitałowego

Projekt symulatora giełdy z implementacją logiki FIFO, persystencji danych i systemu zleceń.

## Struktura projektu

```
src/
├── main/java/com/stockmarket/
│   ├── domain/
│   │   ├── Asset.java (klasa abstrakcyjna)
│   │   ├── AssetType.java (enum)
│   │   ├── Share.java
│   │   ├── Commodity.java
│   │   ├── Currency.java
│   │   ├── PurchaseLot.java
│   │   ├── Order.java
│   │   └── OrderType.java (enum)
│   ├── logic/
│   │   ├── Portfolio.java
│   │   ├── InsufficientFundsException.java
│   │   ├── InsufficientAssetsException.java
│   │   └── SaleResult.java
│   ├── persistence/
│   │   ├── PortfolioPersistence.java
│   │   └── DataIntegrityException.java
│   └── reporting/
│       └── PortfolioReporter.java
└── test/java/com/stockmarket/
    ├── logic/
    │   ├── PortfolioFIFOTest.java
    │   ├── PortfolioPriorityQueueTest.java
    │   └── PortfolioExceptionTest.java
    └── persistence/
        └── PortfolioPersistenceTest.java
```

## Format plików zapisu portfela

System używa własnego formatu tekstowego do zapisu i odczytu stanu portfela.

### Format

Każda linia składa się z prefiksu i wartości oddzielonych separatorem ` | ` (spacja, pipe, spacja).

#### Nagłówek (HEADER)
```
HEADER | CASH | <wartość_gotówki>
```

#### Aktywo (ASSET)
```
ASSET | <typ_aktywa> | <symbol>
```

Dostępne typy aktywów:
- `SHARE` - akcje
- `COMMODITY` - surowce
- `CURRENCY` - waluty

#### Partia zakupowa (LOT)
```
LOT | <data_zakupu> | <ilość> | <cena_jednostkowa>
```

Format daty: `YYYY-MM-DD` (ISO 8601)

### Przykład pliku

```
HEADER | CASH | 10500.50
ASSET | SHARE | AAPL
LOT | 2023-05-10 | 10 | 150.00
LOT | 2023-06-12 | 5 | 155.00
ASSET | COMMODITY | GOLD
LOT | 2023-07-01 | 20 | 2000.00
```

### Walidacja

System waliduje:
- Poprawność formatu każdej linii
- Spójność danych (suma ilości w partiach)
- Poprawność typów danych (liczba, data)
- Niepustość pliku
- Obecność partii dla każdego aktywa

W przypadku błędu walidacji rzucany jest wyjątek `DataIntegrityException`.

## Funkcjonalności

### 1. Model domenowy z Purchase Lots
- Każdy asset ma historię zakupów (lista partii)
- Każda partia zawiera: datę zakupu, cenę jednostkową, ilość
- Obsługa różnych typów aktywów (Share, Commodity, Currency)

### 2. Portfolio z optymalnymi strukturami danych
- **Map** dla dostępu O(1) do aktywów po symbolu
- **PriorityQueue** dla zleceń (sortowanie po atrakcyjności ceny)
- Lista partii zakupowych dla każdego aktywa

### 3. Algorytm FIFO
- Sprzedaż zawsze zaczyna się od najstarszej partii
- Obsługa sprzedaży wielopartiowej
- Precyzyjne obliczanie zysku/straty dla każdej transakcji

### 4. Persystencja
- Zapis/odczyt stanu portfela do/z pliku
- Walidacja spójności danych
- Obsługa błędów I/O

### 5. Raportowanie
- Generowanie raportów tekstowych
- Sortowanie aktywów: Typ -> Wartość rynkowa (malejąco)
- Własny Comparator (bez Stream API)

## Uruchamianie testów

```bash
mvn test
```

lub z Maven Wrapper:

```bash
.\mvnw.cmd test
```

## Ograniczenia techniczne

Projekt spełnia następujące ograniczenia:
- ❌ **Zakaz Stream API** - wszystkie operacje na kolekcjach używają klasycznych pętli
- ❌ **Zakaz Java Records** - używane są klasyczne POJO
- ✅ **Enum zamiast String** - typy aktywów i zleceń reprezentowane przez Enum
- ✅ **Tylko JUnit 5** - brak zewnętrznych frameworków (poza JUnit)
