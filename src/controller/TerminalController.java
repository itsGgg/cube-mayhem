package controller;

import model.level.GameState;
import model.level.UserProgress;
import model.tools.Constants;
import model.tools.MusicBank;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JLabel;

public class TerminalController {
    private GameController gameController;
    private LevelController levelController;

    public TerminalController(GameController gameController) {
        this.gameController = gameController;
        this.levelController = gameController.getLevelController();
    }

    /**
     * Méthode permettant de vérifier si le nom d'utilisateur est valide
     *
     * @param username
     * @return true si nom d'utilisateur valide, false sinon
     */
    public boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]+$") && !username.isEmpty();
    }

    /**
     * Méthode permettant de gérer le menu principal
     */
    void handleMainScreen() {
        System.out.println("Bienvenue dans");
        System.out.println(Constants.Strings.GAME_ASCII);

        while (true) {
            System.out.println(
                    "Veuillez insérer votre nom pour continuer, appuyez sur [L] pour charger une sauvegarde ou [Q] pour quitter le jeu");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "Q":
                    System.exit(0);
                    break;
                case "L":
                    gameController.setUserProgress(UserProgress.loadProgress());

                    if (gameController.getUserProgress() != null) {
                        System.out.println(
                                "Progression chargée pour : " + gameController.getUserProgress().getUserName());
                        levelController.setMaxLevelUnlocked(gameController.getUserProgress().getMaxLevelUnlocked());
                        return;

                    } else {
                        System.out.println("Échec du chargement de la progression. Veuillez réessayer.");
                    }
                    break;
                default:
                    if (isValidUsername(input)) {
                        gameController.setUserProgress(new UserProgress(input, 1));
                        System.out.println("Nouveau joueur créé : " + input);
                        return;
                    } else {
                        System.out.println(
                                "Nom d'utilisateur invalide. Veuillez utiliser uniquement des lettres, des chiffres et des tirets bas (_).");
                    }
                    break;
            }
        }
    }

    /**
     * Crée un panneau de base pour les dialogues de jeu
     *
     * @return JPanel configuré avec GridBagLayout
     */
    private JPanel createBasePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return panel;
    }

    /**
     * Méthode permettant de créer un nouveau joueur
     */
    public void handleNewGame() {
        JPanel panel = createBasePanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Champ de texte pour le nom d'utilisateur
        JTextField usernameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nom d'utilisateur:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Boutons OK et Annuler
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Annuler");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // Création de la boîte de dialogue
        JDialog dialog = new JDialog(gameController.getWindow(), "Nouveau joueur", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(gameController.getWindow());

        // Gestionnaire d'événements pour le bouton OK
        okButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (isValidUsername(username)) {
                UserProgress progress = new UserProgress(username, 1);
                gameController.setUserProgress(progress);
                levelController.setMaxLevelUnlocked(progress.getMaxLevelUnlocked());
                JOptionPane.showMessageDialog(dialog, "Nouveau joueur créé : " + username);
                gameController.getMenuController().getGameState().setCurrentState(GameState.GameMode.LEVEL_SELECTION);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Nom d'utilisateur invalide. Veuillez utiliser uniquement des lettres, des chiffres et des tirets bas (_).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Gestionnaire d'événements pour le bouton Annuler
        cancelButton.addActionListener(e -> {
            dialog.dispose();
            gameController.getGameState().setCurrentState(GameState.GameMode.MAIN_SCREEN);
        });

        // Afficher la boîte de dialogue
        dialog.setVisible(true);
    }

    /**
     * Méthode permettant de charger une sauvegarde
     */
    public void handleLoadGame() {
        JPanel panel = createBasePanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Champ de texte pour le nom d'utilisateur
        JTextField usernameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nom d'utilisateur:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Création de la boîte de dialogue
        JDialog dialog = new JDialog(gameController.getWindow(), "Menu de sauvegarde", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(gameController.getWindow());

        gameController.setUserProgress(UserProgress.loadProgress());
        if (gameController.getUserProgress() != null) {
            JOptionPane.showMessageDialog(dialog,
                    "Progression chargée pour : " + gameController.getUserProgress().getUserName());
            levelController.setMaxLevelUnlocked(gameController.getUserProgress().getMaxLevelUnlocked());
            gameController.getMenuController().getGameState().setCurrentState(GameState.GameMode.LEVEL_SELECTION);
            dialog.dispose();

        } else {
            JOptionPane.showMessageDialog(dialog, "Échec du chargement de la progression. Veuillez réessayer.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            dialog.dispose();
            gameController.getGameState().setCurrentState(GameState.GameMode.MAIN_SCREEN);
        }
    }

    public void handleDisposingInput(GameController gameController) {
        Scanner scanner = new Scanner(System.in);
        String inputDisposing = scanner.nextLine();
        if (inputDisposing.isEmpty())
            return;
        switch (inputDisposing) {
            case "P" -> MusicBank.pause();
            case "O" -> MusicBank.resume();
            case "a" -> gameController.getCurrentLevel().getPlate().print(gameController.getDisplayMode());
            case "d" -> {
                levelController.placeTile(scanner);
                gameController.getGameLock().lock();
            }
            case "r" -> levelController.removeTile(scanner);
            case "M" -> {
                gameController.getGameState().setCurrentState(GameState.GameMode.LEVEL_SELECTION);
                MusicBank.stop();
            }
            case "R" -> {
                restartGameLevel(gameController);
                gameController.getGameState().setCurrentState(GameState.GameMode.DISPOSING);
                MusicBank.stop();
            }
            case "i" -> levelController.info(scanner);
            case "s" -> levelController.placeTheSolution();
            case "f" -> {
                System.out.println("Vous avez quitté la "
                        + ((GameState.GameMode) gameController.getGameState().getCurrentState()).getName());
                gameController.getGameState().setCurrentState(GameState.GameMode.EXECUTING);
            }
            case "q" -> System.exit(0);
            default -> System.out.println("Veuillez insérer seulement la touche correspondante.");
        }
    }

    /**
     * Méthode permettant de rédemarrer le niveau actuel
     *
     * @throws IOException
     */
    public void restartGameLevel(GameController gameController) {
        this.levelController.restartLevel();
        gameController.setCurrentLevel(levelController.getCurrentLevel());
        gameController.getCurrentLevel().setDone(false);
        gameController.setCubeController(
                new CubeController(gameController.getCurrentLevel().getPlate(), gameController.getGameState()));
        // this.window.updateView(currentLevel);
        // this.window3D.restart(currentLevel);
    }
}
