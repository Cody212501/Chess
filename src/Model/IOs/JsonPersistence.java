package Model.IOs;

import Model.*;
import Model.Pieces.*;

import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;

public class JsonPersistence{
    private final Gson gson;

    public JsonPersistence(){
        // Registering our own deserializer for the Piece class(fromJSON can't handle it otherwise)
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Piece.class, new PieceDeserializer());
        builder.setPrettyPrinting();

        //Use pretty printing for readable JSON files (good for debugging).
        this.gson = builder.create();
    }

    /**
     * An inner class, it's purpose is so when we are reading in JSON files,
     * the "type" field gets assigned a correct corresponding piece descendant.
     */
    private static class PieceDeserializer implements JsonDeserializer<Piece> {
        @Override
        public Piece deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            // Reading type and colour from JSON
            String type = jsonObject.get("type").getAsString();
            boolean isWhite = jsonObject.get("isWhite").getAsBoolean();

            // based on type, we create a corresponding piece
            return switch(type){
                case "PAWN" -> new Pawn(isWhite);
                case "ROOK" -> new Rook(isWhite);
                case "KNIGHT" -> new Knight(isWhite);
                case "BISHOP" -> new Bishop(isWhite);
                case "QUEEN" -> new Queen(isWhite);
                case "KING" -> new King(isWhite);
                default -> throw new JsonParseException("Ismeretlen bábu típus: " + type);
            };
        }
    }

    /**
     * Serialises the entire GameState object to a JSON file.
     *
     * @param state The current GameState to save.
     * @param filePath The path to the file where the game will be saved.
     * @throws IOException if an error occurs during writing.
     */
    public void saveGame(GameState state, String filePath) throws IOException {
        // Use try-with-resources to ensure the writer is closed automatically.
        try(Writer writer = new FileWriter(filePath)){
            gson.toJson(state, writer);
        }
    }

    /**
     * Deserialises a GameState object from a JSON file.
     *
     * @param filePath The path to the file to load.
     * @return The reconstructed GameState object.
     * @throws IOException if an error occurs during reading.
     */
    public GameState loadGame(String filePath) throws IOException {
        // Use try-with-resources to ensure the reader is closed.
        try(Reader reader = Files.newBufferedReader(Paths.get(filePath))){
            // Gson handles the reconstruction of the entire object graph.
            return gson.fromJson(reader, GameState.class);
        }
    }
}