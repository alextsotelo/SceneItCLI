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

public class CsvStorage {
    private final Path mediaFile;
    private final Path scenesFile;

    public CsvStorage(Path dataDirectory) {
        mediaFile = dataDirectory.resolve("media.csv");
        scenesFile = dataDirectory.resolve("scenes.csv");
    }


    // Parses the media & scenes CSV files and creates the corresponding media object to add to manager
    // Returns the manager

    public Manager load()  throws IOException {
        Manager manager = new Manager();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

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


                // Construct the object
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

                manager.addMedia(media);
            }

        }

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

                FavoriteScene scene = new FavoriteScene(description);
                owner.setFavoriteScene(scene);
            }
        }

        return manager;
    }

    // Method to save data to media & scenes CSV files

    public void save(Manager manager) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("id", "type", "title", "releaseYear",
                        "genres", "watchStatus", "rating", "notes")
                .get();

        try (
                Writer writer = Files.newBufferedWriter(mediaFile);
                CSVPrinter printer = new CSVPrinter(writer, format);
        ) {
            for (Media media : manager.getMediaList()) {
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

        CSVFormat sceneFormat = CSVFormat.DEFAULT.builder()
                .setHeader("mediaId", "description")
                .get();

        try (
                Writer writer = Files.newBufferedWriter(scenesFile);
                CSVPrinter printer = new CSVPrinter(writer, sceneFormat);
        ) {
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
