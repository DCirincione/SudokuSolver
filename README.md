3D Cube Sudoku Solver using Graph Search Algorithms

This project implements and extends a graph-based Sudoku solver based on the research paper “Comparison Analysis of Breadth First Search and Depth Limited Search Algorithms in Sudoku Game”. It was developed as part of a university assignment to turn theoretical algorithmic research into a practical, portfolio-ready application.

Overview

The core objective was to implement both Breadth-First Search (BFS) and Depth-Limited Search (DLS) algorithms on a Sudoku-solving problem using graph theory, as discussed in the paper. However, instead of solving traditional 2D Sudoku puzzles, we extended the challenge into a 3D cube-shaped Sudoku, increasing complexity and visual appeal.

Features & Functionality
	•Reads in easy, medium, and hard Sudoku puzzles from .txt files.
	•Uses a custom graph structure to track adjacency relationships and Sudoku rules.
	•Implements BFS to search the puzzle space broadly and find partial or multiple solutions.
	•Implements DLS with constraint propagation and MRV heuristic for efficient depth search.
	•Combines both methods into a hybrid solver, where BFS generates promising board states and DLS attempts to complete them.
	•Fully interactive GUI (Graphical User Interface) that visualizes the 3D cube Sudoku in real-time.
	•Live solving animation when using DLS or hybrid mode.
	•Puzzle selector (Easy/Medium/Hard) and Reset functionality built into the GUI.
	•Custom gradient background and cell design for improved user experience.

Why This Project?

In line with the assignment prompt, this project brings a published theory to life and adds a meaningful extension:
	•The original paper compared BFS and DLS in a 2D Sudoku setting.
	•We wanted to test how these algorithms perform when extended to a more complex structure: a 3D 5-face cube-based Sudoku board.
	•Inspired by prior research on Rubik’s Cube solvers using graph search, we created a hybrid model that leverages BFS for breadth and DLS for depth.
	•The hybrid approach improves solvability on harder boards while preserving memory efficiency.

Technologies Used
	•Java (Core language)
	•Swing (for GUI rendering)
	•Custom-built graph structure (adjacency and propagation)
	•BFS, DLS, and hybrid algorithms
	•Constraint propagation and MRV heuristics

Summary of Improvements
	•Extended the domain from 2D to 3D, using five Sudoku faces on a cube with cross-face constraints.
	•Integrated BFS + DLS into a hybrid solver that outperforms standalone BFS on harder puzzles.
	•Implemented visual output, allowing real-time tracking of solver behavior.
	•Scalable and flexible: capable of solving any cube-compliant Sudoku, not just textbook examples.

Evaluation
	•Performance: DLS alone is very fast; hybrid takes longer but solves harder puzzles that BFS alone cannot.
	•Solvability: Hybrid consistently solves all three puzzle difficulties; BFS may struggle on harder inputs.
	•Visualization: Clear, informative GUI makes algorithm behavior more transparent and engaging.

Citation
	•Paper: Raihan, R., Astuti, N., & Adiwijaya (2022). Comparison Analysis of Breadth First Search and Depth Limited Search Algorithms in Sudoku Game. ResearchGate
