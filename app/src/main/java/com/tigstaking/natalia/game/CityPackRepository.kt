package com.tigstaking.natalia.game

import android.content.Context

class CityPackRepository(private val context: Context) {
    fun loadBarcelona(): CityPack = context.assets.open("citypacks/barcelona.json")
        .bufferedReader(Charsets.UTF_8)
        .use { CityPack.parse(it.readText()) }
}
