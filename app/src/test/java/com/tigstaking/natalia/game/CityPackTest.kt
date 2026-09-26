package com.tigstaking.natalia.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CityPackTest {
    @Test
    fun parsesValidCityPackAndStageRewards() {
        val pack = CityPack.parse(validPackJson)

        assertEquals("test-city", pack.id)
        assertEquals(1, pack.places.size)
        assertEquals("test-place", pack.places.single().id)
        assertEquals(50, pack.places.single().rewards.totalXp)
        assertEquals(50, pack.places.single().rewards.totalStars)
        assertEquals("TO-rre · mocne, wibrujące „rr”", pack.places.single().spanishWord.pronunciation)
    }

    @Test
    fun rejectsUnsupportedSchemaVersion() {
        assertThrows(IllegalArgumentException::class.java) {
            CityPack.parse(validPackJson.replace("\"schemaVersion\": 1", "\"schemaVersion\": 2"))
        }
    }

    @Test
    fun rejectsCoordinatesOutsideLatitudeRange() {
        assertThrows(IllegalArgumentException::class.java) {
            CityPack.parse(validPackJson.replace("\"latitude\": 41.0", "\"latitude\": 91.0"))
        }
    }

    @Test
    fun rejectsRewardTotalsThatDoNotMatchPlaceTotals() {
        assertThrows(IllegalArgumentException::class.java) {
            CityPack.parse(validPackJson.replace("\"xp\": 10", "\"xp\": 11"))
        }
    }

    @Test
    fun parsesMultipleChoiceQuestAnswers() {
        val choiceQuestJson = validPackJson.replace(
            "\"type\": \"OBSERVATION\", \"prompt\": \"Look around\"",
            "\"type\": \"MULTIPLE_CHOICE\", \"prompt\": \"Choose\", \"answers\": [\"A\", \"B\"], \"correctAnswerIndex\": 1",
        )
        val quest = CityPack.parse(choiceQuestJson).places.single().quest
        assertEquals(listOf("A", "B"), quest.answers)
        assertEquals(1, quest.correctAnswerIndex)
    }

    private companion object {
        val validPackJson = """
            {
              "schemaVersion": 1,
              "id": "test-city",
              "name": "Test City",
              "country": "ES",
              "mapCenter": { "latitude": 41.0, "longitude": 2.0 },
              "defaultZoom": 14.0,
              "places": [
                {
                  "id": "test-place",
                  "name": "Test Place",
                  "latitude": 41.0,
                  "longitude": 2.0,
                  "geofenceRadiusMeters": 100,
                  "intro": "Welcome",
                  "fact": "A fact",
                  "spanishWord": { "word": "torre", "meaning": "wieża", "pronunciation": "TO-rre · mocne, wibrujące „rr”" },
                  "quest": { "id": "test-quest", "type": "OBSERVATION", "prompt": "Look around" },
                  "quiz": {
                    "id": "test-quiz",
                    "question": "What does torre mean?",
                    "answers": ["tower", "street", "sea"],
                    "correctAnswerIndex": 0
                  },
                  "xp": 50,
                  "stars": 50,
                  "rewards": {
                    "discovery": { "xp": 10, "stars": 10 },
                    "quest": { "xp": 20, "stars": 20 },
                    "spanishWord": { "xp": 10, "stars": 10 },
                    "quiz": { "xp": 10, "stars": 10 }
                  },
                  "badge": "TEST BADGE",
                  "avatarPose": "surprised"
                }
              ]
            }
        """.trimIndent()
    }
}
