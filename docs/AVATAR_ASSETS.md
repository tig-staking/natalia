# Avatar assets

The app currently has no final Natalia artwork. Keep the game playable with text and emoji placeholders; do not block engine work on a photo or illustration.

When art is added, store reusable Android drawable assets under `app/src/main/res/drawable/` (or density-aware `drawable-*` folders for raster files). Use stable resource names for these poses:

- `avatar_default`
- `avatar_map`
- `avatar_detective`
- `avatar_thinking`
- `avatar_surprised`
- `avatar_happy`
- `avatar_victory`
- `avatar_icecream`

UI code should select poses by semantic state, not by place name. Keep source artwork and any private reference photo out of source control unless the owner explicitly intends it to be part of the app. Prefer optimized WebP/VectorDrawable assets where suitable and verify legibility at phone size.
