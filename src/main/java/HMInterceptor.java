/**
 * Created by Rafael on 24/05/2017.
 *
 * @author Rafael
 * @since 24/05/2017
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class HMInterceptor {
    public static xtGraphics xtg;
    private static int createdDates;
    private static long[] dates = new long[2];

    private static boolean fcd;

    public static void sleep(long ms) throws InterruptedException {
        //System.out.println("Tried sleeping for " + ms);

        long delay = 46 - ((System.currentTimeMillis() - dates[0]));
        System.out.println("Delay: " + delay + " / game predicted " + ms);

        int fase = getFase();
        if (fase == 0 || fase == 1 || fase == 2 || fase == 3) {
            Thread.sleep(delay > 16 ? delay : 16); // min out at 16ms to prevent flickering
        } else {
            Thread.sleep(ms); // nfm runs at 27fps in some menus and those seem fine so let it do its thing
        }
        createdDates = 0;
    }

    public static void newDate() {
        if (SuperRunApp.datecnt == 3 && !fcd) {
            // nfm2 and some versions might have one more date than expected at the start, only once in the game.
            // this accoutns for that.
            fcd = true;
        } else {
            dates[createdDates] = System.currentTimeMillis();
            createdDates++;
        }
    }

    private static int getFase() {
        return xtg == null ? -1 : xtg.fase;
    }
}
