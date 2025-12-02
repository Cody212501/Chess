package Model.IOs;

import Model.*;
import Model.Pieces.*;

import java.io.*;
import java.util.regex.*;
import java.nio.file.*;

/**
 * Handles import and export of games in PGN (Portable Game Notation) format.
 *
 * This is a highly complex task.
 * EXPORTING requires converting (from, to) coordinates into Standard
 * Algebraic Notation (e.g., "Nf3", "Qxa4+", "O-O", "O-O-O"), which requires
 * checking for ambiguities (e.g., "Nbd2" vs "Nfd2").
 *
 * IMPORTING is even harder, as it requires a full chess parser
 * that understands the current board state to interpret moves like "e4" or "Nf3".
 */
public class PGNParser{
    /**
     * Exports the game to PGN format.
     * This method delegates the complex task of formatting (disambiguation, headers, etc.)
     * to the specialized PGNFormatter class.
     *
     * @param state The GameState to export.
     * @param filePath The target file path.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void exportGame(GameState state, String filePath) throws IOException {
        // 1. Generate the PGN content using the formatter
        PGNFormatter formatter = new PGNFormatter();
        String pgnContent = formatter.format(state);

        // 2. Write the content to the file
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))){
            writer.write(pgnContent);
        }
    }

    /**
     * Imports a PGN file.
     * This implementation is a basic parser that handles SAN moves (e.g. "e4", "Nf3").
     * It does NOT support recursive variations or comments ({}).
     *
     * @param filePath The path to the .pgn file.
     * @return A new GameState object with the played moves.
     */
    public GameState importGame(String filePath) throws IOException {
        GameState newState = new GameState();
        RuleEngine ruleEngine = new RuleEngine(); // We need logic to disambiguate SAN

        String content = Files.readString(Paths.get(filePath));

        // 1. Parse Tags (simplistic approach)
        parseTags(content, newState);

        // 2. Extract Move Text (remove tags)
        String moveText = content.replaceAll("\\[.*?]", "");
        // Remove move numbers (1. 2. etc) and results (1-0 etc)
        moveText = moveText.replaceAll("\\d+\\.|1-0|0-1|1/2-1/2|\\*", "");
        // Replace newlines with spaces
        moveText = moveText.replace("\n", " ").replace("\r", " ");

        // Split by spaces
        String[] tokens = moveText.trim().split("\\s+");

        for(String token : tokens){
            if(token.isEmpty()) continue;

            // Try to perform the move
            Move move = parseSanMove(newState, ruleEngine, token);
            if(move != null){
                newState.makeMove(move);
            }else{
                System.err.println("Skipping unparseable token: " + token);
            }
        }

        return newState;
    }

    private void parseTags(String content, GameState state){
        Player white = new Player("White", 0);
        Player black = new Player("Black", 0);

        Matcher mWhite = Pattern.compile("\\[White \"(.*?)\"]").matcher(content);
        if(mWhite.find()) white.setName(mWhite.group(1));

        Matcher mBlack = Pattern.compile("\\[Black \"(.*?)\"]").matcher(content);
        if(mBlack.find()) black.setName(mBlack.group(1));

        Matcher mWhiteElo = Pattern.compile("\\[WhiteElo \"(\\d+)\"]").matcher(content);
        if(mWhiteElo.find()) white.setElo(Integer.parseInt(mWhiteElo.group(1)));

        Matcher mBlackElo = Pattern.compile("\\[BlackElo \"(\\d+)\"]").matcher(content);
        if(mBlackElo.find()) black.setElo(Integer.parseInt(mBlackElo.group(1)));

        state.setPlayers(white, black);
    }

