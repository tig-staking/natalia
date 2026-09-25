# Adding a place

Places are data, not separate Compose screens. Edit `app/src/main/assets/citypacks/barcelona.json` and add another item to `places`.

Each place needs a unique `id`, coordinates, a positive proximity radius, introductory copy, one short fact, a Spanish word, a quest, a quiz with a valid correct answer index, non-negative XP and star rewards, a badge label, and an avatar pose. Put the XP and star splits for discovery, quest, Spanish word, and quiz in `rewards`; their totals must equal the place's `xp` and `stars`. Keep the same City Pack schema version until a parser change requires a migration.

Use stable, unique place, quest and quiz IDs. Each reward redemption also needs a unique transaction ID. The progress store uses IDs as idempotency keys to avoid paying out twice after app restarts or repeated location callbacks. Validate the file and game flow before adding more content.
