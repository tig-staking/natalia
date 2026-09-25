package com.tigstaking.natalia.game

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

    private suspend fun awardDiscovery(place: Place) = progress.discoverPlace(
        placeId = place.id,
        xp = place.rewards.discovery.xp,
        stars = place.rewards.discovery.stars,
    )

    suspend fun completeQuest(place: Place) = progress.completeQuest(
        questId = place.quest.id,
        xp = place.rewards.quest.xp,
        stars = place.rewards.quest.stars,
    )

    suspend fun learnSpanishWord(place: Place) = progress.completeSpanishWord(
        placeId = place.id,
        xp = place.rewards.spanishWord.xp,
        stars = place.rewards.spanishWord.stars,
    )

    suspend fun answerQuiz(place: Place, answerIndex: Int): QuizAnswerResult {
        if (answerIndex !in place.quiz.answers.indices || !place.quiz.isCorrect(answerIndex)) {
            return QuizAnswerResult.TryAgain(correctAnswerIndex = place.quiz.correctAnswerIndex)
        }
        return QuizAnswerResult.Correct(progress.completeQuiz(
            quizId = place.quiz.id,
            xp = place.rewards.quiz.xp,
            stars = place.rewards.quiz.stars,
        ))
    }

    suspend fun earnBadge(place: Place) = progress.markBadgeEarned("badge:${place.id}")
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
