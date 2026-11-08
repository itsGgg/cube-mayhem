package controller.listeners3D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import model.level.plate.Case;
import model.level.tiles.TileType;
import model.tools.Direction;
import view.affichage3D.Inventaire3D;
import view.affichage3D.Panel3D;
import view.affichage3D.Plate3D;
import view.affichage3D.polygone.Tile3D;

public class MouseListener3D implements MouseMotionListener, MouseListener {

    private Plate3D plate;
    private ArrayList<Tile3D> tiles;

    public MouseListener3D(Plate3D plate) {
        this.plate = plate;
        this.tiles = plate.getTiles3D();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (Inventaire3D.getSelectedTile() != null) {
            for (Tile3D t : tiles)
                t.mouseEntered(e.getX(), e.getY(), Inventaire3D.getSelectedTile());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Tile3D currentTile = Inventaire3D.getCurrentTile();
        if (currentTile == null) {
            Case currentCase;
            for (Tile3D t : tiles) {

                currentCase = t.mouseClicked(e.getX(), e.getY());
                if (currentCase != null) {

                    if (currentCase.getTile() == null && Inventaire3D.getSelectedTile() != null) {
                        if (Inventaire3D.getSelectedTile() == TileType.TELEPORTATION
                                || Inventaire3D.getSelectedTile() == TileType.SPIN) {
                            Panel3D.getCurrentLevel().getPlate().place(Inventaire3D.getSelectedTile(),
                                    t.getCase().getI(), t.getCase().getJ(), Direction.EAST,
                                    Panel3D.getMenuController().getGameState().getDisplayMode());
                            plate.addAnimationToQueue(t);
                        } else {
                            Inventaire3D.setCurrentTile(t);
                            t.setDuringPlacing(true);
                        }
                    } else if (Panel3D.getCurrentLevel().getPlate().getCase(currentCase.getI(), currentCase.getJ())
                            .getTile() != null) {
                        Panel3D.getCurrentLevel().getPlate().removeTile(currentCase.getI(), currentCase.getJ());
                    }
                    Plate3D.staticLayerNeedsUpdate = true;
                    return;
                }
            }
        } else {
            Direction dir = currentTile.getDirArrows().mouseClicked(e.getX(), e.getY());
            if (dir != null) {
                Panel3D.getCurrentLevel().getPlate().place(Inventaire3D.getSelectedTile(), currentTile.getCase().getI(),
                        currentTile.getCase().getJ(), dir, Panel3D.getMenuController().getGameState().getDisplayMode());
                plate.addAnimationToQueue(currentTile);
            } else {
                currentTile.setDuringPlacing(false);
                currentTile.removeTile();
            }
            Inventaire3D.setCurrentTile(null);
            Plate3D.staticLayerNeedsUpdate = true;
        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
