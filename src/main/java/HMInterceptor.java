/**
 * Created by Rafael on 24/05/2017.
 *
 * @author Rafael
 * @since 24/05/2017
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class HMInterceptor {
    public static xtGraphics xtg;

    public static void sleep(long ms) throws InterruptedException {
        //System.out.println("Tried sleeping for " + ms);

        int fase = getFase();
        if (fase == 0 || fase == 1 || fase == 2 || fase == 3) {
            Thread.sleep(46);
        } else {
            Thread.sleep(ms);
        }
    }

    private static int getFase() {
        return xtg == null ? -1 : xtg.fase;
    }
}
