package com.tigstaking.natalia.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.gameProgressDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_progress")

class GameProgressRepository(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.gameProgressDataStore)

    val progress: Flow<GameProgress> = dataStore.data.map(::decode)

    suspend fun awardOnce(event: RewardEvent): GameProgress {
        var result = GameProgress()
        dataStore.edit { preferences ->
            result = decode(preferences).applyOnce(event)
            encode(preferences, result)
        }
        return result
    }

    suspend fun discoverPlace(placeId: String, xp: Int, stars: Int): GameProgress =
        awardStageOnce(RewardEvent("discovery:$placeId", "DISCOVERY", xp, stars), discoveredPlacesKey, placeId)

    suspend fun completeQuest(questId: String, xp: Int, stars: Int): GameProgress =
        awardStageOnce(RewardEvent("quest:$questId", "QUEST", xp, stars), completedQuestsKey, questId)

    suspend fun completeQuiz(quizId: String, xp: Int, stars: Int): GameProgress =
        awardStageOnce(RewardEvent("quiz:$quizId", "QUIZ", xp, stars), completedQuizzesKey, quizId)

    suspend fun completeSpanishWord(placeId: String, xp: Int, stars: Int): GameProgress =
        awardStageOnce(RewardEvent("word:$placeId", "SPANISH", xp, stars), completedWordsKey, placeId)

    suspend fun redeemOnce(redemptionId: String, cost: Int): Boolean {
        require(redemptionId.isNotBlank()) { "Redemption id must not be blank" }
        require(cost > 0) { "Reward cost must be greater than zero" }
        var redeemed = false
        dataStore.edit { preferences ->
            val current = decode(preferences)
            if (redemptionId in current.redeemedRewardIds) {
                redeemed = true
                return@edit
            }
            if (current.stars < cost) return@edit
            encode(preferences, current.redeemOnce(redemptionId, cost))
            redeemed = true
        }
        return redeemed
    }

    suspend fun markDiscovered(placeId: String) = updateSet(discoveredPlacesKey, placeId)
    suspend fun markQuestCompleted(questId: String) = updateSet(completedQuestsKey, questId)
    suspend fun markQuizCompleted(quizId: String) = updateSet(completedQuizzesKey, quizId)
    suspend fun markBadgeEarned(badgeId: String) = updateSet(earnedBadgesKey, badgeId)

    suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    private suspend fun updateSet(key: Preferences.Key<Set<String>>, id: String) {
        require(id.isNotBlank()) { "Progress id must not be blank" }
        dataStore.edit { prefs -> prefs[key] = prefs[key].orEmpty() + id }
    }

    private suspend fun awardStageOnce(
        event: RewardEvent,
        markerKey: Preferences.Key<Set<String>>,
        markerId: String,
    ): GameProgress {
        require(markerId.isNotBlank()) { "Progress id must not be blank" }
        var result = GameProgress()
        dataStore.edit { preferences ->
            val current = decode(preferences)
            val updated = current.applyOnce(event)
            val recorded = (preferences[markerKey].orEmpty() + markerId)
            preferences[markerKey] = recorded
            result = updated.copy(
                discoveredPlaceIds = if (markerKey == discoveredPlacesKey) recorded else updated.discoveredPlaceIds,
                completedQuestIds = if (markerKey == completedQuestsKey) recorded else updated.completedQuestIds,
                completedQuizIds = if (markerKey == completedQuizzesKey) recorded else updated.completedQuizIds,
                completedWordIds = if (markerKey == completedWordsKey) recorded else updated.completedWordIds,
            )
            encode(preferences, result)
        }
        return result
    }

    private fun decode(prefs: Preferences) = GameProgress(
        xp = prefs[xpKey] ?: 0,
        stars = prefs[starsKey] ?: 0,
        appliedEventIds = prefs[appliedEventsKey].orEmpty(),
        ledger = decodeLedger(prefs[ledgerKey].orEmpty()),
        discoveredPlaceIds = prefs[discoveredPlacesKey].orEmpty(),
        completedQuestIds = prefs[completedQuestsKey].orEmpty(),
        completedQuizIds = prefs[completedQuizzesKey].orEmpty(),
        completedWordIds = prefs[completedWordsKey].orEmpty(),
        earnedBadgeIds = prefs[earnedBadgesKey].orEmpty(),
        redeemedRewardIds = prefs[redeemedRewardsKey].orEmpty(),
    )

    private fun encode(prefs: MutablePreferences, state: GameProgress) {
        prefs[xpKey] = state.xp
        prefs[starsKey] = state.stars
        prefs[appliedEventsKey] = state.appliedEventIds
        prefs[ledgerKey] = encodeLedger(state.ledger)
        prefs[discoveredPlacesKey] = state.discoveredPlaceIds
        prefs[completedQuestsKey] = state.completedQuestIds
        prefs[completedQuizzesKey] = state.completedQuizIds
        prefs[completedWordsKey] = state.completedWordIds
        prefs[earnedBadgesKey] = state.earnedBadgeIds
        prefs[redeemedRewardsKey] = state.redeemedRewardIds
    }

    private fun encodeLedger(ledger: List<LedgerEntry>) = JSONArray().apply {
        ledger.forEach { entry ->
            put(JSONArray().put(entry.eventId).put(entry.source).put(entry.xp).put(entry.stars))
        }
    }.toString()

    private fun decodeLedger(value: String): List<LedgerEntry> = runCatching {
        val array = JSONArray(value)
        (0 until array.length()).map { index ->
            val entry = array.getJSONArray(index)
            LedgerEntry(entry.getString(0), entry.getString(1), entry.getInt(2), entry.getInt(3))
        }
    }.getOrDefault(emptyList())

    private companion object {
        val xpKey = intPreferencesKey("xp")
        val starsKey = intPreferencesKey("stars")
        val appliedEventsKey = stringSetPreferencesKey("applied_events")
        val ledgerKey = stringPreferencesKey("ledger")
        val discoveredPlacesKey = stringSetPreferencesKey("discovered_places")
        val completedQuestsKey = stringSetPreferencesKey("completed_quests")
        val completedQuizzesKey = stringSetPreferencesKey("completed_quizzes")
        val completedWordsKey = stringSetPreferencesKey("completed_words")
        val earnedBadgesKey = stringSetPreferencesKey("earned_badges")
        val redeemedRewardsKey = stringSetPreferencesKey("redeemed_rewards")
    }
}
