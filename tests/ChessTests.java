import Model.*;
import Model.Pieces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChessTests {

    private Board board;
    private RuleEngine ruleEngine;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.setupEmpty(); // we start all tests with a blank board
        gameState = new GameState(board, true); // by default, White is first
        ruleEngine = new RuleEngine();
    }

    // --- 1. Pawn tests ---

    @Test
    void testPawnMove() {
        Piece pawn = new Pawn(true);
        Position start = new Position(6, 4); // e2
        board.setPieceAt(start, pawn);

        Move move = ruleEngine.generateMove(gameState, start, new Position(5, 4)); // e3
        assertNotNull(move, "No pawns move forward should be zero");
        assertEquals(new Position(5, 4), move.getTo());
    }

    @Test
    void testPawnDoubleMove() {
        Piece pawn = new Pawn(true);
        Position start = new Position(6, 4); // e2
        board.setPieceAt(start, pawn);

        Move move = ruleEngine.generateMove(gameState, start, new Position(4, 4)); // e4
        assertNotNull(move, "Initial double move forward");
    }

    @Test
    void testPawnCapture() {
        Piece whitePawn = new Pawn(true);
        Piece blackPawn = new Pawn(false);
        board.setPieceAt(new Position(6, 4), whitePawn); // e2
        board.setPieceAt(new Position(5, 3), blackPawn); // d3

        Move move = ruleEngine.generateMove(gameState, new Position(6, 4), new Position(5, 3));
        assertNotNull(move, "Pawn takes enemy");
    }

    @Test
    void testPawnPromotionFlag() {
        Piece pawn = new Pawn(true);
        board.setPieceAt(new Position(1, 0), pawn); // a7

        Move move = ruleEngine.generateMove(gameState, new Position(1, 0), new Position(0, 0)); // a8
        assertNotNull(move);
        assertTrue(move.isPromotion(), "Promotion flag should be true.");
    }

    // --- 2. Officer movements ---

    @Test
    void testKnightMove() {
        Piece knight = new Knight(true);
        board.setPieceAt(new Position(4, 4), knight); // e4

        Set<Position> moves = ruleEngine.getValidMovesForPiece(gameState, new Position(4, 4));
        assertTrue(moves.contains(new Position(2, 5))); // f6
        assertTrue(moves.contains(new Position(6, 3))); // d2
        assertEquals(8, moves.size());
    }

    @Test
    void testRookBlocked() {
        Piece rook = new Rook(true);
        Piece block = new Pawn(true);
        board.setPieceAt(new Position(7, 0), rook); // a1
        board.setPieceAt(new Position(6, 0), block); // a2 (blocked)

        Set<Position> moves = ruleEngine.getValidMovesForPiece(gameState, new Position(7, 0));
        // Can move horizontally (b1, c1...) but not vertically
        assertFalse(moves.contains(new Position(5, 0)));
        assertTrue(moves.contains(new Position(7, 1)));
    }

    @Test
    void testBishopSliding() {
        Piece bishop = new Bishop(true);
        board.setPieceAt(new Position(4, 4), bishop); // e4

        Set<Position> moves = ruleEngine.getValidMovesForPiece(gameState, new Position(4, 4));
        assertTrue(moves.contains(new Position(0, 0))); // a8
        assertTrue(moves.contains(new Position(7, 7))); // h1
    }

    // --- 3. King and check tests ---

    @Test
    void testKingCannotMoveIntoCheck() {
        Piece king = new King(true);
        Piece enemyRook = new Rook(false);

        board.setPieceAt(new Position(7, 4), king); // e1
        board.setPieceAt(new Position(7, 0), enemyRook); // a1 (attacks rank 1)

        // King is in check. Cannot move to d1 or f1 (still rank 1).
        // Can move to e2, d2, f2.

        Move illegalMove = ruleEngine.generateMove(gameState, new Position(7, 4), new Position(7, 3)); // e1-d1
        assertNull(illegalMove, "Thi King can't step into check");

        Move legalMove = ruleEngine.generateMove(gameState, new Position(7, 4), new Position(6, 4)); // e1-e2
        assertNotNull(legalMove);
    }

    @Test
    void testCheckmateDetection() {
        // Fool's Mate
        // 1. f3 e5 2. g4 Qh4#
        board.initialSetup();

        // 1. white: f2 -> f3
        Move w1 = new Move(new Position(6, 5), new Position(5, 5), board.getPieceAt(new Position(6, 5)));
        gameState.makeMove(w1);

        // 2. black: e7 -> e5
        Move b1 = new Move(new Position(1, 4), new Position(3, 4), board.getPieceAt(new Position(1, 4)));
        gameState.makeMove(b1);

        // 3. white: g2 -> g4
        Move w2 = new Move(new Position(6, 6), new Position(4, 6), board.getPieceAt(new Position(6, 6)));
        gameState.makeMove(w2);

        // 4. black: Qd8 -> Qh4 (MATE)
        Move b2 = ruleEngine.generateMove(gameState, new Position(0, 3), new Position(4, 7));

        // Checking, if RuleEngine notices this move is mate
        assertNotNull(b2, "A mattoló lépésnek érvényesnek kell lennie");
        assertTrue(b2.isCheckmate(), "A lépésnek Sakk-Mattot kell jeleznie");

        // Executing move, so GameState recognizes mate
        gameState.makeMove(b2);

        // Would be White's ture, checking state
        assertTrue(ruleEngine.isCheckmate(gameState), "A játéknak sakk-matt állapotban kell lennie");
    }

    // --- 4. Castling tests ---

    @Test
    void testCastlingLegal() {
        Piece king = new King(true);
        Piece rook = new Rook(true);

        board.setPieceAt(new Position(7, 4), king); // e1
        board.setPieceAt(new Position(7, 7), rook); // h1 (Kingside)

        // Rights are true by default in new GameState

        Move castleMove = ruleEngine.generateMove(gameState, new Position(7, 4),
                new Position(7, 6)); // e1-g1
        assertNotNull(castleMove);
        assertTrue(castleMove.isCastling());
    }

    @Test
    void testCastlingIllegalThroughCheck() {
        Piece king = new King(true);
        Piece rook = new Rook(true);
        Piece enemyRook = new Rook(false);

        board.setPieceAt(new Position(7, 4), king); // e1
        board.setPieceAt(new Position(7, 7), rook); // h1

        // Enemy rook attacks f1 (the square the king crosses)
        board.setPieceAt(new Position(0, 5), enemyRook); // f8 attacking f-file

        Move castleMove = ruleEngine.generateMove(gameState, new Position(7, 4),
                new Position(7, 6));
        assertNull(castleMove, "Can' ccastle through a checkline");
    }

    // --- 5. Other tests ---

    @Test
    void testDiscoveredCheck() {
        // White Rook at e1, White Bishop at e2, Black King at e8
        // Bishop moves, revealing check from Rook
        board.setPieceAt(new Position(7, 4), new Rook(true)); // e1
        board.setPieceAt(new Position(6, 4), new Bishop(true)); // e2
        board.setPieceAt(new Position(0, 4), new King(false)); // e8

        Move move = ruleEngine.generateMove(gameState, new Position(6, 4), new Position(5, 3)); // Be2-d3

        assertNotNull(move);
        assertTrue(move.isCheck(), "We should be detecting a discovered check");
    }

    @Test
    void testPin() {
        // Absolute pin: King e1, Bishop e2, Enemy Rook e8
        // Bishop cannot move out of the e-file
        board.setPieceAt(new Position(7, 4), new King(true));  // e1 (Király)
        board.setPieceAt(new Position(6, 4), new Queen(true)); // e2 (Királynő - Futó helyett)
        board.setPieceAt(new Position(0, 4), new Rook(false)); // e8 (Ellenséges Bástya)

        // 1. Illegal: Kimozdulás a kötésből oldalra (e2 -> d3)
        Move illegalMove = ruleEngine.generateMove(gameState, new Position(6, 4), new Position(5, 3));
        assertNull(illegalMove, "Kötésben lévő bábu nem léphet ki a vonalból");

        // 2. Legal: Mozgás a kötésben lévő vonalon (e2 -> e3)
        // Ez most már működni fog, mert a Királynő tud előre lépni.
        Move legalMove = ruleEngine.generateMove(gameState, new Position(6, 4), new Position(5, 4));
        assertNotNull(legalMove, "Kötésben lévő bábu mozoghat a támadás vonalán");
    }
}