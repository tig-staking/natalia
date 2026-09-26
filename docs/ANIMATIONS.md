# Animations

The game UI uses a reusable Compose celebration banner in `MainActivity.kt`. It fades and scales into view, shows comic-style star marks, then clears after a short interval. Current triggers are place discovery, quest completion, Spanish word completion, quiz outcomes, and parent-approved reward redemption. Feedback is visual and uses text/emoji placeholders, so no character artwork is required.

Keep animation state presentation-only: game rewards and completion markers must be persisted by `GameEngine`/`GameProgressRepository` independently of whether the animation is visible. Do not pay rewards from animation callbacks. When adding a new trigger, set the celebration message only after the corresponding game action succeeds.

XP and star totals animate to their new values. The current component is still a lightweight first pass: XP stage rewards detect a level threshold crossing and change the celebration message to the new level title, and the radial burst/avatar reactions are placeholders. Dedicated avatar poses, confetti, points-flight animation, and badge unlock sequence remain future work. Verify motion and layout on a phone, including reduced-motion/accessibility behavior, before calling the animation milestone complete.
