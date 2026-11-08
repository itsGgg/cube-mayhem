package model.level;

import java.io.Serializable;

/**
 * Classe répresentant les divers états du jeu.
 * Elle permet de gérer et adapter le déroulement du jeu à travers un enum
 * {@link GameMode}
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum DisplayMode {
        TERMINAL, // 0
        GUI_2D, // 1
        GUI_3D, // 2
        MAIN_DISPLAY; // 3

        private final String[] names = { "Mode Terminal", "Mode 2D", "Mode 3D", "Menu principal" };

        public String getName() {
            return names[this.ordinal()];
        }

    }

    public enum GameMode {
        MAIN_SCREEN("Menu Principal"),
        LEVEL_SELECTION("Sélection de Niveau"),
        DISPOSING("Phase de Disposition"),
        EXECUTING("Phase d'Exécution"),
        LEVEL_EDITOR("Éditeur de Niveau"),
        LEVEL_EDITOR_CREATE("Création de Niveau"),
        SAVE_LOAD("Chargement/Sauvegarde"),
        NEW_GAME("Nouvelle Partie"),
        EXTRAS("Extras"),
        SPEED_RUN("Speed Run"),
        CREDITS("Crédits"),
        QUIT("Quitter");

        private String name;

        GameMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private Object currentState;
    private DisplayMode displayMode;

    public GameState(Object initialState) {
        this.currentState = initialState;
    }

    public GameState(Object initialState, DisplayMode displayMode) {
        this(initialState);
        this.displayMode = displayMode;
    }

    public Object getCurrentState() {
        return currentState;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public void setCurrentState(Object newState) {
        this.currentState = newState;
    }

    public boolean isMainDisplay() {
        return this.currentState == DisplayMode.MAIN_DISPLAY;
    }

    public Boolean isDisplayMode() {
        return currentState instanceof DisplayMode;
    }

    /**
     * Méthodes permettant de vérfier l'état actuel du jeu
     */

    public boolean isMainScreen() {
        return this.currentState == GameMode.MAIN_SCREEN;
    }

    public boolean isLevelSelection() {
        return this.currentState == GameMode.LEVEL_SELECTION;
    }

    public boolean isLevelEditor() {
        return this.currentState == GameMode.LEVEL_EDITOR;
    }

    public boolean isControls() {
        return this.currentState == GameMode.EXTRAS;
    }

    public boolean isDisposing() {
        return currentState == GameMode.DISPOSING;
    }

    public boolean isExecuting() {
        return currentState == GameMode.EXECUTING;
    }

    public boolean isLevelEditorCreate() {
        return currentState == GameMode.LEVEL_EDITOR_CREATE;
    }

}
