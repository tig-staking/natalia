# Animations

The game UI uses a reusable Compose celebration banner in `MainActivity.kt`. It fades and scales into view, shows the matching illustrated character reaction, then clears after a short interval. Thinking, discovery, and victory artwork is loaded from semantic drawable resources. Current triggers are place discovery, quest completion, Spanish word completion, quiz outcomes, and parent-approved reward redemption.

Keep animation state presentation-only: game rewards and completion markers must be persisted by `GameEngine`/`GameProgressRepository` independently of whether the animation is visible. Do not pay rewards from animation callbacks. When adding a new trigger, set the celebration message only after the corresponding game action succeeds.

XP and star totals animate to their new values. XP stage rewards detect a level threshold crossing and change the celebration message to the new level title. The radial burst is still code-drawn; confetti, points-flight animation, reduced-motion handling, and a dedicated badge unlock sequence remain future work. Check motion and layout on a phone before calling the animation milestone complete.
