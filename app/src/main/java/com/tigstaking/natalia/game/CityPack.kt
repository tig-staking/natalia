package com.tigstaking.natalia.game

import org.json.JSONObject

data class CityPack(
    val schemaVersion: Int,
    val id: String,
    val name: String,
    val country: String,
    val mapCenter: Coordinates,
    val defaultZoom: Double,
    val places: List<Place>,
) {
    companion object {
        fun parse(json: String): CityPack {
            val root = JSONObject(json)
            val center = root.getJSONObject("mapCenter")
            val places = root.getJSONArray("places")
            return CityPack(
                schemaVersion = root.getInt("schemaVersion"),
                id = root.getString("id"),
                name = root.getString("name"),
                country = root.getString("country"),
                mapCenter = Coordinates(center.getDouble("latitude"), center.getDouble("longitude")),
                defaultZoom = root.getDouble("defaultZoom"),
                places = (0 until places.length()).map { index ->
                    val place = places.getJSONObject(index)
                    val word = place.getJSONObject("spanishWord")
                    val quest = place.getJSONObject("quest")
                    val quiz = place.getJSONObject("quiz")
                    val answers = quiz.getJSONArray("answers")
                    Place(
                        id = place.getString("id"),
                        name = place.getString("name"),
                        coordinates = Coordinates(place.getDouble("latitude"), place.getDouble("longitude")),
                        geofenceRadiusMeters = place.getInt("geofenceRadiusMeters"),
                        intro = place.getString("intro"),
                        fact = place.getString("fact"),
                        spanishWord = SpanishWord(word.getString("word"), word.getString("meaning")),
                        quest = Quest(quest.getString("id"), quest.getString("type"), quest.getString("prompt")),
                        quiz = Quiz(
                            id = quiz.getString("id"),
                            question = quiz.getString("question"),
                            answers = (0 until answers.length()).map(answers::getString),
                            correctAnswerIndex = quiz.getInt("correctAnswerIndex"),
                        ),
                        xp = place.getInt("xp"),
                        stars = place.getInt("stars"),
                        badge = place.getString("badge"),
                        avatarPose = place.getString("avatarPose"),
                    ).also(Place::validate)
                },
            ).also(CityPack::validate)
        }
    }

    private fun validate() {
        require(schemaVersion == 1) { "Unsupported City Pack schema version: $schemaVersion" }
        require(id.isNotBlank() && name.isNotBlank()) { "City Pack id and name are required" }
        require(mapCenter.latitude in -90.0..90.0 && mapCenter.longitude in -180.0..180.0) {
            "City Pack map center is out of range"
        }
        require(defaultZoom > 0.0) { "Default map zoom must be positive" }
        require(places.map(Place::id).distinct().size == places.size) { "Place ids must be unique" }
    }
}

data class Place(
    val id: String,
    val name: String,
    val coordinates: Coordinates,
    val geofenceRadiusMeters: Int,
    val intro: String,
    val fact: String,
    val spanishWord: SpanishWord,
    val quest: Quest,
    val quiz: Quiz,
    val xp: Int,
    val stars: Int,
    val badge: String,
    val avatarPose: String,
) {
    fun validate() {
        require(id.isNotBlank() && name.isNotBlank()) { "Place id and name are required" }
        require(coordinates.latitude in -90.0..90.0 && coordinates.longitude in -180.0..180.0) {
            "Place coordinates are out of range"
        }
        require(geofenceRadiusMeters > 0) { "Geofence radius must be positive" }
        require(quiz.answers.size >= 2 && quiz.correctAnswerIndex in quiz.answers.indices) {
            "Quiz must have answers and a valid correct answer index"
        }
        require(quiz.answers.all(String::isNotBlank)) { "Quiz answers must not be blank" }
        require(xp >= 0 && stars >= 0) { "Place rewards cannot be negative" }
    }
}

data class SpanishWord(val word: String, val meaning: String)
data class Quest(val id: String, val type: String, val prompt: String)
data class Quiz(val id: String, val question: String, val answers: List<String>, val correctAnswerIndex: Int)
