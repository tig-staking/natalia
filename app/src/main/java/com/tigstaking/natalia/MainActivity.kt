package com.tigstaking.natalia

import android.Manifest
import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
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
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.tigstaking.natalia.game.CityPackRepository
import com.tigstaking.natalia.game.GameEngine
import com.tigstaking.natalia.game.GameProgress
import com.tigstaking.natalia.game.GameProgressRepository
import com.tigstaking.natalia.game.Place
import com.tigstaking.natalia.game.PlaceCheckInResult
import com.tigstaking.natalia.game.PlayerLevel
import com.tigstaking.natalia.game.QuizAnswerResult
import com.tigstaking.natalia.game.location.FusedLocationProvider
import com.tigstaking.natalia.security.ParentPinStore
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NataliaNaTropieApp() }
    }
}

private enum class GameScreen { HOME, PLACE, QUEST, WORD, QUIZ, PASSPORT }

@Composable
private fun NataliaNaTropieApp() {
    val context = LocalContext.current
    val progressRepository = remember(context) { GameProgressRepository(context) }
    val parentPinStore = remember(context) { ParentPinStore(context) }
    val engine = remember(progressRepository) { GameEngine(progressRepository) }
    val place = remember(context) { CityPackRepository(context).loadBarcelona().places.first() }
    val locationProvider = remember(context) { FusedLocationProvider(context) }
    val progress by progressRepository.progress.collectAsState(initial = GameProgress())
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(GameScreen.HOME) }
    var message by remember { mutableStateOf("") }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showParentDialog by remember { mutableStateOf(false) }
    var parentAuthenticated by remember { mutableStateOf(false) }
    var parentPin by remember { mutableStateOf("") }
    var pinConfirmation by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf("") }

    suspend fun checkIn() {
        message = "Sprawdzam lokalizację…"
        try {
            val location = locationProvider.currentLocation()
            when (val result = engine.checkIn(place, location.coordinates, location.accuracyMeters.toDouble())) {
                is PlaceCheckInResult.Confirmed -> {
                    message = "Miejsce odkryte! Dokładność GPS ±${location.accuracyMeters.toInt()} m."
                    screen = GameScreen.QUEST
                }
                is PlaceCheckInResult.TooFar ->
                    message = "Jesteś około ${result.distanceMeters.toInt()} m od celu. Podejdź bliżej."
                is PlaceCheckInResult.NeedBetterAccuracy ->
                    message = "GPS jest zbyt niedokładny (±${result.accuracyMeters.toInt()} m). Spróbuj ponownie."
            }
        } catch (error: Exception) {
            message = error.message ?: "Nie udało się pobrać lokalizacji. Spróbuj ponownie."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.any { it }) scope.launch { checkIn() }
        else message = "Zezwól na lokalizację podczas używania aplikacji, aby odkryć miejsce."
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

    val level = PlayerLevel.forXp(progress.xp)
    val nextScreen = nextGameScreen(progress, place)

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("NATALIA NA TROPIE", style = MaterialTheme.typography.headlineMedium)
                Text("Barcelona · Sagrada Família")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${level.title} · Poziom ${PlayerLevel.entries.indexOf(level) + 1}")
                        Text("${progress.xp} XP     ★ ${progress.stars}")
                        Text("Paszport: ${if (place.id in progress.discoveredPlaceIds) "Sagrada Família ✓" else "czeka na pierwsze odkrycie"}")
                    }
                }

                when (screen) {
                    GameScreen.HOME -> {
                        Text("Cześć, Natalia! Twoja przygoda w Barcelonie czeka.")
                        Button(onClick = { screen = nextScreen }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (place.id in progress.discoveredPlaceIds) "KONTYNUUJ PRZYGODĘ" else "POKAŻ MIEJSCE")
                        }
                        Text("Misje     Paszport     Nagrody")
                        val reservedStars = progress.pendingRewardRequests.values.sumOf { it.cost }
                        Button(
                            enabled = progress.stars - reservedStars >= 10,
                            onClick = {
                                scope.launch {
                                    val requestId = "treat-${UUID.randomUUID()}"
                                    val created = progressRepository.requestRedemptionOnce(requestId, 10)
                                    message = if (created) "Prośba o nagrodę wysłana do rodzica (10 ★)."
                                        else "Nie udało się wysłać prośby o nagrodę."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("POPROŚ O MAŁĄ NAGRODĘ · 10 ★") }
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
                        Button(
                            onClick = {
                                scope.launch {
                                    engine.completeQuest(place)
                                    message = "Misja ukończona! +${place.rewards.quest.xp} XP, +${place.rewards.quest.stars} ★"
                                    screen = GameScreen.WORD
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("ZROBIONE!") }
                    }
                    GameScreen.WORD -> {
                        Text("SŁÓWKO PO HISZPAŃSKU", style = MaterialTheme.typography.titleLarge)
                        Text("${place.spanishWord.word} — ${place.spanishWord.meaning}", style = MaterialTheme.typography.headlineSmall)
                        Button(
                            onClick = {
                                scope.launch {
                                    engine.learnSpanishWord(place)
                                    message = "Słówko zapamiętane!"
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
                                            is QuizAnswerResult.TryAgain ->
                                                message = "Spróbuj jeszcze raz."
                                            is QuizAnswerResult.Correct -> {
                                                engine.earnBadge(place)
                                                message = "Dobrze! Odznaka: ${place.badge}"
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
                        Text("✓ ${place.name}")
                        Text("ODZNAKA: ${place.badge}")
                        Button(onClick = { screen = GameScreen.HOME }, modifier = Modifier.fillMaxWidth()) {
                            Text("WRÓĆ DO DOMU")
                        }
                    }
                }

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
                            parentPin = ""
                            pinConfirmation = ""
                            pinMessage = ""
                        },
                        title = { Text(if (parentAuthenticated) "Prośby o nagrody" else "Tryb rodzica") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (parentAuthenticated) {
                                    if (progress.pendingRewardRequests.isEmpty()) {
                                        Text("Brak oczekujących próśb.")
                                    } else {
                                        progress.pendingRewardRequests.values.sortedBy { it.id }.forEach { request ->
                                            Text("Nagroda za ${request.cost} ★")
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                TextButton(onClick = {
                                                    scope.launch {
                                                        val approved = progressRepository.approveRedemption(request.id)
                                                        message = if (approved) "Nagroda zatwierdzona." else "Nie można zatwierdzić tej prośby."
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
                                                pinMessage = ""
                                            } else pinMessage = "Nie udało się zapisać PIN-u na tym urządzeniu."
                                        } else if (parentPinStore.verify(parentPin)) {
                                            parentAuthenticated = true
                                            pinMessage = ""
                                        } else pinMessage = "Nieprawidłowy PIN lub chwilowa blokada."
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
                                parentPin = ""
                                pinConfirmation = ""
                            }) { Text("ZAMKNIJ") }
                        },
                    )
                }
            }
        }
    }
}

private fun nextGameScreen(progress: GameProgress, place: Place): GameScreen = when {
    place.id !in progress.discoveredPlaceIds -> GameScreen.PLACE
    place.quest.id !in progress.completedQuestIds -> GameScreen.QUEST
    place.id !in progress.completedWordIds -> GameScreen.WORD
    place.quiz.id !in progress.completedQuizIds -> GameScreen.QUIZ
    else -> GameScreen.PASSPORT
}

@Composable
private fun DeveloperPanel(
    place: Place,
    engine: GameEngine,
    progress: GameProgress,
    progressRepository: GameProgressRepository,
    onMessage: (String) -> Unit,
    onOpen: (GameProgress) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
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
                                engine.answerQuiz(place, place.quiz.correctAnswerIndex)
                                engine.earnBadge(place)
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
                                stars = 10,
                            ),
                        )
                        onMessage("Tryb deweloperski: dodano 10 ★.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("DODAJ 10 ★ (DEBUG)") }
            Button(
                onClick = {
                    scope.launch {
                        val updated = engine.simulateDiscovery(place)
                        onMessage("Test: Sagrada Família odkryta.")
                        onOpen(updated)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("SIMULATE SAGRADA LOCATION") }
        }
    }
}
