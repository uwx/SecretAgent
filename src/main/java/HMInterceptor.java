import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
        //System.out.println("Sleep " + Arrays.toString(dates));

        long delay = 46 - ((System.currentTimeMillis() - dates[0]));
        //System.out.println("Delay: " + delay + " / game predicted " + ms);

        //int fase = getFase();
        Thread.sleep(delay > 16 ? delay : 16); // min out at 16ms to prevent flickering
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

    public static class RStore {
        RadicalBASS bass;
        boolean isLoaded;
    }
    //
    private static final Map<String, RStore> mods = new HashMap<>();
    private static String lastMod = null;

    public static void load(String mod) {
        System.out.println("Loading " + mod);
        RStore rs = new RStore();
        rs.bass = new RadicalBASS(new File(SuperRunApp.rootDir, mod));
        rs.isLoaded = true;
        mods.put(mod, rs);
    }

    public static void play(String mod) {
        System.out.println("Playing " + mod);
        RStore rs = mods.get(mod);
        // can't have two mods loaded at once!!!! and nfm1 only unloads at game exit, so we have to unload manually
        if (lastMod != null && !lastMod.equals(mod)) {
            System.out.println("Trashing " + lastMod);
            RStore rs2 = mods.get(lastMod);
            if (rs2.isLoaded) {
                rs2.bass.unload();
                rs2.isLoaded = false;
            }
        }
        if (!rs.isLoaded) {
            System.out.println("ReLoading " + mod);
            rs.bass = new RadicalBASS(new File(SuperRunApp.rootDir, mod));
            rs.isLoaded = true;
        }
        rs.bass.play();
        lastMod = mod;
    }

    public static void pause(String mod) {
        System.out.println("Pausing " + mod);
        RStore rs = mods.get(mod);
        rs.bass.setPaused(true);
    }

    public static void unload(String mod) {
        System.out.println("Unloading " + mod);
        RStore rs = mods.get(mod);
        rs.bass.unload();
        rs.isLoaded = false;
    }
}
