package Model.IOs;

import Model.*;
import Model.Pieces.*;

import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * Formats a GameState into a PGN (Portable Game Notation) string.
 *
 * This class assumes that the Move objects within the GameState's history
 * have been "tagged" with relevant information by the RuleEngine at the
 * time of their creation (e.g., isCheck, isCheckmate, isCastling).
 *
 * The most complex part is handling move disambiguation
 * (e.g., determining "Nbd2" vs. "Nfd2").
 */
public class PGNFormatter{
    // A local board used for re-simulating the game to check for ambiguities
    private Board simulationBoard;

    // We need a RuleEngine to check moves for other pieces during simulation
    private RuleEngine ambiguityEngine;

    public PGNFormatter(){
        this.simulationBoard = new Board();
        // RuleEngine is initialised without arguments
        this.ambiguityEngine = new RuleEngine();
    }

    /**
     * Main public method to format a game.
     * @param state The GameState to format.
     * @return A String containing the full PGN text.
     */
    public String format(GameState state){
        StringBuilder sb = new StringBuilder();

        // 1. Append Tag Pairs (Headers)
        appendTagPairs(sb, state);
        sb.append("\n"); // Blank line between tags and movetext

        // 2. Append Movetext
        appendMoveText(sb, state);

        // 3. Append Result
        // Check the FINAL state of the game
        RuleEngine finalRuleEngine = new RuleEngine();

        if (finalRuleEngine.isCheckmate(state)) {
            // If it is White's turn - and it's checkmate - White lost -> 0-1
            if (state.isWhiteTurn()) {
                sb.append(" 0-1");
            } else {
                sb.append(" 1-0");
            }
        } else if (finalRuleEngine.isStalemate(state)) {
            sb.append(" 1/2-1/2");
        } else {
            // The game is still in progress or abandoned
            sb.append(" *");
        }

        return sb.toString();
    }

    /**
     * Appends the PGN tag pairs (e.g., [White "Name"]).
     */
    private void appendTagPairs(StringBuilder sb, GameState state){
        // Get player data, providing defaults if null
        Player white = state.getWhitePlayer() != null ? state.getWhitePlayer() : new Player("White", 0);
        Player black = state.getBlackPlayer() != null ? state.getBlackPlayer() : new Player("Black", 0);

        // Get current date
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        // Append the "Seven Tag Roster"
        appendTag(sb, "Event", "Local Game");
        appendTag(sb, "Site", "Unknown"); // Or get from user
        appendTag(sb, "Date", date);
        appendTag(sb, "Round", "?");
        appendTag(sb, "White", white.getName());
        appendTag(sb, "Black", black.getName());
        appendTag(sb, "Result", "*"); // The result is also appended at the end

        // Append non-standard, but useful tags (ELO)
        if(white.getElo() > 0) appendTag(sb, "WhiteElo", String.valueOf(white.getElo()));
        if(black.getElo() > 0) appendTag(sb, "BlackElo", String.valueOf(black.getElo()));
    }

    /**
     * Helper to format a single [Tag "Value"] line.
     */
    private void appendTag(StringBuilder sb, String tag, String value){
        sb.append("[").append(tag).append(" \"").append(value).append("\"]\n");
    }

    /**
     * Appends the main move text (e.g., "1. e4 e5 2. Nf3 Nc6").
     * This requires re-simulating the game to find move ambiguities.
     */
    private void appendMoveText(StringBuilder sb, GameState state){
        // We must re-simulate the game move by move to check for ambiguities
        this.ambiguityEngine = new RuleEngine();

        int moveNumber = 1;

        for(int i = 0; i < state.getMoveHistory().size(); i++){
            Move move = state.getMoveHistory().get(i);

            // 1. Append move number (e.g., "1. ") for White's move
            if(i % 2 == 0){
                sb.append(moveNumber).append(". ");
                moveNumber++;
            }

            // 2. Generate the Standard Algebraic Notation (SAN) for this move
            String san = generateSanForMove(move);
            sb.append(san).append(" ");

            // 3. Apply this move to our local simulation board
            // This is crucial for the *next* move's ambiguity check.
            this.simulationBoard.applyMove(move);
        }
    }

