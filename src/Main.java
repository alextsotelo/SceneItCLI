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
