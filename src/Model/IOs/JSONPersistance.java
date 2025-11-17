package Model.IOs;

import Model.*;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

public class JSONPersistance{
    private final Gson gson;

    public JsonPersistence() {
        //Use pretty printing for readable JSON files (good for debugging).
        this.json = new GsonBuilder().setPrettyPrinting().create();
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
        try (Writer writer = new FileWriter(filePath)) {
            json.toJson(state, writer);
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
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            // Gson handles the reconstruction of the entire object graph.
            GameState state = json.fromJson(reader, GameState.class);
            return state;
        }
    }
}