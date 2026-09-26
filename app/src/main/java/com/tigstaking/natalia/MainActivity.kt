package com.tigstaking.natalia

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.tigstaking.natalia.game.CityPackRepository
import com.tigstaking.natalia.game.GameEngine
import com.tigstaking.natalia.game.DebugTestPoi
import com.tigstaking.natalia.game.GameProgress
import com.tigstaking.natalia.game.GameProgressRepository
import com.tigstaking.natalia.game.ProgressBackupCodec
import com.tigstaking.natalia.game.Coordinates
import com.tigstaking.natalia.game.Place
import com.tigstaking.natalia.game.PlaceCheckInResult
import com.tigstaking.natalia.game.PlayerLevel
import com.tigstaking.natalia.game.Proximity
import com.tigstaking.natalia.game.ProximityResult
import com.tigstaking.natalia.game.QuizAnswerResult
import com.tigstaking.natalia.game.location.FusedLocationProvider
import com.tigstaking.natalia.game.speech.SpanishSpeechController
import com.tigstaking.natalia.game.speech.SpanishSpeechStatus
import com.tigstaking.natalia.game.location.LocationPermissionRequiredException
import com.tigstaking.natalia.game.location.LocationServicesDisabledException
import com.tigstaking.natalia.game.location.CurrentLocationUnavailableException
import com.tigstaking.natalia.security.ParentPinStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NataliaNaTropieApp() }
    }
}

private enum class GameScreen { ONBOARDING, HOME, MAP, PLACE, QUEST, WORD, QUIZ, PASSPORT }

