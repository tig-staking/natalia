# Natalia na tropie

**Natalia na tropie** to prywatna aplikacja Android tworzona jako interaktywna gra podróżnicza na rodzinny wyjazd do Barcelony.

Aplikacja ma łączyć prawdziwe zwiedzanie miasta z lekką mechaniką gry:

- GPS i wykrywanie odwiedzanych miejsc,
- misje terenowe,
- zagadki i krótkie quizy,
- ciekawostki,
- podstawowe słowa po hiszpańsku,
- XP i gwiazdki,
- odznaki i paszport podróżniczy,
- prawdziwe nagrody ustalane przez rodzica,
- mangowo-komiksowe mikroanimacje,
- spersonalizowaną postać głównej bohaterki.

## Cel projektu

Najważniejszym celem jest stworzenie atrakcyjnej, osobistej aplikacji dla 10-letniej Natalii, która zachęci ją do aktywnego odkrywania Barcelony.

Aplikacja nie ma być klasycznym przewodnikiem.

Ma działać bardziej jak lekka gra terenowa, w której prawdziwe miasto jest planszą.

Przykładowy flow:

```text
GPS wykrywa miejsce
        ↓
NOWA MISJA!
        ↓
krótka animacja
        ↓
zadanie w prawdziwym otoczeniu
        ↓
quiz / hiszpańskie słowo
        ↓
XP + gwiazdki
        ↓
odznaka
        ↓
paszport
```

## Aktualny etap

Na początku budujemy wyłącznie **MVP silnika gry**.

Nie tworzymy jeszcze pełnej zawartości Barcelony.

Pierwsza wersja ma zawierać tylko jeden przykładowy punkt, np. Sagrada Família, aby sprawdzić cały techniczny flow.

MVP powinno umożliwiać:

- wyświetlenie mapy,
- pobranie lokalizacji GPS,
- wykrycie bliskości punktu,
- odblokowanie misji,
- pokazanie mikroanimacji,
- wykonanie prostego questa,
- rozwiązanie quizu,
- zdobycie XP i gwiazdek,
- zdobycie odznaki,
- zapis postępu,
- wymianę gwiazdek na przykładową nagrodę,
- ponowne uruchomienie aplikacji bez utraty danych.

Dopiero po potwierdzeniu, że silnik działa poprawnie, zostanie dodana pełna zawartość Barcelony.

## Technologia

Obecny stack silnika:

- Kotlin
- Jetpack Compose
- Material 3
- DataStore
- Google Play Services Location
- Android TextToSpeech
- MapLibre Native + OpenFreeMap / OpenStreetMap (online street map)

Mapa ulic ładuje się online i nie wymaga klucza Google. Rdzeń gry, check-in i zapis postępu działają lokalnie; pakiet map offline nie jest jeszcze dostępny.

Projekt ma działać możliwie **local-first**.

Nie planujemy w MVP:

- backendu,
- kont użytkowników,
- logowania,
- reklam,
- analytics,
- cloud sync.

## Styl

Aplikacja ma mieć charakter:

- lekko mangowy,
- komiksowy,
- przygodowy,
- kolorowy,
- estetyczny,
- nie infantylny.

Jednym z głównych elementów będą krótkie mikroanimacje przypominające motion comic.

Przykładowe reakcje:

- odkrycie miejsca,
- wykonanie misji,
- poprawna odpowiedź,
- zdobycie gwiazdek,
- level-up,
- zdobycie odznaki,
- odebranie nagrody.

## Architektura

Silnik aplikacji powinien być oddzielony od danych miasta.

Barcelona będzie pierwszym **City Packiem**.

Przykładowo:

```text
assets/
└── citypacks/
    └── barcelona.json
```

Interfejs nie powinien zawierać osobnych ekranów kodowanych na sztywno dla każdej atrakcji.

Zamiast tego jeden uniwersalny `PlaceScreen` powinien renderować dane z City Packa.

Dzięki temu w przyszłości możliwe będzie dodanie kolejnych miast bez przebudowy całego silnika.

## GitHub workflow

To repozytorium jest głównym źródłem prawdy dla kodu.

Projekt ma umożliwiać pracę na wielu komputerach.

Typowy workflow:

```text
git pull
↓
praca w Android Studio / Work / Codex
↓
testy
↓
commit
↓
git push
```

Nie należy używać hardcodowanych lokalnych ścieżek zależnych od konkretnego komputera.

## Uruchomienie i orientacja w kodzie

Instrukcje klonowania, konfiguracji Android Studio, budowania APK i pracy na kilku komputerach znajdują się w [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md). Krótko: `git clone`, otwórz katalog projektu w Android Studio, poczekaj na Gradle Sync i uruchom konfigurację `app`.

### Szybki podgląd obecnej wersji

1. Otwórz projekt w Android Studio i uruchom `app` na emulatorze lub telefonie. Gotowy lokalny plik po komendzie `gradlew.bat assembleDebug` znajduje się w `app/build/outputs/apk/debug/app-debug.apk`.
2. Przejdź onboarding i na ekranie głównym otwórz `Developer mode`. Przycisk `ZALICZ NASTĘPNY ETAP` pozwala przejść kolejno przez odkrycie, misję, słówko, quiz i paszport bez pobytu w Barcelonie.
3. Dodaj testowe gwiazdki w Developer Mode, poproś o lody i otwórz Tryb rodzica. Ustaw czterocyfrowy PIN, zatwierdź prośbę i sprawdź saldo oraz historię. Zamknij i uruchom aplikację ponownie, aby zobaczyć zapisany postęp.
4. Do testu prawdziwego GPS na telefonie wybierz w Developer Mode `UTWÓRZ TESTOWE MIEJSCE TUTAJ`, a następnie użyj `JESTEM NA MIEJSCU — SPRAWDŹ GPS`. Testowy punkt jest zapisany tylko na tym urządzeniu.

Mapa przygody wyświetla online ulice OpenStreetMap przez MapLibre i OpenFreeMap, wraz z punktem misji, obszarem geofence i ostatnią pozycją GPS. Nie wymaga klucza Google Maps. Do działania mapy potrzebny jest internet; obecnie aplikacja nie pobiera jeszcze map offline. Opcjonalny pakiet offline pozostaje kolejnym etapem, z docelowym limitem 200 MB i zależnością od licencji dostawcy danych. Szczegóły są w [docs/LOCATION.md](docs/LOCATION.md).

Opis ról poszczególnych plików źródłowych i wskazówki, gdzie szukać typowych błędów, znajdują się w [docs/CODE_MAP.md](docs/CODE_MAP.md). Bieżący stan prac i następne kroki zapisujemy w [WORKLOG.md](WORKLOG.md).

## Bezpieczeństwo

Nie commitujemy:

- Google Maps API keys,
- tokenów,
- haseł,
- plików `local.properties`,
- innych sekretów.

Sekrety muszą być przechowywane lokalnie.

## Testowanie lokalizacji

Ponieważ development odbywa się poza Barceloną, aplikacja powinna posiadać **Developer Mode**.

Developer Mode ma umożliwiać m.in.:

- symulację wejścia do punktu,
- symulację lokalizacji,
- ręczne odblokowanie misji,
- przyznawanie punktów,
- reset postępu,
- utworzenie testowego punktu GPS w aktualnej lokalizacji telefonu.

Dzięki temu możliwy będzie prawdziwy test terenowy przed wyjazdem.

## Repozytorium

Projekt prywatny.

Aktualny status:

**MVP / development**
