import java.util.List;

/** A movie entry that inherits from Media class */
public class Movie extends Media {

    public Movie(int id, String title, int releaseYear, List<String> genres, WatchStatus watchStatus, Double rating, String notes) {
        super(id, title, releaseYear, genres, watchStatus, rating, notes);
    }


    @Override
    public String getType() {
        return "MOVIE";
    }
}