@Composable
private fun NataliaNaTropieApp() {
    val context = LocalContext.current
    val progressRepository = remember(context) { GameProgressRepository(context) }
    val parentPinStore = remember(context) { ParentPinStore(context) }
    val engine = remember(progressRepository) { GameEngine(progressRepository) }
    val basePlace = remember(context) { CityPackRepository(context).loadBarcelona().places.first() }
    val savedTestPoi by progressRepository.debugTestPoi.collectAsState(initial = null)
    val place = remember(basePlace, savedTestPoi) {
        savedTestPoi?.let { testPoi ->
            basePlace.copy(
                id = testPoi.id,
                name = "Testowy punkt",
                coordinates = testPoi.coordinates,
                geofenceRadiusMeters = 120,
                intro = "To testowy punkt GPS utworzony na bieżącej pozycji.",
                fact = "Wróć do tego miejsca, aby sprawdzić odblokowanie misji.",
                quest = basePlace.quest.copy(
                    id = "${testPoi.id}-quest",
                    prompt = "Rozejrzyj się dookoła i potwierdź, że jesteś w pobliżu punktu.",
                ),
                quiz = basePlace.quiz.copy(id = "${testPoi.id}-quiz"),
                badge = "ODKRYWCA TESTOWEGO PUNKTU",
            )
        } ?: basePlace
    }
    val locationProvider = remember(context) { FusedLocationProvider(context) }
    val spanishSpeech = remember(context) { SpanishSpeechController(context) }
    val speechStatus by spanishSpeech.status.collectAsState()
    DisposableEffect(spanishSpeech) { onDispose { spanishSpeech.close() } }
    val progress by progressRepository.progress.collectAsState(initial = GameProgress())
    val onboardingCompleted by progressRepository.onboardingCompleted.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(GameScreen.ONBOARDING) }
    var message by remember { mutableStateOf("") }
    var celebration by remember { mutableStateOf("") }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showParentDialog by remember { mutableStateOf(false) }
    var parentAuthenticated by remember { mutableStateOf(false) }
    var parentQuestPending by remember { mutableStateOf(false) }
    var parentPin by remember { mutableStateOf("") }
    var pinConfirmation by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf("") }
    var gpsDiagnostic by remember { mutableStateOf("") }
    var createTestPoiAfterPermission by remember { mutableStateOf(false) }
    var mapLocationAfterPermission by remember { mutableStateOf(false) }
    var mapCoordinates by remember { mutableStateOf<Coordinates?>(null) }
    var mapAccuracy by remember { mutableStateOf<Double?>(null) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var importPin by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf("") }
    val exportBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) scope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) { progressRepository.exportBackupJson() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(json) }
                        ?: error("Nie można zapisać pliku.")
                }
            }.onSuccess { message = "Kopia postępu została zapisana." }
                .onFailure { message = "Nie udało się zapisać kopii postępu." }
        }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri) ?: error("Nie można odczytać pliku.")
                    input.use { stream ->
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var total = 0
                        while (true) {
                            val count = stream.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= 1_000_000) { "Plik kopii jest zbyt duży." }
                            output.write(buffer, 0, count)
                        }
                        output.toString("UTF-8").also(ProgressBackupCodec::decode)
                    }
                }
            }.onSuccess {
                pendingImportJson = it
                importPin = ""
                importError = ""
                showParentDialog = false
                parentAuthenticated = false
                parentPin = ""
            }.onFailure { message = "Nieprawidłowa kopia postępu lub błąd odczytu pliku." }
        }
    }
    LaunchedEffect(onboardingCompleted) {
        if (onboardingCompleted == true && screen == GameScreen.ONBOARDING) screen = GameScreen.HOME
    }
    LaunchedEffect(celebration) {
        if (celebration.isNotBlank()) {
            delay(1_400)
            celebration = ""
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                showParentDialog = false
                parentAuthenticated = false
                parentQuestPending = false
                parentPin = ""
                pinConfirmation = ""
                pinMessage = ""
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    suspend fun createTestPoi() {
        message = "Pobieram GPS i tworzę testowy punkt…"
        try {
            val location = locationProvider.currentLocation()
            val testId = "debug-${UUID.randomUUID()}"
            progressRepository.saveDebugTestPoi(DebugTestPoi(testId, location.coordinates))
            screen = GameScreen.PLACE
            message = "Utworzono punkt GPS zapisany na tym urządzeniu, promień 120 m (±${location.accuracyMeters.toInt()} m)."
        } catch (error: LocationPermissionRequiredException) {
            message = "Brak zgody na lokalizację. Zezwól aplikacji na dostęp podczas używania."
        } catch (error: LocationServicesDisabledException) {
            message = "Lokalizacja urządzenia jest wyłączona. Włącz GPS i spróbuj ponownie."
        } catch (error: CurrentLocationUnavailableException) {
            message = "Nie ma jeszcze świeżej pozycji. Wyjdź na otwartą przestrzeń i spróbuj ponownie."
        } catch (error: Exception) {
            message = "Nie udało się utworzyć punktu. Sprawdź GPS i spróbuj ponownie."
        }
    }

    suspend fun checkIn() {
        message = "Sprawdzam lokalizację…"
        try {
            val location = locationProvider.currentLocation()
            when (val result = engine.checkIn(place, location.coordinates, location.accuracyMeters.toDouble())) {
                is PlaceCheckInResult.Confirmed -> {
                    message = "Miejsce odkryte! Dokładność GPS ±${location.accuracyMeters.toInt()} m."
                    celebration = levelUpCelebration(progress.xp, result.progress.xp)
                        ?: "NOWA MISJA! ${place.name}"
                    screen = GameScreen.QUEST
                }
                is PlaceCheckInResult.TooFar ->
                    message = "Jesteś około ${result.distanceMeters.toInt()} m od celu. Podejdź bliżej."
                is PlaceCheckInResult.NeedBetterAccuracy ->
                    message = "GPS jest zbyt niedokładny (±${result.accuracyMeters.toInt()} m). Spróbuj ponownie."
            }
        } catch (error: LocationPermissionRequiredException) {
            message = "Brak zgody na lokalizację. Zezwól aplikacji na dostęp podczas używania."
        } catch (error: LocationServicesDisabledException) {
            message = "Lokalizacja urządzenia jest wyłączona. Włącz GPS i spróbuj ponownie."
        } catch (error: CurrentLocationUnavailableException) {
            message = "Nie ma jeszcze świeżej pozycji. Wyjdź na otwartą przestrzeń i spróbuj ponownie."
        } catch (error: Exception) {
            message = "Nie udało się pobrać lokalizacji. Sprawdź GPS i spróbuj ponownie."
        }
    }

    suspend fun finishQuest(answerIndex: Int? = null, parentConfirmed: Boolean = false): Boolean {
        return try {
            val updated = engine.completeQuest(place, answerIndex, parentConfirmed)
            message = "Misja ukończona! +${place.rewards.quest.xp} XP, +${place.rewards.quest.stars} ★"
            celebration = levelUpCelebration(progress.xp, updated.xp) ?: "JEST! MISJA UKOŃCZONA"
            screen = GameScreen.WORD
            true
        } catch (_: IllegalStateException) {
            message = if (place.quest.type == com.tigstaking.natalia.game.QuestType.MULTIPLE_CHOICE) {
                "Hmm… wybierz właściwą odpowiedź i spróbuj ponownie."
            } else "Nie można jeszcze ukończyć tej misji."
            celebration = "Hmm… SPRÓBUJ PONOWNIE"
            false
        }
    }

    suspend fun diagnoseGps() {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
        val networkEnabled = runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
        val permission = when {
            fineGranted -> "dokładna"
            coarseGranted -> "przybliżona"
            else -> "brak"
        }
        val providers = "GPS: ${if (gpsEnabled) "włączony" else "wyłączony"}, sieć: ${if (networkEnabled) "włączona" else "wyłączona"}"
        gpsDiagnostic = "Uprawnienie: $permission · $providers"
        if (!fineGranted && !coarseGranted) {
            gpsDiagnostic += "\nBrak zgody lokalizacyjnej. Otwórz ekran miejsca, aby jej udzielić."
            return
        }
        if (!gpsEnabled && !networkEnabled) {
            gpsDiagnostic += "\nWłącz lokalizację urządzenia w ustawieniach."
            return
        }
        try {
            val fix = locationProvider.currentLocation()
            val ageSeconds = ((System.currentTimeMillis() - fix.capturedAtMillis).coerceAtLeast(0L) / 1_000L)
            val distance = Proximity.distanceMeters(fix.coordinates, place.coordinates)
            val zone = when (Proximity.classify(
                fix.coordinates, place.coordinates, place.geofenceRadiusMeters, fix.accuracyMeters.toDouble(),
            )) {
                is ProximityResult.Inside -> "w strefie"
                is ProximityResult.Outside -> "poza strefą"
                is ProximityResult.Uncertain -> "niepewna — popraw dokładność GPS"
            }
            gpsDiagnostic += "\nOstatni odczyt: ${"%.5f".format(fix.coordinates.latitude)}, " +
                "${"%.5f".format(fix.coordinates.longitude)}, dokładność ±${fix.accuracyMeters.toInt()} m, wiek $ageSeconds s."
            gpsDiagnostic += "\nOdległość do ${place.name}: ${distance.toInt()} m. Strefa: $zone."
        } catch (error: LocationPermissionRequiredException) {
            gpsDiagnostic += "\nSystem nie przyznał aplikacji uprawnienia do odczytu lokalizacji."
        } catch (error: LocationServicesDisabledException) {
            gpsDiagnostic += "\nUsługi lokalizacyjne są wyłączone."
        } catch (error: CurrentLocationUnavailableException) {
            gpsDiagnostic += "\nBrak świeżej pozycji. Spróbuj na zewnątrz lub ponownie za chwilę."
        } catch (error: Exception) {
            gpsDiagnostic += "\nOdczyt nie powiódł się. Sprawdź ustawienia lokalizacji."
        }
    }

    suspend fun refreshMapLocation() {
        message = "Pobieram pozycję do mapy…"
        try {
            val fix = locationProvider.currentLocation()
            mapCoordinates = fix.coordinates
            mapAccuracy = fix.accuracyMeters.toDouble()
            message = "Pozycja mapy zaktualizowana."
        } catch (error: LocationPermissionRequiredException) {
            message = "Brak zgody na lokalizację."
        } catch (error: LocationServicesDisabledException) {
            message = "Włącz lokalizację urządzenia, aby pokazać swoją pozycję."
        } catch (error: CurrentLocationUnavailableException) {
            message = "Brak świeżej pozycji. Spróbuj ponownie na zewnątrz."
        } catch (error: Exception) {
            message = "Nie udało się pobrać pozycji. Spróbuj ponownie."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val permissionGranted = permissions.values.any { it }
        if (createTestPoiAfterPermission) {
            createTestPoiAfterPermission = false
            if (permissionGranted) scope.launch { createTestPoi() }
            else message = "Zezwól na lokalizację podczas używania aplikacji, aby utworzyć testowy punkt."
        } else if (mapLocationAfterPermission) {
            mapLocationAfterPermission = false
            if (permissionGranted) scope.launch { refreshMapLocation() }
            else message = "Zezwól na lokalizację, aby pokazać swoją pozycję na mapie."
        } else if (permissionGranted) {
            scope.launch { checkIn() }
        } else {
            message = "Zezwól na lokalizację podczas używania aplikacji, aby odkryć miejsce."
        }
    }

    fun requestMapLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) scope.launch { refreshMapLocation() }
        else {
            mapLocationAfterPermission = true
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    fun requestCheckIn() {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) scope.launch { checkIn() }
        else permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }

    fun requestCreateTestPoi() {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            scope.launch { createTestPoi() }
        } else {
            createTestPoiAfterPermission = true
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }

    val level = PlayerLevel.forXp(progress.xp)
    val animatedXp by animateIntAsState(progress.xp, animationSpec = tween(650), label = "player-xp")
    val animatedStars by animateIntAsState(progress.stars, animationSpec = tween(650), label = "player-stars")
    val nextScreen = nextGameScreen(progress, place)

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("NATALIA NA TROPIE", style = MaterialTheme.typography.headlineMedium)
                Text("Barcelona · ${place.name}")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${level.title} · Poziom ${PlayerLevel.entries.indexOf(level) + 1}")
                        Text("$animatedXp XP     ★ $animatedStars")
                        Text("Paszport: ${if (place.id in progress.discoveredPlaceIds) "${place.name} ✓" else "czeka na pierwsze odkrycie"}")
                    }
                }

                when (screen) {
                    GameScreen.ONBOARDING -> {
                        Text("CZEŚĆ, NATALIA!", style = MaterialTheme.typography.headlineMedium)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("NATALIA NA TROPIE", style = MaterialTheme.typography.titleLarge)
                                Text("Czeka na Ciebie przygoda w Barcelonie. Odkrywaj miejsca, wykonuj misje, ucz się hiszpańskiego i zbieraj odznaki.")
                                Text("Twój postęp zapisuje się na tym urządzeniu. Lokalizacja jest używana tylko do sprawdzania, czy jesteś przy miejscu.")
                            }
                        }
                        Button(onClick = {
                            scope.launch {
                                progressRepository.completeOnboarding()
                                screen = GameScreen.HOME
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("ROZPOCZNIJ PRZYGODĘ") }
                    }
                    GameScreen.MAP -> {
                        Text("MAPA PRZYGODY", style = MaterialTheme.typography.titleLarge)
                        val completed = place.quiz.id in progress.completedQuizIds
                        val discovered = place.id in progress.discoveredPlaceIds
                        val mapStatus = when {
                            completed -> "UKOŃCZONE"
                            discovered && place.quest.id in progress.completedQuestIds -> "W TOKU"
                            discovered -> "ODKRYTE"
                            else -> "ZABLOKOWANE"
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${place.name} · $mapStatus", style = MaterialTheme.typography.titleMedium)
                                StreetMap(place.coordinates, mapCoordinates, place.geofenceRadiusMeters)
                                Text("Mapa online: OpenFreeMap · dane OpenStreetMap")
                                Text(place.name)
                                Text("Punkt: ${"%.5f".format(place.coordinates.latitude)}, ${"%.5f".format(place.coordinates.longitude)}")
                                val userPoint = mapCoordinates
                                if (userPoint != null) {
                                    val distance = com.tigstaking.natalia.game.Proximity.distanceMeters(userPoint, place.coordinates)
                                    Text("Twoja pozycja: ${"%.5f".format(userPoint.latitude)}, ${"%.5f".format(userPoint.longitude)}")
                                    Text("Odległość: ${distance.toInt()} m · dokładność ±${mapAccuracy?.toInt() ?: "?"} m")
                                } else Text("Twoja pozycja nie jest jeszcze dostępna.")
                            }
                        }
                        OutlinedButton(onClick = ::requestMapLocation, modifier = Modifier.fillMaxWidth()) {
                            Text("ODŚWIEŻ MOJĄ POZYCJĘ")
                        }
                        Button(onClick = { screen = GameScreen.PLACE }, modifier = Modifier.fillMaxWidth()) {
                            Text("OTWÓRZ MIEJSCE")
                        }
                    }
                    GameScreen.HOME -> {
                        Text("🕵️‍♀️", style = MaterialTheme.typography.displayMedium)
                        Text("Cześć, Natalia! Twoja przygoda w Barcelonie czeka.")
                        Button(onClick = { screen = nextScreen }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (place.id in progress.discoveredPlaceIds) "KONTYNUUJ PRZYGODĘ" else "POKAŻ MIEJSCE")
                        }
                        OutlinedButton(onClick = { screen = GameScreen.MAP; requestMapLocation() }, modifier = Modifier.fillMaxWidth()) { Text("MAPA PRZYGODY") }
                        OutlinedButton(onClick = { screen = nextScreen }, modifier = Modifier.fillMaxWidth()) { Text("MISJE") }
                        OutlinedButton(onClick = { screen = GameScreen.PASSPORT }, modifier = Modifier.fillMaxWidth()) { Text("PASZPORT") }
                        Text("NAGRODY", style = MaterialTheme.typography.titleMedium)
                        val reservedStars = progress.pendingRewardRequests.values.sumOf { it.cost }
                        Button(
                            enabled = progress.stars - reservedStars >= 100,
                            onClick = {
                                scope.launch {
                                    val requestId = "treat-${UUID.randomUUID()}"
                                    val created = progressRepository.requestRedemptionOnce(requestId, 100)
                                    message = if (created) "Prośba o lody wysłana do rodzica (100 ★)."
                                        else "Nie udało się wysłać prośby o nagrodę."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("POPROŚ O LODY · 100 ★") }
                        OutlinedButton(onClick = { showParentDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("TRYB RODZICA")
                        }
                        if (BuildConfig.DEBUG) {
                            DeveloperPanel(
                                place = place,
                                engine = engine,
                                progress = progress,
                                progressRepository = progressRepository,
                                onMessage = { message = it },
                                gpsDiagnostic = gpsDiagnostic,
                                onCheckGps = { scope.launch { diagnoseGps() } },
                                onCreateTestPoi = ::requestCreateTestPoi,
                                hasDebugTestPoi = savedTestPoi != null,
                                onClearTestPoi = {
                                    scope.launch {
                                        progressRepository.clearDebugTestPoi()
                                        screen = GameScreen.HOME
                                        message = "Usunięto testowy punkt GPS; zapisany postęp gry pozostał."
                                    }
                                },
                                onOpen = { updated -> screen = nextGameScreen(updated, place) },
                            )
                        }
                    }
                    GameScreen.PLACE -> {
                        Text(place.name, style = MaterialTheme.typography.headlineSmall)
                        Text(place.intro)
                        Text(place.fact)
                        Button(onClick = ::requestCheckIn, modifier = Modifier.fillMaxWidth()) {
                            Text("JESTEM NA MIEJSCU — SPRAWDŹ GPS")
                        }
                    }
                    GameScreen.QUEST -> {
                        Text("MISJA", style = MaterialTheme.typography.titleLarge)
                        Text(place.quest.prompt)
                        when (place.quest.type) {
                            com.tigstaking.natalia.game.QuestType.MULTIPLE_CHOICE ->
                                place.quest.answers.forEachIndexed { index, answer ->
                                    OutlinedButton(
                                        onClick = { scope.launch { finishQuest(answerIndex = index) } },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) { Text(answer) }
                                }
                            com.tigstaking.natalia.game.QuestType.PARENT_CHECK -> {
                                Text("Rodzic musi potwierdzić wykonanie tej misji.")
                                Button(onClick = {
                                    parentQuestPending = true
                                    showParentDialog = true
                                }, modifier = Modifier.fillMaxWidth()) { Text("POPROŚ RODZICA O POTWIERDZENIE") }
                            }
                            com.tigstaking.natalia.game.QuestType.OBSERVATION,
                            com.tigstaking.natalia.game.QuestType.SAY_PHRASE ->
                                Button(
                                    onClick = { scope.launch { finishQuest() } },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(if (place.quest.type == com.tigstaking.natalia.game.QuestType.SAY_PHRASE) "POWIEDZIANE!" else "ZROBIONE!") }
                        }
                    }
                    GameScreen.WORD -> {
                        Text("SŁÓWKO PO HISZPAŃSKU", style = MaterialTheme.typography.titleLarge)
                        Text("${place.spanishWord.word} — ${place.spanishWord.meaning}", style = MaterialTheme.typography.headlineSmall)
                        when (val status = speechStatus) {
                            SpanishSpeechStatus.Loading -> Text("Przygotowuję wymowę…")
                            SpanishSpeechStatus.Ready -> OutlinedButton(
                                onClick = {
                                    if (!spanishSpeech.speak(place.spanishWord.word)) {
                                        message = "Nie udało się odtworzyć wymowy."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("ODSŁUCHAJ WYMOWĘ (ES-ES)") }
                            is SpanishSpeechStatus.Unavailable -> Text(status.reason)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val updated = engine.learnSpanishWord(place)
                                    message = "Słówko zapamiętane!"
                                    celebration = levelUpCelebration(progress.xp, updated.xp) ?: "SŁÓWKO ZAPAMIĘTANE!"
                                    screen = GameScreen.QUIZ
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("ZAPAMIĘTANE") }
                    }
                    GameScreen.QUIZ -> {
                        Text("QUIZ", style = MaterialTheme.typography.titleLarge)
                        Text(place.quiz.question)
                        place.quiz.answers.forEachIndexed { index, answer ->
                            OutlinedButton(
                                onClick = {
                                    selectedAnswer = index
                                    scope.launch {
                                        when (val result = engine.answerQuiz(place, index)) {
                                            is QuizAnswerResult.TryAgain -> {
                                                message = "Spróbuj jeszcze raz."
                                                celebration = "Hmm… SPRÓBUJ PONOWNIE"
                                            }
                                            is QuizAnswerResult.Correct -> {
                                                message = "Dobrze! Odznaka: ${place.badge}"
                                                celebration = levelUpCelebration(progress.xp, result.progress.xp)
                                                    ?: "BRAWO! ODZNAKA ZDOBYTA"
                                                screen = GameScreen.PASSPORT
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(answer) }
                        }
                    }
                    GameScreen.PASSPORT -> {
                        Text("PASZPORT BARCELONY", style = MaterialTheme.typography.titleLarge)
                        if (place.id in progress.discoveredPlaceIds) {
                            Text("Odkryte: ${place.name}")
                        } else Text("${place.name} czeka na odkrycie.")
                        if ("badge:${place.id}" in progress.earnedBadgeIds) {
                            Text("✓ UKOŃCZONE · ODZNAKA: ${place.badge}")
                        } else Text("Odznaka czeka na ukończenie misji i quizu.")
                        Button(onClick = { screen = GameScreen.HOME }, modifier = Modifier.fillMaxWidth()) {
                            Text("WRÓĆ DO DOMU")
                        }
                    }
                }

                GameCelebration(celebration)

                if (message.isNotBlank()) Text(message)
                if (screen != GameScreen.HOME) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = { screen = GameScreen.HOME }) { Text("STRONA GŁÓWNA") }
                }

                if (showParentDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showParentDialog = false
                            parentAuthenticated = false
                            parentQuestPending = false
                            parentPin = ""
                            pinConfirmation = ""
                            pinMessage = ""
                        },
                        title = { Text("Tryb rodzica") },
                        text = {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (parentAuthenticated) {
                                    val reservedForRewards = progress.pendingRewardRequests.values.sumOf { it.cost }
                                    Text("Stan Natalii: ${progress.xp} XP · ${progress.stars} ★")
                                    Text("Zarezerwowane na nagrody: $reservedForRewards ★")
                                    Text("Kopia postępu nie zawiera PIN-u rodzica ani lokalizacji testowego punktu.")
                                    OutlinedButton(
                                        onClick = { exportBackupLauncher.launch("natalia-na-tropie-postep.json") },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) { Text("EKSPORTUJ POSTĘP") }
                                    OutlinedButton(
                                        onClick = { importBackupLauncher.launch(arrayOf("application/json", "text/*")) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) { Text("IMPORTUJ POSTĘP") }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            scope.launch {
                                                progressRepository.adjustStarsOnce("parent:${UUID.randomUUID()}", 10)
                                                message = "Rodzic dodał 10 ★."
                                            }
                                        }) { Text("DODAJ 10 ★") }
                                        TextButton(
                                            enabled = progress.stars - reservedForRewards >= 10,
                                            onClick = {
                                                scope.launch {
                                                    progressRepository.adjustStarsOnce("parent:${UUID.randomUUID()}", -10)
                                                    message = "Rodzic odjął 10 ★."
                                                }
                                            },
                                        ) { Text("ODEJMIJ 10 ★") }
                                    }
                                    Text("Ostatnie zmiany gwiazdek:")
                                    progress.ledger.filter { it.stars != 0 }.takeLast(5).asReversed().forEach { entry ->
                                        val sign = if (entry.stars > 0) "+" else ""
                                        Text("${entry.source}: $sign${entry.stars} ★")
                                    }
                                    if (parentQuestPending) {
                                        Text("Rodzicu, potwierdź wykonanie misji przez Natalię.")
                                        Button(onClick = {
                                            scope.launch {
                                                if (finishQuest(parentConfirmed = true)) {
                                                    parentQuestPending = false
                                                    showParentDialog = false
                                                    parentAuthenticated = false
                                                }
                                            }
                                        }) { Text("POTWIERDZAM MISJĘ") }
                                    } else if (progress.pendingRewardRequests.isEmpty()) {
                                        Text("Brak oczekujących próśb.")
                                    } else {
                                        progress.pendingRewardRequests.values.sortedBy { it.id }.forEach { request ->
                                            Text("Lody za ${request.cost} ★")
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                TextButton(onClick = {
                                                    scope.launch {
                                                        val approved = progressRepository.approveRedemption(request.id)
                                                        message = if (approved) "Nagroda zatwierdzona." else "Nie można zatwierdzić tej prośby."
                                                        if (approved) celebration = "ZDOBYŁAŚ LODY! 🎉"
                                                    }
                                                }) { Text("ZATWIERDŹ") }
                                                TextButton(onClick = {
                                                    scope.launch { progressRepository.cancelRedemption(request.id) }
                                                }) { Text("ODRZUĆ") }
                                            }
                                        }
                                    }
                                } else {
                                    val setup = !parentPinStore.isConfigured
                                    Text(if (setup) "Ustaw 4-cyfrowy PIN rodzica." else "Wpisz PIN rodzica.")
                                    TextField(
                                        value = parentPin,
                                        onValueChange = { value -> if (value.length <= 4 && value.all { it.isDigit() }) parentPin = value },
                                        label = { Text(if (setup) "Nowy PIN" else "PIN") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                    )
                                    if (setup) {
                                        TextField(
                                            value = pinConfirmation,
                                            onValueChange = { value -> if (value.length <= 4 && value.all { it.isDigit() }) pinConfirmation = value },
                                            label = { Text("Powtórz PIN") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            singleLine = true,
                                        )
                                    }
                                    if (pinMessage.isNotBlank()) Text(pinMessage)
                                    Button(onClick = {
                                        if (setup) {
                                            if (parentPin.length != 4 || parentPin != pinConfirmation) {
                                                pinMessage = "Wpisz ten sam 4-cyfrowy PIN w obu polach."
                                            } else if (parentPinStore.setPin(parentPin)) {
                                                parentAuthenticated = true
                                                parentPin = ""
                                                pinConfirmation = ""
                                                pinMessage = ""
                                            } else pinMessage = "Nie udało się zapisać PIN-u na tym urządzeniu."
                                        } else if (parentPinStore.verify(parentPin)) {
                                            parentAuthenticated = true
                                            parentPin = ""
                                            pinMessage = ""
                                        } else {
                                            parentPin = ""
                                            pinMessage = "Nieprawidłowy PIN lub chwilowa blokada."
                                        }
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (setup) "USTAW PIN I KONTYNUUJ" else "ODBLOKUJ")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showParentDialog = false
                                parentAuthenticated = false
                                parentQuestPending = false
                                parentPin = ""
                                pinConfirmation = ""
                            }) { Text("ZAMKNIJ") }
                        },
                    )
                }
                pendingImportJson?.let { backupJson ->
                    AlertDialog(
                        onDismissRequest = { pendingImportJson = null; importPin = ""; importError = "" },
                        title = { Text("Zastąpić postęp?") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Import zastąpi cały postęp gry na tym telefonie. PIN rodzica i lokalizacja testowego punktu nie są przenoszone.")
                                TextField(
                                    value = importPin,
                                    onValueChange = { value -> if (value.length <= 4 && value.all(Char::isDigit)) importPin = value },
                                    label = { Text("PIN rodzica") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                )
                                if (importError.isNotBlank()) Text(importError)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (!parentPinStore.verify(importPin)) {
                                    importPin = ""
                                    importError = "Nieprawidłowy PIN lub chwilowa blokada."
                                } else scope.launch {
                                    runCatching { progressRepository.importBackupJson(backupJson) }
                                        .onSuccess {
                                            if (!ProgressBackupCodec.decode(backupJson).onboardingCompleted) {
                                                screen = GameScreen.ONBOARDING
                                            }
                                            pendingImportJson = null
                                            importPin = ""
                                            importError = ""
                                            message = "Postęp został zaimportowany."
                                        }
                                        .onFailure { importError = "Nie udało się zaimportować kopii."; importPin = "" }
                                }
                            }) { Text("ZASTĄP POSTĘP") }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingImportJson = null; importPin = ""; importError = "" }) { Text("ANULUJ") }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCelebration(message: String) {
    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter = fadeIn() + scaleIn(),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                MangaBurst()
                val reaction = when {
                    message.startsWith("Hmm") -> "🤔"
                    message.contains("LODY") -> "🍦"
                    message.startsWith("AWANS") -> "🤩"
                    else -> "😄"
                }
                Text(reaction, style = MaterialTheme.typography.headlineMedium)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(message, style = MaterialTheme.typography.titleMedium)
                    Text("✧  ★  ✧")
                }
            }
        }
    }
}

