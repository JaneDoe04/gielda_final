# Instrukcja wrzucenia projektu na GitHub

## Krok 1: Zainstaluj Git (jeśli nie masz)

1. Pobierz Git z: https://git-scm.com/download/win
2. Zainstaluj z domyślnymi ustawieniami
3. **Zamknij i otwórz ponownie PowerShell** po instalacji

## Krok 2: Skonfiguruj Git (jeśli pierwszy raz)

```powershell
git config --global user.name "Twoja Nazwa"
git config --global user.email "twoj@email.com"
```

## Krok 3: Zainicjalizuj repozytorium

```powershell
cd C:\Users\mojez\Desktop\gielda2.0-main\stock-market-sim
git init
```

## Krok 4: Dodaj pliki

```powershell
git add .
```

## Krok 5: Zrób pierwszy commit

```powershell
git commit -m "Initial commit - Stock Market Simulator v3.0"
```

## Krok 6: Dodaj remote i wypchnij

```powershell
git remote add origin https://github.com/JaneDoe04/gielda_final.git
git branch -M main
git push -u origin main
```

## Jeśli potrzebujesz autoryzacji:

GitHub może poprosić o:
- **Username:** `JaneDoe04`
- **Password:** Token dostępu osobistego (NIE hasło do konta!)

### Jak utworzyć token:

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token
3. Wybierz scope: `repo` (pełny dostęp do repozytoriów)
4. Skopiuj token i użyj jako hasła

---

## Alternatywnie: Użyj GitHub Desktop

Jeśli masz GitHub Desktop:
1. File → Add Local Repository
2. Wybierz folder: `C:\Users\mojez\Desktop\gielda2.0-main\stock-market-sim`
3. Publish repository
4. Wybierz: `JaneDoe04/gielda_final`
