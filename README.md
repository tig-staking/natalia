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

Planowany stack:

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- MVVM
- Room
- DataStore
- Google Maps SDK for Android
- Google Play Services Location
- Android TextToSpeech
- lokalne powiadomienia
- opcjonalnie Android Geofencing API

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

## Uruchomienie projektu

Instrukcje klonowania, konfiguracji Android Studio, budowania APK i pracy na kilku komputerach znajdują się w [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md). Krótko: `git clone`, otwórz katalog projektu w Android Studio, poczekaj na Gradle Sync i uruchom konfigurację `app`.

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
