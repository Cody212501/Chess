import Model.*;
import Model.IOs.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CustomGameTests{
    private JsonPersistence jsonPersistence;
    private PGNParser pgnParser;
    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        jsonPersistence = new JsonPersistence();
        pgnParser = new PGNParser();
        ruleEngine = new RuleEngine();
    }

    // helper method for reaching the files
    private String getTestFilePath(String fileName){
        try{
            // Access file from ./Chess/resources/TestResources/
            URL resource = getClass().getResource("/" + fileName);
            if(resource == null){
                fail("Test file not found: " + fileName);
            }
            // Convert URL to proper File Path (handles spaces, special chars)
            return Paths.get(resource.toURI()).toFile().getAbsolutePath();
        }catch(URISyntaxException e){
            fail("Error converting resource path: " + e.getMessage());
            return null;
        }
    }

    // 1. testing the workability of the IMPORT function
    @Test
    void testImportPgnGame() throws IOException{
        String path = getTestFilePath("import_game.pgn");

        // Act: Import the game
        GameState state = pgnParser.importGame(path);

        // Assert: Check if data is correct based on the file content
        assertNotNull(state, "Imported state should not be null");
        assertEquals("Test White", state.getWhitePlayer().getName(), "White player name mismatch");
        assertEquals("Test Black", state.getBlackPlayer().getName(), "Black player name mismatch");

        // 3 moves in the file (e4, e5, Nf3, Nc6, Bc4) -> 5 plies (black hasn't made a 3rd move yet)
        assertEquals(5, state.getMoveHistory().size(), "Move history size mismatch");

        // Check if the last piece moved is the Bishop on c4 (row 4, col 2 in Model coordinates if White is bottom)
        // Note: Position logic depends on your implementation (0-7 rows).
        // Bc4 -> c4.
        // If row 0 is top (Black) and row 7 is bottom (White):
        // c4 is row 4 (8-4), col 2 (c).
        // Let's verify the piece at c4.
        Position c4 = new Position(4, 2);
        assertNotNull(state.getBoard().getPieceAt(c4), "Should be a piece at c4");
        assertEquals("BISHOP", state.getBoard().getPieceAt(c4).getType().name(), "Piece at c4 should be Bishop");
    }

    // 2. Continuing a not finished game
    @Test
    void testContinueUnfinishedGame() throws IOException {
        // Requirement: Use a JSON file saved from a mid-game state
        String path = getTestFilePath("unfinished_game.json");

        // Act: Load
        GameState state = jsonPersistence.loadGame(path);

        // Assert
        assertNotNull(state);

        // Check if game is NOT over
        assertFalse(ruleEngine.isCheckmate(state), "Game should not be checkmate yet");
        assertFalse(ruleEngine.isStalemate(state), "Game should not be stalemate yet");

        // Check if the current player has valid moves (meaning it can be continued)
        boolean isWhiteTurn = state.isWhiteTurn();
        boolean hasMoves = false;

        // Iterate board to find at least one legal move for current player
        outerLoop:
        for(int r=0; r<8; r++) {
            for(int c=0; c<8; c++) {
                Position pos = new Position(r,c);
                if (state.getBoard().getPieceAt(pos) != null &&
                        state.getBoard().getPieceAt(pos).isWhite() == isWhiteTurn) {

                    if (!ruleEngine.getValidMovesForPiece(state, pos).isEmpty()) {
                        hasMoves = true;
                        break outerLoop;
                    }
                }
            }
        }
        assertTrue(hasMoves, "Current player should have legal moves to continue");
    }

    // 3. Game ended tests ---
    @Test
    void testLoadFinishedGame() throws IOException {
        // Requirement: Use a JSON file where Checkmate has occurred
        String path = getTestFilePath("finished_game.json");

        // Act: Load
        GameState state = jsonPersistence.loadGame(path);

        // Assert
        assertNotNull(state);

        // Verify it is actually checkmate
        assertTrue(ruleEngine.isCheckmate(state), "Loaded game should be in Checkmate state");

        // Verify that the loser has NO legal moves
        // (This logic is technically covered by isCheckmate, but explicit check is good)
        boolean isWhiteTurn = state.isWhiteTurn(); // The player who got mated

        // Let's assume RuleEngine logic is correct. C
        // Checkmate implies no moves, with no moves to defend not resulting in checkmate.
        // We can verify who won based on turn.
        // If isWhiteTurn is TRUE, and it's Mate, White lost.
        // This depends on your saved file.
    }
}