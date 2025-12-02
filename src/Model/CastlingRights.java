package Model;

// helper class for GameState
public class CastlingRights{
    boolean whiteCastleKingSide;
    boolean whiteCastleQueenSide;
    boolean blackCastleKingSide;
    boolean blackCastleQueenSide;

    // Default constructor
    CastlingRights(){
        this.whiteCastleKingSide = true;
        this.whiteCastleQueenSide = true;
        this.blackCastleKingSide = true;
        this.blackCastleQueenSide = true;
    }

    // Copying constructor
    public CastlingRights(CastlingRights other){
        this.whiteCastleKingSide = other.whiteCastleKingSide;
        this.whiteCastleQueenSide = other.whiteCastleQueenSide;
        this.blackCastleKingSide = other.blackCastleKingSide;
        this.blackCastleQueenSide = other.blackCastleQueenSide;
    }
    
    public boolean canWhiteCastleKingSide(){
        return whiteCastleKingSide;
    }

    public boolean canWhiteCastleQueenSide(){
        return whiteCastleQueenSide;
    }

    public boolean canBlackCastleKingSide(){
        return blackCastleKingSide;
    }

    public boolean canBlackCastleQueenSide(){
        return blackCastleQueenSide;
    }
}