package Model;

public class Player{
    private String name;
    private int elo;

    /**
     * Default constructor for JSON deserialisation (e.g., by Gson).
     */
    public Player() {}

    public Player(String name, int elo) {
        this.name = name;
        this.elo = elo;
    }

    // These are necessary for data binding and JSON serialisation.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    @Override
    public String toString() {
        return name + " (" + elo + ")";
    }
}