@Composable
private fun MangaBurst() {
    Canvas(Modifier.height(64.dp).fillMaxWidth(.22f)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val inner = size.minDimension * .2f
        val outer = size.minDimension * .48f
        repeat(16) { index ->
            val angle = Math.PI * 2.0 * index / 16.0
            val dx = kotlin.math.cos(angle).toFloat()
            val dy = kotlin.math.sin(angle).toFloat()
            drawLine(Color(0xFFFFB703), Offset(center.x + dx * inner, center.y + dy * inner), Offset(center.x + dx * outer, center.y + dy * outer), 3f)
        }
        drawCircle(Color(0xFFFFD166), size.minDimension * .18f, center)
    }
}

private fun nextGameScreen(progress: GameProgress, place: Place): GameScreen = when {
    place.id !in progress.discoveredPlaceIds -> GameScreen.PLACE
    place.quest.id !in progress.completedQuestIds -> GameScreen.QUEST
    place.id !in progress.completedWordIds -> GameScreen.WORD
    place.quiz.id !in progress.completedQuizIds -> GameScreen.QUIZ
    else -> GameScreen.PASSPORT
}

private fun levelUpCelebration(oldXp: Int, newXp: Int): String? {
    val previous = PlayerLevel.forXp(oldXp)
    val current = PlayerLevel.forXp(newXp)
    return if (current.ordinal > previous.ordinal) "AWANS! ${current.title}" else null
}

