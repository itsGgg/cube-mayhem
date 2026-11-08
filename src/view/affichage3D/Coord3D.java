package view.affichage3D;

import java.lang.Math;

/**
 * Represente des points dans R3 et des vecteurs
 */
public class Coord3D {

    protected double x, y, z;

    /**
     * Crée soit un point soit un vecteur.
     * @param x Coordonné sur l'axe des x
     * @param y Coordonné sur l'axe des y
     * @param z Coordonné sur l'axe des z
     */
    public Coord3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z; 
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false; 
        Coord3D other = (Coord3D) obj;
        double epsilon = 1E-6;
        return Math.abs(other.x - x) < epsilon && Math.abs(other.y - y) < epsilon && Math.abs(other.z - z) < epsilon;
    }

    /**
     * Fait une copie de this
     * @return Un nouvel point avec les mêmes coordonnées que this
     */
    public Coord3D copy() {
        return new Coord3D(x, y, z);
    }

    /**
     * Ajoute le vecteur v à this
     * @param v Le vecteur qu'on ajoute à this
     */
    public void add(Coord3D v) {
        x+=v.x;
        y+=v.y;
        z+=v.z;
    }

    /**
     * Multiplie this par un nombre 
     * @param n Le nombre par lequel on veut multiplier this
     */
    public Coord3D multiply(double n) {
        return new Coord3D(x*n, y*n, z*n);
    }

    /**
     * La division des coordonnés d'un vecteur par un nombre n
     * @param n Le nombre par lequel on veut deviser this
     */
    public Coord3D devide(double n) {
        return new Coord3D(x/n, y/n, z/n);
    }

    /**
     * Sustraction de deux vecteurs, calcule un nouvel vecteur = this v
     * @param v Le vecteur avec lequel on veut calculer this v
     * @return Nouvel vecteur this v
     */
    public Coord3D substract(Coord3D v) {
        return new Coord3D(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Calcule le produit scalaire de deux vecteurs
     * @param v Un vecteur
     * @return Le produit scalaire de this et de v
     */
    public double produitScal(Coord3D v) {
        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    /**
     * Calcule le produit vectoriel this ^ v
     * @param v Le vecteur dont on veux calculer le produit scalaire avec this
     * @return Un nouvel vecteur, le résultat de calcul du produit scalaire
     */
    public Coord3D produitVect(Coord3D v) {
        double newx = y * v.z - z * v.y;
        double newy = z * v.x - x * v.z;
        double newz = x * v.y - y * v.x;
        return new Coord3D(newx, newy, newz);
    }

    /**
     * Calcule la nnome d'un vecteur
     * @return La norme de this 
     */
    public double norme() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    /**
     * La normalisation d'un vecteur
     */
    public void normalize() {
        double norme = norme();
        if(norme == 0) {
            x = 0; 
            y = 0; 
            z = 0; 
        } else {
            x = x/norme;
            y = y/norme;
            z = z/norme;
        }
    }

    /**
     * Tes de nuillité d'un vecteur avec une tolérance (pour vérifier si un vecteur est quasiment nul)
     * @param tolerance Une marge d'erreur
     * @return True si on peut considérer this comme nul, false sinon
     */
    public boolean isZero(double tolerance) {
        return Math.abs(x) < tolerance && Math.abs(y) < tolerance && Math.abs(z) < tolerance;
    }

    @Override
    public String toString() {
        return "x="+x+", y="+y+", z="+z;
    }

    /**
     * Calcule la distance entre this et v
     * @param v Le deuxième point
     * @return La distance entre this et v
     */
    public double distance(Coord3D v) {
        double dx = x - v.x;
        double dy = y - v.y;
        double dz = z - v.z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public void applyTransformations(Transformation3D...transformations) {
        for(Transformation3D t : transformations) t.apply(this);
    }

}
