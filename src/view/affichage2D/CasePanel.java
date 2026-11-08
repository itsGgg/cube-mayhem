package view.affichage2D;

import model.level.Cube;
import model.level.plate.Case;
import model.level.plate.Plate;
import model.tools.Constants;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Cursor;
import java.io.IOException;
import java.awt.Color;
import java.awt.Graphics;

public class CasePanel extends JPanel {

    private Case c;
    private boolean isButton;
    private TileView tileView;
    private CubeView cubeView;
    private Plate plate;

    public TileView getTileView() {
        return tileView;
    }

    public CasePanel(Case c, Plate plate) {
        super();
        this.c = c;
        this.plate = plate;
        this.isButton = false;
        String couleur = Constants.Case.colors[c.getCaseType().getId()];
        Color color = getColorFromString(couleur);
        if (c.getCaseType().getId() == 1) {
            this.isButton = true;
        }
        setBackground(color);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Constants.Tile.SIZE, Constants.Tile.SIZE));

        if (isButton) {
            setBorder(BorderFactory.createRaisedBevelBorder());
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        try {
            updateTileAndCube();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setVisible(true);

    }

    public CasePanel(Case c, boolean isButton, Plate plate) throws IOException {
        this(c, plate);
        this.isButton = isButton;
        updateTileAndCube();
        setVisible(true);
    }

    public static Color getColorFromString(String colorName) {
        try {
            // Récupération du champ statique correspondant au nom de la couleur
            return (Color) Color.class.getField(colorName.toUpperCase()).get(null);
        } catch (Exception e) {
            // Gérer les cas où la couleur n'existe pas
            System.out.println("Couleur inconnue : " + colorName);
            return null;
        }
    }

    public void updateTileAndCube() throws IOException {
        removeAll(); // on supprime l'ancienne TileView
        tileView = null; // Réinitialiser la référence
        cubeView = null;

        if (!c.isEmpty()) {
            tileView = new TileView(c.getTile());
            add(tileView, BorderLayout.CENTER);
        }

        Cube cube = plate.getCube();
        if (cube != null && cube.isOnCase(c)) {
            cubeView = new CubeView(cube);
            add(cubeView, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isButton) {
            if (getMousePosition() != null) {
                setBorder(BorderFactory.createLoweredBevelBorder());
            } else {
                setBorder(BorderFactory.createRaisedBevelBorder());
            }
        }
    }

    public boolean isButton() {
        return isButton;
    }

    public void updateButton(boolean isButton) {
        this.isButton = isButton;
    }

    public Case getCase() {
        return c;
    }
}
