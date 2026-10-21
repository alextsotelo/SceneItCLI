/** One optional favorite-scene (Sceneit) is allowed per entry */
public class FavoriteScene {
    private String description;


    public FavoriteScene(String description) {
        setDescription(description);
    }


    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be blank.");
        }

        this.description = description.trim();
    }

    public String getDescription() {
        return description;
    }
}
