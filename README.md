# Dota 2 Spleen Sweeper Bot

A Java desktop application that automatically plays the **Spleen Sweeper** Minesweeper-style minigame in Dota 2.

The bot captures the game board directly from the screen, identifies the state of each cell using image-template comparison, applies standard Minesweeper deduction rules to determine safe moves and mines, and then performs the corresponding mouse actions automatically.

The application does not access Dota 2's internal game state or API. It works entirely from the information visible on the screen.

## Features

* Automatic screen capture of the Spleen Sweeper board
* Template-based recognition of:

  * Empty cells
  * Numbered cells
  * Covered cells
  * Flagged cells
* Automatic identification of guaranteed safe moves
* Automatic placement of flags on cells that must contain mines
* Mouse automation using Java's `Robot` API
* Support for all five Spleen Sweeper stage sizes
* Resolution-specific board configurations and image templates
* Support for **1080p** and **2K** resolutions
* Swing-based graphical user interface
* Start, stop and stage-selection controls
* Runs the solving process on a separate thread so the interface remains responsive

> **Note:** 4K configuration and template resources are present in the project, but 4K support is currently disabled in the user interface and should be considered unfinished.

## How It Works

The bot operates in a repeated capture-analyze-act cycle.

### 1. Screen Capture

`ScreenCapture` uses Java's `Robot` class to capture the exact area of the screen containing the Spleen Sweeper board.

The dimensions and screen coordinates of the board depend on:

* The selected Spleen Sweeper stage
* The selected display resolution

Each of the five stages has its own board dimensions, row and column count, and cell size.

### 2. Cell Recognition

`BoardAnalyzer` divides the captured board image into individual cells.

Each cell is compared against reference images stored in:

```text
src/main/resources/templates/
```

Separate template sets are provided for different display resolutions.

The image-recognition system compares the RGB values of pixels in the captured cell against each template and calculates a match percentage. The closest sufficiently similar template determines the cell's state.

Recognized states include:

```text
Empty
Number 1
Number 2
...
Flagged
Covered
```

### 3. Board Representation

Every recognized square is represented by a `Cell` object containing:

* Its current state
* Row position
* Column position

The complete board is stored as a two-dimensional `Cell[][]` array.

### 4. Minesweeper Logic

For every numbered cell, the bot examines its neighboring cells.

Two standard deterministic Minesweeper rules are used:

**Safe-cell rule**

If the number of already flagged neighboring cells equals the number shown on the cell, all other covered neighboring cells are safe and can be opened.

**Mine rule**

If:

```text
flagged neighbors + covered neighbors = displayed number
```

then every remaining covered neighboring cell must contain a mine and can be flagged.

The bot repeatedly captures and analyzes the board after performing actions.

If it reaches a position where it cannot determine another guaranteed move using these rules, it stops rather than making a random guess.

### 5. Automated Input

`ActionExecutor` uses Java's `Robot` API to interact with the game.

It performs:

* Left clicks to open safe cells
* Right clicks to flag mines
* Mouse movement
* An initial click to activate the game window

## Application Structure

```text
src/
└── main/
    ├── java/
    │   └── significant/
    │       └── minesweeperbotjava/
    │           ├── ActionExecutor.java
    │           ├── BoardAnalyzer.java
    │           ├── BotUI.java
    │           ├── Cell.java
    │           └── ScreenCapture.java
    │
    └── resources/
        └── templates/
            ├── 1080p/
            ├── 2K/
            └── 4K/
```

### `BotUI`

Provides the Swing graphical user interface and controls the main execution loop.

The user can:

* Select the display resolution
* Select Spleen Sweeper stages 1–5
* Start the bot
* Stop the bot
* Quit the application

It also contains the screen coordinates, dimensions and cell sizes used for each stage and supported resolution.

### `ScreenCapture`

Captures a specified rectangular section of the screen using `java.awt.Robot`.

### `BoardAnalyzer`

Handles the main recognition and solving logic.

Its responsibilities include:

* Splitting screenshots into individual cells
* Comparing cells against image templates
* Building an internal representation of the board
* Finding neighboring cells
* Applying Minesweeper deduction rules
* Determining which cells should be opened or flagged

### `ActionExecutor`

Handles desktop automation using `java.awt.Robot`.

It performs the mouse movements, left clicks and right clicks requested by the board analyzer.

### `Cell`

Represents an individual Minesweeper cell and stores its:

* State
* Row
* Column

## Technologies

* **Java 19**
* **Java Swing**
* **Java AWT / Robot**
* **BufferedImage / ImageIO**
* **Maven**
* **Maven Shade Plugin**
* Template-based image recognition
* Pixel-level RGB comparison
* Desktop automation
* Algorithmic problem solving

The project uses only the Java standard library at runtime and currently has no third-party Java dependencies.

## Requirements

* Java 19 or newer
* Apache Maven
* Dota 2
* A compatible display resolution
* Dota 2 running in either exclusive fullscreen or borderless windowed mode

Currently supported resolutions:

* 1920×1080
* 2560×1440 / 2K

4K support is unfinished and disabled in the application.

## Building

Clone the repository:

```bash
git clone https://github.com/SimplyPhantomDev/minesweeper-bot.git
cd minesweeper-bot
```

Build the project using Maven:

```bash
mvn clean package
```

The Maven configuration uses the Shade Plugin to create an executable JAR with:

```text
significant.minesweeperbotjava.BotUI
```

as the application's main class.

After building, the executable JAR will be generated in the `target` directory.

Run it with:

```bash
java -jar target/MineSweeperBotJava-1.0-SNAPSHOT.jar
```

## Limitations

The bot currently relies on predefined board coordinates and resolution-specific image templates. Because of this, Dota 2 must occupy the full screen area using either exclusive fullscreen or borderless windowed mode, and the display resolution must be supported by the bot.

Its solving algorithm performs deterministic Minesweeper deductions and does not currently implement probability-based guessing or more advanced multi-cell constraint analysis. If no guaranteed move can be found, the bot stops.

4K support has been partially prepared but is not currently available through the application.

## Project Motivation

I created this project as an exercise in combining several areas of software development into a single working application: screen capture, image comparison, desktop automation, user-interface development and algorithmic problem solving.

Rather than receiving the board state directly, the program has to interpret what is visible on the screen, convert that visual information into a structured representation of the game board, reason about that representation, and translate the resulting decisions back into mouse input.
