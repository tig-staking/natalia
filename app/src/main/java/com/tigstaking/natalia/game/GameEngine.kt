package com.tigstaking.natalia.game

class GameEngine(private val progress: GameProgressRepository) {
    suspend fun discover(place: Place) = progress.discoverPlace(
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
