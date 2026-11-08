package model.tools;

public class Debug {
    public static void send(Class<?> clazz, String message) {
        if(clazz == null) return;
        System.out.println("\u001B[34m" + "{Debug@" + clazz.getName() + "} > " + message + "\u001B[0m");
    }
}
