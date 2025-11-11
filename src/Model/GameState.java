package Model;

import java.util.*;

public class GameState{
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private boolean isWhiteTurn;

    // A JTable (lépéslista) modellje is ebből fog dolgozni
    private List<Move> moveHistory;

    // A döntetlen-ajánlási logika követéséhez
    private boolean canWhiteOfferDraw;
    private boolean canBlackOfferDraw;

    // Ide jöhetnek még az időzítők állapotai (pl. long whiteTimeLeft)

    public GameState() {
        // Alaphelyzet beállítása
        this.board = new Board(); // Feltölti a bábukkal
        this.moveHistory = new ArrayList<>();
        this.isWhiteTurn = true;
        this.canWhiteOfferDraw = true;
        this.canBlackOfferDraw = true;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }


    public void setPlayers(Player white, Player black) {
        this.whitePlayer = white;
        this.blackPlayer = black;
    }

    // Ez a metódus hajtja végre a lépést és frissíti az állapotot
    public void makeMove(Move move) {
        board.applyMove(move); // Logikai lépés a táblán
        moveHistory.add(move);
        isWhiteTurn = !isWhiteTurn;

        // Döntetlen logika: ha a másik lép, újra ajánlhatsz
        if (isWhiteTurn) {
            canWhiteOfferDraw = true;
        } else {
            canBlackOfferDraw = true;
        }
    }

    //Draw-offer
    public boolean canCurrentPlayerOfferDraw() {
        return isWhiteTurn ? canWhiteOfferDraw : canBlackOfferDraw;
    }

    public void recordDrawOfferRejection() {
        if (isWhiteTurn) {
            canWhiteOfferDraw = false;
        } else {
            canBlackOfferDraw = false;
        }
    }
}