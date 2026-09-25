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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.launch

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
    val engine = remember(progressRepository) { GameEngine(progressRepository) }
    val place = remember(context) { CityPackRepository(context).loadBarcelona().places.first() }
    val locationProvider = remember(context) { FusedLocationProvider(context) }
    val progress by progressRepository.progress.collectAsState(initial = GameProgress())
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(GameScreen.HOME) }
    var message by remember { mutableStateOf("") }
    var selectedAnswer by remember { mutableIntStateOf(-1) }

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
                        if (BuildConfig.DEBUG) {
                            DeveloperPanel(
                                place = place,
                                engine = engine,
                                onMessage = { message = it },
                                onOpen = { screen = nextGameScreen(progress, place) },
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
    onMessage: (String) -> Unit,
    onOpen: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { expanded = !expanded }) { Text("Developer mode") }
        if (expanded) {
            Text("Testowy skrót do sprawdzenia przepływu gry.")
            Button(
                onClick = {
                    scope.launch {
                        engine.simulateDiscovery(place)
                        onMessage("Test: Sagrada Família odkryta.")
                        onOpen()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("SIMULATE SAGRADA LOCATION") }
        }
    }
}
