import java.nio.file.Path;

public class Main {

    // Loads data & creates manager before running the program
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

