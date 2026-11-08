package view.affichage3D;

public class PointOfVue extends Coord3D{

    public PointOfVue(double x, double y, double z) {
        super(x, y, z);
    }

    public void rotateX(double angle) {
        double newY = y * Math.cos(angle) - z * Math.sin(angle);
        double newZ = y * Math.sin(angle) + z * Math.cos(angle);
        this.y = newY;
        this.z = newZ;
    }

    // Applique une rotation sur le point de vue autour de l'axe Y
    public void rotateY(double angle) {
        double newX =  x * Math.cos(angle) + z * Math.sin(angle);
        double newZ = - x * Math.sin(angle) + z * Math.cos(angle);
        this.x = newX;
        this.z = newZ;
    }

    // Applique une rotation sur le point de vue autour de l'axe Z
    public void rotateZ(double angle) {
        double newX = x * Math.cos(angle) - y * Math.sin(angle);
        double newY = x * Math.sin(angle) + y * Math.cos(angle);
        this.x = newX;
        this.y = newY;
    }


}
