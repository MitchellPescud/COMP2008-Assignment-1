# Parks List AU

An Android app for discovering parks around Australia. Browse a curated list, search and filter by what you want to do, save favourites, and keep your own visited marks and ratings.

## Features
- **Explore** – browse every park in a scrolling list with a photo, location and rating.
- **Search and filter** – free-text search across name, location, activities and features, combined with activity chips (Walking, Picnics, Cycling, Photography, ...) and feature chips (Playground, Lookout, Cafe, Lake, BBQ Area, ...).
- **Park details** – description, average rating, activities and features, plus your own star rating and a visited toggle.
- **Favourites** – heart any park and find them again on a dedicated screen. Favourites, ratings and visited state persist between launches.

## Tech
- Kotlin, Android Views (XML layouts), `RecyclerView` with a custom adapter
- Material Components (`SearchView`, `ChipGroup`, rating bars)
- `SharedPreferences` for favourites, visited state and user ratings
- Park data loaded from a bundled text file (`assets/parks.txt`) into a `Park` data class (ID, name, location, activities, features, description, rating)
- Min SDK 24, target SDK 36

## Project structure
| File | Role |
|---|---|
| `MainActivity` | landing screen with Explore / Favourites entry points |
| `Explore` | search box, filter chips, results list |
| `ParkAdapter` | list item binding and favourite toggling |
| `ParkDetailedView` | detail screen, ratings and visited state |
| `Favourites` / `FavouritesStore` | favourites screen and its persistence |
| `ImportParkRepository` | parses `parks.txt` into `Park` objects |

## Build and run
1. Open the project in Android Studio (recent stable release).
2. Let Gradle sync, then run on an emulator or device with Android 7.0 (API 24) or newer.

## Adding a park
Add a line to `app/src/main/assets/parks.txt`:
```
park_id | Park Name | City, STATE | Activity, Activity | Feature, Feature | Description | 4.5
```
Optionally add a matching drawable named after the `park_id` for its photo; otherwise a default image is shown.
