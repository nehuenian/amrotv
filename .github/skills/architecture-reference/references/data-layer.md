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
      TmdbMovieDataSource.kt          ← Retrofit + DTO→domain (DTOs private here)
      TmdbApiService.kt
      dto/  (TrendingMoviesResponseDto, MovieDto, MovieDetailDto, GenreListResponseDto, GenreDto)
  local/
    LocalMovieDataSource.kt           ← interface, accepts/returns domain models
    room/
      RoomMovieDataSource.kt          ← Room calls + entity→domain (entities private here)
      AmroDatabase.kt
      MovieDao.kt / GenreDao.kt
      entity/  (MovieEntity, GenreEntity)
repository/
  MovieRepositoryImpl.kt              ← orchestrates interfaces only, no mapping
di/
  DataModule.kt                       ← @Provides TmdbApiService, AmroDatabase, DAOs
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

> DTOs will be created in Commit 8 at:
> `feature/movies/data/src/main/kotlin/nl/abnamro/amrotv/feature/movies/data/datasource/remote/tmdb/dto/`
>
> See `plan.md` for the full field list. Key notes:
> - `budget` and `revenue` are `Long` (TMDB returns `0` for unknown) — mapper converts `0` → `null` in domain
> - `MovieDetailDto` has `genres` as full objects (`List<GenreDto>`), unlike `MovieDto` which has `genreIds: List<Int>`
> - All fields have defaults to be resilient to missing JSON keys

---

## TmdbMovieDataSource — Dynamic Page Count

> Read the implementation once created in Commit 8:
> `feature/movies/data/src/main/kotlin/.../datasource/remote/tmdb/TmdbMovieDataSource.kt`
>
> Key requirement: page size is **not hardcoded** — derive `pagesNeeded` from the first-page response:
> `pagesNeeded = ceil(100.0 / pageSize).toInt()`
> Fetch remaining pages in parallel with `coroutineScope { (2..n).map { async { ... } }.awaitAll() }`.

---

## Room Entities (private to `room/entity/`)

> Entities will be created in Commit 8 at `feature/movies/data/src/.../datasource/local/room/entity/`
>
> Key design choices:
> - `genreIds` stored as a comma-separated string (e.g. `"28,12,878"`) — no join table needed for MVP
> - `RoomMovieDataSource` maps `Movie ↔ MovieEntity` and `Genre ↔ GenreEntity` internally — entities never cross the `LocalMovieDataSource` boundary

---

## MovieRepositoryImpl — Orchestration Only

> Read the implementation once created in Commit 8:
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

> Standard `@Binds` pattern. Read an existing module (e.g. `core/network/`) for reference.
> `DataBindingsModule` binds: `TmdbMovieDataSource → RemoteMovieDataSource`, `RoomMovieDataSource → LocalMovieDataSource`, `MovieRepositoryImpl → MovieRepository`.

