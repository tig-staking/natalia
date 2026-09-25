package com.tigstaking.natalia.game

data class RewardEvent(
    val id: String,
    val source: String,
    val xp: Int = 0,
    val stars: Int = 0,
)

data class LedgerEntry(
    val eventId: String,
    val source: String,
    val xp: Int,
    val stars: Int,
)

data class RewardRequest(val id: String, val cost: Int)

data class GameProgress(
    val xp: Int = 0,
    val stars: Int = 0,
    val appliedEventIds: Set<String> = emptySet(),
    val ledger: List<LedgerEntry> = emptyList(),
    val discoveredPlaceIds: Set<String> = emptySet(),
    val completedQuestIds: Set<String> = emptySet(),
    val completedQuizIds: Set<String> = emptySet(),
    val completedWordIds: Set<String> = emptySet(),
    val earnedBadgeIds: Set<String> = emptySet(),
    val redeemedRewardIds: Set<String> = emptySet(),
    val pendingRewardRequests: Map<String, RewardRequest> = emptyMap(),
) {
    fun applyOnce(event: RewardEvent): GameProgress {
        require(event.id.isNotBlank()) { "Reward event id must not be blank" }
        require(event.xp >= 0) { "XP cannot be spent" }
        require(event.stars >= 0) { "Use a redemption event to spend stars" }
        if (event.id in appliedEventIds) return this

        return copy(
            xp = xp + event.xp,
            stars = stars + event.stars,
            appliedEventIds = appliedEventIds + event.id,
            ledger = ledger + LedgerEntry(event.id, event.source, event.xp, event.stars),
        )
    }

    fun requestRedemption(requestId: String, cost: Int): GameProgress {
        require(requestId.isNotBlank()) { "Redemption id must not be blank" }
        require(cost > 0) { "Reward cost must be greater than zero" }
        if (requestId in redeemedRewardIds) return this
        pendingRewardRequests[requestId]?.let { existing ->
            require(existing.cost == cost) { "A pending request with this id has a different cost" }
            return this
        }
        val reservedStars = pendingRewardRequests.values.sumOf { it.cost }
        require(stars - reservedStars >= cost) { "Not enough unreserved stars" }
        return copy(pendingRewardRequests = pendingRewardRequests + (requestId to RewardRequest(requestId, cost)))
    }

    fun approveRedemption(requestId: String): GameProgress {
        require(requestId.isNotBlank()) { "Redemption id must not be blank" }
        if (requestId in redeemedRewardIds) {
            return copy(pendingRewardRequests = pendingRewardRequests - requestId)
        }
        val request = requireNotNull(pendingRewardRequests[requestId]) { "No pending request for this reward" }
        val withoutRequest = copy(pendingRewardRequests = pendingRewardRequests - requestId)
        return withoutRequest.redeemOnce(request.id, request.cost)
    }

    fun cancelRedemption(requestId: String): GameProgress {
        require(requestId.isNotBlank()) { "Redemption id must not be blank" }
        return copy(pendingRewardRequests = pendingRewardRequests - requestId)
    }

    fun redeemOnce(redemptionId: String, cost: Int): GameProgress {
        require(redemptionId.isNotBlank()) { "Redemption id must not be blank" }
        require(cost > 0) { "Reward cost must be greater than zero" }
        if (redemptionId in redeemedRewardIds) return this
        val reservedStars = pendingRewardRequests.values.sumOf { it.cost }
        require(stars - reservedStars >= cost) { "Not enough unreserved stars" }
        return copy(
            stars = stars - cost,
            redeemedRewardIds = redeemedRewardIds + redemptionId,
            ledger = ledger + LedgerEntry("reward:$redemptionId", "REWARD", xp = 0, stars = -cost),
        )
    }
}

enum class PlayerLevel(val title: String, val minXp: Int) {
    EXPLORER("ODKRYWCZYNI", 0),
    TRACKER("TROPICIELKA", 50),
    DETECTIVE("DETEKTYWKA", 100),
    MASTER("MISTRZYNI", 200),
    ;

    companion object {
        fun forXp(xp: Int): PlayerLevel {
            require(xp >= 0) { "XP cannot be negative" }
            return entries.last { xp >= it.minXp }
        }
    }
}

data class Coordinates(val latitude: Double, val longitude: Double)

sealed interface ProximityResult {
    data class Inside(val distanceMeters: Double) : ProximityResult
    data class Outside(val distanceMeters: Double) : ProximityResult
    data class Uncertain(val distanceMeters: Double, val accuracyMeters: Double) : ProximityResult
}

object Proximity {
    fun distanceMeters(from: Coordinates, to: Coordinates): Double {
        require(from.latitude in -90.0..90.0 && from.longitude in -180.0..180.0)
        require(to.latitude in -90.0..90.0 && to.longitude in -180.0..180.0)
        val earthRadiusMeters = 6_371_000.0
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(to.longitude - from.longitude)
        val a = (kotlin.math.sin(deltaLat / 2).let { it * it } +
            kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
            kotlin.math.sin(deltaLon / 2).let { it * it }).coerceIn(0.0, 1.0)
        return earthRadiusMeters * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }

    fun classify(
        user: Coordinates,
        place: Coordinates,
        radiusMeters: Int,
        accuracyMeters: Double,
    ): ProximityResult {
        require(radiusMeters > 0) { "Radius must be greater than zero" }
        require(accuracyMeters.isFinite() && accuracyMeters >= 0.0) { "Accuracy must be finite and non-negative" }
        val distance = distanceMeters(user, place)
        return when {
            distance - accuracyMeters > radiusMeters -> ProximityResult.Outside(distance)
            distance + accuracyMeters <= radiusMeters -> ProximityResult.Inside(distance)
            else -> ProximityResult.Uncertain(distance, accuracyMeters)
        }
    }

    fun isInside(user: Coordinates, place: Coordinates, radiusMeters: Int): Boolean {
        require(radiusMeters > 0) { "Radius must be greater than zero" }
        return distanceMeters(user, place) <= radiusMeters
    }
}
