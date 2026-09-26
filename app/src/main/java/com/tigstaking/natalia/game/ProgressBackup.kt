package com.tigstaking.natalia.game

import org.json.JSONArray
import org.json.JSONObject

data class ProgressBackup(
    val onboardingCompleted: Boolean,
    val progress: GameProgress,
)

object ProgressBackupCodec {
    private const val FORMAT_VERSION = 1

    fun encode(backup: ProgressBackup): String = JSONObject().apply {
        put("formatVersion", FORMAT_VERSION)
        put("onboardingCompleted", backup.onboardingCompleted)
        put("xp", backup.progress.xp)
        put("stars", backup.progress.stars)
        put("appliedEventIds", backup.progress.appliedEventIds.toSortedJsonArray())
        put("discoveredPlaceIds", backup.progress.discoveredPlaceIds.toSortedJsonArray())
        put("completedQuestIds", backup.progress.completedQuestIds.toSortedJsonArray())
        put("completedQuizIds", backup.progress.completedQuizIds.toSortedJsonArray())
        put("completedWordIds", backup.progress.completedWordIds.toSortedJsonArray())
        put("earnedBadgeIds", backup.progress.earnedBadgeIds.toSortedJsonArray())
        put("redeemedRewardIds", backup.progress.redeemedRewardIds.toSortedJsonArray())
        put("ledger", JSONArray().apply {
            backup.progress.ledger.forEach { entry ->
                put(JSONObject()
                    .put("eventId", entry.eventId)
                    .put("source", entry.source)
                    .put("xp", entry.xp)
                    .put("stars", entry.stars))
            }
        })
        put("pendingRewardRequests", JSONArray().apply {
            backup.progress.pendingRewardRequests.values.sortedBy { it.id }.forEach { request ->
                put(JSONObject().put("id", request.id).put("cost", request.cost))
            }
        })
    }.toString(2)

    fun decode(json: String): ProgressBackup {
        val root = JSONObject(json)
        require(root.getInt("formatVersion") == FORMAT_VERSION) { "Unsupported backup format version." }
        val xp = root.getInt("xp")
        val stars = root.getInt("stars")
        require(xp >= 0 && stars >= 0) { "XP and stars cannot be negative." }
        val redeemedIds = root.getStringSet("redeemedRewardIds")

        val pendingRequests = root.getJSONArray("pendingRewardRequests").let { array ->
            (0 until array.length()).associate { index ->
                val requestJson = array.getJSONObject(index)
                val request = RewardRequest(
                    id = requestJson.getString("id").also { require(it.isNotBlank()) },
                    cost = requestJson.getInt("cost").also { require(it > 0) },
                )
                require(request.id !in redeemedIds) { "A redeemed reward cannot also be pending." }
                request.id to request
            }.also { requests ->
                require(requests.size == array.length()) { "Duplicate pending reward request id." }
                require(requests.values.sumOf { it.cost } <= stars) { "Pending rewards exceed the star balance." }
            }
        }

        val progress = GameProgress(
            xp = xp,
            stars = stars,
            appliedEventIds = root.getStringSet("appliedEventIds"),
            ledger = root.getJSONArray("ledger").let { array ->
                (0 until array.length()).map { index ->
                    val entry = array.getJSONObject(index)
                    LedgerEntry(
                        eventId = entry.getString("eventId").also { require(it.isNotBlank()) },
                        source = entry.getString("source").also { require(it.isNotBlank()) },
                        xp = entry.getInt("xp").also { require(it >= 0) },
                        stars = entry.getInt("stars"),
                    )
                }
            },
            discoveredPlaceIds = root.getStringSet("discoveredPlaceIds"),
            completedQuestIds = root.getStringSet("completedQuestIds"),
            completedQuizIds = root.getStringSet("completedQuizIds"),
            completedWordIds = root.getStringSet("completedWordIds"),
            earnedBadgeIds = root.getStringSet("earnedBadgeIds"),
            redeemedRewardIds = redeemedIds,
            pendingRewardRequests = pendingRequests,
        )
        require(progress.ledger.map { it.eventId }.distinct().size == progress.ledger.size) {
            "Duplicate event in reward ledger."
        }
        require(progress.ledger.sumOf { it.xp.toLong() } == xp.toLong()) { "XP does not match the reward ledger." }
        require(progress.ledger.sumOf { it.stars.toLong() } == stars.toLong()) { "Stars do not match the reward ledger." }
        val nonRedemptionEvents = progress.ledger.filter { it.source != "REWARD" }.map { it.eventId }.toSet()
        require(nonRedemptionEvents == progress.appliedEventIds) { "Applied events do not match the reward ledger." }
        val ledgerRedemptions = progress.ledger.filter { it.source == "REWARD" }.mapNotNull { entry ->
            entry.eventId.takeIf { it.startsWith("reward:") }?.removePrefix("reward:")
        }.toSet()
        require(ledgerRedemptions == progress.redeemedRewardIds) { "Redeemed rewards do not match the reward ledger." }
        return ProgressBackup(root.getBoolean("onboardingCompleted"), progress)
    }

    private fun Set<String>.toSortedJsonArray() = JSONArray().also { array ->
        sorted().forEach(array::put)
    }

    private fun JSONObject.getStringSet(key: String): Set<String> = getJSONArray(key).let { array ->
        (0 until array.length()).map { array.getString(it).also { value -> require(value.isNotBlank()) } }
            .toSet()
            .also { values -> require(values.size == array.length()) { "Duplicate value in $key." } }
    }
}
