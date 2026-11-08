package controller;

import model.exceptions.WrongDimensionException;
import model.level.Cube;
import model.level.GameState;
import model.level.Level;
import model.level.GameState.DisplayMode;
import model.level.GameState.GameMode;
import model.level.plate.Case;
import model.level.plate.Plate;
import model.level.tiles.TileType;
import view.Window;

import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

public class CubeController {
    private Plate plate;
    private Cube cube;
    private GameState gameState;
    private DisplayMode displayMode;

    public CubeController(Plate plate, GameState gameState) {
        this.plate = plate;
        this.cube = plate.getCube();
        this.gameState = gameState;
        displayMode = gameState.getDisplayMode();
        this.cube.setGameState(gameState);
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public void info() {
        if (displayMode == DisplayMode.TERMINAL) {
            System.out.println(">>> Informations du cube :");
            System.out.println("i: " + cube.getI() + ", j: " + cube.getJ() + ", angle: " + cube.getAngle());
            try {
                cube.getCase().info(displayMode);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Direction: " + cube.getFacingDirection());
            System.out.println("Est arrivé? " + (cube.isArrived() ? "Oui" : "Non"));
        }
    }

    public void move(Level currentLevel) {
        try {
            cube.move();

            currentLevel.getPlate().print(displayMode);
            checkWin(currentLevel);

        } catch (WrongDimensionException e) {
            System.out.println("Le cube a quitté le plateau...");
            this.gameState.setCurrentState(GameMode.DISPOSING);
            restartCubePosition();
        }
    }

    private boolean shouldMove() {
        return cube.getCase() != null && !gameState.isDisposing()
                && (cube.getCase().getTile() == null || (cube.getCase().getTile().getTileType() != TileType.END));
    }

    public void moveTillPossible(Level currentLevel, Window window) {
        CountDownLatch latch = new CountDownLatch(1);
        int delay = 400;
        Timer timer = new Timer(delay, new ActionListener() { // Exécute toutes les 4ms
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((displayMode != DisplayMode.GUI_3D) || (displayMode == DisplayMode.GUI_3D
                        && window.getPanel3D().getGamePanel3D().getPlate().getGameCube3D().isAnimationFinished())) {
                    if (checkWin(currentLevel) || cube.getCase() == null || gameState.isDisposing()) {
                        ((Timer) e.getSource()).stop(); // Stoppe le timer quand le cube ne peut plus avancer
                        latch.countDown();
                        return;
                    }
                }

                if (shouldMove())
                    move(currentLevel); // Fait avancer le cube
            }
        });
        timer.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean checkWin(Level currentLevel) {
        if (cube.isArrived()) {
            // this.gameState.setCurrentState(GameMode.DISPOSING);
            currentLevel.setDone(true);
            return true;
        }
        return false;
    }

    /**
     * Méthode permettant de repositionner le cube à la case de départ
     */
    public void restartCubePosition() {
        Case startCase = plate.getStartCase();
        cube.setCase(startCase);
        cube.setI(startCase.getI());
        cube.setJ(startCase.getJ());
        cube.setFacingDirection(startCase.getDirection());
    }
}
