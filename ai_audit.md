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