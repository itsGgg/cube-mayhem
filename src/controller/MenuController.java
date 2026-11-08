package controller;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import controller.phases.DisposingTerminal;
import controller.phases.ExecutingTerminal;
import model.level.GameState;
import model.level.Level;
import model.level.plate.Case;
import model.level.GameState.DisplayMode;
import model.level.GameState.GameMode;
import model.tools.MusicBank;
import view.Window;
import view.affichage3D.polygone.GameCube3D;
import view.affichage3D.polygone.Tile3D;

/* 
 * Cette classe est le contrôleur du menu
 * Elle gère le menu, les changement de statue
 * Elle soccupe de la gestion des entrées de window
 */
public class MenuController {
    private GameController gameController;
    private LevelController levelController;
    private GameState gameState;
    public DisplayMode displayMode;
    private Level currentLevel;
    private Window window;

    public MenuController(GameController gameController) {
        this.gameController = gameController;
        this.gameState = new GameState(DisplayMode.MAIN_DISPLAY, DisplayMode.MAIN_DISPLAY);
        this.levelController = new LevelController(gameState);
        this.currentLevel = levelController.getCurrentLevel();
        this.displayMode = gameState.getDisplayMode();
        // this.window = gameController.getWindow();
    }

    public GameController getGameController() {
        return gameController;
    }

