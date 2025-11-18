package Model.IOs;

import Model.*;
import Model.Pieces.*;

import java.io.*;
import java.util.*;
import java.nio.file.*;

/**
 * Handles simple import/export of a game's move list to/from a CSV file.
 * Format: move_number,white_move,black_move
 * e.g., "1,e2e4,e7e5"
 * This uses coordinate notation, not Standard Algebraic Notation.
 */
public class CSVParser{
    private static final String CSV_HEADER = "Move,White,Black\n";

    /**
     * Exports the move history to a simple CSV file.
     * @param state The GameState containing the move history.
     * @param filePath The target file path.
     * @throws IOException
     */
    public void exportGame(GameState state, String filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            writer.write(CSV_HEADER);

            List<Move> moves = state.getMoveHistory();
            int moveNumber = 1;
            for (int i = 0; i < moves.size(); i += 2) {
                String whiteMove = formatMove(moves.get(i));

                String blackMove = "";
                if (i + 1 < moves.size()) {
                    blackMove = formatMove(moves.get(i + 1));
                }

                writer.write(String.format("%d,%s,%s\n", moveNumber, whiteMove, blackMove));
                moveNumber++;
            }
        }
    }

    /**
     * Imports a move list from a CSV file.
     * NOTE: This is a simple import. It re-plays the moves
     * using the RuleEngine's basic validation.
     *
     * @param filePath The file to read.
     * @return A new GameState populated with the imported moves.
     * @throws IOException
     */
    public GameState importGame(String filePath) throws IOException {
        GameState newState = new GameState(); // Creates a new board in start pos
        RuleEngine ruleEngine = new RuleEngine();

        newState.getMoveHistory().clear(); // Clear the history (even though it's new, artifacts could have remained)

        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // Skip header line
        List<String> moveLines = lines.stream()
                .skip(1)
                .filter(line -> !line.trim().isEmpty())
                .toList();

        for (String line : moveLines) {
            String[] parts = line.split(",");
            if (parts.length < 2) continue; // Skip malformed lines

            // 1. Add white move
            Move whiteMove = parseMoveNotation(newState, ruleEngine, parts[1]);
            if (whiteMove != null) {
                newState.makeMove(whiteMove);
            }

            // 2. Add black move
            if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                Move blackMove = parseMoveNotation(newState, ruleEngine, parts[3]);
                if (blackMove != null) {
                    newState.makeMove(blackMove);
                }
            }
        }
        return newState;
    }

    /**
     * Helper to parse a "fromto" string (e.g., "e2e4") into a Move object.
     */
    private Move parseMoveNotation(GameState state, RuleEngine engine, String notation) {
        if (notation == null || notation.length() != 4) {
            System.err.println("Could not parse move notation: " + notation);
            return null;
        }

        try {
            //converting the string of positions
            Position from = notationToPosition(notation.substring(0, 2));
            Position to = notationToPosition(notation.substring(2, 4));

            /**
             * Asking RuleEngine to generate the given move
             * This checks for its legality, any possible checks, and handles special moves.
             * If the move is not legal, returns a null.*/
            Move validMove = engine.generateMove(state, from, to);

            // Check with the RuleEngine
            if (validMove != null) {
                Piece piece = state.getBoard().getPieceAt(from);
                return new Move(from, to, piece);
            } else {
                System.err.println("Illegal move in CSV: " + notation);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Could not parse move: " + notation + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a Move object into "fromto" notation (e.g., "e2e4").
     */
    private String formatMove(Move move) {
        return positionToNotation(move.getFrom()) + positionToNotation(move.getTo());
    }

    /**
     * Converts a Position object (e.g., row=6, col=4) to notation (e.g., "e2").
     */
    private String positionToNotation(Position pos) {
        char file = (char) ('a' + pos.column());
        char rank = (char) ('8' - pos.row());
        return "" + file + rank;
    }

    /**
     * Converts notation (e.g., "e2") to a Position object (e.g., row=6, col=4).
     */
    private Position notationToPosition(String notation) {
        int col = notation.charAt(0) - 'a';
        int row = '8' - notation.charAt(1);
        return new Position(row, col);
    }
}