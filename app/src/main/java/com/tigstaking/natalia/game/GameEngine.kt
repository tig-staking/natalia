package com.tigstaking.natalia.game

import kotlinx.coroutines.flow.first

class GameEngine(private val progress: GameProgressRepository) {
    suspend fun checkIn(
        place: Place,
        userLocation: Coordinates,
        accuracyMeters: Double,
    ): PlaceCheckInResult = when (
        val proximity = Proximity.classify(
            user = userLocation,
            place = place.coordinates,
            radiusMeters = place.geofenceRadiusMeters,
            accuracyMeters = accuracyMeters,
        )
    ) {
        is ProximityResult.Outside -> PlaceCheckInResult.TooFar(proximity.distanceMeters)
        is ProximityResult.Uncertain -> PlaceCheckInResult.NeedBetterAccuracy(
            proximity.distanceMeters,
            proximity.accuracyMeters,
        )
        is ProximityResult.Inside -> PlaceCheckInResult.Confirmed(
            proximity.distanceMeters,
            awardDiscovery(place),
        )
    }

    suspend fun simulateDiscovery(place: Place) = awardDiscovery(place)

    suspend fun resetPlace(place: Place): GameProgress = progress.resetPlace(place)

    private suspend fun awardDiscovery(place: Place) = progress.discoverPlace(
        placeId = place.id,
        xp = place.rewards.discovery.xp,
        stars = place.rewards.discovery.stars,
    )

    suspend fun completeQuest(place: Place): GameProgress {
        requireDiscovered(place)
        return progress.completeQuest(
            questId = place.quest.id,
            xp = place.rewards.quest.xp,
            stars = place.rewards.quest.stars,
        )
    }

    suspend fun learnSpanishWord(place: Place): GameProgress {
        val state = progress.progress.first()
        check(place.id in state.discoveredPlaceIds) { "Discover this place before learning its word" }
        check(place.quest.id in state.completedQuestIds) { "Complete the quest before learning its word" }
        return progress.completeSpanishWord(
            placeId = place.id,
            xp = place.rewards.spanishWord.xp,
            stars = place.rewards.spanishWord.stars,
        )
    }

    suspend fun answerQuiz(place: Place, answerIndex: Int): QuizAnswerResult {
        val state = progress.progress.first()
        check(place.id in state.discoveredPlaceIds) { "Discover this place before starting its quiz" }
        check(place.quest.id in state.completedQuestIds) { "Complete the quest before starting its quiz" }
        check(place.id in state.completedWordIds) { "Learn the word before starting the quiz" }
        if (answerIndex !in place.quiz.answers.indices || !place.quiz.isCorrect(answerIndex)) {
            return QuizAnswerResult.TryAgain(correctAnswerIndex = place.quiz.correctAnswerIndex)
        }
        return QuizAnswerResult.Correct(progress.completeQuiz(
            quizId = place.quiz.id,
            xp = place.rewards.quiz.xp,
            stars = place.rewards.quiz.stars,
        ))
    }

    suspend fun earnBadge(place: Place): GameProgress {
        val state = progress.progress.first()
        check(place.id in state.discoveredPlaceIds) { "Discover this place before earning its badge" }
        check(place.quest.id in state.completedQuestIds) { "Complete the quest before earning its badge" }
        check(place.id in state.completedWordIds) { "Learn the word before earning its badge" }
        check(place.quiz.id in state.completedQuizIds) { "Complete the quiz before earning its badge" }
        progress.markBadgeEarned("badge:${place.id}")
        return progress.progress.first()
    }

    private suspend fun requireDiscovered(place: Place) {
        val state = progress.progress.first()
        check(place.id in state.discoveredPlaceIds) { "Discover this place before starting its quest" }
    }
}

sealed interface QuizAnswerResult {
    data class TryAgain(val correctAnswerIndex: Int) : QuizAnswerResult
    data class Correct(val progress: GameProgress) : QuizAnswerResult
}

sealed interface PlaceCheckInResult {
    data class TooFar(val distanceMeters: Double) : PlaceCheckInResult
    data class NeedBetterAccuracy(val distanceMeters: Double, val accuracyMeters: Double) : PlaceCheckInResult
    data class Confirmed(val distanceMeters: Double, val progress: GameProgress) : PlaceCheckInResult
}
