/**
 * Created by Rafael on 02/05/2017.
 *
 * @author Rafael
 * @since 02/05/2017
 */
public class GSPatch extends GameSparker {

    @Override
    public int readcookie(String var1) {
        return 11;
    }

    @Override
    public void savecookie(String var1, String var2) {
    }
}
