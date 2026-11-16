import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class VirtualArtGallery extends JFrame {
    private static final Color BACKGROUND = new Color(246, 246, 249);
    private static final Color SIDEBAR = new Color(27, 29, 39);
    private static final Color INK = new Color(35, 37, 47);
    private static final Color MUTED = new Color(125, 128, 140);
    private static final Color PURPLE = new Color(126, 91, 220);

    private static final String HOME = "home";
    private static final String GALLERY_PAGE = "gallery";
    private static final String FAVORITES_PAGE = "favorites";
    private static final String CATEGORIES_PAGE = "categories";
    private static final String ADMIN_PAGE = "admin";
    private static final String DETAILS_PAGE = "details";
    private static final Path DATA_FILE = Paths.get("artworks.csv");

    private final Gallery gallery = new Gallery();
    private final Admin admin = new Admin(gallery);
    private final User currentUser = new User("Gallery Guest", gallery);
    private final GalleryDatabase database = new GalleryDatabase();
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);
    private final JPanel galleryCards = createGridPanel();
    private final JPanel favoriteCards = createGridPanel();
    private final JPanel manageList = new JPanel();
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> categoryFilter =
            new JComboBox<String>(new String[] { "All artworks", "Illustration", "Photography", "3D Art" });
    private final JTextField titleInput = new JTextField();
    private final JTextField artistInput = new JTextField();
    private final JComboBox<String> categoryInput =
            new JComboBox<String>(new String[] { "Illustration", "Photography", "3D Art" });
    private final JTextArea descriptionInput = new JTextArea(5, 24);
    private final List<JButton> navigationButtons = new ArrayList<JButton>();
    private Artwork selectedArtwork;
    private JPanel detailsPreviewHolder;

    public VirtualArtGallery() {
        super("Studio | Virtual Art Gallery");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1240, 800);
        setLocationRelativeTo(null);
        loadArtworks();

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(createMainPages(), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                refreshGallery();
            }

            public void removeUpdate(DocumentEvent event) {
                refreshGallery();
            }

            public void changedUpdate(DocumentEvent event) {
                refreshGallery();
            }
        });
        categoryFilter.addActionListener(event -> refreshGallery());
        refreshGallery();
        showPage(HOME);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(218, 0));
        sidebar.setBorder(new EmptyBorder(27, 17, 22, 17));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("  STUDIO");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        sidebar.add(logo);
        JLabel tagline = new JLabel("  DIGITAL ART GALLERY");
        tagline.setFont(new Font("SansSerif", Font.BOLD, 9));
        tagline.setForeground(new Color(164, 159, 190));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(42));

        addNavigationButton(sidebar, "⌂   Home", HOME);
        addNavigationButton(sidebar, "▦   Explore gallery", GALLERY_PAGE);
        addNavigationButton(sidebar, "♡   Favorites", FAVORITES_PAGE);
        addNavigationButton(sidebar, "◈   Categories", CATEGORIES_PAGE);
        addNavigationButton(sidebar, "＋   Manage artworks", ADMIN_PAGE);

        sidebar.add(Box.createVerticalGlue());
        JPanel account = new JPanel();
        account.setOpaque(false);
        account.setLayout(new BoxLayout(account, BoxLayout.Y_AXIS));
        JLabel accountName = new JLabel(currentUser.getName());
        accountName.setFont(new Font("SansSerif", Font.BOLD, 13));
        accountName.setForeground(Color.WHITE);
        JLabel accountType = new JLabel("Art enthusiast");
        accountType.setFont(new Font("SansSerif", Font.PLAIN, 11));
        accountType.setForeground(new Color(164, 159, 190));
        account.setBorder(new EmptyBorder(14, 8, 0, 0));
        account.add(accountName);
        account.add(Box.createVerticalStrut(4));
        account.add(accountType);
        sidebar.add(account);
        return sidebar;
    }

    private void addNavigationButton(JPanel sidebar, String label, String page) {
        JButton button = new JButton(label);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setForeground(new Color(207, 207, 220));
        button.setBackground(SIDEBAR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setBorder(new EmptyBorder(0, 11, 0, 8));
        button.addActionListener(event -> showPage(page));
        navigationButtons.add(button);
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }

    private JPanel createMainPages() {
        pages.setBackground(BACKGROUND);
        pages.add(createHomePage(), HOME);
        pages.add(createGalleryPage(), GALLERY_PAGE);
        pages.add(createFavoritesPage(), FAVORITES_PAGE);
        pages.add(createCategoriesPage(), CATEGORIES_PAGE);
        pages.add(createAdminPage(), ADMIN_PAGE);
        pages.add(createDetailsPage(), DETAILS_PAGE);
        return pages;
    }

    private JPanel createHomePage() {
        JPanel page = createPage();
        page.add(createPageHeading("Good day, art lover", "Discover thoughtful work from digital artists."), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(7, 0, 0, 0));

        JPanel hero = new JPanel(new GridLayout(1, 2, 28, 0));
        hero.setBackground(new Color(236, 230, 250));
        hero.setBorder(new EmptyBorder(30, 32, 30, 32));
        hero.setPreferredSize(new Dimension(900, 315));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 315));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("YOUR PERSONAL DIGITAL EXHIBITION");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        eyebrow.setForeground(PURPLE);
        JLabel title = new JLabel("<html>Art that makes<br>you pause.</html>");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(INK);
        JLabel message = new JLabel("<html>A curated space for bold ideas,<br>beautiful details, and new perspectives.</html>");
        message.setFont(new Font("SansSerif", Font.PLAIN, 14));
        message.setForeground(MUTED);
        JButton explore = primaryButton("Explore the collection");
        explore.addActionListener(event -> showPage(GALLERY_PAGE));
        copy.add(Box.createVerticalGlue());
        copy.add(eyebrow);
        copy.add(Box.createVerticalStrut(14));
        copy.add(title);
        copy.add(Box.createVerticalStrut(12));
        copy.add(message);
        copy.add(Box.createVerticalStrut(21));
        copy.add(explore);
        copy.add(Box.createVerticalGlue());

        Artwork featured = gallery.getArtworks().isEmpty()
                ? Gallery.createSampleArtworks().get(0) : gallery.getArtworks().get(0);
        ArtworkPreview feature = new ArtworkPreview(featured);
        feature.setPreferredSize(new Dimension(360, 250));
        hero.add(copy);
        hero.add(feature);
        content.add(hero);
        content.add(Box.createVerticalStrut(22));

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(createStatCard(String.valueOf(gallery.getArtworks().size()), "Artworks in collection"));
        stats.add(createStatCard(String.valueOf(currentUser.getFavorites().getAll().size()), "Saved favorites"));
        stats.add(createStatCard(String.valueOf(ArtCategory.values().length), "Creative categories"));
        content.add(stats);
        content.add(Box.createVerticalStrut(25));

        JPanel sectionHeading = new JPanel(new BorderLayout());
        sectionHeading.setOpaque(false);
        sectionHeading.add(sectionTitle("A few to get you started"), BorderLayout.WEST);
        JButton seeAll = textButton("See all artworks  →");
        seeAll.addActionListener(event -> showPage(GALLERY_PAGE));
        sectionHeading.add(seeAll, BorderLayout.EAST);
        content.add(sectionHeading);
        content.add(Box.createVerticalStrut(13));

        JPanel featuredCards = createGridPanel();
        List<Artwork> collection = gallery.getArtworks();
        for (int i = 0; i < Math.min(3, collection.size()); i++) {
            featuredCards.add(createArtworkCard(collection.get(i)));
        }
        content.add(featuredCards);
        page.add(new JScrollPane(content), BorderLayout.CENTER);
        return page;
    }

    private JPanel createGalleryPage() {
        JPanel page = createPage();
        page.add(createPageHeading("Explore the gallery", "Search and browse the complete collection."), BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setToolTipText("Search by artwork title, artist, category, or description");
        styleInput(searchField);
        categoryFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryFilter.setPreferredSize(new Dimension(175, 42));
        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(categoryFilter, BorderLayout.EAST);
        content.add(toolbar, BorderLayout.NORTH);

        galleryCards.setBorder(new EmptyBorder(1, 1, 16, 1));
        JScrollPane scroll = new JScrollPane(galleryCards);
        styleScrollPane(scroll);
        content.add(scroll, BorderLayout.CENTER);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createFavoritesPage() {
        JPanel page = createPage();
        page.add(createPageHeading("Your favorites", "A personal shortlist of artwork you love."), BorderLayout.NORTH);
        favoriteCards.setBorder(new EmptyBorder(2, 2, 18, 2));
        JScrollPane scroll = new JScrollPane(favoriteCards);
        styleScrollPane(scroll);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel createCategoriesPage() {
        JPanel page = createPage();
        page.add(createPageHeading("Browse by category", "Find work by the medium or style that inspires you."), BorderLayout.NORTH);
        JPanel categoryCards = new JPanel(new GridLayout(0, 2, 16, 16));
        categoryCards.setOpaque(false);
        categoryCards.setBorder(new EmptyBorder(2, 2, 18, 2));
        Color[] colors = {
            new Color(235, 222, 243), new Color(216, 233, 239), new Color(237, 228, 210)
        };
        int index = 0;
        for (ArtCategory category : ArtCategory.values()) {
            JPanel card = new JPanel(new BorderLayout(0, 14));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(233, 233, 239)),
                    new EmptyBorder(20, 20, 20, 20)));
            JPanel colorBlock = new JPanel();
            colorBlock.setBackground(colors[index++ % colors.length]);
            colorBlock.setPreferredSize(new Dimension(250, 145));
            JLabel artworkSymbol = new JLabel(category == ArtCategory.ILLUSTRATION ? "✳"
                    : category == ArtCategory.PHOTOGRAPHY ? "◉" : "◇");
            artworkSymbol.setFont(new Font("SansSerif", Font.PLAIN, 68));
            artworkSymbol.setForeground(new Color(255, 255, 255, 210));
            colorBlock.add(artworkSymbol);
            JLabel name = new JLabel(category.getDisplayName());
            name.setFont(new Font("SansSerif", Font.BOLD, 19));
            JLabel count = new JLabel(countFor(category) + " artworks");
            count.setFont(new Font("SansSerif", Font.PLAIN, 13));
            count.setForeground(MUTED);
            JButton browse = textButton("Browse category  →");
            browse.addActionListener(event -> {
                categoryFilter.setSelectedItem(category.getDisplayName());
                showPage(GALLERY_PAGE);
            });
            JPanel lower = new JPanel(new BorderLayout());
            lower.setOpaque(false);
            JPanel labels = new JPanel();
            labels.setOpaque(false);
            labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
            labels.add(name);
            labels.add(Box.createVerticalStrut(5));
            labels.add(count);
            lower.add(labels, BorderLayout.WEST);
            lower.add(browse, BorderLayout.EAST);
            card.add(colorBlock, BorderLayout.CENTER);
            card.add(lower, BorderLayout.SOUTH);
            categoryCards.add(card);
        }
        page.add(categoryCards, BorderLayout.CENTER);
        return page;
    }

    private JPanel createAdminPage() {
        JPanel page = createPage();
        page.add(createPageHeading("Manage artworks", "Add new work or remove items from your collection."), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setOpaque(false);
        JPanel formCard = createWhiteCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.add(sectionTitle("Add an artwork"));
        formCard.add(Box.createVerticalStrut(18));
        formCard.add(fieldLabel("Artwork title"));
        styleInput(titleInput);
        formCard.add(titleInput);
        formCard.add(Box.createVerticalStrut(13));
        formCard.add(fieldLabel("Artist name"));
        styleInput(artistInput);
        formCard.add(artistInput);
        formCard.add(Box.createVerticalStrut(13));
        formCard.add(fieldLabel("Category"));
        categoryInput.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        formCard.add(categoryInput);
        formCard.add(Box.createVerticalStrut(13));
        formCard.add(fieldLabel("Description"));
        descriptionInput.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descriptionInput.setLineWrap(true);
        descriptionInput.setWrapStyleWord(true);
        descriptionInput.setBorder(new EmptyBorder(10, 11, 10, 11));
        JScrollPane descriptionScroll = new JScrollPane(descriptionInput);
        descriptionScroll.setPreferredSize(new Dimension(300, 120));
        descriptionScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        formCard.add(descriptionScroll);
        formCard.add(Box.createVerticalStrut(18));
        JButton saveButton = primaryButton("Add to gallery");
        saveButton.addActionListener(event -> addArtwork());
        formCard.add(saveButton);
        formCard.add(Box.createVerticalGlue());

        JPanel listCard = createWhiteCard();
        listCard.setLayout(new BorderLayout(0, 14));
        listCard.add(sectionTitle("Current collection"), BorderLayout.NORTH);
        manageList.setOpaque(false);
        manageList.setLayout(new BoxLayout(manageList, BoxLayout.Y_AXIS));
        JScrollPane listScroll = new JScrollPane(manageList);
        styleScrollPane(listScroll);
        listCard.add(listScroll, BorderLayout.CENTER);
        content.add(formCard);
        content.add(listCard);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createDetailsPage() {
        JPanel page = createPage();
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JButton back = textButton("← Back to gallery");
        back.addActionListener(event -> showPage(GALLERY_PAGE));
        heading.add(back, BorderLayout.WEST);
        page.add(heading, BorderLayout.NORTH);

        JPanel detailContent = new JPanel(new GridLayout(1, 2, 26, 0));
        detailContent.setOpaque(false);
        detailsPreviewHolder = new JPanel(new BorderLayout());
        detailsPreviewHolder.setBackground(Color.WHITE);
        JPanel info = createWhiteCard();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel category = new JLabel("ARTWORK DETAILS");
        category.setFont(new Font("SansSerif", Font.BOLD, 11));
        category.setForeground(PURPLE);
        JLabel title = new JLabel();
        title.setName("detailsTitle");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(INK);
        JLabel artist = new JLabel();
        artist.setName("detailsArtist");
        artist.setFont(new Font("SansSerif", Font.PLAIN, 15));
        artist.setForeground(MUTED);
        JLabel categoryName = new JLabel();
        categoryName.setName("detailsCategory");
        JTextArea description = new JTextArea();
        description.setName("detailsDescription");
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(new Font("SansSerif", Font.PLAIN, 15));
        description.setForeground(INK);
        description.setOpaque(false);
        description.setBorder(null);
        JButton favorite = primaryButton("♡  Add to favorites");
        favorite.setName("detailsFavorite");
        favorite.addActionListener(event -> {
            if (selectedArtwork != null) {
                currentUser.getFavorites().toggle(selectedArtwork);
                saveArtworks();
                updateDetails();
                refreshAllPages();
            }
        });
        info.add(category);
        info.add(Box.createVerticalStrut(15));
        info.add(title);
        info.add(Box.createVerticalStrut(8));
        info.add(artist);
        info.add(Box.createVerticalStrut(18));
        info.add(categoryName);
        info.add(Box.createVerticalStrut(20));
        info.add(description);
        info.add(Box.createVerticalGlue());
        info.add(favorite);
        detailContent.add(detailsPreviewHolder);
        detailContent.add(info);
        page.add(detailContent, BorderLayout.CENTER);
        return page;
    }

    private void showPage(String page) {
        pageLayout.show(pages, page);
        for (JButton button : navigationButtons) {
            button.setBackground(SIDEBAR);
            button.setForeground(new Color(207, 207, 220));
        }
        int selectedIndex = page.equals(HOME) ? 0
                : page.equals(GALLERY_PAGE) || page.equals(DETAILS_PAGE) ? 1
                : page.equals(FAVORITES_PAGE) ? 2 : page.equals(CATEGORIES_PAGE) ? 3 : 4;
        JButton active = navigationButtons.get(selectedIndex);
        active.setBackground(new Color(71, 58, 112));
        active.setForeground(Color.WHITE);
        if (page.equals(GALLERY_PAGE) || page.equals(FAVORITES_PAGE)
                || page.equals(CATEGORIES_PAGE) || page.equals(ADMIN_PAGE)) {
            refreshAllPages();
        }
    }

    private void refreshAllPages() {
        refreshGallery();
        refreshFavorites();
        refreshManageList();
    }

    private void refreshGallery() {
        if (galleryCards == null) {
            return;
        }
        galleryCards.removeAll();
        ArtCategory category = selectedCategory();
        List<Artwork> results = gallery.search(searchField.getText(), category, false);
        if (results.isEmpty()) {
            galleryCards.setLayout(new BorderLayout());
            galleryCards.add(emptyState("No artwork found", "Try another search or category."), BorderLayout.NORTH);
        } else {
            galleryCards.setLayout(new GridLayout(0, 3, 16, 16));
            for (Artwork artwork : results) {
                galleryCards.add(createArtworkCard(artwork));
            }
        }
        galleryCards.revalidate();
        galleryCards.repaint();
    }

    private void refreshFavorites() {
        favoriteCards.removeAll();
        List<Artwork> favorites = currentUser.getFavorites().getAll();
        if (favorites.isEmpty()) {
            favoriteCards.setLayout(new BorderLayout());
            favoriteCards.add(emptyState("No favorites yet", "Open the gallery and tap the heart on an artwork."), BorderLayout.NORTH);
        } else {
            favoriteCards.setLayout(new GridLayout(0, 3, 16, 16));
            for (Artwork artwork : favorites) {
                favoriteCards.add(createArtworkCard(artwork));
            }
        }
        favoriteCards.revalidate();
        favoriteCards.repaint();
    }

    private void refreshManageList() {
        manageList.removeAll();
        for (Artwork artwork : gallery.getArtworks()) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(new EmptyBorder(10, 4, 10, 4));
            JPanel labels = new JPanel();
            labels.setOpaque(false);
            labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(artwork.getTitle());
            title.setFont(new Font("SansSerif", Font.BOLD, 13));
            JLabel metadata = new JLabel(artwork.getArtist() + " · " + artwork.getCategory());
            metadata.setFont(new Font("SansSerif", Font.PLAIN, 11));
            metadata.setForeground(MUTED);
            labels.add(title);
            labels.add(Box.createVerticalStrut(4));
            labels.add(metadata);
            JButton remove = new JButton("Remove");
            remove.addActionListener(event -> removeArtwork(artwork));
            row.add(labels, BorderLayout.CENTER);
            row.add(remove, BorderLayout.EAST);
            manageList.add(row);
            manageList.add(new javax.swing.JSeparator());
        }
        manageList.revalidate();
        manageList.repaint();
    }

    private JPanel createArtworkCard(final Artwork artwork) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(232, 232, 238)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        ArtworkPreview preview = new ArtworkPreview(artwork);
        preview.setPreferredSize(new Dimension(250, 185));
        preview.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        preview.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent event) {
                showArtwork(artwork);
            }
        });
        card.add(preview, BorderLayout.CENTER);

        JPanel details = new JPanel(new BorderLayout(8, 4));
        details.setBackground(Color.WHITE);
        details.setBorder(new EmptyBorder(12, 13, 12, 10));
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(artwork.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel byline = new JLabel(artwork.getArtist() + "  ·  " + artwork.getCategory());
        byline.setFont(new Font("SansSerif", Font.PLAIN, 11));
        byline.setForeground(MUTED);
        labels.add(title);
        labels.add(Box.createVerticalStrut(5));
        labels.add(byline);

        JButton favorite = new JButton(artwork.isFavorite() ? "♥" : "♡");
        favorite.setFont(new Font("SansSerif", Font.BOLD, 21));
        favorite.setForeground(new Color(202, 83, 112));
        favorite.setBorderPainted(false);
        favorite.setContentAreaFilled(false);
        favorite.setFocusPainted(false);
        favorite.setToolTipText(artwork.isFavorite() ? "Remove from favorites" : "Add to favorites");
        favorite.addActionListener((ActionEvent event) -> {
            currentUser.getFavorites().toggle(artwork);
            saveArtworks();
            refreshAllPages();
        });
        details.add(labels, BorderLayout.CENTER);
        details.add(favorite, BorderLayout.EAST);
        card.add(details, BorderLayout.SOUTH);
        return card;
    }

    private void showArtwork(Artwork artwork) {
        selectedArtwork = artwork;
        updateDetails();
        showPage(DETAILS_PAGE);
    }

    private void updateDetails() {
        if (selectedArtwork == null || detailsPreviewHolder == null) {
            return;
        }
        detailsPreviewHolder.removeAll();
        ArtworkPreview preview = new ArtworkPreview(selectedArtwork);
        detailsPreviewHolder.add(preview, BorderLayout.CENTER);
        setLabelText(pages, "detailsTitle", selectedArtwork.getTitle());
        setLabelText(pages, "detailsArtist", "Created by " + selectedArtwork.getArtist());
        setLabelText(pages, "detailsCategory", selectedArtwork.getCategory().getDisplayName());
        setTextArea(pages, "detailsDescription", selectedArtwork.getDescription());
        JButton favorite = findButton(pages, "detailsFavorite");
        favorite.setText(selectedArtwork.isFavorite() ? "♥  Saved to favorites" : "♡  Add to favorites");
        detailsPreviewHolder.revalidate();
        detailsPreviewHolder.repaint();
    }

    private void addArtwork() {
        String title = titleInput.getText().trim();
        String artist = artistInput.getText().trim();
        String description = descriptionInput.getText().trim();
        if (title.isEmpty() || artist.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a title, artist, and description before adding artwork.",
                    "Required information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int style = gallery.getArtworks().size() % 6;
        try {
            admin.addArtwork(new Artwork(title, artist,
                    ArtCategory.fromDisplayName((String) categoryInput.getSelectedItem()), description,
                    firstColorFor(style), secondColorFor(style), style));
            saveArtworks();
            titleInput.setText("");
            artistInput.setText("");
            descriptionInput.setText("");
            refreshAllPages();
            JOptionPane.showMessageDialog(this, "Artwork added to your gallery.",
                    "Artwork added", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Could not add artwork", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeArtwork(Artwork artwork) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove \"" + artwork.getTitle() + "\" from the gallery?",
                "Remove artwork", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            admin.removeArtwork(artwork);
            currentUser.getFavorites().removeArtwork(artwork);
            saveArtworks();
            refreshAllPages();
        }
    }

    private void loadArtworks() {
        try {
            boolean currentFormat = database.hasCurrentFormat(DATA_FILE);
            List<Artwork> loaded = database.load(DATA_FILE);
            for (Artwork artwork : loaded) {
                admin.addArtwork(artwork);
            }
            if (loaded.isEmpty() && !currentFormat) {
                for (Artwork artwork : Gallery.createSampleArtworks()) {
                    admin.addArtwork(artwork);
                }
                saveArtworks();
            } else if (!currentFormat) {
                addMissingSampleArtworks();
            }
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this,
                    "Could not read " + DATA_FILE + ": " + exception.getMessage()
                            + "\nThe sample collection will be used for this session.",
                    "Gallery data error", JOptionPane.ERROR_MESSAGE);
            for (Artwork artwork : Gallery.createSampleArtworks()) {
                admin.addArtwork(artwork);
            }
        }
    }

    private void addMissingSampleArtworks() {
        for (Artwork sample : Gallery.createSampleArtworks()) {
            boolean found = false;
            for (Artwork existing : gallery.getArtworks()) {
                if (existing.getTitle().equalsIgnoreCase(sample.getTitle())
                        && existing.getArtist().equalsIgnoreCase(sample.getArtist())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                admin.addArtwork(sample);
            }
        }
        saveArtworks();
    }

    private void saveArtworks() {
        try {
            database.save(DATA_FILE, gallery.getArtworks());
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this,
                    "Could not save gallery data to " + DATA_FILE + ": " + exception.getMessage(),
                    "Gallery save error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int countFor(ArtCategory category) {
        int count = 0;
        for (Artwork artwork : gallery.getArtworks()) {
            if (artwork.getCategory() == category) {
                count++;
            }
        }
        return count;
    }

    private ArtCategory selectedCategory() {
        String selected = (String) categoryFilter.getSelectedItem();
        return selected == null || "All artworks".equals(selected)
                ? null : ArtCategory.fromDisplayName(selected);
    }

    private static JPanel createPage() {
        JPanel page = new JPanel(new BorderLayout(0, 22));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(28, 30, 25, 30));
        return page;
    }

    private static JPanel createPageHeading(String title, String subtitle) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        titleLabel.setForeground(INK);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);
        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitleLabel);
        heading.add(Box.createVerticalStrut(18));
        return heading;
    }

    private static JPanel createGridPanel() {
        JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
        grid.setOpaque(false);
        return grid;
    }

    private static JPanel createWhiteCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(232, 232, 238)),
                new EmptyBorder(20, 20, 20, 20)));
        return card;
    }

    private static JPanel createStatCard(String number, String label) {
        JPanel card = createWhiteCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        numberLabel.setForeground(PURPLE);
        JLabel text = new JLabel(label);
        text.setFont(new Font("SansSerif", Font.PLAIN, 12));
        text.setForeground(MUTED);
        card.add(numberLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(text);
        return card;
    }

    private static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        label.setForeground(INK);
        return label;
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(INK);
        return label;
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(PURPLE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
        button.setAlignmentX(LEFT_ALIGNMENT);
        return button;
    }

    private static JButton textButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(PURPLE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }

    private static void styleInput(JTextField input) {
        input.setFont(new Font("SansSerif", Font.PLAIN, 13));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 231)),
                new EmptyBorder(10, 11, 10, 11)));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        input.setAlignmentX(LEFT_ALIGNMENT);
    }

    private static void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
    }

    private static JPanel emptyState(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(55, 20, 40, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(INK);
        heading.setAlignmentX(CENTER_ALIGNMENT);
        JLabel message = new JLabel(subtitle);
        message.setFont(new Font("SansSerif", Font.PLAIN, 13));
        message.setForeground(MUTED);
        message.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(7));
        panel.add(message);
        return panel;
    }

    private static void setLabelText(JPanel root, String name, String text) {
        java.awt.Component component = findComponent(root, name);
        if (component instanceof JLabel) {
            ((JLabel) component).setText(text);
        }
    }

    private static void setTextArea(JPanel root, String name, String text) {
        java.awt.Component component = findComponent(root, name);
        if (component instanceof JTextArea) {
            ((JTextArea) component).setText(text);
        }
    }

    private static JButton findButton(JPanel root, String name) {
        java.awt.Component component = findComponent(root, name);
        if (!(component instanceof JButton)) {
            throw new IllegalStateException("Could not find button: " + name);
        }
        return (JButton) component;
    }

    private static java.awt.Component findComponent(JPanel root, String name) {
        if (name.equals(root.getName())) {
            return root;
        }
        for (java.awt.Component child : root.getComponents()) {
            if (name.equals(child.getName())) {
                return child;
            }
            if (child instanceof JPanel) {
                java.awt.Component found = findComponent((JPanel) child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static Color firstColorFor(int style) {
        Color[] colors = {
            new Color(251, 171, 130), new Color(102, 156, 177), new Color(177, 154, 218),
            new Color(247, 202, 105), new Color(105, 127, 158), new Color(143, 190, 166)
        };
        return colors[style % colors.length];
    }

    private static Color secondColorFor(int style) {
        Color[] colors = {
            new Color(244, 110, 125), new Color(218, 174, 123), new Color(236, 196, 151),
            new Color(123, 177, 155), new Color(202, 149, 135), new Color(225, 160, 177)
        };
        return colors[style % colors.length];
    }

    private static class ArtworkPreview extends JPanel {
        private final Artwork artwork;

        private ArtworkPreview(Artwork artwork) {
            this.artwork = artwork;
            setBackground(artwork.getFirstColor());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g.setPaint(new GradientPaint(0, 0, artwork.getFirstColor(),
                    width, height, artwork.getSecondColor()));
            g.fillRect(0, 0, width, height);

            switch (artwork.getStyle() % 3) {
                case 0:
                    g.setColor(new Color(255, 255, 255, 150));
                    g.fillOval(width / 5, height / 5, height / 2, height / 2);
                    g.setColor(new Color(255, 255, 255, 190));
                    g.fillOval(width / 2, height / 3, height / 3, height / 3);
                    g.setColor(new Color(85, 100, 91, 100));
                    g.fillRect(0, height * 3 / 4, width, height / 4);
                    break;
                case 1:
                    g.setColor(new Color(255, 255, 255, 145));
                    g.fillOval(width / 4, height / 6, height / 2, height / 2);
                    g.setColor(new Color(48, 69, 89, 90));
                    int[] xPoints = { 0, width / 3, width * 2 / 3, width };
                    int[] yPoints = { height, height / 2, height * 3 / 4, height / 3 };
                    g.fillPolygon(xPoints, yPoints, 4);
                    break;
                default:
                    g.setColor(new Color(255, 255, 255, 135));
                    g.fillRoundRect(width / 4, height / 4, width / 2, height / 2, 35, 35);
                    g.setColor(new Color(255, 255, 255, 90));
                    g.fillOval(width / 2, height / 3, height / 3, height / 3);
                    g.setColor(new Color(65, 65, 75, 35));
                    g.fillOval(width / 5, height * 3 / 4, width * 3 / 5, 16);
                    break;
            }
            g.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            System.err.println("Could not apply the system look and feel: " + exception.getMessage());
        }
        SwingUtilities.invokeLater(() -> new VirtualArtGallery().setVisible(true));
    }
}
