import java.nio.file.Path;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.io.Writer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Loads and saves the local CSV files without handling console input or output.
 * Media records are stored separately from their optional favorite scenes.
 */
public class CsvStorage {
    private final Path mediaFile;
    private final Path scenesFile;

    // Creates the path to both data files necessary
    public CsvStorage(Path dataDirectory) {
        mediaFile = dataDirectory.resolve("media.csv");
        scenesFile = dataDirectory.resolve("scenes.csv");
    }


    /**
     * Creates a Manager from media.csv, then links scenes.csv records to their owners.
     * File errors are passed to the caller
     */

    public Manager load()  throws IOException {
        Manager manager = new Manager();
        // Each new parser uses the first record of its own file as the header
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        // Load media first so scene owners can be found by ID in the next block
        try (
                Reader reader = Files.newBufferedReader(mediaFile);
                CSVParser parser = format.parse(reader);
        ) {
            for (CSVRecord record : parser) {
                int id = Integer.parseInt(record.get("id"));
                String type = record.get("type");
                String title = record.get("title");
                int releaseYear = Integer.parseInt(record.get("releaseYear"));

                String genresText = record.get("genres");
                String watchStatusText = record.get("watchStatus");
                String ratingText = record.get("rating").trim();
                String notes =  record.get("notes");

                List<String> genres = Arrays.asList(genresText.split("\\|"));
                WatchStatus watchStatus = WatchStatus.valueOf(watchStatusText);
                Double rating = null;
                if (!ratingText.isEmpty()) {
                    rating = Double.parseDouble(ratingText);
                }


                // Assign the type of record and create the object
                Media media;
                if (type.equals("MOVIE")) {
                    media = new Movie(
                            id, title, releaseYear, genres, watchStatus, rating, notes
                    );
                } else if (type.equals("TV_SHOW")) {
                    media = new TvShow(
                            id, title, releaseYear, genres, watchStatus, rating, notes
                    );
                } else {
                    throw new IllegalArgumentException("Unknown record type: " + type);
                }

                // Add the entry to the manager list
                manager.addMedia(media);
            }

        }

        // Attach scenes to existing objects instead of creating duplicate media.
        try (
                Reader reader = Files.newBufferedReader(scenesFile);
                CSVParser parser = format.parse(reader);
        ) {
            for (CSVRecord record : parser) {
                int mediaId = Integer.parseInt(record.get("mediaId"));
                String description = record.get("description");

                Media owner = manager.findById(mediaId);

                if (owner == null) {
                    throw new IllegalArgumentException("Scene references unknown media id: " + mediaId);
                }

                if (owner.getFavoriteScene() != null) {
                    throw new IllegalArgumentException("Duplicate SceneIt for media ID: " + mediaId);
                }

                // Create the Sceneit object and add to our manager list
                FavoriteScene scene = new FavoriteScene(description);
                owner.setFavoriteScene(scene);
            }
        }

        return manager;
    }

    /**
     * Replaces both CSV files with the Manager's current entries and scenes.
     * CSVPrinter handles quoting automatically.
     */

    public void save(Manager manager) throws IOException {
        // Set the format for the media.csv file
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("id", "type", "title", "releaseYear",
                        "genres", "watchStatus", "rating", "notes")
                .get();

        try (
                Writer writer = Files.newBufferedWriter(mediaFile);
                CSVPrinter printer = new CSVPrinter(writer, format);
        ) {
            for (Media media : manager.getMediaList()) {
                // Field order matches the header
                printer.printRecord(
                        media.getId(),
                        media.getType(),
                        media.getTitle(),
                        media.getReleaseYear(),
                        String.join("|", media.getGenres()),
                        media.getWatchStatus().name(),
                        media.getRating(),
                        media.getNotes()
                );
            }
        }

        // Set format for the scenes.csv file
        CSVFormat sceneFormat = CSVFormat.DEFAULT.builder()
                .setHeader("mediaId", "description")
                .get();

        try (
                Writer writer = Files.newBufferedWriter(scenesFile);
                CSVPrinter printer = new CSVPrinter(writer, sceneFormat);
        ) {
            // For every entry that has a Sceneit, get their ID and description and write it to scenes.csv
            for (Media media : manager.getMediaList()) {
                FavoriteScene scene = media.getFavoriteScene();
                if (scene != null) {
                    printer.printRecord(
                            media.getId(),
                            scene.getDescription()
                    );
                }
            }
        }

    }

}
