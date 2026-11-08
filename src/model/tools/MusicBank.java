package model.tools;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe gérant les musiques de fond
 *
 * Toutes les musiques proviennent du jeu Geometry Dash
 */
public class MusicBank {
    private static final Map<Integer, Clip> tracks = new HashMap<>();
    private static final Map<Integer, String> tracksNames = new HashMap<>();
    private static int currentTrackPlayed = 0;
    private static float musicVolume = 0.69f;
    private static float effectsVolume = 1.0f;
    private static boolean musicEnabled = true;
    private static boolean effectsEnabled = true;

    static {
        addTrack(1, "1_StereoMadness.wav");
        addTrack(2, "2_BackOnTrack.wav");
        addTrack(3, "3_Polargeist.wav");
        addTrack(4, "4_DryOut.wav");
        addTrack(5, "5_BaseAfterBase.wav");
        addTrack(6, "6_CantLetGo.wav");
        addTrack(7, "7_Jumper.wav");
        addTrack(8, "8_TimeMachine.wav");
        addTrack(11, "11_Clutterfunk.wav");
        addTrack(100, "100_PracticeMode.wav");
        // son de clic de bouton
        addTrack(101, "Minimalist1.wav");

        setVolume(musicVolume);
    }

    private static void initializeClip(int id, String filePath) {
        try {
            File musicFile = new File(filePath);
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
            Clip clip = AudioSystem.getClip();

            clip.open(audioInput);
            tracks.put(id, clip);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
            System.out.println("Impossible d'initialiser le fichier audio " + filePath + " : " + e.getMessage());
        }
    }

    private static void addTrack(int id, String filePath) {
        String fullPath = Constants.Path.SOUNDS + filePath;

        File file = new File(fullPath);

        if (!file.exists()) {
            System.out.println("Le fichier " + fullPath + " n'existe pas!");
            return;
        }
        tracksNames.put(id, filePath);
        initializeClip(id, fullPath);
    }

    /**
     * Active ou désactive la musique de fond
     * 
     * @param enabled true pour activer, false pour désactiver
     */
    public static void setMusicEnabled(boolean enabled) {
        // Si l'état ne change pas, ne rien faire
        if (musicEnabled == enabled) {
            return;
        }

        musicEnabled = enabled;
        if (!enabled) {
            // Mémoriser la piste en cours et la mettre en pause
            pause();
        } else {
            // Reprendre la lecture si une piste était en cours
            if (currentTrackPlayed != 0) {
                resume();
            } else {
                // Si aucune piste n'était en cours, jouer la piste par défaut
                play(100, true); // Piste PracticeMode par défaut
            }
        }
    }

    /**
     * Active ou désactive les effets sonores
     * 
     * @param enabled true pour activer, false pour désactiver
     */
    public static void setEffectsEnabled(boolean enabled) {
        // Si l'état ne change pas, ne rien faire
        if (effectsEnabled == enabled) {
            return;
        }

        effectsEnabled = enabled;
        // Pas besoin d'arrêter ou reprendre quoi que ce soit,
        // car les effets sont des sons courts joués ponctuellement
    }

    /**
     * @return true si la musique est activée
     */
    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * @return true si les effets sonores sont activés
     */
    public static boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    /**
     * Définit le volume de la musique de fond
     * 
     * @param volume Volume entre 0.0 et 1.0
     */
    public static void setVolume(float volume) {
        musicVolume = volume;
        if (musicEnabled) {
            tracks.values().forEach(track -> {
                if (track != null) {
                    FloatControl gainControl = (FloatControl) track.getControl(FloatControl.Type.MASTER_GAIN);
                    float min = gainControl.getMinimum();
                    float max = gainControl.getMaximum();
                    float dB = min + (max - min) * volume; // volume entre 0.0 et 1.0
                    gainControl.setValue(dB);
                }
            });
        }
    }

    /**
     * Définit le volume des effets sonores (clics, etc.)
     * 
     * @param volume Volume entre 0.0 et 1.0
     */
    public static void setEffectsVolume(float volume) {
        effectsVolume = volume;
    }

    public static synchronized void play(int id, boolean looping) {
        if (!tracks.containsKey(id)) {
            System.out.println("Track " + id + " pas trouvé...");
            return;
        }

        if (!musicEnabled) {
            return;
        }

        if (looping) {
            loop(id);
        } else {
            // verifie si une autre track est en cours et la stop
            if (currentTrackPlayed != 0 && currentTrackPlayed != id) {
                stop();
            }

            Clip clip = tracks.get(id);
            Thread musicThread = new Thread(() -> {
                try {
                    if (!clip.isRunning()) {
                        clip.setMicrosecondPosition(0);
                        clip.start();
                        currentTrackPlayed = id;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            musicThread.setName("MusicThread-" + id);
            musicThread.setDaemon(true);
            musicThread.start();
        }
    }

    /**
     * Joue un son en parallèle sans arrêter les autres sons
     * 
     * @param id ID du son à jouer
     */
    public static synchronized void playParallel(int id) {
        if (!tracks.containsKey(id)) {
            System.out.println("Track " + id + " pas trouvé...");
            return;
        }

        if (!effectsEnabled) {
            return;
        }

        Clip clip = tracks.get(id);
        Thread musicThread = new Thread(() -> {
            try {
                if (!clip.isRunning()) {
                    clip.setMicrosecondPosition(0);
                    // Applique le volume des effets pour les sons d'effets (ID >= 100)
                    if (id >= 100) {
                        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float min = gainControl.getMinimum();
                        float max = gainControl.getMaximum();
                        float dB = min + (max - min) * effectsVolume;
                        gainControl.setValue(dB);
                    }
                    clip.start();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        musicThread.setName("MusicParallelThread-" + id);
        musicThread.setDaemon(true);
        musicThread.start();
    }

    /**
     * Joue une piste en boucle
     * 
     * @param id ID de la piste à jouer
     */
    public static synchronized void loop(int id) {
        if (!tracks.containsKey(id)) {
            System.out.println("Track " + id + " pas trouvé...");
            return;
        }
        stop();

        Clip clip = tracks.get(id);
        Thread loopThread = new Thread(() -> {
            try {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                currentTrackPlayed = id;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        loopThread.setName("MusicLoopThread-" + id);
        loopThread.setDaemon(true);
        loopThread.start();
    }

    public static void pause() {
        if (currentTrackPlayed != 0) {
            Clip clip = tracks.get(currentTrackPlayed);

            if (clip.isRunning()) {
                clip.stop();
                System.out.println("La track " + tracksNames.get(currentTrackPlayed) + " a été mise en pause.");
            }
        }
    }

    public static void resume() {
        if (currentTrackPlayed != 0) {
            Clip clip = tracks.get(currentTrackPlayed);

            if (!clip.isRunning()) {
                clip.start();
                System.out.println("La track " + tracksNames.get(currentTrackPlayed) + " a repris.");
            }
        }
    }

    public static void stop() {
        if (currentTrackPlayed != 0) {
            Clip clip = tracks.get(currentTrackPlayed);

            clip.stop();
            clip.setMicrosecondPosition(0);
            currentTrackPlayed = 0;
        }
    }
}