    /**
     * This is the core logic. Converts a Move object into a SAN string.
     * @param move The Move object (assumed to be tagged with check/mate etc.)
     * @return A string like "Nf3", "exd5", "O-O", "e8=Q#"
     */
    private String generateSanForMove(Move move){
        Piece piece = move.getPieceMoved();

        //1. Handle Castling (Special Case)
        // We rely on the move being tagged.
        if(move.isCastling()){
            // Kingside (short) castle
            if(move.getTo().column() == 6) return "O-O" + getCheckOrMateSymbol(move);
            // Queenside (long) castle
            if(move.getTo().column() == 2) return "O-O-O" + getCheckOrMateSymbol(move);
        }

        StringBuilder san = new StringBuilder();

        //2. Piece Symbol (e.g., "N", "B", "Q". Pawns are empty)
        String pieceSymbol = getPgnPieceSymbol(piece);
        san.append(pieceSymbol);

        //3. Handle Captures (e.g., "x")
        boolean isCapture =(move.getPieceCaptured() != null);

        if(isCapture && piece.getType() == PieceType.PAWN){
            // Pawn captures include the departure file (e.g., "exd5")
            san.append(getFileChar(move.getFrom().column()));
        }

        //4. Disambiguation (The Hardest Part)
        // If it's not a pawn, we check if another identical piece
        // could have moved to the same square.
        if(piece.getType() != PieceType.PAWN){
            String disambiguation = findDisambiguation(move);
            san.append(disambiguation);
        }

        //5. Add Capture 'x'
        if(isCapture){
            san.append("x");
        }

        //6. Target Square (e.g., "f3", "d5")
        san.append(positionToNotation(move.getTo()));

        //7. Promotion (e.g., "=Q")
        if(move.isPromotion()){
            san.append("=").append(getPgnPieceSymbol(move.getPromotionPiece()));
        }

        //8. Check/Checkmate (e.g., "+", "#")
        san.append(getCheckOrMateSymbol(move));

        return san.toString();
    }

    /**
     * Checks if another piece of the same type could have moved to the
     * same target square.
     * @param move The move being made.
     * @return A disambiguation string (e.g., "b", "1", "b1") or "" if clear.
     */
    private String findDisambiguation(Move move){
        Piece movingPiece = move.getPieceMoved();
        Position target = move.getTo();
        boolean isWhite = movingPiece.isWhite();

        Position ambiguitySource = null; // Position of the *other* piece in question

        // Iterate over the whole (simulation) board
        for(int r = 0; r < 8; r++){
            for(int c = 0; c < 8; c++){
                Position currentPos = new Position(r, c);

                // Skip the piece that is actually moving
                if(currentPos.equals(move.getFrom())) continue;

                Piece otherPiece = this.simulationBoard.getPieceAt(currentPos);

                // Check if we can fund a piece of the same type and color
                if(otherPiece != null &&
                        otherPiece.isWhite() == isWhite &&
                        otherPiece.getType() == movingPiece.getType()){
                    // Found another piece of the same type and colour.
                    // Can it also LEGALLY move to the target square?

                    // 1. Create a temporary GameState representing the current simulation step.
                    //    we have added this constructor already.
                    GameState tempState = new GameState(this.simulationBoard, isWhite);

                    // 2. Ask the RuleEngine for valid moves for this 'otherPiece'.
                    //    This checks for checks, pins, etc.
                    Set<Position> legalMoves = ambiguityEngine.getValidMovesForPiece(tempState, currentPos);

                    if(legalMoves.contains(target)){
                        // AMBIGUITY FOUND: Another piece can legally move to the same square.
                        ambiguitySource = currentPos;
                        break; // We found at least one ambiguity, that's enough to require disambiguation logic
                    }
                }
            }
            if(ambiguitySource != null){
                break;
            }
        }

        if(ambiguitySource == null){
            return ""; // No ambiguity
        }

        //If ambiguity exists, resolve it

        // 1. If files (columns) are different, use the file (e.g., "Nbd2")
        if(move.getFrom().column() != ambiguitySource.column()){
            return getFileChar(move.getFrom().column());
        }

        // 2. If files are same, use the rank (row) (e.g., "R1e2")
        if(move.getFrom().row() != ambiguitySource.row()){
            return getRankChar(move.getFrom().row());
        }

        // 3. If file AND rank are same (e.g., promoted pawns), use full coordinates (e.g. "Qe1e5")
        // This is rare but possible.
        return positionToNotation(move.getFrom());
    }


    //Helpers
    /**
     * Gets the single-character PGN symbol for a piece.
     * Note: Pawn is an empty string.
     */
    private String getPgnPieceSymbol(Piece piece){
        switch(piece.getType()){
            case KNIGHT: return "N";

            case BISHOP: return "B";

            case ROOK:   return "R";

            case QUEEN:  return "Q";

            case KING:   return "K";

            case PAWN:
            default:     return "";
        }
    }

    /**
     * Returns "#" for checkmate, "+" for check, or "" otherwise.
     * Relies on the Move object being tagged.
     */
    private String getCheckOrMateSymbol(Move move){
        if(move.isCheckmate()) return "#";
        if(move.isCheck()) return "+";
        return "";
    }

    /**
     * Converts a Position object (e.g., row=6, col=4) to notation (e.g., "e2").
     */
    private String positionToNotation(Position pos){
        return getFileChar(pos.column()) + getRankChar(pos.row());
    }

    /**
     * Gets the file character (e.g., 'a' for col 0).
     */
    private String getFileChar(int col){
        return String.valueOf((char) ('a' + col));
    }

    /**
     * Gets the rank character (e.g., '8' for row 0).
     */
    private String getRankChar(int row){
        return String.valueOf((char) ('8' - row));
    }
}