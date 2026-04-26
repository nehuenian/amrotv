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

```kotlin
@Serializable data class TrendingMoviesResponseDto(
    @SerialName("page") val page: Int,
    @SerialName("results") val results: List<MovieDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)

@Serializable data class MovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("popularity") val popularity: Double = 0.0,
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("overview") val overview: String = "",
)

@Serializable data class MovieDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(), // full objects, not IDs
    @SerialName("overview") val overview: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("budget") val budget: Long = 0L,    // 0 = not available → map to null
    @SerialName("revenue") val revenue: Long = 0L,  // 0 = not available → map to null
    @SerialName("status") val status: String = "",
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("release_date") val releaseDate: String = "",
)

@Serializable data class GenreListResponseDto(@SerialName("genres") val genres: List<GenreDto>)
@Serializable data class GenreDto(@SerialName("id") val id: Int, @SerialName("name") val name: String)
```

---

## TmdbMovieDataSource — Dynamic Page Count

Page size is **not hardcoded** — derive `pagesNeeded` from the first-page response size:

```kotlin
class TmdbMovieDataSource @Inject constructor(
    private val apiService: TmdbApiService,
) : RemoteMovieDataSource {
    override suspend fun getTrendingMovies(): List<Movie> {
        val firstPage = apiService.getTrendingMovies(page = 1)
        val pageSize = firstPage.results.size.takeIf { it > 0 } ?: return emptyList()
        val pagesNeeded = ceil(100.0 / pageSize).toInt()
        val remaining = if (pagesNeeded > 1) coroutineScope {
            (2..pagesNeeded).map { page -> async { apiService.getTrendingMovies(page) } }.awaitAll()
        } else emptyList()
        return (firstPage.results + remaining.flatMap { it.results })
            .take(100)
            .map { it.toDomain() }
    }
}
```

---

## Room Entities (private to `room/entity/`)

```kotlin
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val genreIds: String,  // serialized as comma-separated "28,12,878"
    val popularity: Double,
    val releaseDate: String,
    val voteAverage: Double,
    val overview: String,
)

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey val id: Int,
    val name: String,
)
```

`RoomMovieDataSource` maps `Movie ↔ MovieEntity` and `Genre ↔ GenreEntity` internally.

---

## MovieRepositoryImpl — Orchestration Only

Repository contains **no mapping logic, no DTOs, no entities**. It orchestrates data sources only.

```kotlin
class MovieRepositoryImpl @Inject constructor(
    private val remote: RemoteMovieDataSource,
    private val local: LocalMovieDataSource,
    private val logger: Logger,
) : MovieRepository {
    override fun getTrendingMovies(): Flow<List<Movie>> = flow {
        try {
            val movies = remote.getTrendingMovies()
            local.saveMovies(movies)
            emit(movies)
        } catch (e: Exception) {
            logger.e(TAG, "Remote failed, trying cache", e)
            val cached = local.getCachedMovies()
            if (cached.isNotEmpty()) emit(cached) else throw e
        }
    }
    // getMovieDetail + getGenres: same pattern
}
```

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

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds abstract fun bindRemoteDataSource(impl: TmdbMovieDataSource): RemoteMovieDataSource
    @Binds abstract fun bindLocalDataSource(impl: RoomMovieDataSource): LocalMovieDataSource
    @Binds abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository
}
```
