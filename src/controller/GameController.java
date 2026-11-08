package controller;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFileChooser;

import model.level.GameState;
import model.level.Level;
import model.level.UserProgress;
import model.level.LevelEditor;
import model.level.LevelFileManager;
import model.level.GameState.DisplayMode;
import model.level.GameState.GameMode;
import model.level.tiles.TileType;
import model.level.tiles.TileBank;
import model.tools.MusicBank;
import view.Window;

/**
 * Classe GameController : contrôleur principal du jeu
 * permet de gérer le déroulement du jeu
 */
public class GameController implements Runnable {
    private MenuController menuController;
    private CubeController cubeController;
    private LevelController levelController;
    private Level currentLevel;
    private TerminalController terminalController;
    private GameState gameState;// état du jeu ou fase du jeu
    private DisplayMode displayMode;// mode de jeu terminal, 2d ou 3d

    private boolean isRunning;
    private boolean isPaused;
    private Window window;
    private TileType selectedTileType = null; // stocke la tuile sélectionnée avec la souris
    private UserProgress userProgress;

    private final ReentrantLock gameLock = new ReentrantLock();
    private Thread gameThread;
    private Thread renderThread;
    // private Thread inputThread;
    // private Thread cubeThread;

    public GameController() throws IOException {
        // pour changer le mode on change ici et on commente dans setMenuController
        // this.displayMode = DisplayMode.GUI_2D;
        this.gameState = new GameState(GameMode.MAIN_SCREEN);
        this.isRunning = false;
        this.isPaused = false;

        // Charger les tuiles au démarrage
        TileBank.loadTiles("tiles");

        // Créer les répertoires si ils n'existent psa
        LevelFileManager.initializeDirectories();

        this.menuController = new MenuController(this);

        this.displayMode = menuController.getDisplayMode();
        this.gameState = menuController.getGameState();
        this.levelController = menuController.getLevelController();
        // Initialiser le premier niveau
        if (this.levelController.getCurrentLevel() != null) {
            this.currentLevel = this.levelController.getCurrentLevel();
            this.cubeController = new CubeController(currentLevel.getPlate(), gameState);
            /*
             * en cours de dev
             * if (gameState.getCurrentState() == GameMode.LEVEL_EDITOR) {
             * this.window = new Window(currentLevel, true);
             * }
             */
        }
        this.terminalController = new TerminalController(this);

        this.isRunning = false;
        this.isPaused = false;

        // Créer la fenêtre seulement si on n'est pas en mode terminal

        window = new Window(menuController);

    }

    public void handleNewGame() {
        this.terminalController.handleNewGame();
    }

    public void handleLoadGame() {
        this.terminalController.handleLoadGame();
    }

    public void handleMainScreen() {
        this.terminalController.handleMainScreen();
    }

