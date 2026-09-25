# Location

The MVP uses a one-shot foreground location check when the player asks to check in. `FusedLocationProvider` checks that coarse or precise permission is granted, checks that device location services are enabled, and requests a fresh high-accuracy location. It does not request background location or save a route.

On Android 12 and later, the player can grant approximate location even when precise location is requested. The game should explain when accuracy is too low to confirm a small geofence and offer another check; it should not treat permission denial as an app error.

Location access is not wired to a screen yet. Before field testing, test denial, approximate access, disabled device location, unavailable fixes, and successful approach on a real phone. Foreground checks are the required baseline; background geofencing remains optional.
