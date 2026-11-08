package view.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import controller.MenuController;
import model.UI.GameButton;
import model.level.GameState.DisplayMode;
import model.level.GameState.GameMode;
import view.Window;
import model.tools.MusicBank;
import model.level.GameState;
import model.level.tiles.TileType;

/**
 * Classe gérant les boutons de contrôle communs à tous les menus
 */
public class ControlButtons {
    private MenuController menuController;
    private Component parent;
    private GameButton exitButton, resetButton, solutionButton;
    private GameButton deleteProgressButton, musicButton, soundButton;
    private JPanel homePanel, extrasPanel, levelsPanel;
    private JPanel panel;
    private boolean isMenuControls = false;
    private boolean is3DMode = false;
    private int buttonSize = 44;
    private int spacing = 20;
    private List<GameButton> buttons;

    public ControlButtons(MenuController menuController, Component parent) {
        if (menuController == null || parent == null) {
            throw new IllegalArgumentException("MenuController et parent ne peuvent pas être null");
        }

        this.menuController = menuController;
        this.parent = parent;
        this.buttons = new ArrayList<>();
        this.panel = (JPanel) parent;

        // Déterminer si on est en mode 3D
        // this.is3DMode = panel.getParent() instanceof view.affichage3D.Panel3D;

        Object currentState = menuController.getGameState().getCurrentState();
        if (currentState == GameMode.DISPOSING ||
                currentState == GameMode.LEVEL_EDITOR ||
                currentState == GameMode.LEVEL_EDITOR_CREATE) {
            addLevelControlButtons();
        } else {
            addMenuControlButtons();
        }

        // resize();
    }

