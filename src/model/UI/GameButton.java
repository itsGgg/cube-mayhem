package model.UI;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.ImageIcon;

import model.tools.LoadSave;
import model.tools.MusicBank;
import model.level.GameState;
import controller.MenuController;
import view.menu.MenuView;
import view.affichage2D.InventoryPanel;
import view.Window;
import model.tools.Constants;

/**
 * Classe générique pour les boutons du jeu.
 * Gère l'affichage, l'animation et les interactions avec les boutons.
 */
public class GameButton extends JButton {
    private String spriteName;
    private int xPos, yPos; // Position du bouton
    private int width, height; // Largeur et hauteur du bouton
    private int originalWidth, originalHeight; // Dimensions originales du bouton
    private int rowIndex; // Index de la ligne dans l'atlas des boutons
    protected int index; // Index de l'état du bouton (0: normal, 1: survolé, 2: pressé)
    private BufferedImage[] imgs; // Images des différents états du bouton
    private int nbButton; // Nombre de boutons
    private float alpha = 1.0f; // Transparence du bouton
    private Object state; // État du jeu ou le Mode d'affichage associé au bouton
    private final ReentrantLock stateLock = new ReentrantLock(); // Lock pour synchroniser les accès à l'état
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private static final float DRAW_SCALE_FACTOR = 0.8f; // Facteur de réduction pour le dessin

