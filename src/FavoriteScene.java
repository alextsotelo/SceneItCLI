/** One optional favorite-scene description; its owner supplies rating and metadata. */
public class FavoriteScene {
    private String description;

    public FavoriteScene(String description) {
        setDescription(description);
    }

    /** A scene may be absent, but an existing scene must have nonblank text. */
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