    public void addLevelControlButtons() {
        // Boutons de jeu (exit, reset, solution)
        exitButton = new GameButton("spriteControlButtons", 3, 0, 0, buttonSize, buttonSize, 2, GameMode.MAIN_SCREEN);
        resetButton = new GameButton("spriteControlButtons", 3, 0, 0, buttonSize, buttonSize, 3, GameMode.DISPOSING);
        solutionButton = new GameButton("spriteControlButtons", 3, 0, 0, buttonSize, buttonSize, 4, GameMode.DISPOSING);

        // En 3D, les boutons sont en haut à droite
        if (is3DMode) {
            exitButton.positionAtTopRight(0, spacing, 20);
            resetButton.positionAtTopRight(1, spacing, 20);
            solutionButton.positionAtTopRight(2, spacing, 20);
        } else {
            // En 2D, les boutons sont en bas à gauche du panneau de contrôle
            positionLevelButtons2D();
        }

        // Ajout des boutons directement au panneau
        panel.add(exitButton);
        panel.add(resetButton);
        panel.add(solutionButton);

        // Gestionnaire d'événements pour les boutons de contrôle
        exitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("pressed");
                exitButton.update();
                exitButton.animate();
                Object currentState = menuController.getGameState().getCurrentState();
                if (currentState == GameMode.DISPOSING || currentState == GameMode.LEVEL_EDITOR_CREATE) {
                    // Si une progression est déjà chargée, revenir à la liste des niveaux
                    // plutôt qu'à l'écran d'accueil initial
                    boolean hasLoadedProgress = currentState == GameMode.DISPOSING
                            && menuController.getGameController().getUserProgress() != null;
                    menuController.getGameState().setCurrentState(
                            hasLoadedProgress ? GameMode.LEVEL_SELECTION : GameMode.MAIN_SCREEN);
                    menuController.getWindow().goToMenu();
                }
            }

        });

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("pressed reset");
                resetButton.update();
                resetButton.animate();
                if (menuController.getGameState().getCurrentState() == GameMode.DISPOSING ||
                        menuController.getGameState().getCurrentState() == GameMode.LEVEL_EDITOR_CREATE) {
                    menuController.getGameController().restartGameLevel();
                }
            }

        });
        solutionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                solutionButton.update();
                solutionButton.animate();
                if (menuController.getGameState().getCurrentState() == GameMode.DISPOSING) {
                    Map<TileType, Integer> availableTiles = menuController.getGameController().getLevelController()
                            .placeTheSolution();
                    // Mettre à jour l'inventaire avec les nouvelles valeurs
                    if (menuController.getWindow().getPanel2D() != null) {
                        menuController.getWindow().getPanel2D().getInventoryPanel().updateInventory(availableTiles);
                    }
                }
            }


        });

        // Ajout d'un listener de redimensionnement au panneau
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resize();
                repositionButtons();
            }
        });
    }

    public void addMenuControlButtons() {
        if (isMenuControls)
            return; // Éviter la duplication

        isMenuControls = true;
        buttonSize = 44;

        // Création des boutons avec position initiale
        deleteProgressButton = new GameButton("spriteControlButtons", 2, 0, 0, buttonSize, buttonSize, 5);
        musicButton = new GameButton("spriteControlButtons", 2, 0, 0, buttonSize, buttonSize, 6);
        soundButton = new GameButton("spriteControlButtons", 2, 0, 0, buttonSize, buttonSize, 7);

        // Positionnement dans le coin inférieur droit
        deleteProgressButton.positionAtBottomRight(2, spacing, 20);
        musicButton.positionAtBottomRight(1, spacing, 20);
        soundButton.positionAtBottomRight(0, spacing, 20);

        // Initialiser l'état visuel des boutons
        updateMusicButtonState();
        updateEffectsButtonState();

        // Ajout des boutons directement au panneau
        panel.add(deleteProgressButton);
        panel.add(musicButton);
        panel.add(soundButton);

        // Ajout des listeners aux boutons
        deleteProgressButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                deleteProgressButton.update();
                deleteProgressButton.animate();
                // TODO

                System.out.println("Suppression de la progression");
            }
        });

        musicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicButton.update();
                musicButton.animate();
                boolean newState = !MusicBank.isMusicEnabled();
                MusicBank.setMusicEnabled(newState);
                updateMusicButtonState();
            }
        });

        soundButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                soundButton.update();
                soundButton.animate();
                boolean newState = !MusicBank.isEffectsEnabled();
                MusicBank.setEffectsEnabled(newState);
                updateEffectsButtonState();
            }

        });
        repositionButtons();
        // Ajout d'un listener de redimensionnement au panneau
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButtons();
            }
        });

        // Enregistrement du panneau
        if (panel instanceof HomeMenu) {
            homePanel = panel;
        } else if (panel instanceof ExtrasMenu) {
            extrasPanel = panel;
        } else if (panel instanceof LevelsMenu) {
            levelsPanel = panel;
        }
    }

    public void repositionButtons() {
        if (isMenuControls && deleteProgressButton != null && musicButton != null && soundButton != null) {
            deleteProgressButton.positionAtBottomRight(2, spacing, 20);
            musicButton.positionAtBottomRight(1, spacing, 20);
            soundButton.positionAtBottomRight(0, spacing, 20);

            // Mettre à jour l'état visuel des boutons
            updateControlButtonsState();
        } else if (!isMenuControls && exitButton != null && resetButton != null && solutionButton != null) {
            if (is3DMode) {
                exitButton.positionAtTopLeft(0, spacing, 20);
                resetButton.positionAtTopLeft(1, spacing, 20);
                solutionButton.positionAtTopLeft(2, spacing, 20);
            } else {
                positionLevelButtons2D();
            }
        }
    }

    /**
     * Positionne exit/reset/solution en bas à gauche du panneau de contrôle
     * lui-même (et non de la fenêtre entière : en 2D ce panneau est une bande
     * réduite en haut de l'écran, donc une position relative à la fenêtre les
     * ferait sortir de ses limites et seraient rognés/écrasés par le FlowLayout).
     */
    private void positionLevelButtons2D() {
        int panelHeight = panel.getHeight() > 0 ? panel.getHeight() : 90;
        int y = panelHeight - buttonSize - 10;
        exitButton.updatePosition(spacing, y);
        resetButton.updatePosition(spacing + (buttonSize + spacing), y);
        solutionButton.updatePosition(spacing + 2 * (buttonSize + spacing), y);
    }

    public void resize() {
        if (!isMenuControls) {
            //
        } else {
            int x = (int) Window.width - 300;
            int y = 30;
            panel.setBounds(x, y, 300, 50);
            panel.setPreferredSize(new Dimension(300, 50));

            // Redimensionner aussi les boutons du menu
            float scaleX = (float) Window.width / 800;
            float scaleY = (float) Window.height / 600;

            if (deleteProgressButton != null)
                deleteProgressButton.resize(scaleX, scaleY);
            if (musicButton != null)
                musicButton.resize(scaleX, scaleY);
            if (soundButton != null)
                soundButton.resize(scaleX, scaleY);
        }
    }

    /**
     * Met à jour l'apparence du bouton de musique selon son état
     */
    private void updateMusicButtonState() {
        if (musicButton != null) {
            if (MusicBank.isMusicEnabled()) {
                musicButton.setIndex(0);
            } else {
                musicButton.setIndex(1);
            }
            musicButton.updateIcon();
        }
    }

    /**
     * Met à jour l'apparence du bouton d'effets selon son état
     */
    private void updateEffectsButtonState() {
        if (soundButton != null) {
            if (MusicBank.isEffectsEnabled()) {
                soundButton.setIndex(0);
            } else {
                soundButton.setIndex(1);
            }
            soundButton.updateIcon();
        }
    }

    /**
     * Met à jour l'état visuel des boutons de contrôle selon leur état respectif
     */
    private void updateControlButtonsState() {
        updateMusicButtonState();
        updateEffectsButtonState();
    }

    /**
     * @return Le bouton de contrôle de la musique
     */
    public GameButton getMusicButton() {
        return musicButton;
    }

    /**
     * @return Le bouton de contrôle des effets sonores
     */
    public GameButton getEffectsButton() {
        return soundButton;
    }

    /**
     * Nettoie les ressources des boutons de contrôle
     */
    public void cleanup() {
        if (buttons != null) {
            buttons.clear();
        }
    }
}
