package Model;

import Model.Pieces.*;

public class Move{
    private final Position from;
    private final Position to;
    private final Piece pieceMoved;

    //Additional info
    private Piece pieceCaptured;
    private Piece promotionPiece;

    //Flags for special moves
    private boolean isCastling = false;
    private boolean isEnPassant = false;
    private boolean isPromotion = false;

    //Flags for PGN formatting
    private boolean isCheck = false;
    private boolean isCheckmate = false;

    public Move(Position from, Position to, Piece pieceMoved) {
        this.from = from;
        this.to = to;
        this.pieceMoved = pieceMoved;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getPieceMoved() {
        return pieceMoved;
    }

    public Piece getPieceCaptured() {
        return pieceCaptured;
    }

    public void setPieceCaptured(Piece pieceCaptured) {
        this.pieceCaptured = pieceCaptured;
    }

    //special move functions
    public boolean isPromotion() {
        return promotionPiece != null;
    }
    public void setPromotion(boolean promotion) { isPromotion = promotion; }

    public Piece getPromotionPiece() { return promotionPiece; }
    public void setPromotionPiece(Piece promotionPiece) {
        this.promotionPiece = promotionPiece;
        this.isPromotion = (promotionPiece != null);
    }

    public boolean isCastling() { return isCastling; }
    public void setCastling(boolean castling) { isCastling = castling; }

    public boolean isEnPassant() { return isEnPassant; }
    public void setEnPassant(boolean enPassant) { isEnPassant = enPassant; }

    public boolean isCheck() { return isCheck; }
    public void setCheck(boolean check) { isCheck = check; }

    public boolean isCheckmate() { return isCheckmate; }
    public void setCheckmate(boolean checkmate) { isCheckmate = checkmate; }

    @Override
    public String toString() {
        // Simple string representation for possible debugging
        return pieceMoved.getType() + " from " + from + " to " + to;
    }
}