import Model.*;
import Model.IOs.*;
import Model.Pieces.*;

import java.io.*;
import java.nio.file.*;

import com.google.gson.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class FileTests {
    private JsonPersistence jsonPersistence;
    private PGNParser pgnParser;
    private GameState sampleGameState;

    // JUnit's @TempDir annotation automatically creates (and deletes) a temporary directory for the tests.
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jsonPersistence = new JsonPersistence();
        pgnParser = new PGNParser();

        // Létrehozunk egy minta játékállapotot néhány lépéssel
        sampleGameState = new GameState();
        sampleGameState.setPlayers(new Player("WhiteTest", 1200), new Player("BlackTest", 1200));

        // We simulate 1-2 moves, so the game's history won't be empty
        // 1. e4
        Pawn whitePawn = new Pawn(true);
        Move m1 = new Move(new Position(6, 4), new Position(4, 4), whitePawn);
        sampleGameState.makeMove(m1);

        // 1. -- e5 (the most played opening in the game)
        Pawn blackPawn = new Pawn(false);
        Move m2 = new Move(new Position(1, 4), new Position(3, 4), blackPawn);
        sampleGameState.makeMove(m2);
    }

    // --- JSON Tests ---

    @Test
    void testJsonSaveAndLoadSuccess() throws IOException {
        // 1. Saving
        File jsonFile = tempDir.resolve("test_game.json").toFile();
        jsonPersistence.saveGame(sampleGameState, jsonFile.getPath());

        assertTrue(jsonFile.exists(), "The JSON file should exist after saving");
        assertTrue(jsonFile.length() > 0, "File can't be empty");

        // 2. Betöltés
        GameState loadedState = jsonPersistence.loadGame(jsonFile.getPath());

        assertNotNull(loadedState, "A betöltött állapot nem lehet null");
        assertEquals(2, loadedState.getMoveHistory().size(), "A lépések számának egyeznie kell");
        assertEquals("WhiteTest", loadedState.getWhitePlayer().getName(), "A játékos nevének egyeznie kell");

        // Ellenőrizzük, hogy a tábla állása is helyreállt-e (pl. e4-en van-e a paraszt)
        assertNotNull(loadedState.getBoard().getPieceAt(new Position(4, 4)), "Az e4 mezőn parasztnak kell lennie");
    }

    @Test
    void testJsonLoadFileNotFound() {
        // Olyan fájlt próbálunk betölteni, ami nem létezik
        File nonExistentFile = tempDir.resolve("ghost.json").toFile();

        assertThrows(IOException.class, () -> {
            jsonPersistence.loadGame(nonExistentFile.getPath());
        }, "IOException-t kell dobnia, ha a fájl nem létezik");
    }

    @Test
    void testJsonLoadCorruptedFile() throws IOException {
        // Létrehozunk egy fájlt érvénytelen tartalommal (nem JSON)
        File badFile = tempDir.resolve("bad.json").toFile();
        Files.writeString(badFile.toPath(), "{ ez nem valid json ... ");

        // A Gson JsonSyntaxException-t (vagy RuntimeException-t) dob, ha a formátum rossz
        assertThrows(JsonSyntaxException.class, () -> {
            jsonPersistence.loadGame(badFile.getPath());
        }, "Hibát kell dobnia érvénytelen JSON tartalom esetén");
    }

    // --- PGN TESZTEK ---

    @Test
    void testPgnExportSuccess() throws IOException {
        File pgnFile = tempDir.resolve("test_game.pgn").toFile();

        // Exportáljuk a minta játékot
        pgnParser.exportGame(sampleGameState, pgnFile.getPath());

        assertTrue(pgnFile.exists());

        // Olvassuk vissza szövegként és ellenőrizzük a tartalmat
        String content = Files.readString(pgnFile.toPath());

        assertTrue(content.contains("[White \"WhiteTest\"]"), "A fejlécnek tartalmaznia kell a fehér játékos nevét");
        assertTrue(content.contains("1. e4 e5"), "A lépéseknek szerepelniük kell a fájlban");
    }

    @Test
    void testPgnImportSuccess() throws IOException {
        // Létrehozunk egy egyszerű, valid PGN fájlt
        File pgnFile = tempDir.resolve("valid.pgn").toFile();
        String pgnContent = """
                [Event "Test Game"]
                [White "PlayerA"]
                [Black "PlayerB"]
                
                1. e4 e5 2. Nf3 Nc6 *
                """;
        Files.writeString(pgnFile.toPath(), pgnContent);

        // Importálás
        GameState importedState = pgnParser.importGame(pgnFile.getPath());

        assertNotNull(importedState);
        assertEquals("PlayerA", importedState.getWhitePlayer().getName());
        assertEquals(4, importedState.getMoveHistory().size(), "4 fél-lépésnek (ply) kell lennie");

        // Ellenőrizzük az utolsó lépést (Nc6 -> c6-on Huszár van-e)
        // c6 = row 2, col 2 (mert row 0 a teteje, de a Position logikád lehet más, ellenőrizzük)
        // A PGN parsered a Position(row, col)-t használja.
        // e4: (4, 4), e5: (3, 4) ... (ha a White lent van)
        // A te kódodban a 0. sor a teteje (fekete), a 7. az alja (fehér).
        // Nc6 -> Fekete Huszár a c6-ra. c6 a 2. sor (index) 2. oszlop (index).
        // A te kódodban (Position): row=2, col=2.

        assertNotNull(importedState.getBoard().getPieceAt(new Position(2, 2)), "A c6 mezőn bábu kell legyen (Nc6)");
    }

    @Test
    void testPgnImportFileNotFound() {
        File nonExistent = tempDir.resolve("missing.pgn").toFile();
        assertThrows(IOException.class, () -> {
            pgnParser.importGame(nonExistent.getPath());
        });
    }

    @Test
    void testPgnImportInvalidMove() throws IOException {
        // Olyan PGN, ami lehetetlen lépést tartalmaz (pl. gyalog teleportál)
        File invalidMoveFile = tempDir.resolve("invalid_move.pgn").toFile();
        String content = "1. e4 e5 2. Ke8 *"; // A király nem ugorhat e8-ra a startról (e1) gyalogokon át
        Files.writeString(invalidMoveFile.toPath(), content);

        // A PGNParser-ed jelenlegi implementációja logolja a hibás lépéseket ("Skipping unparseable...")
        // Ez a teszt azt ellenőrzi, hogy a program nem omlik össze, és a hibás lépést nem hajtja végre.

        GameState state = pgnParser.importGame(invalidMoveFile.getPath());

        // Csak az első 2 lépést (e4, e5) kellett volna sikerrel feldolgoznia
        assertEquals(2, state.getMoveHistory().size(), "A hibás lépést (Ke8) nem szabadott volna végrehajtani");
        //megtörténik, de dob egy error printet is
    }
}