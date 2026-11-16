public class Admin {
    private final Gallery gallery;

    public Admin(Gallery gallery) {
        if (gallery == null) {
            throw new IllegalArgumentException("Gallery cannot be null.");
        }
        this.gallery = gallery;
    }

    public void addArtwork(Artwork artwork) {
        gallery.addArtwork(artwork);
    }

    public boolean removeArtwork(Artwork artwork) {
        return gallery.removeArtwork(artwork);
    }
}
