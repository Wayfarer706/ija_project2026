/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Launcher class serves as the entry point for the application. It contains the main method which is responsible for starting the application by invoking the main method of the App class. It's needed to generated Fat JAR with JavaFX dependencies, so that user can run the game just by launching the .jar file.
 */
package xyuguyn00;

public class Launcher {
    public static void main(String[] args) {
        App.main(args); 
    }
}