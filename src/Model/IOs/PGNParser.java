package Model.IOs;

import model.*;

import java.io.*;
import java.util.*;
import java.nio.file.*;

/**
 * Handles import and export of games in PGN (Portable Game Notation) format.
 *
 * This is a highly complex task.
 * EXPORTING requires converting (from, to) coordinates into Standard
 * Algebraic Notation (e.g., "Nf3", "Qxa4+", "O-O"), which requires
 * checking for ambiguities (e.g., "Nbd2" vs "Nfd2").
 *
 * IMPORTING is even harder, as it requires a full chess parser
 * that understands the current board state to interpret moves like "e4" or "Nf3".
 *
 * This class provides the basic structure, but the core logic
 * (marked with TODO) is non-trivial and often requires a dedicated library.
 */
public class PGNParser{
    /**
     * Exports the game to PGN format.
     * @param state The GameState to export.
     * @param filePath The target file path.
     * @throws IOException
     */
    public void exportGame(GameState state, String filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            // 1. Write PGN Headers (Tags)
            writePgnHeader(writer, state);

            // 2. Write Move Text
            String moveText = formatMoveText(state.getMoveHistory());
            writer.write(moveText);

            // 3. Write Result (e.g., "1-0", "0-1", "1/2-1/2")
            // TODO: Determine game result
            writer.write(" *"); // "*" means result is unknown
        }
    }

    private void writePgnHeader(Writer writer, GameState state) throws IOException {
        Player white = state.getWhitePlayer();
        Player black = state.getBlackPlayer();

        writer.write("[Event \"Local Game\"]\n");
        writer.write("[Site \"Unknown\"]\n");
        writer.write(String.format("[Date \"%s\"]\n", java.time.LocalDate.now()));
        writer.write("[Round \"1\"]\n");
        writer.write(String.format("[White \"%s\"]\n", white != null ? white.getName() : "White"));
        writer.write(String.format("[Black \"%s\"]\n", black != null ? black.getName() : "Black"));
        writer.write(String.format("[WhiteElo \"%d\"]\n", white != null ? white.getElo() : 0));
        writer.write(String.format("[BlackElo \"%d\"]\n", black != null ? black.getElo() : 0));
        writer.write("[Result \"*\"]\n"); // Unknown result
        writer.write("\n"); // Empty line before moves
    }

    /**
     * This is the hardest part of PGN EXPORT.
     * It needs to convert Move(from, to) into "e4", "Nf3", "Qxh7#", etc.
     */
    private String formatMoveText(List<Move> moves) {
        // TODO: Implement a full Standard Algebraic Notation (SAN) converter.
        // This requires:
        // 1. Knowing the piece type (Pawn moves are just "e4", others "Nf3").
        // 2. Knowing if it was a capture ("exd5", "Qxa4").
        // 3. Checking for check/checkmate ("+", "#").
        // 4. Resolving ambiguities (e.g., if two Knights can go to f3,
        //    it must be "Ngf3" or "Ndf3").
        // 5. Handling castling ("O-O", "O-O-O").

        // For now, we return a placeholder.
        StringBuilder sb = new StringBuilder();
        int moveNumber = 1;
        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                sb.append(moveNumber).append(". ");
                moveNumber++;
            }
            // Using the simple "from-to" as a placeholder
            sb.append(positionToNotation(moves.get(i).getFrom()));
            sb.append(positionToNotation(moves.get(i).getTo())); // PGN does not use '-', but 'x' for capture
            sb.append(" ");
        }

        System.err.println("WARNING: PGN move formatting is not fully implemented. Using simple notation.");
        return sb.toString();
    }

    /**
     * This is the hardest part of PGN IMPORT.
     * It needs to parse "e4", "Nf3" etc. back into (from, to) coordinates
     * based on the current board state.
     */
    public GameState importGame(String filePath) throws IOException {
        // TODO: Implement a full PGN parser.
        // 1. Read the file, separating tags from move text.
        // 2. Parse tags to set up player names, ELO, etc.
        // 3. Create a new GameState and a RuleEngine.
        // 4. Iterate through the move text (e.g., "1. e4 e5 2. Nf3 Nc6").
        // 5. For EACH move string (e.g., "Nf3"):
        //    a. Ask the RuleEngine: "What piece of the current player can
        //       legally move to 'f3'?"
        //    b. If it's ambiguous (e.g., "Nf3" when "Ngf3" was required),
        //       throw an error.
        //    c. Get the resulting Move(from, to, piece) object.
        //    d. Apply the move to the board (state.makeMove(move)).
        //    e. Repeat for the next move.

        System.err.println("ERROR: PGN import is not implemented.");
        throw new UnsupportedOperationException("PGN Import is a highly complex feature and is not implemented.");
    }

    // Helper methods (from CsvPersistence)
    private String positionToNotation(Position pos) {
        char file = (char) ('a' + pos.column());
        char rank = (char) ('8' - pos.row());
        return "" + file + rank;
    }
}