    public LevelController getLevelController() {
        return levelController;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Window getWindow() {
        return gameController.getWindow();
    }

    void update() {
        if (window == null)
            window = getWindow();
        // 1. Mise à jour du jeu selon l'état
        if (!gameState.isDisplayMode()) {
            switch ((GameMode) gameState.getCurrentState()) {
                case MAIN_SCREEN -> {
                    if (displayMode == DisplayMode.TERMINAL) {
                        gameController.handleMainScreen();
                        this.gameState.setCurrentState(GameMode.LEVEL_SELECTION);
                    } else {
                        window.getMenuView().showHomeMenu();
                    }
                    // Si on est en mode graphique, on reste dans le menu GameMode
                }

                case LEVEL_SELECTION -> {
                    if (displayMode == DisplayMode.TERMINAL) {
                        handleLevelSelection();
                    } else {
                        // En mode graphique, on gère la sélection de niveau différemment
                        // handleGraphicLevelSelection();
                        window.getMenuView().showLevelsMenu();
                        // System.out.println("level selection");
                    }
                }

                case LEVEL_EDITOR -> {
                    try {
                        gameController.handleLevelEditor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                case DISPOSING -> {
                    try {
                        // Resynchroniser le niveau courant (nécessaire après un changement de niveau
                        // depuis le menu de sélection, sinon la victoire est vérifiée sur l'ancien niveau)
                        this.currentLevel = levelController.getCurrentLevel();
                        // System.out.println("disposing");
                        // musicBank.play(currentLevel.getLevelNumber(), false); Bug du au multi Thread?
                        if (displayMode == DisplayMode.TERMINAL) {
                            MusicBank.play(currentLevel.getLevelNumber(), true);
                            new DisposingTerminal(this).send();
                        }
                        handleDisplayMode(displayMode);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                case EXECUTING -> {
                    if (displayMode == DisplayMode.TERMINAL) {
                        new ExecutingTerminal(this).send();
                    } else {
                        gameController.getCubeController().moveTillPossible(currentLevel, window);
                    }
                }
                case SAVE_LOAD -> {
                    gameController.handleLoadGame();
                }
                case NEW_GAME -> {
                    if (displayMode == DisplayMode.TERMINAL) {
                        gameController.handleNewGame();
                    } else {
                        window.getMenuView().showNewGamePanel();
                    }
                }

                case EXTRAS -> {
                    gameState.setCurrentState(GameMode.EXTRAS);
                    window.getMenuView().showExtrasMenu();
                    // System.out.println("showExtrasMenu appelé");
                }

                case CREDITS -> {
                    window.getMenuView().showCreditsMenu();
                }
                case QUIT -> {
                    if (gameController.getUserProgress() != null)
                        gameController.getUserProgress().saveProgress();
                    System.exit(0);
                }
                default -> {
                    // System.exit(0);
                }
            }
        } else if (gameState.isMainDisplay()) {
            switch (displayMode) {
                case TERMINAL -> {
                    gameController.handleDisplayMode(displayMode);
                    gameState.setCurrentState(GameMode.MAIN_SCREEN);
                    this.getGameController().getWindow().dispose();
                }
                case GUI_2D, GUI_3D -> {
                    gameController.handleDisplayMode(displayMode);
                    gameState.setCurrentState(GameMode.MAIN_SCREEN);
                    window.getMenuView().showHomeMenu();
                }
                default -> {
                    gameController.handleDisplayMode(displayMode);
                }
            }
        }

        // Vérification de la victoire (uniquement pendant une partie en cours, pour
        // éviter de re-déclencher le message de victoire une fois revenu au menu)
        if ((gameState.isDisposing() || gameState.isExecuting()) && levelController.isLevelComplete(currentLevel)) {
            if (gameController.getCubeController().checkWin(currentLevel)) {
                if (displayMode == DisplayMode.TERMINAL) {
                    System.out.println("Bravo tu as fini ce niveau !");
                    levelCompletionPhase();
                } else {
                    handleWinMessage();
                }
                MusicBank.stop();
                gameController.handleLevelCompletion();
            }
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode mode) {
        gameState.setDisplayMode(mode);
        this.displayMode = mode;

        // Propager le changement aux autres contrôleurs
        if (gameController != null) {
            gameController.handleDisplayMode(mode);
            if (gameController.getCubeController() != null) {
                gameController.getCubeController().setDisplayMode(mode);
            }
        }

        if (levelController != null) {
            levelController.setDisplayMode(mode);
        }
    }

    public void handleDisplayMode(DisplayMode mode) {
        gameController.handleDisplayMode(mode);
    }

    /**
     * Méthode permettant au joueur de choisir le niveau qu'il veut jouer
     * 
     * @throws NumberFormatException si la chaîne posé par le joueur n'est pas un
     *                               entier
     */
    public void handleLevelSelection() {
        if (displayMode != DisplayMode.TERMINAL) {// on remplace par le Mode
            return;
        }

        if (levelController == null) {
            System.err.println("Erreur: LevelController n'est pas initialisé");
            return;
        }
        System.out.println("---------------------------------");
        System.out.println(gameController.getUserProgress());
        System.out.println("Liste des niveaux disponibles : ");
        for (int i = 1; i <= levelController.getMaxLevels(); i++) {
            System.out.println("Level " + i);
        }
        System.out.println(
                "Veuillez insérez le numéro de niveau que vous vouliez jouer, [S] pour sauvegarder votre progression ou [Q] pour quitter le jeu");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine().toUpperCase();
        switch (input) {
            case "S":
                gameController.getUserProgress().saveProgress();
                System.out.println("Progression sauvegardée avec succès !");
                break;
            case "Q":
                System.exit(0);
                break;
            default:
                try {
                    int level_chosen = Integer.parseInt(input);
                    if (level_chosen > levelController.getMaxLevelUnlocked()) {
                        System.out.println("Vous ne pouvez pas accedéer à ce niveau.");
                    } else {
                        // Mise a jour du niveau choisi
                        System.out.println("currentLevel avant:" + levelController.getCurrentLevelNumber());
                        levelController.setCurrentLevelNumber(level_chosen);
                        System.out.println("currentLevel avant:" + levelController.getCurrentLevelNumber());

                        currentLevel = levelController.loadLevel();
                        this.gameState.setCurrentState(GameMode.DISPOSING);
                        levelController.setCurrentLevel(currentLevel);
                        gameController.setCurrentLevel(currentLevel);
                        gameController.setCubeController(new CubeController(currentLevel.getPlate(), gameState));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrée invalide. Veuillez réesayer.");
                    System.out.println("---------------------------------");
                    handleLevelSelection();
                    return;
                }
        }

    }

    private void levelCompletionPhase() {
        if (!currentLevel.isDone())
            return;
        System.out.println("[R] Redémarrer le niveau");
        System.out.println("[N] Niveau suivant");
        System.out.println("[M] Revenir au Menu");
        System.out.println("[S] Sauvegarder la progression");
        System.out.println("[Q] Quitter le jeu");
        System.out.print("Votre action : ");
        gameController.handleLevelCompletion();
        System.out.println(("---------------------------------"));
    }

    public Level getCurrentLevel() {
        currentLevel = levelController.getCurrentLevel();
        return currentLevel;
    }

    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
        gameController.setCurrentLevel(currentLevel);
    }

    /**
     * Gère l'affichage du message de victoire
     */
    public void handleWinMessage() {
        if (displayMode != DisplayMode.TERMINAL) {
            window.showWinMessage();
        }
    }

    /**
     * Gère la fermeture du message de victoire
     */
    public void hideWinMessage() {
        if (displayMode != DisplayMode.TERMINAL) {
            window.hideWinMessage();

            // Supprimer l'instance dans MenuView
            // window.getMenuView().hideWinMessage();

            // // Mettre à jour l'état du jeu
            // gameState.setCurrentState(GameMode.LEVEL_SELECTION);

            // window.getMenuView().getLevelsMenu().refreshLevelButtons();
        }
    }

}
