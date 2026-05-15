Project Name: Advance Wars Clone
Team name: xyuguyn00
Authors: Nazar Yuguy (xyuguyn00), Mariia Zhdaniuk (xzhdaniukm00)

Description:
A JavaFX-based clone of the classic turn-based strategy game Advance Wars. The application supports 3 modes: Player vs Player, Player vs Bot and Bot vs Bot (spectator mode).

Directory Structure:
- src/ : Contains all Java source code packages.
- data/ : Contains initial map layouts, unit statistics, and terrain rules (JSON/TSV).
- lib/ : Contains external graphical assets (textures, UI elements) and third-party libraries.
- pom.xml : Maven configuration file for building the project.

Compilation Instructions (Maven):
This project uses Maven for dependency management and building. To compile the source code, generate the program documentation, and create the executable JAR archive, run the following command from the root directory:

    mvn clean package javadoc:javadoc

Execution Instructions:
Once compiled, you can run the application using the generated executable JAR file located in the target directory (replace the version number if necessary):

    java -jar target/advance-wars-clone-1.0-SNAPSHOT.jar

Alternatively, if you have the JavaFX Maven plugin configured, you can run it directly via:

    mvn javafx:run