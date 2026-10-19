import java.util.List;

/** A TV entry; each tracked season is a separate entry identified in its title. */
public class TvShow extends Media {
    public TvShow(int id, String title, int releaseYear, List<String> genres, WatchStatus watchStatus, Double rating, String notes) {
        super(id, title, releaseYear, genres, watchStatus, rating, notes);
    }

    @Override
    public String getType() {
        return "TV_SHOW";
    }
}
