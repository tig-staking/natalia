# NataliaQuest — UI Direction Comparison

## Purpose and scope

This document compares three visual interpretations of the same product and the same design system. It does not select a final direction and does not authorize a production UI rewrite.

The comparison is grounded in the current application flow and uses the same four product moments in every direction:

1. **Home** — identity, player status, continuation, and primary navigation.
2. **Map / Place** — real-world context, GPS check-in, and active destination.
3. **Quest / Quiz** — the main interaction loop and answer feedback.
4. **Passport / Reward** — completion, collection, progression, and a real reward.

These moments cover the current engine from entry through exploration and completion. They also expose the largest visual trade-offs: character prominence, photography, information density, gamification, and emotional feedback.

The generated comparison boards are exploratory references only. They are intentionally kept outside the repository until a direction is selected. Their text and individual details are illustrative, not approved product copy.

## Shared foundation

All three directions use:

- the palette and semantic color rules from `docs/DESIGN_SYSTEM.md`,
- Nunito as the intended primary typeface,
- clean Material 3-compatible cards and controls,
- 12dp, 16dp, and 24dp corner tokens,
- a predictable four-item primary navigation model,
- authentic destination photography where it adds real-world context,
- the same core hierarchy and game actions,
- manga artwork as a character and emotion layer,
- short, purposeful animation,
- readable outdoor contrast and large touch targets.

The current app does not yet implement this shared foundation in code. It uses default Material 3 styling, a single scrolling column, text/emoji placeholders, and screen state held in `MainActivity.kt`.

## Direction A — Clean Adventure

### Character

Modern, spacious, and restrained. The application feels like a polished travel companion with a light game layer.

### Layout and hierarchy

- Large whitespace and a strong single-column hierarchy.
- One primary action per screen.
- Compact player progress near the top rather than a persistent game dashboard.
- Photography carries destination context.
- Cards remain visually quiet and use consistent spacing and elevation.

### Color

- Warm neutral background and surfaces dominate.
- Coral marks the primary action.
- Teal is reserved for location and exploration.
- Gold appears only for earned progress and rewards.

### Natalia character

- Used on onboarding or the Home hero and major success moments.
- Usually absent from dense map and form-like interactions.
- Clean Anime treatment with restrained expression and minimal decorative framing.

### Photography

- Prominent destination card on Map / Place.
- Large, clean crop with simple metadata below it.
- Photography provides authenticity while illustration provides personality.

### Components

- Hero card.
- Destination photo card.
- Compact progress summary.
- Standard answer rows with state icons.
- Passport stamps with restrained empty and locked states.

### Navigation

A stable bottom bar is the clearest production direction if the application keeps four primary destinations. It replaces the current stack of Home menu buttons and reduces repeated “return home” actions.

### Motion and microinteractions

- 200ms fades and small state changes.
- 300ms card or screen transitions.
- 700ms reward celebration with a brief character appearance.
- Minimal persistent motion.

### Strengths

- Highest readability and easiest outdoor use.
- Lowest risk of visual fatigue.
- Scales well as Barcelona content grows.
- Keeps destination photography and useful data central.

### Limitations

- Can feel less personal if Natalia is reduced too far.
- Emotional moments require careful art placement to avoid feeling generic.

### Risk of appearing too childish

Low. The main risk is the opposite: becoming a generic travel app if character moments and progress feedback are too subtle.

### Compose implementation difficulty

Low to medium. Most patterns map directly to Material 3 components and a small token/component layer. Photography, bottom navigation, and responsive hero layouts are straightforward.

## Direction B — Manga Travel

### Character

Narrative, warm, and personal. The application remains modern, but Natalia reacts to key travel and game moments.

### Layout and hierarchy

- Clean functional cards with selected illustrated moments.
- Character crops may overlap a hero/photo boundary but do not cover controls.
- Quest and quiz feedback can include a small reaction portrait.
- Passport completion combines a real destination stamp with character celebration.

### Color

- The same semantic palette, with slightly more visible coral and teal framing.
- Brush-like accents may appear around character art only.
- Surfaces remain neutral so illustration does not compete with content.

### Natalia character

- Welcome illustration on Home.
- Small thinking reaction for wrong quiz answers or hints.
- Discovery and success reactions for meaningful transitions.
- Not automatically present on every functional screen.

### Photography

- Destination photography remains important on the map/place screen and mission context.
- Illustration may overlap or respond to a photo, but never replace useful landmark recognition.

### Components

- Narrative hero.
- Destination photo card.
- Reaction panel for quiz feedback.
- Achievement stamp and celebration panel.
- Optional short caption in a restrained handwritten display treatment; body text stays Nunito.

### Navigation

The same predictable bottom navigation as Direction A. Narrative character moments happen inside content, not through a separate navigation metaphor.

