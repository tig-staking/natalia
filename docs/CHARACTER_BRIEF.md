# NataliaQuest — Character Brief

## 1. Purpose

Natalka is the recurring protagonist and travel companion of NataliaQuest.

The illustrated character should be based on a real reference photograph supplied by the user. The goal is not to create a generic animated girl. The goal is to create a consistent stylized animated interpretation of the real person while preserving recognizable features.

## 2. Reference source

When a suitable reference photograph is provided, use it as the primary source for:

- facial proportions,
- hairstyle,
- hair color,
- eye appearance,
- general facial character,
- recognizable features,
- age-appropriate appearance.

Do not invent major physical features that contradict the reference image.

The illustration does not need to be photorealistic, but it should remain recognizably inspired by the photographed person.

Real reference photographs are private source material. Keep them outside Git and never commit them to the repository. The local `foty/` directory is ignored for this purpose.

## 3. Target style

Preferred direction: **expressive, polished 3D animated travel adventure** — warm and appealing like a high-quality family animated feature (Pixar-like as a broad shorthand), while remaining an original character design.

The character should feel:

- youthful,
- modern,
- energetic,
- curious,
- confident,
- warm,
- adventurous.

She should not feel:

- preschool-like,
- chibi,
- excessively kawaii,
- exaggerated,
- toy-like or heavily exaggerated,
- mature beyond her age.

## 4. Visual treatment

Prefer:

- carefully sculpted 3D forms and a hand-drawn sensibility,
- soft studio-quality shading and natural light,
- moderate, mobile-readable detail,
- expressive eyes while retaining the reference's natural eye shape,
- recognizable face and natural facial proportions,
- believable child body proportions and age,
- contemporary clothing,
- a cohesive palette.

The character should remain readable at mobile-app sizes. Avoid excessive tiny detail that disappears in UI usage.

## 5. Consistency

All character assets should preserve:

- the same facial identity,
- the same hairstyle unless explicitly changed,
- consistent eye color,
- consistent proportions,
- the same 3D animated rendering and material treatment,
- a consistent palette,
- a consistent age appearance.

Different poses should look like the same character rather than separate AI-generated people.

## 6. Role in the application

Natalka functions as:

- protagonist,
- travel companion,
- emotional guide,
- reaction character,
- progress companion.

Possible uses:

- welcome moments,
- discoveries,
- hints,
- success states,
- achievements,
- rewards,
- narrative transitions,
- onboarding,
- selected empty states,
- major travel moments.

## 7. Usage rule

The character should not appear automatically on every screen.

Use her where she adds emotion, context, storytelling, personality, or reward value. Avoid placing her in dense functional screens where she would reduce usability.

## 8. Character framing

Useful asset types may include:

- full body,
- three-quarter body,
- waist-up,
- portrait reaction,
- small UI reaction.

Whenever possible, assets intended for UI should work on transparent backgrounds.

## 9. Base outfit

The character should have one recognizable default travel outfit. The selected B concept outfit is the current starting point: navy-and-white striped T-shirt with coral trim, light denim shorts, gray walking sneakers, and a teal travel backpack. Keep this outfit consistent across the new concepts unless the user later changes it.

Preferred qualities:

- contemporary,
- comfortable,
- travel-friendly,
- suitable for warm weather,
- visually distinctive,
- easy to reproduce consistently.

Possible elements include sneakers, a T-shirt, shorts or other casual travel clothing, a light backpack or small bag, and subtle accessories.

Do not overdesign the outfit.

## 10. Color relationship

The character should harmonize with the application palette without looking artificially color-matched.

Application accent colors may appear subtly in accessories, clothing details, a backpack, or small visual elements. Do not force every app color into the outfit.

## 11. Initial MVP pose set

The initial character set should remain small.

Recommended starting set:

1. **Welcome** — friendly, relaxed, slightly excited.
2. **Explorer** — looking ahead, pointing, or interacting with a map.
3. **Thinking** — useful for puzzles, quizzes, and hints.
4. **Discovery** — excited or pleasantly surprised.
5. **Success** — happy reaction for completing an objective.
6. **Achievement** — proud or confident pose.
7. **Hint** — friendly explanatory gesture.
8. **Celebration** — reserved for larger rewards or important milestones.

Do not generate all poses before the base character design has been approved.

## 12. Character creation workflow

### Stage 1 — Reference

Use the real photograph as the visual reference.

### Stage 2 — Character concepts

Create approximately three stylistic interpretations of the same person, all within the newly selected stylized 3D animated family-adventure direction:

- **A. Clean 3D Animation** — restrained, modern, and UI-friendly, with natural proportions and soft materials.
- **B. Feature Animation** — warmer facial acting and richer sculpted lighting, like a polished family adventure film; original design, not a copy of any studio character.
- **C. 3D Game Adventure** — a more graphic silhouette and slightly more dynamic pose, still grounded, age-appropriate, and non-fantasy.

All three should preserve the same identity, haircut, age, and base outfit, and harmonize with `docs/DESIGN_SYSTEM.md`.

### Stage 3 — Selection

Choose one direction before producing a full asset library.

### Stage 4 — Master character

Create one definitive base character. This becomes the visual reference for all later character assets.

### Stage 5 — Pose set

Create only the required expressions and poses.

## 13. Important AI generation rule

Once the master character has been selected, later generations should use the approved character artwork as a visual reference whenever the generation system supports image reference.

Do not rely solely on a text description for later poses if a visual reference can be supplied. This is critical for character consistency.

## 14. Avoid

Do not:

- randomly alter facial identity,
- change hairstyle between assets,
- significantly change age appearance,
- use chibi proportions,
- exaggerate the eyes, head, or kawaii traits,
- sexualize the character,
- create inconsistent eye or hair colors,
- create unrelated outfits for every image,
- turn the character into a generic anime or animation archetype,
- copy a specific existing studio character or add recognizable studio branding.

## 15. Relationship to the design system

This file defines the character system. `docs/DESIGN_SYSTEM.md` defines the overall visual language.

If there is a conflict, review the two documents together and update them intentionally rather than silently allowing them to diverge.
