import java.util.ArrayList;
import java.util.List;

// Parent abstract class used as a blueprint for Movie & TvShow classes
public abstract class Media {
    // The ID stays unchanged so a saved scene continues to reference its owner
    private final int id;
    private String title;
    private int releaseYear;
    private List<String> genres;
    private WatchStatus watchStatus;
    private Double rating; // null means the entry has not been rated (Want to Watch)
    private String notes;
    private FavoriteScene favoriteScene; // null means no SceneIt; otherwise one Sceneit per entry

    /** Creates a valid entry: Watched requires a rating; Want to Watch requires null. */
    protected Media(int id, String title, int releaseYear, List<String> genres,
                    WatchStatus watchStatus, Double rating, String notes) {

        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive.");
        }
        if (watchStatus == null) {
            throw new IllegalArgumentException("Watch status is required.");
        }

        this.id = id;
        setTitle(title);
        setReleaseYear(releaseYear);
        setGenres(genres);
        setNotes(notes);

        if (watchStatus == WatchStatus.WATCHED) {
            if (rating == null) {
                throw new IllegalArgumentException(
                        "Watched media must have a rating.");
            }
            markWatched(rating);
        } else {
            if (rating != null) {
                throw new IllegalArgumentException(
                        "Want to Watch media cannot have a rating.");
            }
            this.watchStatus = WatchStatus.WANT_TO_WATCH;
        }
    }

    // Subclasses supply the type label used when displaying and saving entries
    public abstract String getType();

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public List<String> getGenres() {
        return new ArrayList<>(genres);
    }

    public WatchStatus getWatchStatus() {
        return watchStatus;
    }

    // Returns null for Want to Watch, or the required user rating for Watched
    public Double getRating() {
        return rating;
    }

    public String getNotes() {
        return notes;
    }

    // Returns the optional scene object, or null when the entry has none
    public FavoriteScene getFavoriteScene() {
        return favoriteScene;
    }

    // Rejects missing/blank titles and removes surrounding whitespace
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be blank.");
        }
        this.title = title.trim();
    }

    public void setReleaseYear(int releaseYear) {
        if (releaseYear <= 0) {
            throw new IllegalArgumentException("Year must be positive.");
        }
        this.releaseYear = releaseYear;
    }

    public void setGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one genre is required.");
        }

        List<String> cleanedGenres = new ArrayList<>();

        for (String genre : genres) {
            if (genre == null || genre.trim().isEmpty()) {
                throw new IllegalArgumentException("Genre cannot be blank.");
            }
            if (genre.contains("|")) {
                throw new IllegalArgumentException(
                        "Genre cannot contain the | separator.");
            }
            cleanedGenres.add(genre.trim());
        }

        this.genres = cleanedGenres;
    }

    public void setNotes(String notes) {
        if (notes == null) {
            this.notes = "";
        } else {
            this.notes = notes.trim();
        }
    }

    // Updates status and rating together, preventing an unrated Watched entry
    public void markWatched(double rating) {
        if (!Double.isFinite(rating) || rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }

        this.rating = rating;
        this.watchStatus = WatchStatus.WATCHED;
    }

    // One field means each media item can hold at most one favorite scene
    public void setFavoriteScene(FavoriteScene favoriteScene) {
        if (watchStatus != WatchStatus.WATCHED) {
            throw new IllegalStateException(
                    "Only Watched media can have a SceneIt.");
        }
        if (favoriteScene == null) {
            throw new IllegalArgumentException("Favorite scene is required.");
        }
        this.favoriteScene = favoriteScene;
    }

    // Clears only the scene, preserving watch status, rating, and general notes
    public void removeFavoriteScene() {
        this.favoriteScene = null;
    }
}
