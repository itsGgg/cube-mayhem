package model.level;

import model.tools.Constants.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UserProgress implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userName;
    private int maxLevelUnlocked;

    public UserProgress(String userName, int maxLevelUnlocked) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'utilisateur ne peut pas être vide");
        }
        if (maxLevelUnlocked < 0) {
            throw new IllegalArgumentException("Le niveau maximum débloqué ne peut pas être négatif");
        }
        this.userName = userName;
        this.maxLevelUnlocked = maxLevelUnlocked;
    }

    /**
     * Méthode permettant de sauvegarder la progression du joueur
     * en utilisant la sérialisation
     */
    public void saveProgress() {
        JFileChooser fileChooser = new JFileChooser(Path.GAME_PROGRESS_DIRECTORY) {
            @Override
            public void approveSelection() {
                File selectedFile = getSelectedFile();

                // Vérifier si le fichier existe déjà
                if (selectedFile.exists()) {
                    int response = JOptionPane.showConfirmDialog(
                            this,
                            "Êtes-vous sûr de vouloir remplacer ce progrès? Vous ne pourrez pas revenir en arrière.",
                            "Confirmation de remplacement",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                super.approveSelection();
            }
        };
        // Configuration du JFileChooser
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogTitle("Sauvegarder la progression");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Fichiers sérialisés (*.ser)", "ser"));

        // Définir le nom de fichier par défaut avec le nom d'utilisateur
        fileChooser.setSelectedFile(new File(userName + ".ser"));

        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".ser")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".ser");
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(selectedFile))) {
                oos.writeObject(this);
                JOptionPane.showMessageDialog(
                        null,
                        "Progression sauvegardée avec succès!",
                        "Sauvegarde réussie",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Erreur lors de la sauvegarde: " + e.getMessage(),
                        "Erreur de sauvegarde",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Méthode permettant de recuperer le progres du joueur
     * en utilisant la deserialisation
     * 
     * @return le progres du joueur ou null si une erreur est survenue
     * @throws FileNotFoundException  si le fichier n'a pas été trouvé
     * @throws IOException            si une erreur est survenue lors de la lecture
     *                                du fichier
     * @throws ClassNotFoundException si le fichier n'est pas un fichier de
     *                                progression valide
     * @throws ClassCastException     si le fichier n'est pas un fichier de
     *                                progression valide
     */
    public static UserProgress loadProgress() {
        // faire appeler configureFileChooser pour recuperer le fichier
        File selectedFile = getSelectedFile();
        if (selectedFile == null) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
            return (UserProgress) ois.readObject();
        } catch (FileNotFoundException e) {
            showErrorDialog("Le fichier de sauvegarde n'a pas été trouvé");
        } catch (IOException e) {
            showErrorDialog("Erreur lors de la lecture du fichier de sauvegarde");
        } catch (ClassNotFoundException e) {
            showErrorDialog("Format de fichier invalide");
        } catch (ClassCastException e) {
            showErrorDialog("Le fichier de sauvegarde n'est pas un fichier de progression valide");
        }
        return null;
    }

    /**
     * Méthode permettant de recuperer le fichier selectionné par l'utilisateur
     * 
     * @return le fichier selectionné ou null si aucun fichier n'a été selectionné
     */
    private static File getSelectedFile() {
        JFileChooser fileChooser = LevelFileManager.configureFileChooser(Path.GAME_PROGRESS_DIRECTORY, "Load Progress",
                "ser",
                "Fichiers sérialisés (*.ser)");
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(null);
        return option == JFileChooser.APPROVE_OPTION
                ? fileChooser.getSelectedFile()
                : null;
    }

    /**
     * Méthode permettant d'afficher le message d'erreur en utilisant un JOptionPane
     * 
     * @param message
     */
    private static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null,
                message,
                "Loading Error",
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String toString() {
        return "Joueur : " + userName + "; Dèrnier niveau débloqué : " + maxLevelUnlocked;
    }

    /* Getters */
    public String getUserName() {
        return userName;
    }

    public int getMaxLevelUnlocked() {
        return maxLevelUnlocked;
    }

    /* Setter */

    public void setMaxLevelUnlocked(int maxLevelUnlocked) {
        this.maxLevelUnlocked = maxLevelUnlocked;
    }
}
