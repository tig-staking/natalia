# NataliaQuest — Design System

## 1. Purpose

This document is the visual source of truth for NataliaQuest.

All screens, UI components, typography, colors, icons, illustrations, animations, and visual interactions should follow this design system.

The design system is intentionally independent from the current list of screens and features. As the application evolves, new functionality should reuse this visual language rather than introduce unrelated styles.

## 2. Visual direction

NataliaQuest should feel:

- modern,
- youthful,
- adventurous,
- polished,
- manga-inspired,
- travel-oriented,
- slightly game-like,
- warm and energetic.

The application must not feel:

- preschool-like,
- overly childish,
- excessively kawaii,
- visually chaotic,
- like a generic educational app,
- like a fantasy RPG.

Target direction: a modern travel-adventure application with clean mobile UI, soft manga character artwork, subtle gamification, warm Mediterranean-inspired colors, and polished microinteractions.

## 3. Core principle: UI is not manga

The core application interface should remain clean, readable, and modern.

Manga-inspired artwork should provide personality, emotion, and storytelling. Illustration should complement the interface rather than dominate it.

Use manga artwork primarily for:

- important moments,
- welcome states,
- discoveries,
- hints,
- achievements,
- quest moments,
- rewards,
- narrative transitions,
- empty states where appropriate.

Do not transform every button, card, or panel into comic-style artwork.

## 4. Target audience

The visual language should work for a pre-teen or young-teen user without appearing childish.

The application should feel cool, adventurous, and personal rather than educational or toy-like.

## 5. Color palette

### Core colors

| Token | Value | Purpose |
| --- | --- | --- |
| `Background` | `#FFF9F3` | Main background |
| `Surface` | `#F6EFE6` | Cards and secondary surfaces |
| `Primary` | `#FF786B` | Main actions and important interactions |
| `Secondary` | `#4BC4BF` | Exploration and discovery |
| `Reward` | `#F5B942` | Rewards, XP, and achievements |
| `Accent` | `#9A8CFF` | Supporting interactive accent |
| `TextPrimary` | `#263247` | Main text |
| `TextSecondary` | `#667085` | Supporting text |

### State colors

| Token | Value |
| --- | --- |
| `Success` | `#56B881` |
| `Error` | `#E85D68` |
| `Info` | `#5C9CE6` |
| `Disabled` | `#BABFC8` |

## 6. Color rules

Do not use every accent color simultaneously.

Prefer one dominant visual accent for a major interaction or section.

Suggested semantic associations:

- exploration and location → `Secondary`,
- main action and active mission → `Primary`,
- rewards and progress → `Reward`,
- secondary interactive experiences → `Accent`.

Do not create arbitrary near-duplicate colors when an existing token is sufficient.

## 7. Typography

Primary font: **Nunito**.

Recommended weights:

- ExtraBold — hero titles,
- Bold — screen titles,
- SemiBold — card titles,
- Regular — body text,
- Bold — buttons and important values.

Fallback candidate: **Poppins**.

Do not mix multiple body fonts.

Avoid:

- comic fonts,
- handwriting fonts for normal UI,
- decorative fonts in body content,
- excessive uppercase,
- ultra-thin weights.

Decorative typography may be used sparingly for branding or special titles only.

## 8. Layout principles

Prefer:

- generous spacing,
- clear hierarchy,
- comfortable touch targets,
- predictable navigation,
- limited primary actions,
- visually separated content,
- strong outdoor readability.

Avoid:

- overly dense screens,
- excessive decoration,
- multiple competing calls to action,
- unnecessarily complex layouts.

## 9. Shape system

| Token | Value | Suggested usage |
| --- | --- | --- |
| `CornerSmall` | `12dp` | Small controls |
| `CornerMedium` | `16dp` | Cards and buttons |
| `CornerLarge` | `24dp` | Hero panels and special sections |

Avoid arbitrary radius values. Do not make every element pill-shaped.

## 10. Cards and surfaces

Cards should generally use:

- clean surfaces,
- subtle elevation,
- generous padding,
- restrained decoration,
- consistent corner radii.

Cards may contain text, imagery, progress, status, rewards, illustrations, and icons.

Reuse existing card patterns before creating new ones.

## 11. Buttons

### Primary

Use `Primary` for the most important action.

### Secondary

Use neutral surfaces and dark text.

### Reward

Use `Reward` only for actions genuinely related to rewards, achievements, or progression.

