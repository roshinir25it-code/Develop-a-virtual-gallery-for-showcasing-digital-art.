public enum ArtCategory {
    ILLUSTRATION("Illustration"),
    PHOTOGRAPHY("Photography"),
    THREE_D_ART("3D Art");

    private final String displayName;

    ArtCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ArtCategory fromDisplayName(String displayName) {
        for (ArtCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown artwork category: " + displayName);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
