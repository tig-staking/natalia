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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NataliaNaTropieApp() }
    }
}

@Composable
private fun NataliaNaTropieApp() {
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
                        Text("ODKRYWCZYNI · Poziom 1")
                        Spacer(Modifier.height(8.dp))
                        Text("0 XP     ★ 0")
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
