import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Created by Rafael on 02/05/2017.
 *
 * @author Rafael
 * @since 02/05/2017
 */
public class GSPatch extends GameSparker {
    // todo
    @Override
    public int readcookie(String var1) {
        try {
            Path file = Paths.get(SuperRunApp.originalRoot, "gameData", SuperRunApp.gameHash, var1 + ".txt");
            int i = Integer.parseInt(Files.readAllLines(file, StandardCharsets.UTF_8).get(0));
            System.err.println("Reading cookie " + var1 + ":" + i);
            return i;
        } catch (IOException e) {
            if ((e instanceof NoSuchFileException)) {
                System.err.println("No saved data for " + var1);
            } else {
                e.printStackTrace();
            }
            return 0;
        }
    }

    @Override
    public void savecookie(String var1, String var2) {
        Path folder = Paths.get(SuperRunApp.originalRoot, "/gameData/", SuperRunApp.gameHash);
        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
        } catch (IOException e) {
            System.err.println("Could not mkdirs " + folder);
            e.printStackTrace();
            return;
        }
        System.out.println("Writing cookie " + var1 + ":" + var2);
        try {
            Files.write(folder.resolve(var1 + ".txt"), Collections.singleton(var2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
