# AI Audit Log - Team xyuguyn00 

**Last Updated:** April 15, 2026

---

## 1. Observer Architecture (MVC) & Data Models
* **Tool:** Gemini
* **Date:** March 29, 2026
* **Prompt:** > "Design the base class structure for an MVC architecture according to the provided Homework2Test.java file."
* **Student Modification:** The AI provided the initial structure and interface definitions. I completely refactored the generated code to fit our specific implementation needs. 
* **Generation Ratio:** 30 % (initial structure generation), 70 % (student refactoring and implementation).

---

## 2. Pathfinding (Dijkstra Algorithm) & Movement Logic
* **Tool:** Gemini
* **Date:** April 1, 2026
* **Prompt:** > "Implement the Dijkstra algorithm in the getReachableTiles method to calculate reachable positions based on movement range and specific terrain costs."
* **Student Modification:** The AI provided the core while-loop logic for the Dijkstra priority queue. I heavily refactored the generated code to fit the project's specific MVC architecture. I manually implemented the `getTerrainAt` map parsing logic, the `moveUnit`, and refactored the terrain cost calculations out of the game engine and into the `Unit` class.
* **Generation Ratio:** 40 % (core Dijkstra algorithm and coordinate logic), 60 % (student refactoring, map parsing, movement implementation, and OOP architectural design).

---

## 3. Additional test cases
* **Tool:** ChatGPT
* **Date:** April 10, 2026
* **Prompt:** "Give me example test cases to cover cases such as map edges, cheaper path is choosen, etc."
* **Student Modification:** The AI suggested several possible edge cases and map examples that could be used to test implementation for bugs and possible issues. I used some of these ideas when creating my tests and also add my own test cases, including larger maps such as five by five map.
* **Generation Ratio:** 30% (edge cases ideas and map examples), 70% (additional test cases and tests implementation).

---

## 4. Data-Driven Engine Refactoring (TSV Parsers)
* **Tool:** Gemini
* **Date:** April 14, 2026
* **Prompt:** > "The AI assisted by suggesting the structure of Java 14 records (`TerrainData`, `UnitData`, `UnitDamageData`) and a basic `DataLoader` concept. I implemented and refined these components, adapted them to the required `xyuguyn00.model` and `xyugyn00.util` packages, and ensured compatibility with the existing codebase. I also designed and implemented JUnit 5 tests (using `@TempDir`) to validate parsing against .tsv files provided by the professor"
* **Generation Ratio:** 40 % (initial structure and suggestions), 60 % (implementation, integration, and testing).

---

## 5. JavaFX Entry Point & Factory Bootstrapping (App.java)
* **Tool:** Gemini
* **Date:** April 14, 2026
* **Prompt:** > "I need to generate an App.java template for the initial View using JavaFX."
* **Student Modification:** The AI provided the boilerplate for the `javafx.application.Application` lifecycle, including the `Stage` and `Scene` setup. I reviewed the `GameFactory` bootstrapping logic, ensuring the `App` class correctly loads the `.tsv` files from the root `data/` directory and handles initialization failures gracefully via a native UI Alert.
* **Generation Ratio:** 90 % (AI boilerplate and error handling), 10 % (student review and integration).

---

## 6. Maven Build Configuration (pom.xml)
* **Tool:** Gemini
* **Date:** April 14, 2026
* **Prompt:** > "Generate a pom.xml file to support maven and JUnit 5."
* **Student Modification:** The AI generated the complete Maven configuration file, including the `maven-compiler-plugin`, JavaFX dependencies, and JUnit 5 test scope. I copy-pasted the file directly into the project root and executed `mvn clean compile` to synchronize the workspace.
* **Generation Ratio:** 100 % AI.

---

## 6. Turn Management & Sidebar UI
* **Tool:** Gemini
* **Date:** April 15, 2026
* **Prompt:** > "I think the game will look like this. At the right we have main board and on the left we have two cards representing a Player 1 and Player 2. We will indicate the player's turn by highlighting the borders of player's cards. Initially it will only have Player 1 and Player 2 text, but in the future we will include info about the gold amount etc."
* **Student Modification:** The AI provided the initial JavaFX layout template for the `PlayerSidebar` component.
* **Generation Ratio:** 100 % AI.

---

## 7. Combat System & Interaction UI Overhaul
* **Tool:** Gemini
* **Date:** May 6, 2026
* **Prompt:** > "The unit type determines the distance that a particular unit can move per turn. We don't determine the distance that the particular unit can attack an enemy? I need to implement the combat rules and UI interactions based on the strict specs provided."
* **Student Modification:** I implemented the core logic for the combat system and the data parsing for the new properties (min/max attack ranges, terrain defense bonuses, counter-attacks, and strict Manhattan distance validations). The AI helped me with the overall architectural concept (decoupling the visual move preview from the engine state) and assisted with the JavaFX UI implementation for the combat system, including the targeting mode crosshairs and thread safety.
* **Generation Ratio:** 40% (AI architectural concepts and UI state-machine boilerplate), 60% (student implementation of deterministic combat math, parsing logic, and engine integration).

