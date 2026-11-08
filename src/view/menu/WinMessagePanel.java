package view.menu;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import controller.MenuController;
import model.UI.BaseGameMenu;
import model.UI.GameButton;
import model.level.GameState;
import model.tools.Constants;
import view.Window;

/**
 * Panneau affichant le message de victoire à la fin d'un niveau.
 * Ce panneau contient un message de félicitations et deux boutons :
 * - Un bouton pour passer au niveau suivant
 * - Un bouton pour retourner au menu principal
 * 
 * Le panneau apparaît avec un effet de fondu (fade-in) et un fond
 * semi-transparent.
 */
public class WinMessagePanel extends BaseGameMenu {
    private GameButton nextLevelButton;
    private GameButton menuButton;
    private float alpha = 0f;
    private static final float ALPHA_SPEED = 0.02f;
    private static final float TARGET_ALPHA = 0.75f;

    /**
     * Constructeur du panneau de message de victoire.
     * 
     * @param menuController Le contrôleur du menu pour gérer les actions des
     *                       boutons
     * @param gameState      L'état actuel du jeu
     */
    public WinMessagePanel(MenuController menuController, GameState gameState) {
        super(menuController, gameState);
        setLayout(null);
        setOpaque(false);
        setBounds(0, 0, Constants.Window.DEFAULT_WIDTH, Constants.Window.DEFAULT_HEIGHT);
        setPreferredSize(new Dimension(Constants.Window.DEFAULT_WIDTH, Constants.Window.DEFAULT_HEIGHT));

        // Ajouter un listener pour détecter les changements de taille
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                handleResize();
            }
        });

        startFadeIn();
    }

    /**
     * Initialise les composants du panneau de victoire.
     */
    @Override
    public void initializeComponents() {
        initButtons();
    }

    /**
     * Initialise les boutons du panneau de victoire.
     * Crée et configure les boutons "Niveau suivant" et "Menu" avec leurs actions
     * respectives.
     */
    private void initButtons() {
        int defaultWidth = Constants.Window.DEFAULT_WIDTH;
        int defaultHeight = Constants.Window.DEFAULT_HEIGHT;

        nextLevelButton = new GameButton(
                "startPauseButtons",
                3,
                (int) (defaultWidth * 0.6),
                (int) (defaultHeight * 0.7),
                88,
                88,
                0,
                "NIVEAU SUIVANT");

        menuButton = new GameButton(
                "spriteNewGame",
                3,
                (int) (defaultWidth * 0.3),
                (int) (defaultHeight * 0.7),
                150,
                88,
                1,
                "MENU");

        nextLevelButton.addActionListener(e -> {
            try {
                // Débloquer le niveau suivant si nécessaire
                if (menuController.getLevelController().getCurrentLevelNumber() == menuController.getLevelController()
                        .getMaxLevelUnlocked()) {
                    menuController.getLevelController().increaseMaxLevelUnlocked();
                    menuController.getGameController().getUserProgress()
                            .setMaxLevelUnlocked(menuController.getLevelController().getMaxLevelUnlocked());
                    menuController.getGameController().getUserProgress().saveProgress();
                }

                // Fermer le message de victoire
                menuController.hideWinMessage();
                menuController.getGameState().setCurrentState(GameState.GameMode.LEVEL_SELECTION);
                // Mettre à jour l'interface
                menuController.getWindow().getMenuView().getLevelsMenu().refreshLevelButtons();
                menuController.getWindow().goToMenu();
            } catch (RuntimeException ex) {
                System.out.println("Erreur : " + ex.getMessage());
                menuController.hideWinMessage();
                menuController.getGameState().setCurrentState(GameState.GameMode.LEVEL_SELECTION);
                menuController.getWindow().goToMenu();
            }
        });

        menuButton.addActionListener(e -> {
            menuController.hideWinMessage();
            menuController.getGameController().restartGameLevel();
            menuController.getGameState().setCurrentState(GameState.GameMode.LEVEL_SELECTION);
            menuController.getWindow().getMenuView().getLevelsMenu().refreshLevelButtons();
            menuController.getWindow().goToMenu();
        });

        addButton(nextLevelButton);
        addButton(menuButton);

        add(menuButton);
        add(nextLevelButton);

        handleResize();
    }

    /**
     * Démarre l'animation de fondu (fade-in) du panneau.
     * L'animation augmente progressivement l'opacité jusqu'à atteindre la valeur
     * cible.
     */
    private void startFadeIn() {
        new Thread(() -> {
            while (alpha < TARGET_ALPHA) {
                alpha += ALPHA_SPEED;
                if (alpha > TARGET_ALPHA)
                    alpha = TARGET_ALPHA;
                repaint();
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Dessine le contenu du panneau.
     * Affiche le fond semi-transparent et le message de victoire.
     * 
     * @param g Le contexte graphique pour le dessin
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fond semi-transparent
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(new Color(0, 0, 0, 180)); // Noir avec alpha
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Message de victoire
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.WHITE);
        Font font = new Font("Press Start 2P", Font.BOLD, 88);
        g2d.setFont(font);

        String message = "WELL DONE!";
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        int x = (getWidth() - textWidth) / 2;
        g2d.drawString(message, x, 100);
    }

    /**
     * Met à jour l'état des boutons.
     */
    @Override
    public void updateComponents() {
        super.updateComponents();
    }

    /**
     * Gère la navigation du menu
     */
    @Override
    public void handleNavigation() {
        // Pas d'action spécifique pour la navigation dans ce panneau
    }

    /**
     * Applique le thème au panneau
     */
    @Override
    public void applyTheme() {
        setOpaque(false);
    }

    /**
     * Gère le redimensionnement du panneau
     */
    @Override
    public void handleResize() {
        // System.out.println("WinMessagePanel - handleResize appelé, dimensions: " +
        // getWidth() + "x" + getHeight());

        // Ajuster la taille du panneau
        int currentWidth = getWidth() > 0 ? getWidth() : Constants.Window.DEFAULT_WIDTH;
        int currentHeight = getHeight() > 0 ? getHeight() : Constants.Window.DEFAULT_HEIGHT;

        setBounds(0, 0, currentWidth, currentHeight);
        setPreferredSize(new Dimension(currentWidth, currentHeight));

        // Calcul des facteurs d'échelle
        float scaleX = (float) currentWidth / Constants.Window.DEFAULT_WIDTH;
        float scaleY = (float) currentHeight / Constants.Window.DEFAULT_HEIGHT;

        System.out.println("Facteurs d'échelle: " + scaleX + ", " + scaleY);

        // Redimensionner et repositionner les boutons
        if (nextLevelButton != null) {
            // Position relative à la taille actuelle du panneau
            int newX = (int) (currentWidth * 0.6) - (nextLevelButton.getWidth() / 2);
            int newY = (int) (currentHeight * 0.7) - (nextLevelButton.getHeight() / 2);

            nextLevelButton.resize(scaleX, scaleY);
            nextLevelButton.updatePosition(newX, newY);
        }

        if (menuButton != null) {
            // Position relative à la taille actuelle du panneau
            int newX = (int) (currentWidth * 0.3) - (menuButton.getWidth() / 2);
            int newY = (int) (currentHeight * 0.7) - (menuButton.getHeight() / 2);

            menuButton.resize(scaleX, scaleY);
            menuButton.updatePosition(newX, newY);
        }

        // Forcer la mise à jour de l'affichage
        revalidate();
        repaint();
    }

    /**
     * Nettoie les ressources du panneau
     */
    @Override
    public void cleanup() {
        super.cleanup();
        if (nextLevelButton != null) {
            nextLevelButton.removeAll();
            nextLevelButton = null;
        }
        if (menuButton != null) {
            menuButton.removeAll();
            menuButton = null;
        }
        removeAll();
        revalidate();
        repaint();
    }
}