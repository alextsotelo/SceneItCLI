import java.util.List;
import java.util.ArrayList;

/** Manages the media collection and provides lookup and filtered lists for UI class. */
public class Manager {
    private final List<Media> mediaList;

    /** Starts an empty ArrayList to store media objects */
    public Manager() {
        mediaList = new ArrayList<>();
    }

    /** Finds an entry by its persistent ID, or returns null when absent. */
    public Media findById(int id) {
        for (Media item : mediaList) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    /** Chooses an ID above the current maximum to ensure IDs are unique */
    public int getNextId() {
        int highestId = 0;
        for (Media media : mediaList) {
            if (media.getId() > highestId) {
                highestId = media.getId();
            }
        }

        return highestId + 1;
    }

    /** Adds an entry only if it is nonnull and its ID is not already in use. */
    public void addMedia(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("Media cannot be null.");
        }
        if (findById(media.getId()) != null) {
            throw new IllegalArgumentException("Media ID already exists.");
        }

        mediaList.add(media);
    }

    /** Removes the stored object */
    public boolean removeMedia(Media media) {
        return mediaList.remove(media);
    }

    /** Returns a separate list containing references to the same media objects. */
    public List<Media> getMediaList() {
        return new ArrayList<>(mediaList);
    }

    /** Selects a watch list (either Want to Watch or Watched) while retaining references to the original media objects. */
    public List<Media> getByStatus(WatchStatus status) {
        List<Media> result = new ArrayList<>();
        for (Media item : mediaList) {
            if (item.getWatchStatus() == status) {
                result.add(item);
            }
        }
        return result;
    }

    /** SceneIt is optional (max of one per entry) */
    public List<Media> getSceneIts() {
        List<Media> result = new ArrayList<>();

        for (Media item : mediaList) {
            if (item.getFavoriteScene() != null) {
                result.add(item);
            }
        }

        return result;
    }
}
