# Avatar assets

The app now includes its first integrated Natalia artwork, based on the user-approved hand-drawn 2D/painterly direction. Keep future generated art visually consistent with these references.

When art is added, store reusable Android drawable assets under `app/src/main/res/drawable/` (or density-aware `drawable-*` folders for raster files). Use stable resource names for these poses:

- `avatar_default`
- `avatar_map`
- `avatar_detective`
- `avatar_thinking`
- `avatar_surprised`
- `avatar_happy`
- `avatar_victory`
- `avatar_icecream`

Currently integrated under `app/src/main/res/drawable-nodpi/`:

- `natalka_barcelona_hero.png` — Home hero scene, including the approved Barcelona illustration.
- `avatar_thinking.png` — quiz retry / hint reaction.
- `avatar_surprised.png` — discovery reaction.
- `avatar_victory.png` — completion, badge, and success reaction.
- `avatar_icecream.png` — approved real-world reward celebration.

The default pose is still a future addition. Generated character illustrations are app assets; never add the original user photographs from `foty/` to Git.

UI code should select poses by semantic state, not by place name. Keep source artwork drafts and any private reference photo out of source control unless the owner explicitly intends them to be app assets. Prefer optimized WebP/VectorDrawable assets where suitable and verify legibility at phone size.