    /*
     * Méthode permettant de gérer le mode d'affichage
     * 
     * @param mode Mode d'affichage à appliquer
     */
    public void handleDisplayMode(DisplayMode mode) {
        this.displayMode = mode;

        if (mode != DisplayMode.TERMINAL) {
            try {
                if (gameState.isMainDisplay()) {
                    // System.out.println("showDisplayModesMenu");
                    window.getMenuView().showDisplayModesMenu();
                    window.goToMenu();
                } else if (!gameState.isDisplayMode()) {
                    // System.out.println("Thread actuel dans handleDisplayMode : " +
                    // Thread.currentThread().getName());
                    // System.out.println("switchToDisplay");
                    if (window.panelIsEmpty(menuController)) {
                        window.switchToDisplay(menuController, false);
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void startGame() {
        isRunning = true;
        // Vérifier et initialiser le niveau si nécessaire
        if (currentLevel == null) {
            this.currentLevel = levelController.loadLevel();
            this.cubeController = new CubeController(currentLevel.getPlate(), gameState);
        }
        gameThread = new Thread(this);
        if (displayMode != DisplayMode.TERMINAL) {
            renderThread = new Thread(() -> {
                try {
                    renderLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            renderThread.start();
        }
        gameThread.start();
        MusicBank.play(100, true);
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!isPaused) {
                gameLock.lock();
                try {
                    this.gameState.setCurrentState(menuController.getGameState().getCurrentState());
                    menuController.update();
                    Thread.sleep(16); // Ajout d'un délai de 16ms pour limiter la fréquence à ~60 FPS
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gameLock.unlock();
            }
        }
    }

    private void renderLoop() throws IOException {
        while (isRunning) {
            try {
                if (!isPaused) {
                    synchronized (gameLock) {
                        if (window != null && currentLevel != null && displayMode != DisplayMode.MAIN_DISPLAY) {
                            if (gameState.isDisposing() || gameState.isExecuting() || gameState.isLevelEditorCreate()) {
                                window.update(menuController);
                            }
                        } else {
                            window.repaint();
                            window.revalidate();
                        }
                    }
                }

                // Calcul dynamique du temps de pause pour maintenir ~30 FPS
                long startTime = System.nanoTime();
                Thread.sleep(30);
                long endTime = System.nanoTime();
                long sleepTime = 30 - ((endTime - startTime) / 1_000_000);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (

            InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Erreur dans la boucle de rendu : " + e.getMessage());
                e.printStackTrace();
                // On continue la boucle malgré l'erreur
                continue;
            }
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
    }

    private void quit() {
        isRunning = false;
        try {
            gameThread.join();
            renderThread.join();
            // inputThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.exit(0);
    }

    public void handleDisposingInput() {
        this.terminalController.handleDisposingInput(this);
    }

    public void restartGameLevel() {
        if (this.gameState.getCurrentState() == GameMode.LEVEL_EDITOR_CREATE) {
            this.currentLevel.getPlate().resetEditorPlate();
        } else {
            this.terminalController.restartGameLevel(this);
            try {
                this.cubeController.restartCubePosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Méthode permettant de passer au niveau suivant
     */
    public void loadNextLevel() {
        this.levelController.loadNextLevel();
        this.currentLevel = levelController.getCurrentLevel();
        currentLevel.setDone(false);
        this.cubeController = new CubeController(currentLevel.getPlate(), gameState);

        this.gameState.setCurrentState(GameMode.DISPOSING);
    }

    public void handleExecutingInput() {

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.isEmpty())
            return;
        switch (input) {
            case "P" -> MusicBank.pause();
            case "O" -> MusicBank.resume();
            case "a" -> cubeController.move(currentLevel);
            case "A" -> cubeController.moveTillPossible(currentLevel, null);
            case "e" -> cubeController.info();
            case "f" -> {
                System.out.println("Vous avez quitté la " + ((GameMode) this.gameState.getCurrentState()).getName());
                this.gameState.setCurrentState(GameMode.DISPOSING);
            }
            case "M" -> this.gameState.setCurrentState(GameMode.LEVEL_SELECTION);
            case "R" -> restartGameLevel();
            case "q" -> System.exit(0);
            default -> {
                System.out.println("Veuillez insérer seulement la touche correspondante.");
            }
        }
    }

    void handleLevelCompletion() {
        if (!currentLevel.isDone()) {
            return;
        }
        if (levelController.getCurrentLevelNumber() == levelController.getMaxLevelUnlocked()) {
            levelController.increaseMaxLevelUnlocked();
            userProgress.setMaxLevelUnlocked(levelController.getMaxLevelUnlocked());
        }
        if (displayMode == DisplayMode.TERMINAL) {

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().toUpperCase();

            switch (input) {
                case "R":
                    restartGameLevel();
                    break;
                case "N":
                    loadNextLevel();
                    break;
                case "M":
                    this.gameState.setCurrentState(GameMode.LEVEL_SELECTION);
                    return;
                case "S":
                    userProgress.saveProgress();
                    return;
                case "Q":
                    System.exit(0);
                    break;
                default:
                    handleLevelCompletion(); // appel récursif
            }
        } else {
            // this.menuController.getGameState().setCurrentState(GameMode.LEVEL_SELECTION);
            // window.getMenuView().showLevelsMenu();
            // window.showWinMessage();
            menuController.handleWinMessage();

        }
    }

    public void handleLevelEditor() throws IOException {
        String[] options = { "Créer un nouveau niveau", "Jouer à un niveau créé" };
        int choix = JOptionPane.showOptionDialog(null, "Que voulez-vous faire ?", "Éditeur de niveaux",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        LevelEditor levelEditor = null;

        if (choix == JOptionPane.CLOSED_OPTION) {
            return;
        }

        if (choix == 0) {
            Integer[] dimensions = new Integer[20];
            for (int i = 0; i < dimensions.length; i++) {
                dimensions[i] = i + 1; // 1 à 20
            }

            JComboBox<Integer> IBox = new JComboBox<>(dimensions);
            JComboBox<Integer> JBox = new JComboBox<>(dimensions);

            JPanel panel = new JPanel();
            panel.add(new JLabel("Lignes :"));
            panel.add(IBox);
            panel.add(new JLabel("Colonnes :"));
            panel.add(JBox);

            int result = JOptionPane.showConfirmDialog(null, panel, "Choisissez les dimensions du plateau",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                int i = (Integer) IBox.getSelectedItem();
                int j = (Integer) JBox.getSelectedItem();

                levelEditor = new LevelEditor(i, j);
            } else {
                return;
            }
        } else if (choix == 1) { // Chargement d'un niveau déjà créé
            File directory = new File("SaveData/EditorLevels");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            JFileChooser fileChooser = new JFileChooser(directory);
            fileChooser.setDialogTitle("Choisir un niveau à charger");
            fileChooser.setAcceptAllFileFilterUsed(false); // empêche le filtre "Tous les fichiers"
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // que des fichiers
            fileChooser.setCurrentDirectory(directory); // démarre dans EditorLevels

            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {

                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Fichier sélectionné : " + selectedFile.getAbsolutePath());
                if (!selectedFile.exists()) {
                    JOptionPane.showMessageDialog(null, "Le fichier sélectionné n'existe pas !");
                    return;
                }

                try {
                    levelEditor = new LevelEditor(selectedFile.getPath());
                } catch (IOException | RuntimeException e) {
                    JOptionPane.showMessageDialog(null, "Erreur lors du chargement du niveau !");
                    e.printStackTrace();
                    return;
                }
            } else {
                return;
            }
        }

        this.currentLevel = levelEditor.getLevel();

        if (choix == 0) {
            gameState.setCurrentState(GameMode.LEVEL_EDITOR_CREATE);
            this.currentLevel.getPlate().setLevelEditor(true);
            if (menuController.getDisplayMode() == DisplayMode.GUI_2D) {
                window.initialisePanel2D(menuController, true);
                window.getPanel2D().setEditorMode(menuController);
            } else {
                // window.initialisePanel3D(menuController, true);
                // window.getPanel3D().setEditorMode(menuController);
            }
            window.switchToDisplay(menuController, true);

        } else if (choix == 1) {
            setLevelEverywhere(currentLevel);
            this.currentLevel.getPlate().setLevelEditor(false);
            gameState.setCurrentState(GameMode.DISPOSING);
            menuController.update();
            window.switchToDisplay(menuController, false);

        }

        levelController.setCurrentLevel(currentLevel);
        window.update(menuController);
    }

    public MenuController getMenuController() {
        return menuController;
    }

    public CubeController getCubeController() {
        return cubeController;
    }

    public void setCubeController(CubeController cubeController) {
        this.cubeController = cubeController;
    }

    public LevelController getLevelController() {
        return levelController;
    }

    public TerminalController getTerminalController() {
        return terminalController;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void setLevelEverywhere(Level level) {
        this.currentLevel = level;
        menuController.setCurrentLevel(level);
        levelController.setCurrentLevel(level);
        cubeController = new CubeController(level.getPlate(), gameState);
    }

    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public Window getWindow() {
        return window;
    }

    public UserProgress getUserProgress() {
        return userProgress;
    }

    public void setUserProgress(UserProgress userProgress) {
        this.userProgress = userProgress;
    }

    // public MusicBank getMusicBank() {
    // return null; // La classe est maintenant statique
    // }

    public ReentrantLock getGameLock() {
        return gameLock;
    }

    public GameState getGameState() {
        return gameState;
    }

}
