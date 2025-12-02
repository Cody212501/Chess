package Model;

public class Player{
    private String name;
    private int elo;

    public Player(String name, int elo){
        this.name = name;
        this.elo = elo;
    }

    // These are necessary for data binding and JSON serialization.
    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getElo(){
        return elo;
    }

    public void setElo(int elo){
        this.elo = elo;
    }

    @Override
    public String toString(){
        return name + " (" + elo + ")";
    }
}