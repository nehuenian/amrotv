# Data Layer — Full Reference

## Design: DataSource Abstraction (multi-source ready)

`MovieRepositoryImpl` depends on *interfaces*, never on `TmdbApiService` or Room DAOs directly.
Adding a new API source = implement `RemoteMovieDataSource` + rebind Hilt. Repository never changes.

```
MovieRepositoryImpl
  ├── RemoteMovieDataSource (interface)  ← TmdbMovieDataSource (now)
  │                                        OmdbMovieDataSource, TraktMovieDataSource… (future)
  └── LocalMovieDataSource  (interface)  ← RoomMovieDataSource
```

Each data source **maps its own DTOs/entities → domain models internally**.
No DTOs or Room entities cross the interface boundary.

---

## Package Layout

```
datasource/
  remote/
    RemoteMovieDataSource.kt          ← interface, returns domain models
    tmdb/
      TmdbMovieDataSource.kt          ← Retrofit + DTO→domain mapping via Mapper classes
      TmdbApiService.kt
      dto/  (TrendingMoviesResponseDto, MovieDto, MovieDetailDto, GenreListResponseDto, GenreDto)
            (GenreDataToDomainMapper, MovieDataToDomainMapper, MovieDetailDataToDomainMapper)
  local/
    LocalMovieDataSource.kt           ← interface, accepts/returns domain models
    NoOpLocalMovieDataSource.kt       ← active MVP binding (no-op stub)
    room/                             ← planned, not yet implemented
      (RoomMovieDataSource, AmroDatabase, DAOs, entities to be added here)
repository/
  MovieRepositoryImpl.kt              ← orchestrates interfaces only, no mapping
di/
  DataModule.kt                       ← @Provides TmdbApiService
  DataBindingsModule.kt               ← @Binds impls → interfaces
```

---

## DataSource Interfaces

```kotlin
interface RemoteMovieDataSource {
    suspend fun getTrendingMovies(): List<Movie>   // handles paging internally
    suspend fun getMovieDetail(id: Int): MovieDetail
    suspend fun getGenres(): List<Genre>
}

interface LocalMovieDataSource {
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun getCachedMovies(): List<Movie>
    suspend fun saveGenres(genres: List<Genre>)
    suspend fun getCachedGenres(): List<Genre>
}
```

---

## DTOs (kotlinx.serialization, inside `tmdb/dto/`)

> Read the DTO files at:
> `feature/movies/data/src/main/kotlin/nl/abnamro/amrotv/feature/movies/data/datasource/remote/tmdb/dto/`
>
> Key notes:
> - `budget` and `revenue` are `Long` (TMDB returns `0` for unknown) — mapper converts `0` → `null` in domain
> - `MovieDetailDto` has `genres` as full objects (`List<GenreDto>`), unlike `MovieDto` which has `genreIds: List<Int>`
> - All fields have defaults to be resilient to missing JSON keys

---

## TmdbMovieDataSource — Dynamic Page Count

> Read the implementation:
> `feature/movies/data/src/main/kotlin/.../datasource/remote/tmdb/TmdbMovieDataSource.kt`
>
> Key behaviour: pages are fetched sequentially until 100 unique movies are collected.
> Page 1 failure throws to the caller. Any subsequent page failure stops pagination early,
> returning however many movies were collected — preventing cascading requests on rate limits
> or auth errors. Duplicate IDs across pages are skipped.

---

## Room Entities (private to `room/entity/`)

> **Room is scaffolded but not active in MVP.** `NoOpLocalMovieDataSource` is the current
> active Hilt binding. The package layout below shows the planned structure for a future
> implementation — the entities and DAOs are present but `RoomMovieDataSource` is not wired up.
> Activating the cache requires implementing `RoomMovieDataSource` and swapping the `@Binds`
> in `DataBindingsModule`.
>
> Key design choices for the planned implementation:
> - `genreIds` stored as a comma-separated string (e.g. `"28,12,878"`) — no join table needed for MVP
> - `RoomMovieDataSource` maps `Movie ↔ MovieEntity` and `Genre ↔ GenreEntity` internally — entities never cross the `LocalMovieDataSource` boundary

---

## MovieRepositoryImpl — Orchestration Only

> Read the implementation:
> `feature/movies/data/src/main/kotlin/.../repository/MovieRepositoryImpl.kt`
>
> Key rule: **no mapping logic, no DTOs, no entities** — orchestrates data sources only.
> Pattern: try remote first → save to local → emit; on error → load from local; if local empty → propagate error.

---

## Adding a New API Source in the Future

```kotlin
// 1. Implement the interface for the new source
class OmdbMovieDataSource @Inject constructor(...) : RemoteMovieDataSource { ... }

// 2. Or aggregate with Composite pattern
class CompositeMovieDataSource @Inject constructor(
    private val tmdb: TmdbMovieDataSource,
    private val omdb: OmdbMovieDataSource,
) : RemoteMovieDataSource { ... }

// 3. Rebind in Hilt — MovieRepositoryImpl is untouched
```

---

## Hilt Wiring (`DataBindingsModule`)

> Read `feature/movies/data/src/main/kotlin/.../di/DataBindingsModule.kt`.
> `DataBindingsModule` binds: `TmdbMovieDataSource → RemoteMovieDataSource`,
> `NoOpLocalMovieDataSource → LocalMovieDataSource` (active binding in MVP — swap to
> `RoomMovieDataSource` to activate offline cache), `MovieRepositoryImpl → MovieRepository`.

