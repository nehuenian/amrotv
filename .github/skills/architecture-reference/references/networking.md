# Networking — TMDB API Reference

## Base URL

`https://api.themoviedb.org/3/`

---

## TMDB Endpoints

| Endpoint | Method | Notes |
|----------|--------|-------|
| `/trending/movie/{time_window}?language=en-US` | GET | `time_window = "week"`. Page size not guaranteed — derive `pagesNeeded = ceil(100.0 / pageSize).toInt()` from page 1 response. |
| `/movie/{id}?language=en-US` | GET | Returns full `MovieDetailDto` with `genres` as objects (not IDs). |
| `/genre/movie/list?language=en-US` | GET | Returns `{ genres: [{id, name}] }`. |

---

## Local Cache Strategy (network-first, cache-on-error)

1. Try network first
2. Success → overwrite cache (`upsertAll`) → emit result
3. Network error + cache non-empty → emit cached data (stale, but useful)
4. Network error + empty cache → propagate error (let the ViewModel show the error state)

---

## Image Base URL

`https://image.tmdb.org/t/p/w500{poster_path}`

`poster_path` may be `null` — always guard before loading with Coil.

---

## IMDB Link

Construct from `imdbId` field in `MovieDetail`: `"https://www.imdb.com/title/${imdbId}/"`

---

## API Token Setup

Use the **API Read Access Token** (v4, JWT-style `eyJhbGci...`) from TMDB settings — **not** the shorter API Key.
Sent as a Bearer header (more secure, never exposed in URLs).

Stored in **`amrotv.properties`** (project root, **never committed** — add to `.gitignore`):
```
TMDB_READ_ACCESS_TOKEN=eyJhbGci...
```

Read in `:app/build.gradle.kts`:
```kotlin
val amrotvProps = java.util.Properties().apply {
    val file = rootProject.file("amrotv.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    buildFeatures { buildConfig = true }
    defaultConfig {
        buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", "\"${amrotvProps["TMDB_READ_ACCESS_TOKEN"] ?: ""}\"")
    }
}
```

In CI (GitHub Actions), create the file from a secret before building:
```yaml
- name: Create amrotv.properties
  run: echo "TMDB_READ_ACCESS_TOKEN=${{ secrets.TMDB_READ_ACCESS_TOKEN }}" > amrotv.properties
```

Passed into `NetworkModule` via a dedicated Hilt qualifier (`@TmdbApiKey`).

---

## AuthInterceptor

```kotlin
class AuthInterceptor @Inject constructor(
    @TmdbApiKey private val apiKey: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        return chain.proceed(request)
    }
}
```

---

## NetworkResult

```kotlin
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int?, val message: String?) : NetworkResult<Nothing>
    data object Loading : NetworkResult<Nothing>
}
```
