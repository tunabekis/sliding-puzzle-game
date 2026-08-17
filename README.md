# Sliding Puzzle Game

A desktop sliding tile puzzle game built with Java Swing. Rearrange shuffled
tiles back into order by sliding them into the single empty slot, either with
plain numbered tiles or tiles cut from a picture of your choice.

## Features

- Square boards from 3x3 up to 8x8, selectable from the UI
- Two tile modes: numbered tiles or a picture split into tiles
- Six built-in pictures to choose from
- Guaranteed-solvable shuffling (simulated via random legal moves)
- Move counter and a congratulations screen on completion, with a
  one-click "New Game" restart
- Sound effects for valid/invalid moves, plus toggleable looping
  background music

## Technologies

- Java (Swing / AWT for the UI, `javax.sound.sampled` for audio)
- Eclipse project structure (`.project` / `.classpath`)

## Project Structure

```
src/
  puzzle/
    Main.java              Application entry point
    SlidingPuzzleGame.java Game window, menu, board logic, and tile interaction
    SoundPlayer.java       Loads and plays sound effects / background music
  media/
    *.jpg, *.png           Puzzle pictures and window icon
    *.wav                  Sound effects and background music
```

## How to Run

### From Eclipse

1. Import the project via **File > Import > Existing Projects into Workspace**
   and select this folder.
2. Run `src/puzzle/Main.java` as a Java Application.

### From the command line

Run these commands from the project root (so relative paths to `src/media/`
resolve correctly):

```bash
mkdir -p bin
javac -d bin $(find src -name "*.java")
java -cp bin puzzle.Main
```

## How to Play

1. Choose a board size, then a tile type (numbers or picture).
2. Use the **Menu > Shuffle** option to scramble the board.
3. Click any tile adjacent to the empty slot to slide it into place.
4. Restore the original order (numbers 1..N in reading order) to win.
