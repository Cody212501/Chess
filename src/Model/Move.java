package Model;

import Model.Pieces.*;

public class Move{
    private final Position from;
    private final Position to;
    private final Piece pieceMoved;

    //Additional info
    private Piece pieceCaptured;
    private Piece promotionPiece;

    public Move(Position from, Position to, Piece pieceMoved) {
        this.from = from;
        this.to = to;
        this.pieceMoved = pieceMoved;
        this.pieceCaptured = null;
        this.promotionPiece = null;
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

    public boolean isPromotion() {
        return promotionPiece != null;
    }

    public void setPromotionPiece(Piece promotionPiece) {
        this.promotionPiece = promotionPiece;
    }

    @Override
    public String toString() {
        // Simple string representation for possible debugging
        return pieceMoved.getType() + " from " + from + " to " + to;
    }
}