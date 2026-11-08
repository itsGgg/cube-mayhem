package view.menu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import controller.GameController;
import controller.LevelController;
import controller.MenuController;
import model.UI.BaseGameMenu;
import model.UI.GameButton;
import model.level.GameState;
import model.level.GameState.GameMode;
import model.level.Level;
import model.tools.Constants;
import view.Window;

/**
 * Menu de sélection des niveaux
 */
public class LevelsMenu extends BaseGameMenu {
    private GameController gameController;
    private LevelController levelController;
    private JPanel gridPanel;
    private JPanel levelSelectPanel;
    private GameButton backButton;
    private JLayeredPane layeredPane;

    // Facteurs d'échelle pour le redimensionnement
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    // Constantes pour le dimensionnement
    private static final int DEFAULT_BUTTON_SIZE = 88;
    private static final int DEFAULT_BUTTON_SPACING = 20;
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 5;
    private static final int MAX_LEVELS = 22;

    public LevelsMenu(MenuController menuController, GameState gameState) {
        super(menuController, gameState);
        this.gameController = menuController.getGameController();
        this.levelController = menuController.getLevelController();

        // Ajout d'un écouteur de redimensionnement
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateScaling();
                updateLayout();
            }
        });
    }

    /**
     * Met à jour les facteurs d'échelle en fonction de la taille de la fenêtre
     */
    private void updateScaling() {
        if (getParent() != null) {
            float windowWidth = (float) Window.width;
            float windowHeight = (float) Window.height;
            scaleX = windowWidth / Constants.Window.DEFAULT_WIDTH;
            scaleY = windowHeight / Constants.Window.DEFAULT_HEIGHT;
        }
    }

    /**
     * Met à jour la disposition des composants après un redimensionnement
     */
    private void updateLayout() {
        if (levelSelectPanel == null || gridPanel == null)
            return;

        int scaledButtonSize = (int) (DEFAULT_BUTTON_SIZE * Math.min(scaleX, scaleY));
        int scaledSpacing = (int) (DEFAULT_BUTTON_SPACING * Math.min(scaleX, scaleY));

        // Mise à jour des dimensions du panneau de sélection
        int panelWidth = GRID_COLS * scaledButtonSize + (GRID_COLS - 1) * scaledSpacing;
        int panelHeight = GRID_ROWS * scaledButtonSize + (GRID_ROWS - 1) * scaledSpacing;
        levelSelectPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));

        // Mise à jour de la grille
        gridPanel.setLayout(new GridLayout(GRID_ROWS, GRID_COLS, scaledSpacing, scaledSpacing));

        // Redimensionnement des boutons
        for (GameButton button : buttons) {
            button.resize(scaleX, scaleY);
        }

        // Repositionner le bouton retour
        if (backButton != null) {
            backButton.positionAtBottomLeft(0, scaledSpacing, scaledSpacing);
        }

        // Mettre à jour le layeredPane pour qu'il occupe tout l'espace
        if (layeredPane != null) {
            layeredPane.setBounds(0, 0, getWidth(), getHeight());

            // Centrer le levelSelectPanel
            levelSelectPanel.setBounds(
                    (getWidth() - panelWidth) / 2,
                    (getHeight() - panelHeight) / 2,
                    panelWidth,
                    panelHeight);
        }

        revalidate();
        repaint();
    }

    @Override
    public void initializeComponents() {
        updateScaling();

        // Utiliser GridBagLayout pour le panel principal afin de centrer facilement les
        // composants
        setLayout(new BorderLayout());

        // Créer un JLayeredPane pour permettre la superposition du levelSelectPanel et
        // du backButton
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // Création du panneau de sélection
        levelSelectPanel = new JPanel();
        levelSelectPanel.setOpaque(false);
        levelSelectPanel.setLayout(new BorderLayout(
                (int) (DEFAULT_BUTTON_SPACING * scaleX),
                (int) (DEFAULT_BUTTON_SPACING * scaleY)));

        int panelWidth = GRID_COLS * (int) (DEFAULT_BUTTON_SIZE * scaleX)
                + (GRID_COLS - 1) * (int) (DEFAULT_BUTTON_SPACING * scaleX);
        int panelHeight = GRID_ROWS * (int) (DEFAULT_BUTTON_SIZE * scaleY)
                + (GRID_ROWS - 1) * (int) (DEFAULT_BUTTON_SPACING * scaleY);
        levelSelectPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));

        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setLayout(new GridLayout(
                GRID_ROWS,
                GRID_COLS,
                (int) (DEFAULT_BUTTON_SPACING * scaleX),
                (int) (DEFAULT_BUTTON_SPACING * scaleY)));

        initButtons();
        createBackButton();

        // Ajouter levelSelectPanel au JLayeredPane
        layeredPane.add(levelSelectPanel, JLayeredPane.DEFAULT_LAYER);

        // Une fois tous les composants ajoutés, on les positionne
        SwingUtilities.invokeLater(this::updateLayout);

        // Ajout des boutons de contrôle
        controlButtons = new ControlButtons(menuController, this);

        setBackground(Constants.Color.MENU_BG);
        setOpaque(true);
    }

    private void createBackButton() {
        // Bouton retour
        backButton = new GameButton("backButton", 3, 0, 0,
                (int) (DEFAULT_BUTTON_SIZE * scaleX),
                (int) (DEFAULT_BUTTON_SIZE * scaleY),
                0);
        backButton.addActionListener(e -> {
            gameState.setCurrentState(GameMode.MAIN_SCREEN);
            menuController.getWindow().getMenuView().showHomeMenu();
        });
        addButton(backButton);

        // Positionner le bouton en bas à gauche
        int scaledSpacing = (int) (DEFAULT_BUTTON_SPACING * Math.min(scaleX, scaleY));
        backButton.positionAtBottomLeft(0, scaledSpacing, scaledSpacing);

        // Ajouter au JLayeredPane
        if (layeredPane != null) {
            layeredPane.add(backButton, JLayeredPane.PALETTE_LAYER);
        }
    }

    private void initButtons() {
        // Création des boutons de niveau
        for (int i = 1; i <= MAX_LEVELS; i++) {
            final int levelNumber = i;
            File levelFile = new File(Constants.Path.LEVEL + "Level" + i + ".txt");

            GameButton levelBtn;
            if (!levelController.isLevelUnlocked(i) || !levelFile.exists()) {
                levelBtn = new GameButton("spriteOpenLevel", 3, 0, 0,
                        DEFAULT_BUTTON_SIZE,
                        DEFAULT_BUTTON_SIZE,
                        22);
            } else {
                levelBtn = new GameButton("spriteOpenLevel", 3, 0, 0,
                        DEFAULT_BUTTON_SIZE,
                        DEFAULT_BUTTON_SIZE,
                        i - 1);
                levelBtn.addActionListener(e -> {
                    try {
                        levelController.setCurrentLevelNumber(levelNumber);
                        Level currentLevel = levelController.loadLevel();
                        levelController.setCurrentLevel(currentLevel);
                        gameController.restartGameLevel();
                        menuController.getWindow().update(menuController);
                        gameState.setCurrentState(GameMode.DISPOSING);
                    } catch (RuntimeException ex) {
                        System.out.println("Erreur lors du chargement du niveau : " + ex.getMessage());
                    }
                });
            }
            addButton(levelBtn);
            gridPanel.add(levelBtn);
        }

        levelSelectPanel.add(gridPanel, BorderLayout.CENTER);
    }

    @Override
    public void handleNavigation() {
        // La navigation est gérée par les boutons via leurs états
    }

    @Override
    public void applyTheme() {
        setBackground(Constants.Color.MENU_BG);
        setOpaque(true);
    }

    @Override
    public void handleResize() {
        updateScaling();
        updateLayout();

        if (controlButtons != null) {
            controlButtons.repositionButtons();
        }
    }

    /**
     * Rafraîchit l'état des boutons de niveau
     */
    public void refreshLevelButtons() {
        gridPanel.removeAll();
        buttons.clear();
        initButtons();
        createBackButton();
        revalidate();
        repaint();
    }
}