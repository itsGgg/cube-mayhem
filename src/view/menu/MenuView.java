package view.menu;

import java.awt.CardLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import controller.MenuController;
import model.UI.BaseGameMenu;
import model.level.GameState;
import model.tools.LoadSave;

/**
 * Vue principale du menu du jeu.
 * Gère l'affichage des différents sous-menus et leurs transitions.
 */
public class MenuView extends BaseGameMenu {

    private BufferedImage backgroundImg;
    private CardLayout cardLayout;
    // Sous-menus
    private DisplayModesMenu displayModesMenu; // 0
    private HomeMenu homeMenu; // 1
    private LevelsMenu levelsMenu; // 2
    private ExtrasMenu extrasMenu; // 3
    private CreditsMenu creditsMenu; // 4
    private WinMessagePanel winMessagePanel; // 5
    private NewGamePanel newGamePanel; // 6
    private int currentMenu = 0;

    /**
     * Constructeur de la vue du menu.
     * 
     * @param menuController Contrôleur associé au menu
     */
    public MenuView(MenuController menuController) {
        super(menuController, menuController.getGameState());
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        backgroundImg = LoadSave.GetSprite("bckgMenu");
    }

    @Override
    public void initializeComponents() {
        // Création des sous-menus
        displayModesMenu = new DisplayModesMenu(menuController, gameState);
        homeMenu = new HomeMenu(menuController, gameState);
        levelsMenu = new LevelsMenu(menuController, gameState);
        extrasMenu = new ExtrasMenu(menuController, gameState);
        creditsMenu = new CreditsMenu(menuController, gameState);
        newGamePanel = new NewGamePanel(menuController, gameState);

        // Initialisation des sous-menus
        displayModesMenu.initialize();
        homeMenu.initialize();
        levelsMenu.initialize();
        extrasMenu.initialize();
        creditsMenu.initialize();
        newGamePanel.initialize();

        // Ajout des sous-menus au CardLayout
        add(displayModesMenu, "DISPLAY_MODES");
        add(homeMenu, "HOME");
        add(levelsMenu, "LEVELS");
        add(extrasMenu, "EXTRAS");
        add(creditsMenu, "CREDITS");
        add(newGamePanel, "NEW_GAME");
    }

    @Override
    public void updateComponents() {
        switch (currentMenu) {
            case 0 -> displayModesMenu.updateComponents();
            case 1 -> homeMenu.updateComponents();
            case 2 -> levelsMenu.updateComponents();
            case 3 -> extrasMenu.updateComponents();
            case 4 -> creditsMenu.updateComponents();
            case 5 -> {
                if (winMessagePanel != null) {
                    winMessagePanel.updateComponents();
                }
            }
            case 6 -> newGamePanel.updateComponents();
        }
    }

    @Override
    public void handleNavigation() {
        // La navigation est gérée via les méthodes show*Menu
    }

    @Override
    public void applyTheme() {
        setOpaque(false);
    }

    @Override
    public void handleResize() {
        displayModesMenu.handleResize();
        homeMenu.handleResize();
        levelsMenu.handleResize();
        extrasMenu.handleResize();
        creditsMenu.handleResize();
        newGamePanel.handleResize();
        if (winMessagePanel != null) {
            winMessagePanel.handleResize();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);
        }
    }

    // Méthodes de navigation
    public void showDisplayModesMenu() {
        currentMenu = 0;
        cardLayout.show(this, "DISPLAY_MODES");
    }

    public void showHomeMenu() {
        currentMenu = 1;
        cardLayout.show(this, "HOME");
    }

    public void showLevelsMenu() {
        currentMenu = 2;
        cardLayout.show(this, "LEVELS");
    }

    public void showExtrasMenu() {
        currentMenu = 3;
        cardLayout.show(this, "EXTRAS");
    }

    public void showCreditsMenu() {
        currentMenu = 4;
        cardLayout.show(this, "CREDITS");
    }

    public void showNewGamePanel() {
        currentMenu = 6;
        cardLayout.show(this, "NEW_GAME");
    }

    public void showWinMessage() {
        if (winMessagePanel == null) {
            winMessagePanel = new WinMessagePanel(menuController, gameState);
            winMessagePanel.initialize();
            add(winMessagePanel, "WIN");
        }
        currentMenu = 5;
        cardLayout.show(this, "WIN");
    }

    public void hideWinMessage() {
        if (winMessagePanel != null) {
            remove(winMessagePanel);
            winMessagePanel = null;
        }
        showHomeMenu();
    }

    /**
     * Retourne le menu des niveaux
     */
    public LevelsMenu getLevelsMenu() {
        return levelsMenu;
    }
}