Do not introduce a new button style for every feature.

## 12. Icons

Use one coherent icon family wherever possible.

Preferred direction:

- rounded,
- modern,
- outline or soft-filled,
- simple silhouettes,
- consistent stroke weight.

Avoid mixing emoji, realistic icons, 3D icons, unrelated line styles, and cartoon icons.

## 13. Character illustration style

The main character should follow a **soft contemporary manga/anime** direction.

Characteristics:

- clean line work,
- soft shading,
- moderate detail,
- expressive but natural face,
- contemporary clothing,
- believable proportions,
- warm and cohesive color treatment.

Avoid:

- chibi proportions,
- exaggerated kawaii styling,
- preschool cartoon styling,
- highly sexualized styling,
- inconsistent rendering between assets.

The illustrated character should remain recognizable as the same person across all assets.

## 14. Character role

The main character is a recurring travel companion and emotional guide.

She should support storytelling, discoveries, reactions, encouragement, progression, rewards, and hints.

She should not appear automatically on every screen. The interface remains primary.

## 15. Photography and real places

Where the application references real destinations or landmarks, authentic photography may be used.

Recommended visual combination:

- real photography,
- clean modern UI,
- manga character reactions,
- stylized gamification.

Do not replace useful real-world context with generated art unless there is a clear reason.

## 16. Gamification

Gamification may include XP, quests, achievements, collections, badges, progress, milestones, and unlocks.

Gamification should feel motivating without overwhelming the travel experience.

Avoid heavy fantasy RPG styling.

## 17. Animation

Animations should be short, smooth, purposeful, subtle, and satisfying.

| Token | Value |
| --- | --- |
| `AnimationFast` | `200ms` |
| `AnimationNormal` | `300ms` |
| `AnimationReward` | `700ms` |

Recommended patterns:

- fade,
- subtle slide,
- scale,
- progress interpolation,
- small reward celebrations.

Avoid constant bouncing, unnecessary motion, flashing, and long blocking animations.

## 18. Celebration effects

Allowed sparingly:

- subtle sparkles,
- light confetti,
- small stars,
- brief glow,
- scale effects.

Reserve these for meaningful moments such as achievements, milestones, important completions, rewards, and level-ups.

## 19. Navigation

Navigation should remain simple, predictable, consistent, and easy to understand.

The design system does not prescribe a fixed list of screens. Navigation should follow the actual product structure.

## 20. Accessibility

Requirements:

- sufficient contrast,
- readable text sizes,
- comfortable touch targets,
- states not represented by color alone,
- good outdoor readability,
- animations not required to understand functionality.

## 21. Dark mode

Dark mode is not part of the initial requirement. Do not implement it unless explicitly requested later.

## 22. Design tokens

Centralize reusable values in the codebase.

```text
Background    #FFF9F3
Surface       #F6EFE6

Primary       #FF786B
Secondary     #4BC4BF
Reward        #F5B942
Accent        #9A8CFF

TextPrimary   #263247
TextSecondary #667085

Success       #56B881
Error         #E85D68
Info          #5C9CE6
Disabled      #BABFC8

CornerSmall   12dp
CornerMedium  16dp
CornerLarge   24dp

AnimationFast   200ms
AnimationNormal 300ms
AnimationReward 700ms
```

Avoid hardcoding these values repeatedly across screens.

## 23. Consistency rules

- Use one primary typography system.
- Use centralized design tokens.
- Use one coherent icon family.
- Use one consistent manga illustration style.
- Keep UI cleaner than illustrations.
- Reuse existing components before creating new ones.
- Do not create screen-specific visual systems.
- Avoid decorative elements without purpose.
- Extend this document intentionally when new visual conventions are required.
- Preserve consistency across future destinations and features.

## 24. AI agent guidance

Any AI agent working on NataliaQuest should treat `docs/DESIGN_SYSTEM.md` as the visual source of truth.

Before significant UI work:

- read this file,
- inspect existing reusable components,
- reuse existing tokens,
- preserve the established visual language.

Do not independently invent new palettes, unrelated typography, new icon styles, new illustration styles, arbitrary spacing systems, or unrelated animation conventions.

If the product genuinely requires a new design convention, propose an intentional update to this document.

## 25. Evolution

This design system is expected to evolve as the application grows.

Changes should be deliberate, documented, and reusable across the project rather than introduced ad hoc in individual screens.
