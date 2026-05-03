package nl.abnamro.amrotv.feature.movies.ui.preview

import kotlinx.collections.immutable.persistentListOf
import nl.abnamro.amrotv.feature.movies.presentation.api.model.GenrePresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MovieDetailPresentationModel
import nl.abnamro.amrotv.feature.movies.presentation.api.model.MoviePresentationModel

internal object UIPreviewData {

    object Genres {
        val action = GenrePresentationModel(id = 28, name = "Action")
        val sciFi = GenrePresentationModel(id = 878, name = "Sci-Fi")
        val drama = GenrePresentationModel(id = 18, name = "Drama")
        val crime = GenrePresentationModel(id = 80, name = "Crime")
        val thriller = GenrePresentationModel(id = 53, name = "Thriller")
        val adventure = GenrePresentationModel(id = 12, name = "Adventure")
        val history = GenrePresentationModel(id = 36, name = "History")
        val all = persistentListOf(action, sciFi, drama, crime, thriller, adventure, history)
    }

    object Movies {
        val darkKnight =
            MoviePresentationModel(
                id = 1,
                title = "The Dark Knight",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.action.id, Genres.crime.id),
                popularity = 100.0,
                releaseYear = "2008",
                formattedRating = "9.0",
            )
        val inception =
            MoviePresentationModel(
                id = 2,
                title = "Inception",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.action.id, Genres.sciFi.id),
                popularity = 90.0,
                releaseYear = "2010",
                formattedRating = "8.8",
            )
        val interstellar =
            MoviePresentationModel(
                id = 3,
                title = "Interstellar",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.sciFi.id, Genres.drama.id),
                popularity = 88.0,
                releaseYear = "2014",
                formattedRating = "8.6",
            )
        val shawshankRedemption =
            MoviePresentationModel(
                id = 4,
                title = "The Shawshank Redemption",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.drama.id),
                popularity = 85.0,
                releaseYear = "1994",
                formattedRating = "9.3",
            )
        val pulpFiction =
            MoviePresentationModel(
                id = 5,
                title = "Pulp Fiction",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.crime.id, Genres.thriller.id),
                popularity = 82.0,
                releaseYear = "1994",
                formattedRating = "8.9",
            )
        val theMatrix =
            MoviePresentationModel(
                id = 6,
                title = "The Matrix",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.action.id, Genres.sciFi.id),
                popularity = 80.0,
                releaseYear = "1999",
                formattedRating = "8.7",
            )
        val goodfellas =
            MoviePresentationModel(
                id = 7,
                title = "Goodfellas",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.crime.id, Genres.drama.id),
                popularity = 78.0,
                releaseYear = "1990",
                formattedRating = "8.7",
            )
        val oppenheimer =
            MoviePresentationModel(
                id = 8,
                title = "Oppenheimer",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.drama.id, Genres.history.id),
                popularity = 76.0,
                releaseYear = "2023",
                formattedRating = "8.3",
            )
        val dunePartTwo =
            MoviePresentationModel(
                id = 9,
                title = "Dune: Part Two",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.sciFi.id, Genres.adventure.id),
                popularity = 74.0,
                releaseYear = "2024",
                formattedRating = "8.5",
            )
        val everythingEverywhereAllAtOnce =
            MoviePresentationModel(
                id = 10,
                title = "Everything Everywhere All at Once",
                posterUrl = null,
                backdropUrl = null,
                genreIds = persistentListOf(Genres.action.id, Genres.adventure.id, Genres.sciFi.id),
                popularity = 72.0,
                releaseYear = "2022",
                formattedRating = "7.8",
            )
        val all =
            persistentListOf(
                darkKnight,
                inception,
                interstellar,
                shawshankRedemption,
                pulpFiction,
                theMatrix,
                goodfellas,
                oppenheimer,
                dunePartTwo,
                everythingEverywhereAllAtOnce,
            )
    }

    object MovieDetails {
        val darkKnight =
            MovieDetailPresentationModel(
                id = 155,
                title = "The Dark Knight",
                tagline = "Why so serious?",
                posterUrl = null,
                backdropUrl = null,
                genres =
                    persistentListOf(
                        GenrePresentationModel(id = 28, name = "Action"),
                        GenrePresentationModel(id = 80, name = "Crime"),
                        GenrePresentationModel(id = 53, name = "Thriller"),
                    ),
                overview =
                    """
                        Set in the sprawling, decaying metropolis of Gotham City, the story follows Bruce Wayne as he \
                        confronts the most dangerous adversary he has ever faced. When the menace known as the Joker \
                        wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest \
                        psychological and physical tests of his ability to fight injustice.

                        The Joker — a mysterious criminal mastermind with no discernible motive beyond the desire to \
                        watch the world burn — systematically dismantles every institution Batman and Commissioner Gordon \
                        have built. He manipulates Gotham's underworld, corrupts its legal system, and turns ordinary \
                        citizens against one another, proving that anarchy is only one bad day away.

                        Harvey Dent, the city's charismatic new district attorney and Batman's greatest ally in the war \
                        on crime, becomes the focal point of the Joker's most twisted scheme. As Dent's life unravels \
                        through tragedy, he transforms into Two-Face: a villain who pursues a merciless, coin-flip \
                        brand of justice that mirrors the randomness the Joker embodies.

                        The film asks a profound question — what does it take to remain a hero when the world gives you \
                        every reason to abandon hope? Batman is forced to choose between saving lives and protecting \
                        his identity, between upholding his one rule and stopping a monster who will keep killing until \
                        that rule is broken. Every decision carries consequences that echo through Gotham for years.

                        Ultimately, the story is a meditation on the nature of heroism, the corrupting influence of \
                        power, and the impossibility of remaining purely good in an impure world. The Joker's greatest \
                        victory is not physical — it is philosophical. And Batman's greatest act is choosing, in the \
                        face of that, to remain the hero the city needs rather than the one it deserves.
                    """
                        .trimIndent()
                        .replace("\\\n                ", " "),
                formattedRating = "9.0",
                voteCount = 30_455,
                formattedBudget = "$185,000,000.00",
                formattedRevenue = "$1,004,934,033.00",
                imdbId = "tt0468569",
                status = "Released",
                runtimeInMinutes = 152,
                releaseYear = "2008",
            )
        val interstellar =
            MovieDetailPresentationModel(
                id = 157336,
                title = "Interstellar",
                tagline = "Mankind was born on Earth. It was never meant to die here.",
                posterUrl = null,
                backdropUrl = null,
                genres =
                    persistentListOf(
                        GenrePresentationModel(id = 878, name = "Science Fiction"),
                        GenrePresentationModel(id = 12, name = "Adventure"),
                        GenrePresentationModel(id = 18, name = "Drama"),
                    ),
                overview =
                    """
                        In the near future, Earth faces a catastrophic blight that threatens all agriculture. \
                        Former NASA pilot Cooper is recruited for a secret mission through a newly discovered \
                        wormhole near Saturn, tasked with finding a new home for humanity among the stars.

                        The journey takes Cooper and his crew — Brand, Romilly, and Doyle — through the wormhole \
                        and into another galaxy entirely, where three potentially habitable worlds orbit a \
                        supermassive black hole named Gargantua. Time dilation near the event horizon means that \
                        hours spent on one planet can equal decades back on Earth, and every decision carries an \
                        immense human cost.

                        Back home, Cooper's daughter Murph — now a grown physicist working with Professor Brand — \
                        searches for an equation that could save humanity, driven by a mysterious gravitational \
                        anomaly in her childhood bedroom. The two storylines converge across decades of subjective \
                        time, separated by unfathomable distance, bound only by love and data encoded in the \
                        ticking of a wristwatch.

                        The film is a rare collision of hard science, emotional depth, and visual spectacle. \
                        It raises questions about survival, sacrifice, and the bonds that persist even when time \
                        itself becomes the enemy. The climax ventures into the heart of a black hole, visualising \
                        theoretical physics in ways no film had attempted before.
                    """
                        .trimIndent()
                        .replace("\\\n                ", " "),
                formattedRating = "8.6",
                voteCount = 34_721,
                formattedBudget = "$165,000,000.00",
                formattedRevenue = "$701,729,206.00",
                imdbId = "tt0816692",
                status = "Released",
                runtimeInMinutes = 169,
                releaseYear = "2014",
            )
    }
}
