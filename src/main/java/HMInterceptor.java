import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
        //System.out.println("Delay: " + delay + " / game predicted " + ms);

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

    public static void log(Throwable e) {
        System.err.println("Caught internal: " + e);
    }

    public static InputStream getStreamFromJar(String name) {
        try(JarFile file = new JarFile(SuperRunApp.jarPath)) {
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryPath = entry.toString();
                if (entryPath.equals(name)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    try (InputStream is = file.getInputStream(entry)) {
                        int readBytes;
                        while ((readBytes = is.read()) != -1) {
                            baos.write((char) readBytes);
                        }
                    }

                    return new ByteArrayInputStream(baos.toByteArray());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.print("Found no file " + name + " or errored!");
        return null;
    }
}
