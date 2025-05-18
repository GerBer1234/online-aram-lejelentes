# Online Áram Lejelentés

Ez az Android alkalmazás egy online áramfogyasztás lejelentő app, mely lehetővé teszi a felhasználók
számára, hogy:

- Regisztráljanak és bejelentkezzenek (Firebase Authentication)
- Lejelentsék aktuális áramóra-állásaikat
- Megtekintsék korábbi lejelentéseiket
- Visszajelzést kapjanak fogyasztásukról és várható költségükről
- Értesítést kapjanak havonta a lejelentés esedékességéről
- Felhasználóbarát és reszponzív felületet használjanak

## Funkciók

- **Felhasználói regisztráció és bejelentkezés**
- **Áramfogyasztás rögzítése időszak szerint**
- **Éves fogyasztási összesítés és költségbecslés**
- **Havi emlékeztető értesítés**
- **Bejegyzések listázása és szerkesztése**
- **Időkorlátozott törlési lehetőség (5 percen belül)**
- **Bejegyzések megjegyzés mezőjének módosítása**
- **Automatikus lakcím kitöltés GPS alapján regisztrációkor**
- **Splash képernyő animációval és hanggal**

## Fejlesztési környezet

- Android Studio | Meerkat
- Java
- Firebase
- Google Play Services
- GitHub

## Felépítés

- `HomeActivity`: főképernyő, bejelentések, statisztika
- `RegisterActivity`, `MainActivity`: regisztráció és belépés
- `NewReadingActivity`: új lejelentés létrehozása
- `ReadingAdapter`: lista megjelenítése
- `NotificationHandler`: havi értesítések kezelése
- `Reading`: adatmodell

## Fejlesztő

B8B2HT
