package test;

public class Launcher {
    public static void main(String[] args) {
        // Cela appelle MainApp sans que Java ne détecte l'héritage Application au démarrage
        MainApp.main(args);
    }
}