    /**
     * Parses a single Standard Algebraic Notation (SAN) move.
     * e.g. "e4", "Nf3", "O-O", "Rxe1+"
     */
    private Move parseSanMove(GameState state, RuleEngine engine, String san){
        // Clean the SAN (remove check/mate symbols)
        String cleanSan = san.replace("+", "").replace("#", "");

        // 1. Handle Castling
        if(cleanSan.equals("O-O") || cleanSan.equals("0-0")){
            return findCastlingMove(state, engine, true); // Kingside
        }
        if(cleanSan.equals("O-O-O") || cleanSan.equals("0-0-0")){
            return findCastlingMove(state, engine, false); // Queenside
        }

        // 2. Identify Target Square (last 2 chars)
        // e.g. "Nf3" -> f3, "exd5" -> d5, "Qh4" -> h4
        // Promotion case: "e8=Q" -> last 2 chars are "=Q", need to look before that
        String targetStr;
        String promotionChar = null;

        if(cleanSan.contains("=")){
            int eqIndex = cleanSan.indexOf("=");
            targetStr = cleanSan.substring(eqIndex - 2, eqIndex);
            promotionChar = cleanSan.substring(eqIndex + 1);
            cleanSan = cleanSan.substring(0, eqIndex); // Remove promotion part for parsing
        }else{
            if(cleanSan.length() < 2) return null;
            targetStr = cleanSan.substring(cleanSan.length() - 2);
        }
        Position targetPos = notationToPosition(targetStr);
        if(targetPos == null) return null;

        // 3. Identify Piece Type
        // If starts with Uppercase (B, N, R, Q, K), it's a piece. Otherwise, it is a Pawn.
        char firstChar = cleanSan.charAt(0);
        PieceType type = PieceType.PAWN; // Default

        if(Character.isUpperCase(firstChar)) {
            switch(firstChar) {
                case 'N': type = PieceType.KNIGHT; break;
                case 'B': type = PieceType.BISHOP; break;
                case 'R': type = PieceType.ROOK; break;
                case 'Q': type = PieceType.QUEEN; break;
                case 'K': type = PieceType.KING; break;
            }
            // Remove piece char for ambiguity checking
            cleanSan = cleanSan.substring(1);
        }

        // 4. Handle Ambiguity (e.g. "Nbd2", "R1e1", "exd5")
        // Remaining text in cleanSan (e.g. "bd2", "1e1", "xd5")
        // Actually, at this point cleanSan might be "bd2" (from Nbd2), "f3" (from Nf3).
        // We remove the target square from the end.
        String disambiguation = "";
        if(cleanSan.length() > 2){
            // Remove target square from end
            disambiguation = cleanSan.substring(0, cleanSan.length() - 2);
            // Remove capture 'x'
            disambiguation = disambiguation.replace("x", "");
        }

        // 5. Find the move using RuleEngine
        /**
         * We iterate over all pieces of the current player that match the type.
         * We check if they can move to 'targetPos'.
         * We filter by 'disambiguation' info if present.
         */
        for(int r = 0; r < 8; r++){
            for(int c = 0; c < 8; c++){
                Position currentPos = new Position(r, c);
                Piece p = state.getBoard().getPieceAt(currentPos);

                if(p != null && p.isWhite() == state.isWhiteTurn() && p.getType() == type){
                    // Check if matches disambiguation
                    if(!disambiguation.isEmpty()){
                        char fileChar = (char) ('a' + c);
                        char rankChar = (char) ('8' - r);
                        boolean matchesFile = disambiguation.indexOf(fileChar) >= 0;
                        boolean matchesRank = disambiguation.indexOf(rankChar) >= 0;

                        if(!matchesFile && !matchesRank) continue; // Matches neither info provided
                    }

                    // Check if legal move
                    Move legalMove = engine.generateMove(state, currentPos, targetPos);
                    if(legalMove != null){
                        // Handle Promotion Piece if parsed
                        if(legalMove.isPromotion() && promotionChar != null){
                            Piece promoPiece;
                            switch(promotionChar){
                                case "R": promoPiece = new Rook(state.isWhiteTurn()); break;
                                case "B": promoPiece = new Bishop(state.isWhiteTurn()); break;
                                case "N": promoPiece = new Knight(state.isWhiteTurn()); break;
                                default: promoPiece = new Queen(state.isWhiteTurn());
                            }
                            legalMove.setPromotionPiece(promoPiece);
                        }
                        return legalMove;
                    }
                }
            }
        }

        return null;
    }

    private Move findCastlingMove(GameState state, RuleEngine engine, boolean kingside){
        int r = state.isWhiteTurn() ? 7 : 0;
        Position kingPos = new Position(r, 4);
        Position targetPos = new Position(r, kingside ? 6 : 2);
        return engine.generateMove(state, kingPos, targetPos);
    }


    // Helper method
    private Position notationToPosition(String notation){
        if(notation.length() != 2) return null;
        int col = notation.charAt(0) - 'a';
        int row = '8' - notation.charAt(1);
        if(col < 0 || col > 7 || row < 0 || row > 7) return null;
        return new Position(row, col);
    }
}