    /**
     * Constructeur pour un bouton avec position fixe
     */
    public GameButton(String name, int nbButton, int xPos, int yPos, int width, int height, int rowIndex,
            Object state) {
        super();
        this.spriteName = name;
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.originalWidth = width;
        this.originalHeight = height;
        this.rowIndex = rowIndex;
        this.state = state;
        this.nbButton = nbButton;

        imgs = new BufferedImage[nbButton];
        loadImgs(0, nbButton);
        initButton();

        addActionListener(e -> {
            System.out.println("Bouton cliqué");
            MusicBank.playParallel(101);
            update();
            animate();
            applyGamestate();
        });

        // Ajouter un écouteur de redimensionnement
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateScaling();
            }
        });
    }

    /**
     * Constructeur pour un bouton avec position fixe
     */
    public GameButton(String name, int nbButton, int xPos, int yPos, int width, int height, int rowIndex) {
        super();
        this.spriteName = name;
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.originalWidth = width;
        this.originalHeight = height;
        this.rowIndex = rowIndex;
        this.nbButton = nbButton;

        imgs = new BufferedImage[nbButton];
        loadImgs(0, nbButton);
        initButton();
        addActionListener(e -> {
            System.out.println("Bouton cliqué");
            MusicBank.playParallel(101);
            update();
            animate();
        });
    }

    /**
     * Redimensionne le bouton en fonction d'un facteur d'échelle
     * 
     * @param scaleX Facteur d'échelle horizontal
     * @param scaleY Facteur d'échelle vertical
     */
    public int[] resize(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.width = (int) (originalWidth * scaleX);
        this.height = (int) (originalHeight * scaleY);
        setBounds(xPos, yPos, width, height);
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));

        // Recharger les images après le redimensionnement
        if (imgs != null) {
            loadImgs(0, nbButton);
        }

        revalidate();
        repaint();
        return new int[] { width, height };
    }

    /**
     * Initialise le bouton avec ses propriétés.
     */
    private void initButton() {
        setBounds(xPos, yPos, width, height);
        setRolloverEnabled(true);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setIcon(null); // Pas d'icône, nous dessinons nous-mêmes
        repaint();
    }

    /**
     * Met à jour l'icône du bouton selon son état.
     */
    public void updateIcon() {
        // Ne pas utiliser setIcon car nous dessinons l'image nous-mêmes
        setIcon(null); // Supprime toute icône existante
        repaint();
    }

    /**
     * Met à jour l'animation de transparence du bouton.
     */
    public void animate() {
        if (getModel().isRollover() && alpha < 1.0f) {
            alpha += 0.5f;
        } else if (!getModel().isRollover() && alpha > 0.5f) {
            alpha -= 0.5f;
        }

        if (getModel().isPressed()) {
            alpha = 0.5f;
        }
        repaint();
    }

    /**
     * Dessine le bouton avec son état actuel.
     * 
     * @param g Contexte graphique
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Nettoie le fond du composant
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (imgs != null && index >= 0 && index < imgs.length && imgs[index] != null) {
            // Calcul des dimensions réduites pour le dessin
            int drawWidth = (int) (getWidth() * DRAW_SCALE_FACTOR);
            int drawHeight = (int) (getHeight() * DRAW_SCALE_FACTOR);
            // Centrer le dessin
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;
            // Pas d'icône, nous dessinons nous-mêmes
            g2d.drawImage(imgs[index], x, y, drawWidth, drawHeight, this);
        }
    }

    /**
     * Charge les images des différents états du bouton depuis le sprite.
     * 
     * @param d Début de l'index
     * @param f Fin de l'index
     */
    private void loadImgs(int d, int f) {
        if (d < 0 || f < 0) {
            throw new IllegalArgumentException("d et f doivent être supérieurs à 0");
        }
        if (d > f) {
            throw new IllegalArgumentException("d doit être inférieur à f");
        }

        BufferedImage temp = LoadSave.GetSprite(spriteName);
        int spriteWidth = temp.getWidth();
        int spriteHeight = temp.getHeight();

        // Vérification des dimensions
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Les dimensions du bouton doivent être positives");
        }

        // Vérification que l'index de ligne est valide
        if (rowIndex < 0 || (rowIndex + 1) * originalHeight > spriteHeight) {
            throw new IllegalArgumentException("L'index de ligne est invalide pour l'image source");
        }

        // Vérification que les indices horizontaux sont valides
        if (f * originalWidth > spriteWidth) {
            throw new IllegalArgumentException("Les indices horizontaux dépassent la largeur de l'image");
        }

        for (int i = d; i < f; i++) {
            imgs[i] = temp.getSubimage(i * originalWidth, rowIndex * originalHeight, originalWidth, originalHeight);
        }
    }

    public void applyGamestate() {
        stateLock.lock();
        try {
            if (state != null) {
                if (state instanceof GameState.GameMode) {
                    GameState.GameMode mode = (GameState.GameMode) state;
                    // Appliquer le GameMode de jeu
                    if (getParent() != null) {
                        MenuController menuController = null;

                        // d'abord on cherche le MenuController via l'InventoryPanel

                        if (getParent() instanceof InventoryPanel) {
                            menuController = ((InventoryPanel) getParent()).getMenuController();
                        }

                        // Si on n'a pas trouvé, on cherche via la hiérarchie des composants
                        if (menuController == null) {

                            java.awt.Container parent = getParent();
                            while (parent != null && !(parent instanceof MenuView)) {
                                parent = parent.getParent();
                            }
                            if (parent != null) {
                                MenuView menuView = (MenuView) parent;
                                menuController = menuView.getMenuController();
                            }
                        }

                        if (menuController != null) {
                            if ((menuController.getGameState()
                                    .getCurrentState() == GameState.GameMode.LEVEL_EDITOR_CREATE)
                                    && (mode == GameState.GameMode.EXECUTING || mode == GameState.GameMode.DISPOSING)) {
                                System.out.println("Action ignorée en mode éditeur");
                                return;
                            }
                            System.out.println("menuController != null");
                            System.out.println("state = " + menuController.getGameState().getCurrentState());
                            menuController.getGameState().setCurrentState(mode);
                            System.out.println("state = " + menuController.getGameState().getCurrentState());
                        }
                    }
                } else if (state instanceof GameState.DisplayMode) {
                    GameState.DisplayMode mode = (GameState.DisplayMode) state;
                    // Appliquer le DisplayMode
                    if (getParent() != null) {
                        MenuController menuController = null;

                        // d'abord on cherche le MenuController via l'InventoryPanel
                        if (getParent() instanceof InventoryPanel) {
                            menuController = ((InventoryPanel) getParent()).getMenuController();
                        }

                        // Si on n'a pas trouvé, on cherche via la hiérarchie des composants
                        if (menuController == null) {
                            java.awt.Container parent = getParent();
                            while (parent != null && !(parent instanceof MenuView)) {
                                parent = parent.getParent();
                            }
                            if (parent != null) {
                                MenuView menuView = (MenuView) parent;
                                menuController = menuView.getMenuController();
                            }
                        }

                        if (menuController != null) {
                            menuController.setDisplayMode(mode);
                        }
                    }
                }
            }
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Met à jour l'état du bouton.
     */
    public void update() {
        // updatePosition(xPos, yPos);
        if (getModel().isPressed()) {
            index = 2;
        } else if (getModel().isRollover()) {
            index = 1;
        } else {
            index = 0;
        }
        updateIcon();
    }

    /**
     * Met à jour la position du bouton.
     * 
     * @param xPos Nouvelle position X
     * @param yPos Nouvelle position Y
     */
    public void updatePosition(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        setBounds(xPos, yPos, width, height);
    }

    /**
     * Positionne le bouton dans le coin inférieur gauche de la fenêtre
     * 
     * @param buttonOrder   L'ordre du bouton (0, 1, 2, etc.)
     * @param buttonSpacing L'espacement entre les boutons
     * @param windowSpacing L'espacement entre le bouton et la fenêtre
     */
    public void positionAtBottomLeft(int buttonOrder, int buttonSpacing, int windowSpacing) {
        int buttonX = windowSpacing + buttonOrder * (width + buttonSpacing);
        int buttonY = (int) Window.height - height - 50;
        updatePosition(buttonX, buttonY);
    }

    /**
     * Positionne le bouton dans le coin inférieur droit de la fenêtre
     * 
     * @param buttonOrder   L'ordre du bouton à partir de la droite (0, 1, 2, etc.)
     * @param buttonSpacing L'espacement entre les boutons
     * @param windowSpacing L'espacement entre le bouton et la fenêtre
     */
    public void positionAtBottomRight(int buttonOrder, int buttonSpacing, int windowSpacing) {
        int buttonX = (int) Window.width - width - windowSpacing - buttonOrder * (width + buttonSpacing);
        int buttonY = (int) Window.height - height - 50;
        updatePosition(buttonX, buttonY);
    }

    /**
     * Positionne le bouton dans le coin supérieur gauche de la fenêtre
     * 
     * @param buttonOrder   L'ordre du bouton (0, 1, 2, etc.)
     * @param buttonSpacing L'espacement entre les boutons
     * @param windowSpacing L'espacement entre le bouton et la fenêtre
     */
    public void positionAtTopLeft(int buttonOrder, int buttonSpacing, int windowSpacing) {
        int buttonX = windowSpacing + buttonOrder * (width + buttonSpacing);
        int buttonY = 10;
        updatePosition(buttonX, buttonY);
    }

    /**
     * Positionne le bouton dans le coin supérieur droit de la fenêtre
     * 
     * @param buttonOrder   L'ordre du bouton à partir de la droite (0, 1, 2, etc.)
     * @param buttonSpacing L'espacement entre les boutons
     * @param windowSpacing L'espacement entre le bouton et la fenêtre
     */
    public void positionAtTopRight(int buttonOrder, int buttonSpacing, int windowSpacing) {
        int buttonX = (int) Window.width - width - windowSpacing - buttonOrder * (width + buttonSpacing);
        int buttonY = 10;
        updatePosition(buttonX, buttonY);
    }

    /**
     * Positionne le bouton au centre de la fenêtre
     * 
     * @param buttonOrder   L'ordre du bouton (0, 1, 2, etc.)
     * @param buttonSpacing L'espacement entre les boutons
     * @param windowSpacing L'espacement entre le bouton et la fenêtre
     */
    public void positionAtCenter(int buttonOrder, int buttonSpacing, int windowSpacing) {
        int buttonX = (int) (Window.width / 2) - (width / 2) + buttonOrder * (width + buttonSpacing);
        int buttonY = (int) (Window.height / 2) - (height / 2);
        updatePosition(buttonX, buttonY);
    }

    /**
     * @return L'état du jeu associé au bouton
     */
    public Object getState() {
        stateLock.lock();
        try {
            return state;
        } finally {
            stateLock.unlock();
        }
    }

    /**
     * Définit manuellement l'index du sprite à utiliser
     * 
     * @param newIndex Le nouvel index du sprite
     */
    public void setIndex(int newIndex) {
        if (newIndex >= 0 && newIndex < nbButton) {
            this.index = newIndex;
        }
    }

    private void updateScaling() {
        if (getParent() != null) {
            float windowWidth = (float) Window.width;
            float windowHeight = (float) Window.height;
            scaleX = windowWidth / Constants.Window.DEFAULT_WIDTH;
            scaleY = windowHeight / Constants.Window.DEFAULT_HEIGHT;
            resize(scaleX, scaleY);
        }
    }
}