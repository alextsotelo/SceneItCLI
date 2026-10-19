import java.nio.file.Path;

/** Loads the collection and connects it to the console controller. */
public class Main {
    public static void main(String[] args) {
        CsvStorage storage = new CsvStorage(Path.of("data"));

        try {
            Manager manager = storage.load();

            ConsoleUI app = new ConsoleUI(manager, storage);
            app.run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

/* Things to fix:
 - How do we go about displaying Notes for viewing "Want to Watch" list? Our current displaying of Want to Watch doesn't show anything
 - Same goes for viewing "Watched" List
 - We can view these in "View Entry" but should it work this way or should we change it?
 */

