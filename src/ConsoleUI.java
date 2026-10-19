import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;

/** Handles console menus and input while models and storage remain reusable in JavaFX. */
public class ConsoleUI {
    private final Manager manager;
    private final CsvStorage storage;
    private final Scanner input;

    public ConsoleUI(Manager manager, CsvStorage storage) {
        this.manager = manager;
        this.storage = storage;
        this.input = new Scanner(System.in);
    }

    private void displayMenu() {
        System.out.print("""
                ========== SceneIt ==========
                View:
                    1. Want to Watch
                    2. Watched
                    3. SceneIts
                    4. Entry Details
                Manage:
                    5. Add new Want to Watch
                    6. Add new Watched
                    7. Edit entry
                    8. Mark as Watched
                    9. Add SceneIt
                    10. Edit SceneIt
                    11. Remove SceneIt
                    12. Delete entry
                Exit/Save:
                    13. Save Changes
                    14. Save & Exit\n\n""");
    }

    /** Displays titles without exposing the IDs used for storage relationships. */
    private void displayMediaList(List<Media> entries) {
        if (entries.isEmpty()) {
            System.out.println("No media found!");
        }

        for (Media media : entries) {
            System.out.println(
                    media.getTitle() + " | "
                    + media.getReleaseYear()
            );
        }
    }

    /** Returns an existing entry chosen by its displayed list position, or null if empty. */
    private Media selectMedia(List<Media> entries) {

        if (entries.isEmpty()) {
            System.out.println("No entries found!");
            return null;
        }

        for (int i = 0; i < entries.size(); i++) {
            Media media = entries.get(i);
            System.out.println(
                    (i + 1) + ". " + media.getTitle()
                    + " (" + media.getReleaseYear() + ")");
        }

        while (true) {

            int choice = readInt("Select an entry: ");
            if (choice  < 1 || choice > entries.size()) {
                System.out.println("Invalid entry!");
            } else {
                return entries.get(choice - 1);
            }

        }
    }

    /** Shows general media information; favorite-scene text belongs in the SceneIts view. */
    private void viewDetails() {
        List<Media> entries = manager.getMediaList();
        Media media = selectMedia(entries);

        if (media == null) {
            return;
        }

        System.out.println("Title: " + media.getTitle());
        System.out.println("Type: " + media.getType());
        System.out.println("Year: " + media.getReleaseYear());
        System.out.println("Genres: " + String.join(", ", media.getGenres()));
        System.out.println("Status: " + media.getWatchStatus());

        if (media.getRating() != null) {
            System.out.println("Rating: " + media.getRating());
        }

        if (!media.getNotes().isEmpty()) {
            System.out.println("Notes: " + media.getNotes());
        }
    }
    
    /** Shows a favorite scene alongside metadata from its owning media entry. */
    private void viewSceneIt() {
        List<Media> sceneIts = manager.getSceneIts();
        Media media = selectMedia(sceneIts);

        if (media == null) {
            return;
        }

        System.out.println(media.getTitle() + " (" + media.getReleaseYear() + ")");
        System.out.println("Genres: " + String.join(", ", media.getGenres()));
        System.out.println("Rating: " + media.getRating() + " / 5");
        System.out.println("Favorite scene:");
        System.out.println(media.getFavoriteScene().getDescription());
    }

    // Both add options share their input flow; only Watched requires a rating.
    private void addMedia(WatchStatus status) {
        int type = readInt("1. Movie\n2. TV show/season\nChoose type: ");
        if (type != 1 && type != 2) {
            System.out.println("Invalid type. Entry was not added.");
            return;
        }

        System.out.print("Title (include 'Season X' for TV Shows): ");
        String title = input.nextLine().trim();

        int year = readInt("Release Year: ");

        System.out.println("Genres (Separate with | like: Drama|Comedy): ");
        String genresText = input.nextLine().trim();
        List<String> genres = Arrays.asList(genresText.split("\\|", -1));

        String ratingText = "";
        if (status == WatchStatus.WATCHED) {
            System.out.print("Rating (Decimal from 1 to 5): ");
            ratingText = input.nextLine().trim();
        }

        System.out.println("Notes (optional; Enter to skip): ");
        String notesText = input.nextLine().trim();

        try {
            Double rating = null;
            if (status == WatchStatus.WATCHED) {
                rating = Double.parseDouble(ratingText);
            }
            int id = manager.getNextId();
            Media media;

            if (type == 1) { // MOVIE
                media = new Movie(
                        id, title, year, genres, status, rating, notesText);
            } else {
                media = new TvShow(
                        id, title, year, genres, status, rating, notesText);
            }

            manager.addMedia(media);
            System.out.println("Successfully added!");
        } catch (IllegalArgumentException e) {
            System.out.println("Entry was not added: " + e.getMessage());
        }
    }

