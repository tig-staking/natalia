package com.tigstaking.natalia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tigstaking.natalia.game.GameProgress
import com.tigstaking.natalia.game.GameProgressRepository
import com.tigstaking.natalia.game.PlayerLevel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NataliaNaTropieApp() }
    }
}

@Composable
private fun NataliaNaTropieApp() {
    val context = LocalContext.current
    val progressRepository = remember(context) { GameProgressRepository(context) }
    val progress by progressRepository.progress.collectAsState(initial = GameProgress())
    val level = PlayerLevel.forXp(progress.xp)

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("NATALIA NA TROPIE", style = MaterialTheme.typography.headlineMedium)
                Text("Barcelona · Twoja przygoda czeka")
                Spacer(Modifier.height(24.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Cześć, Natalia!", style = MaterialTheme.typography.titleLarge)
                        Text("${level.title} · Poziom ${PlayerLevel.entries.indexOf(level) + 1}")
                        Spacer(Modifier.height(8.dp))
                        Text("${progress.xp} XP     ★ ${progress.stars}")
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("KONTYNUUJ PRZYGODĘ")
                }
                Spacer(Modifier.height(12.dp))
                Text("Mapa     Misje     Paszport     Nagrody")
            }
        }
    }
}
