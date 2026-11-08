package view.affichage3D;

import java.awt.geom.AffineTransform;

public class Transformation3D {

    /** La matrice de teransformation */
    private double[][] matrice;

    /**
     * Constructeur de Transformation3D 
     * @param matrice La matrice de transformation
     */
    public Transformation3D(double[][] matrice) {
        this.matrice = matrice;
    }

    /**
     * Constructeur de Transformation3D qui l'initialise avec une matrice identité
     */
    public Transformation3D() {
        this(new double[][] 
            {{1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}});  
    }

    /**
     * Applique la transformation a un vecteur, c'est a dire multiplie la matrice par le vecteur/point, mais on fait la
     * multiplication comme si v avait une quatrieme ligne avec la valeur 1
     * @param v Le vecteur qu'on veut transformer
     * @return Un vecteur transformé
     */
    public void apply(Coord3D v) {
        double x = matrice[0][0] * v.getX() + matrice[0][1] * v.getY() + matrice[0][2] * v.getZ() + matrice[0][3]; 
        double y = matrice[1][0] * v.getX() + matrice[1][1] * v.getY() + matrice[1][2] * v.getZ() + matrice[1][3]; 
        double z = matrice[2][0] * v.getX() + matrice[2][1] * v.getY() + matrice[2][2] * v.getZ() + matrice[2][3]; 
        v.setX(x);
        v.setY(y);
        v.setZ(z);
    }

    /**
     * Projection de la transformation en 2D
     * @return
     */
    public AffineTransform projection2D() {
        return new AffineTransform(matrice[0][0], matrice[1][0], matrice[0][1], matrice[1][1], matrice[0][2], matrice[1][2]);
    }

    /**
     * Rotation autour de l'axe des x
     * @param angle L'angle de la transformation
     * @return Une Transformation3D avec la rotation donnée
     */
    public static Transformation3D rotationX(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        return new Transformation3D(new double[][]
            {{1, 0, 0, 0},
            {0, cos, -sin, 0},
            {0, sin, cos, 0},
            {0, 0, 0, 1}});
    }

    /**
     * Rotation autour de l'axe des y
     * @param angle L'angle de la transformation
     * @return Une Transformation3D avec la rotation donnée
     */
    public static Transformation3D rotationY(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        return new Transformation3D(new double[][]
            {{cos, 0, sin, 0},
            {0, 1, 0, 0},
            {-sin, 0, cos, 0},
            {0, 0, 0, 1}});
    }

    /**
     * Rotation autour de l'axe des z
     * @param angle L'angle de la transformation
     * @return Une Transformation3D avec la rotation donnée
     */
    public static Transformation3D rotationZ(double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        return new Transformation3D(new double[][]
            {{cos, -sin, 0, 0},
            {sin, cos, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}});
    }

    public static Transformation3D personalizedRotation(double angle, Coord3D[] rotPoints) {
        if(rotPoints.length != 2) {
            throw new IllegalArgumentException("La longeur de la liste de points de rotation n'est pas égale à 2");
        }
        return personalizedRotation(angle, rotPoints[0], rotPoints[1]);
    }

    public static Transformation3D personalizedRotation(double angle, Coord3D p1, Coord3D p2) {
        
        Coord3D v = p2.substract(p1);
        v.normalize();
        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
    
        double[][] m = {
            {0, -z, y},
            {z, 0, -x},
            {-y, x, 0}
        };
    
        double[][] id = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
    
        // la matrice m à la puissance 2
        double[][] m2 = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m2[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    m2[i][j] += m[i][k] * m[k][j];
                }
            }
        }
    
        // matrice de rotation
        double[][] rotation = new double[4][4];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                rotation[i][j] = id[i][j] + sin * m[i][j] + (1 - cos) * m2[i][j];
            }
        }
        
        rotation[3][3] = 1;
    
        Transformation3D t = Transformation3D.translation(-p1.getX(), -p1.getY(), -p1.getZ());
        Transformation3D tRot = new Transformation3D(rotation);
        Transformation3D tInv = Transformation3D.translation(p1.getX(), p1.getY(), p1.getZ());

        return tInv.composition(tRot).composition(t);
    }

    /**
     * Crée une transformation pour une simple transformation
     * @param tx Déplacement sur l'axe des x
     * @param ty Déplacement sur l'axe des y
     * @param tz Déplacement sur l'axe des z
     * @return Une transformation qui déplace des objets
     */
    public static Transformation3D translation(double tx, double ty, double tz) {
        Transformation3D t = new Transformation3D();
        t.matrice[0][3] = tx;
        t.matrice[1][3] = ty;
        t.matrice[2][3] = tz;
        return t;
    }

    public static Transformation3D translation(Coord3D v) {
        Transformation3D t = new Transformation3D();
        t.matrice[0][3] = v.getX();
        t.matrice[1][3] = v.getY();
        t.matrice[2][3] = v.getZ();
        return t;
    }
    
    /**
     * Composition de deux transformation
     * @param t La matrice par laquelle on veut multiplier this
     */
    public Transformation3D composition(Transformation3D t) {
        double[][] result = new double[4][4];
        for(int i = 0; i<4; i++) {
            for(int j = 0; j<4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.matrice[i][k] * t.matrice[k][j];
                }
            }
        }
        return new Transformation3D(result);
    }

    public static Transformation3D scale(double sx, double sy, double sz) {
        return new Transformation3D(new double[][]
            {{sx, 0, 0, 0},
             {0, sy, 0, 0},
             {0, 0, sz, 0},
             {0, 0, 0, 1}});
    }

}
