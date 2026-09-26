# Location

The MVP uses a one-shot foreground location check when the player asks to check in. `FusedLocationProvider` checks that coarse or precise permission is granted, checks that device location services are enabled, and requests a fresh high-accuracy location. It does not request background location or save a route.

## Map data and offline use

The map screen now uses MapLibre Native with the OpenFreeMap Positron vector style. It centers on the active POI and draws the POI marker, geofence, and the latest foreground GPS marker. [OpenFreeMap](https://openfreemap.org/) serves OpenStreetMap-derived tiles online without an API key; MapLibre displays provider attribution. If the style cannot load after 12 seconds, the screen falls back to the local coordinate preview, so location check-in remains available without map connectivity.

Keep map data separate from the APK and City Pack. Online map viewing is implemented. Next, add an optional offline pack before travel. First measure whether a citywide Barcelona pack fits at or below 200 MB at walking zoom levels; if so, offer full-city offline download. If it exceeds the budget, reduce maximum zoom or offer packs for planned POI neighborhoods. Show the estimated and actual sizes, progress, and a delete action; never silently expand into gigabytes. Keep POI coordinates and game progress available if map data is missing. GPS distance and geofence decisions use the City Pack coordinates and fresh location fix, not rendered map pixels or tile resolution.

MapLibre is the renderer, not a tile provider. OpenFreeMap is the current online provider; its public instance is free and requires attribution, but its [terms](https://openfreemap.org/tos/) are provided as-is without availability guarantees. The terms prohibit automated collection without permission, so they are not sufficient authorization for our offline pack downloader. Choose a provider or data source whose terms explicitly permit city or region downloads before enabling that feature. Do not bulk-download from the public [OpenStreetMap standard tile service](https://operations.osmfoundation.org/policies/tiles/); its usage policy disallows prefetching. [MapLibre Native supports offline regions](https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20-android/org.maplibre.android.offline/-offline-manager/index.html), but each provider's terms still apply.

On Android 12 and later, the player can grant approximate location even when precise location is requested. The game should explain when accuracy is too low to confirm a small geofence and offer another check; it should not treat permission denial as an app error.

Before field testing, test denial, approximate access, disabled device location, unavailable fixes, and successful approach on a real phone. Foreground checks are the required baseline; background geofencing remains optional.
