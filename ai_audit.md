# AI Audit Log - Team xyuguyn00 

**Last Updated:** March 29, 2026

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

## 3. Additional test cases
* **Tool:** ChatGPT
* **Date:** April 10, 2026
* **Prompt:** "Give me example test cases to cover cases such as map edges, cheaper path is choosen, etc."
* **Student Modification:** The AI suggested several possible edge cases and map examples that could be used to test implementation for bugs and possible issues. I used some of these ideas when creating my tests and also add my own test cases, including larger maps such as five by five map.
* **Generation Ratio:** 30% (edge cases ideas and map examples), 70% (additional test cases and tests implementation).