@Composable
private fun DeveloperPanel(
    place: Place,
    engine: GameEngine,
    progress: GameProgress,
    progressRepository: GameProgressRepository,
    onMessage: (String) -> Unit,
    gpsDiagnostic: String,
    onCheckGps: () -> Unit,
    onCreateTestPoi: () -> Unit,
    hasDebugTestPoi: Boolean,
    onClearTestPoi: () -> Unit,
    onOpen: (GameProgress) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var confirmPlaceReset by remember { mutableStateOf(false) }
    var latitudeInput by remember(place.id) { mutableStateOf(place.coordinates.latitude.toString()) }
    var longitudeInput by remember(place.id) { mutableStateOf(place.coordinates.longitude.toString()) }
    var accuracyInput by remember(place.id) { mutableStateOf("10") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { expanded = !expanded }) { Text("Developer mode") }
        if (expanded) {
            Text("Narzędzia testowe. Nie są dostępne w wydaniu produkcyjnym.")
            val scope = rememberCoroutineScope()
            val nextStageLabel = when {
                place.id !in progress.discoveredPlaceIds -> "ODKRYCIE"
                place.quest.id !in progress.completedQuestIds -> "MISJA"
                place.id !in progress.completedWordIds -> "SŁÓWKO"
                place.quiz.id !in progress.completedQuizIds -> "QUIZ I ODZNAKA"
                else -> "GRA UKOŃCZONA"
            }
            OutlinedButton(
                enabled = nextStageLabel != "GRA UKOŃCZONA",
                onClick = {
                    scope.launch {
                        val updated = when (nextStageLabel) {
                            "ODKRYCIE" -> engine.simulateDiscovery(place)
                            "MISJA" -> engine.completeQuest(place)
                            "SŁÓWKO" -> engine.learnSpanishWord(place)
                            "QUIZ I ODZNAKA" -> {
                                (engine.answerQuiz(place, place.quiz.correctAnswerIndex) as QuizAnswerResult.Correct).progress
                            }
                            else -> progress
                        }
                        onMessage("Tryb deweloperski: ukończono etap $nextStageLabel.")
                        onOpen(updated)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("ZALICZ NASTĘPNY ETAP: $nextStageLabel") }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        progressRepository.awardOnce(
                            com.tigstaking.natalia.game.RewardEvent(
                                id = "debug-stars:${UUID.randomUUID()}",
                                source = "DEBUG",
                                stars = 100,
                            ),
                        )
                        onMessage("Tryb deweloperski: dodano 100 ★.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("DODAJ 100 ★ (DEBUG)") }
            OutlinedButton(onClick = onCheckGps, modifier = Modifier.fillMaxWidth()) {
                Text("SPRAWDŹ DIAGNOSTYKĘ GPS")
            }
            if (gpsDiagnostic.isNotBlank()) Text(gpsDiagnostic)
            OutlinedButton(onClick = onCreateTestPoi, modifier = Modifier.fillMaxWidth()) {
                Text("UTWÓRZ TESTOWY PUNKT TU, GDZIE JESTEM")
            }
            if (hasDebugTestPoi) {
                OutlinedButton(onClick = onClearTestPoi, modifier = Modifier.fillMaxWidth()) {
                    Text("USUŃ TESTOWY PUNKT")
                }
            }

            fun simulateLocation(coordinates: Coordinates, accuracyMeters: Double) {
                scope.launch {
                    when (val result = engine.checkIn(place, coordinates, accuracyMeters)) {
                        is PlaceCheckInResult.Confirmed -> {
                            onMessage("Symulacja GPS: miejsce odkryte przy dokładności ±${accuracyMeters.toInt()} m.")
                            onOpen(result.progress)
                        }
                        is PlaceCheckInResult.TooFar ->
                            onMessage("Symulacja GPS: poza strefą celu (około ${result.distanceMeters.toInt()} m).")
                        is PlaceCheckInResult.NeedBetterAccuracy ->
                            onMessage("Symulacja GPS: pozycja niepewna (±${result.accuracyMeters.toInt()} m).")
                    }
                }
            }

            Text("Ręczna symulacja współrzędnych i dokładności")
            TextField(
                value = latitudeInput,
                onValueChange = { latitudeInput = it },
                label = { Text("Szerokość geograficzna") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            TextField(
                value = longitudeInput,
                onValueChange = { longitudeInput = it },
                label = { Text("Długość geograficzna") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            TextField(
                value = accuracyInput,
                onValueChange = { accuracyInput = it },
                label = { Text("Dokładność GPS w metrach") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            val overrideLatitude = latitudeInput.toDoubleOrNull()
            val overrideLongitude = longitudeInput.toDoubleOrNull()
            val overrideAccuracy = accuracyInput.toDoubleOrNull()
            val overrideIsValid = overrideLatitude != null && overrideLatitude in -90.0..90.0 &&
                overrideLongitude != null && overrideLongitude in -180.0..180.0 &&
                overrideAccuracy != null && overrideAccuracy.isFinite() && overrideAccuracy >= 0.0
            OutlinedButton(
                enabled = overrideIsValid,
                onClick = {
                    val latitude = overrideLatitude ?: return@OutlinedButton
                    val longitude = overrideLongitude ?: return@OutlinedButton
                    val accuracy = overrideAccuracy ?: return@OutlinedButton
                    simulateLocation(Coordinates(latitude, longitude), accuracy)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("SPRAWDŹ PODANE WSPÓŁRZĘDNE") }
            OutlinedButton(
                onClick = { simulateLocation(place.coordinates, 10.0) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("SYMULUJ GPS: W CELU") }
            OutlinedButton(
                onClick = {
                    simulateLocation(
                        place.coordinates.copy(latitude = (place.coordinates.latitude + 0.03).coerceAtMost(89.0)),
                        5.0,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("SYMULUJ GPS: POZA STREFĄ") }
            OutlinedButton(
                onClick = { simulateLocation(place.coordinates, place.geofenceRadiusMeters + 100.0) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("SYMULUJ GPS: NISKA DOKŁADNOŚĆ") }

            val nextLevel = PlayerLevel.entries.firstOrNull { progress.xp < it.minXp }
            OutlinedButton(
                enabled = nextLevel != null,
                onClick = {
                    val targetLevel = nextLevel ?: return@OutlinedButton
                    scope.launch {
                        progressRepository.awardOnce(
                            com.tigstaking.natalia.game.RewardEvent(
                                id = "debug-level:${UUID.randomUUID()}",
                                source = "DEBUG",
                                xp = (targetLevel.minXp - progress.xp).coerceAtLeast(0),
                            ),
                        )
                        onMessage("Tryb deweloperski: ustawiono poziom ${PlayerLevel.entries.indexOf(targetLevel) + 1}.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(nextLevel?.let { "USTAW POZIOM ${PlayerLevel.entries.indexOf(it) + 1} (DEBUG)" } ?: "MAKSYMALNY POZIOM") }

            val availableStars = progress.stars - progress.pendingRewardRequests.values.sumOf { it.cost }
            OutlinedButton(
                enabled = availableStars >= 10,
                onClick = {
                    scope.launch {
                        val redeemed = progressRepository.redeemOnce("debug-reward:${UUID.randomUUID()}", 10)
                        onMessage(if (redeemed) "Tryb deweloperski: wydano 10 ★." else "Za mało wolnych gwiazdek.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("WYDAJ 10 ★ (DEBUG)") }

            OutlinedButton(onClick = { confirmPlaceReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text("RESETUJ TEN PUNKT (DEBUG)")
            }
            if (confirmPlaceReset) {
                AlertDialog(
                    onDismissRequest = { confirmPlaceReset = false },
                    title = { Text("Wyczyścić postęp tego punktu?") },
                    text = {
                        Text("Etapy i odznaka zostaną zresetowane. Zdobyte XP, gwiazdki i historia nagród zostają; ponowne przejście nie przyzna ich drugi raz.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                val updated = engine.resetPlace(place)
                                confirmPlaceReset = false
                                onMessage("Wyczyszczono etapy punktu. Historia nagród została zachowana.")
                                onOpen(updated)
                            }
                        }) { Text("RESETUJ PUNKT") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmPlaceReset = false }) { Text("ANULUJ") }
                    },
                )
            }

            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text("RESETUJ CAŁY POSTĘP (DEBUG)")
            }
            if (confirmReset) {
                AlertDialog(
                    onDismissRequest = { confirmReset = false },
                    title = { Text("Zresetować postęp?") },
                    text = { Text("To usunie lokalne XP, gwiazdki, odznaki i oczekujące prośby.") },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                progressRepository.reset()
                                confirmReset = false
                                onMessage("Tryb deweloperski: postęp został wyczyszczony.")
                                onOpen(GameProgress())
                            }
                        }) { Text("RESETUJ") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmReset = false }) { Text("ANULUJ") }
                    },
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        val updated = engine.simulateDiscovery(place)
                        onMessage("Test: ${place.name} odkryte.")
                        onOpen(updated)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("ODKRYJ MIEJSCE BEZ SPRAWDZANIA GPS (DEBUG)") }
        }
    }
}
