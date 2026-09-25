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
                    val rewards = place.getJSONObject("rewards")
                    Place(
                        id = place.getString("id"),
                        name = place.getString("name"),
                        coordinates = Coordinates(place.getDouble("latitude"), place.getDouble("longitude")),
                        geofenceRadiusMeters = place.getInt("geofenceRadiusMeters"),
                        intro = place.getString("intro"),
                        fact = place.getString("fact"),
                        spanishWord = SpanishWord(word.getString("word"), word.getString("meaning")),
                        quest = Quest(
                            quest.getString("id"),
                            QuestType.valueOf(quest.getString("type")),
                            quest.getString("prompt"),
                        ),
                        quiz = Quiz(
                            id = quiz.getString("id"),
                            question = quiz.getString("question"),
                            answers = (0 until answers.length()).map(answers::getString),
                            correctAnswerIndex = quiz.getInt("correctAnswerIndex"),
                        ),
                        xp = place.getInt("xp"),
                        stars = place.getInt("stars"),
                        rewards = PlaceRewards(
                            discovery = rewards.getJSONObject("discovery").toRewardAmount(),
                            quest = rewards.getJSONObject("quest").toRewardAmount(),
                            spanishWord = rewards.getJSONObject("spanishWord").toRewardAmount(),
                            quiz = rewards.getJSONObject("quiz").toRewardAmount(),
                        ),
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
        require(places.map(Place::quest).map(Quest::id).distinct().size == places.size) {
            "Quest ids must be unique within a City Pack"
        }
        require(places.map(Place::quiz).map(Quiz::id).distinct().size == places.size) {
            "Quiz ids must be unique within a City Pack"
        }
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
    val rewards: PlaceRewards,
    val badge: String,
    val avatarPose: String,
) {
    fun validate() {
        require(id.isNotBlank() && name.isNotBlank()) { "Place id and name are required" }
        require(intro.isNotBlank() && fact.isNotBlank()) { "Place intro and fact are required" }
        require(spanishWord.word.isNotBlank() && spanishWord.meaning.isNotBlank()) { "Spanish word is required" }
        require(quest.id.isNotBlank() && quest.prompt.isNotBlank()) { "Quest id and prompt are required" }
        require(quiz.id.isNotBlank() && quiz.question.isNotBlank()) { "Quiz id and question are required" }
        require(coordinates.latitude in -90.0..90.0 && coordinates.longitude in -180.0..180.0) {
            "Place coordinates are out of range"
        }
        require(geofenceRadiusMeters > 0) { "Geofence radius must be positive" }
        require(quiz.answers.size >= 2 && quiz.correctAnswerIndex in quiz.answers.indices) {
            "Quiz must have answers and a valid correct answer index"
        }
        require(quiz.answers.all(String::isNotBlank)) { "Quiz answers must not be blank" }
        require(xp >= 0 && stars >= 0) { "Place rewards cannot be negative" }
        require(rewards.totalXp == xp && rewards.totalStars == stars) {
            "Stage reward totals must match the place XP and stars"
        }
    }
}

data class SpanishWord(val word: String, val meaning: String)
enum class QuestType { OBSERVATION, SAY_PHRASE, MULTIPLE_CHOICE, PARENT_CHECK }
data class Quest(val id: String, val type: QuestType, val prompt: String)
data class Quiz(val id: String, val question: String, val answers: List<String>, val correctAnswerIndex: Int) {
    fun isCorrect(answerIndex: Int) = answerIndex == correctAnswerIndex
}

data class RewardAmount(val xp: Int, val stars: Int) {
    init {
        require(xp >= 0 && stars >= 0) { "Reward amounts cannot be negative" }
    }
}

data class PlaceRewards(
    val discovery: RewardAmount,
    val quest: RewardAmount,
    val spanishWord: RewardAmount,
    val quiz: RewardAmount,
) {
    val totalXp get() = discovery.xp + quest.xp + spanishWord.xp + quiz.xp
    val totalStars get() = discovery.stars + quest.stars + spanishWord.stars + quiz.stars
}

private fun JSONObject.toRewardAmount() = RewardAmount(getInt("xp"), getInt("stars"))
