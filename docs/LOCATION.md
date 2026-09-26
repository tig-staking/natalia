# Location

The MVP uses a one-shot foreground location check when the player asks to check in. `FusedLocationProvider` checks that coarse or precise permission is granted, checks that device location services are enabled, and requests a fresh high-accuracy location. It does not request background location or save a route.

## Map data and offline use

The current map screen is an offline coordinate preview: it plots the active POI, geofence, and fresh player fix without downloading street tiles. This is enough for the engine check-in flow, but it is not a street map.

For a geographic basemap, use MapLibre as the map renderer and keep map data separate from the APK and City Pack. The app should load the visible area online when connected and offer an offline download before travel. First try a citywide Barcelona pack at the walking zoom levels needed for the game. Keep the total downloaded offline map budget at or below 200 MB, with a lower size preferred. Measure the actual pack size with the selected provider and style before promising citywide coverage. If the complete city exceeds the budget, reduce maximum zoom or offer packs for the planned POI neighborhoods. Show the estimate before download, progress while downloading, the downloaded size, and a delete action; never silently expand the download into gigabytes. Keep POI coordinates and game progress available if map data is missing. GPS distance and geofence decisions use the City Pack coordinates and fresh location fix, not rendered map pixels or tile resolution.

MapLibre is the renderer, not a tile provider. Select a provider whose current terms explicitly permit offline downloads and local caching before enabling packs. Do not bulk-download from the public OpenStreetMap standard tile service; its usage policy disallows prefetching. Avoid a mandatory online-only map because connectivity and roaming can be unreliable during a trip. The current MVP has no external map provider configured, so no map network request, account, or API key is required.

On Android 12 and later, the player can grant approximate location even when precise location is requested. The game should explain when accuracy is too low to confirm a small geofence and offer another check; it should not treat permission denial as an app error.

Before field testing, test denial, approximate access, disabled device location, unavailable fixes, and successful approach on a real phone. Foreground checks are the required baseline; background geofencing remains optional.