### Motion and microinteractions

- Subtle slide and fade for reaction portraits.
- A short “thinking” transition after a wrong answer.
- Character discovery/reward sequence within the 700ms reward token where practical.
- Motion never blocks persistence or the next action.

### Strengths

- Strongest personal identity and emotional connection.
- Best match for the “motion comic travel companion” idea.
- Makes errors, hints, and achievements feel supportive rather than mechanical.

### Limitations

- Requires a consistent master character and carefully maintained pose library.
- More art direction and asset QA than Direction A.
- Character crops need responsive rules for different phone sizes.

### Risk of appearing too childish

Medium. The risk increases with oversized expressions, too many speech bubbles, bright accents on every card, or character art on every screen. Natural proportions and restrained placement keep the direction age-appropriate.

### Compose implementation difficulty

Medium. The base UI remains standard Compose, but layered hero compositions, transparent character assets, reaction transitions, and small-screen fallback arrangements require reusable layout components.

## Direction C — Game Adventure

### Character

Dynamic and progression-oriented. The travel experience is presented as a clear sequence of missions, unlocks, XP, stars, and collections.

### Layout and hierarchy

- Player level and current mission are highly visible on Home.
- Map markers display status and completion.
- Quest stages use a progress rail.
- Passport emphasizes collection and locked content.
- Reward cost and availability are explicit.

### Color

- Strongest semantic use of the full palette.
- Coral marks active missions.
- Teal marks exploration and verified GPS.
- Gold marks rewards.
- Violet is limited to supporting achievement states.

### Natalia character

- Dynamic Home hero and major achievement reaction.
- Less frequent within dense mission and passport grids.
- Stronger pose and silhouette without fantasy equipment or combat language.

### Photography

- Used in mission cards, map markers, and collection stamps.
- Crops are smaller and more modular than in Direction A or B.

### Components

- Level card and progress bar.
- Active mission card.
- Stage rail.
- XP/star reward feedback.
- Badge and destination collection grid.
- Locked/redeemable reward card.

### Navigation

Stable bottom navigation plus strong deep links from active mission and reward cards. Avoid adding multiple nested game menus during the MVP.

### Motion and microinteractions

- Progress interpolation and count-up.
- Clear unlock transition for stages and stamps.
- Short XP/star feedback after completion.
- Restrained confetti or glow for badges, rewards, and level-up.

### Strengths

- Makes progression and available actions immediately understandable.
- Best support for future multi-place content, collections, and repeat visits.
- Strong motivation for users who enjoy visible completion systems.

### Limitations

- Highest information density.
- Travel context can become secondary if progress widgets dominate.
- Requires disciplined prioritization as more mechanics are added.

### Risk of appearing too childish

Medium. Large badges, saturated colors, toy-like icons, and oversized reward effects would quickly lower the perceived age. Clean typography, real photography, and neutral surfaces are essential.

### Compose implementation difficulty

Medium to high. The direction needs more reusable components, progress states, grids, responsive layouts, and motion. The underlying game state already supports most displayed information, but presentation architecture should be separated from the current monolithic screen before full implementation.

## Comparison

| Dimension | A — Clean Adventure | B — Manga Travel | C — Game Adventure |
| --- | --- | --- | --- |
| Character presence | Low | High at emotional moments | Medium, strongest at milestones |
| Travel photography | High | High | Medium |
| Gamification visibility | Subtle | Balanced | Strong |
| Information density | Low | Medium | High |
| Emotional storytelling | Medium | High | Medium-high |
| Art production cost | Low | High | Medium-high |
| Compose complexity | Low-medium | Medium | Medium-high |
| Childishness risk | Low | Medium | Medium |

## Elements that can be mixed

- Direction A's spacing and visual restraint can underpin either B or C.
- Direction B's reaction portraits can be used for wrong answers, hints, discoveries, and major rewards in either other direction.
- Direction C's stage rail and explicit reward availability can be used selectively in A or B.
- All directions can share the same theme tokens, buttons, cards, navigation, typography, photography component, and motion timing.
- The approved master character can support different emotional intensity without changing identity or rendering style.

## Elements that conflict

- A persistent, dense game dashboard conflicts with Direction A's restrained hierarchy.
- Frequent full-size character art conflicts with Direction A and with the usability of Direction C's dense screens.
- Decorative manga framing on routine controls conflicts with all three directions and with the design system.
- Heavy collection grids and multiple simultaneous progress systems conflict with Direction B's narrative pacing unless moved to a dedicated Passport view.

## Decision boundary

No direction is selected in this document. The next production step should begin only after the user chooses a base direction and identifies any elements to borrow from the other two.

The three character concepts are also exploratory. Do not generate the full pose set until one base character is approved as the master reference.