    /** Applies each nonblank field immediately; Enter preserves that field's value. */
    private void editEntry() {
        int listChoice = readInt("1. Want to Watch\n2. Watched\nChoose a list: ");
        if (listChoice != 1 && listChoice != 2) {
            System.out.println("Invalid list selected.");
            return;
        }

        WatchStatus status;
        if (listChoice == 1) {
            status = WatchStatus.WANT_TO_WATCH;
        } else {
            status = WatchStatus.WATCHED;
        }

        List<Media> chosenList = manager.getByStatus(status);
        Media media = selectMedia(chosenList);
        if (media == null) {
            return;
        }

        try {
            System.out.print("Title [" + media.getTitle() + "]: ");
            String title = input.nextLine().trim();

            if (!title.isEmpty()) {
                media.setTitle(title);
            }

            System.out.print("Release year [" + media.getReleaseYear() + "]: ");
            String yearText = input.nextLine().trim();

            if (!yearText.isEmpty()) {
                media.setReleaseYear(Integer.parseInt(yearText));
            }

            System.out.print(
                    "Genres [" + String.join("|", media.getGenres()) + "]: ");
            String genresText = input.nextLine().trim();

            if (!genresText.isEmpty()) {
                media.setGenres(
                        Arrays.asList(genresText.split("\\|", -1)));
            }

            System.out.print("Notes [" + media.getNotes()
                    + "] (Enter to keep, - to clear): ");
            String notes = input.nextLine().trim();

            if (notes.equals("-")) {
                media.setNotes("");
            } else if (!notes.isEmpty()) {
                media.setNotes(notes);
            }

            if (media.getWatchStatus() == WatchStatus.WATCHED) {
                System.out.print("Rating [" + media.getRating() + "]: ");
                String ratingText = input.nextLine().trim();

                if (!ratingText.isEmpty()) {
                    media.markWatched(Double.parseDouble(ratingText));
                }
            }

            System.out.println("Editing complete. Use Save to keep your changes.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid value: " + ex.getMessage());
            System.out.println("Any earlier valid edits have been kept.");
        }
    }

    /** Requires a valid rating before moving a queued entry to Watched. */
    private void markAsWatched() {
        List<Media> wantToWatch = manager.getByStatus(WatchStatus.WANT_TO_WATCH);
        Media media = selectMedia(wantToWatch);

        if (media == null) {
            return;
        }

        while (true) {
            System.out.print("Rating from 1 to 5 (decimals allowed): ");
            String ratingText = input.nextLine().trim();

            try {
                double rating = Double.parseDouble(ratingText);
                media.markWatched(rating);
                System.out.println(media.getTitle()
                        + " marked as Watched. Use Save to keep your changes.");
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("Please enter a rating from 1 to 5.");
            }
        }
    }


    /** Adds a scene only to Watched entries that do not already have one. */
    private void addSceneIt() {
        List<Media> watched = manager.getByStatus(WatchStatus.WATCHED);
        List<Media> eligible = new ArrayList<>();

        for (Media media : watched) {
            if (media.getFavoriteScene() == null) {
                eligible.add(media);
            }
        }

        Media media = selectMedia(eligible);

        if (media == null) {
            return;
        }

        while (true) {
            System.out.print("Favorite scene description: ");
            String description = input.nextLine().trim();

            try {
                media.setFavoriteScene(new FavoriteScene(description));
                System.out.println("SceneIt added. Use Save to keep your changes.");
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /** Edits the existing scene; Enter preserves its current description. */
    private void editSceneIt() {
        Media media = selectMedia(manager.getSceneIts());

        if (media == null) {
            return;
        }

        FavoriteScene scene = media.getFavoriteScene();
        System.out.println("Current description: " + scene.getDescription());
        System.out.println("New scene description (Press ENTER to keep current): ");
        String description = input.nextLine().trim();

        if (description.isEmpty()) {
            System.out.println("Favorite scene unchanged.");
            return;
        }

        scene.setDescription(description);
        System.out.println("SceneIt updated. Use Save to keep your changes.");
    }

    /** Removes only the scene, preserving its owner, rating, and general notes. */
    private void removeSceneIt() {
        Media media = selectMedia(manager.getSceneIts());
        if (media == null) {
            return;
        }

        media.removeFavoriteScene();
        System.out.println("SceneIt removed. Use Save to keep your changes.");
    }

    /** Removes the owner; saving also omits its scene from scenes.csv. */
    private void deleteEntry() {
        List<Media> mediaList = manager.getMediaList();
        Media media = selectMedia(mediaList);
        if (media == null) {
            return;
        }

        manager.removeMedia(media);
        System.out.println(media.getTitle()
                + " deleted. Use Save to keep your changes.");
    }

    /** Returns success so Save & Exit keeps running when saving fails. */
    private boolean saveChanges() {
        try {
            storage.save(manager);
            System.out.println("Changes saved successfully.");
            return true;
        } catch (java.io.IOException ex) {
            System.out.println("Could not save changes: " + ex.getMessage());
            return false;
        }
    }

    /** Reads a whole number and retries input that cannot be converted to an int. */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = input.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /** Dispatches menu choices until Save & Exit succeeds. */
    public void run() {
        while (true) {
            displayMenu();
            int choice = readInt("Choose an option: ");

            switch(choice) {
                case 1:
                    List<Media> wantToWatchList = manager.getByStatus(WatchStatus.WANT_TO_WATCH);
                    displayMediaList(wantToWatchList);
                    break;
                case 2:
                    List<Media> watchedList = manager.getByStatus(WatchStatus.WATCHED);
                    displayMediaList(watchedList);
                    break;
                case 3:
                    viewSceneIt();
                    break;
                case 4:
                    viewDetails();
                    break;
                case 5:
                    addMedia(WatchStatus.WANT_TO_WATCH);
                    break;
                case 6:
                    addMedia(WatchStatus.WATCHED);
                    break;
                case 7:
                    editEntry();
                    break;
                case 8:
                    markAsWatched();
                    break;
                case 9:
                    addSceneIt();
                    break;
                case 10:
                    editSceneIt();
                    break;
                case 11:
                    removeSceneIt();
                    break;
                case 12:
                    deleteEntry();
                    break;
                case 13:
                    saveChanges();
                    break;
                case 14:
                    if (saveChanges()) {
                        return;
                    }
                    break;
                default:
                    System.out.println("Please enter a valid option.");
            }
        }
    }

}
