import java.io.File;

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
        return 11;
    }

    @Override
    public void savecookie(String var1, String var2) {
        if (new File(SuperRunApp.originalRoot + "/cookies/"))
    }
}