---

## 8.JSON Map Parsing & Dynamic Initialization
* **Tool:** Gemini
* **Date:** May 6, 2026
* **Prompt:** > "Let's use Jackson for JSON parsing. The professor didn't provide the game_stats.json file, so I need to design one. How should we structure the parsing and the dynamic initialization for buildings and ownership?"
* **Student Modification:** I designed the Java data models (Building.java to track dynamic state like ownership and capture points, and GameMapData.java records). I implemented the Jackson parsing logic within DataLoader, and completely refactored GameFactory and App.java to spawn the grid, buildings, and units from the JSON data. I also updated the GameView rendering loop to visually represent building ownership. The AI assisted by suggesting a clean JSON schema structure and providing the initial Maven dependency configuration for Jackson.
* **Generation Ratio:** 20% (AI JSON schema design and Jackson setup), 80% (student implementation of parsing logic, data models, engine integration, and UI rendering).

---

## 9. Economics, Turn Lifecycle Refactoring & Validation Layer
* **Tool: Gemini**
* **Date:** May 6, 2026
* **Prompt:** > "I found a bug where gold is not added correctly on turn 1... Also, tanks can't enter the HQ because it was placed in water. Should I add logic to validate the game_stats.json to prevent invalid placements?"
* **Student Modification:** I implemented the core economic engine, dynamically tracking player funds and connecting them to the JavaFX PlayerSidebar. I refactored the turn management lifecycle in Game.java to strictly enforce the "Income and Repair" at the start of every turn. During testing, I discovered critical pathfinding blocks caused by invalid data configurations. I integrated a validation layer (validateMapData) within the Factory to check JSON inputs, ensuring buildings and units are placed on valid, passable terrain before the engine initializes. The AI assisted by providing the UI data-binding templates.
* **Generation Ratio:** 15% (UI data-binding templates), 85% (student implementation, rigorous engine testing, bug discovery, and data validation engineering).

---

## 10. GameFactory Validation Test Suite
* **Tool:** Gemini
* **Date:** May 6, 2026
* **Prompt:** > "I found out that if we parse the json with more than one unit on the same tile it accepts it and only one utnit is rendered on that tile. I think we need to refactor the validation or better let's implement tests for the validation logic to be 100% sure in it's logic and never return to that"
* **Student Modification:** I found a bug where units overwrite each other if placed on the exact same tile in the JSON. I told the AI to update the validation rules and write a JUnit 5 test suite (GameFactoryValidationTest.java) to check for map borders, impassable terrain, and stacked units. I added this code to the project.
* **Generation Ratio:** 100% AI.

---

## 11. Game Logging
* **Tool:** ChatGPT
* **Date:** May 11, 2026
* **Prompt:** > I need to log game process (every turn, attack, etc.) to a file. I also need to load game state from this file, move backward and forward. How can I conceptually design this?
* **Student Modification:** I designed and implemented the logger service, that provides methods for creating snapshots of the current game state, moving backward and forward through saved states, and continuing the game from the selected state.
* **Generation Ratio:** 25% AI (concept description), 75% (adaptation to the project, implementation of service, and integration with the current codebase.).

---

## 12. .log File Structure
* **Tool:** ChatGPT
* **Date:** May 11, 2026
* **Prompt:** > I need to store data about each player and their turns, including all data about the actual game state, so that it is possible to move to the previous or next turn. How can I do it? Take into account that I do not have any turn identifier.
* **Student Modification:** I designed and implemented the record classes used for log data representation, including snapshots of the game state, units, buildings, positions, and log entries.
* **Generation Ratio:** 20% AI (data structures description), 80% (adaptation to the project, implementation of records, and integration with the current codebase.).

---

## 13. Main Menu & Game Over Overlay
* **Tool:** Gemini
* **Date:** May 12, 2026
* **Prompt:** > We need a main menu with 3 modes (PvP, PvB, BvB). Also, we need a pop-up window to appear when someone captures the enemy HQ, with Retry and Return to Menu buttons.
* **Student Modification:** AI helped structure the JavaFX scene switching (`App.java`) and the global `GameObserver` trigger for the end-game condition. I adapted the UI styling, constructed the layouts, and handled the clean-up logic to safely stop background game loops when returning to the menu.
* **Generation Ratio:** 60% AI (scene management logic), 40% Student (UI layout and styling).

---

## 14. Texture Rendering Bug Fixes (Layering & Auto-tiling)
* **Tool:** Gemini
* **Date:** May 12, 2026
* **Prompt:** > Mountains and forest tiles should have something under them like grass. Water textures are not generated at all.
* **Student Modification:** AI diagnosed that JavaFX `StackPane` rendering required an explicit bottom layer for transparent textures, and found a flipped Row/Column coordinate bug causing out-of-bounds errors for the water auto-tiling. I applied these fixes directly to the `TileRenderer` and verified the seamless terrain connections across the map grid.
* **Generation Ratio:** 80% AI (bug diagnosis), 20% Student (code application and verification).