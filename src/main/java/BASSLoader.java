import jouvieje.bass.BassInit;
import jouvieje.bass.exceptions.BassException;

import java.lang.reflect.Field;

/**
 * The Class BASSLoader. This class loads the BASS library. <br>
 * <br>
 * This is a utility class, so it can't be inherited.
 */
public final class BASSLoader {
    private static final String operatingSystem = System.getProperty("os.name").toLowerCase();
    public static final String is64bit = System.getProperty("sun.arch.data.model").equals("64") ? "64" : "32";
    public static final boolean isUnix = operatingSystem.indexOf("nix") == 0 || operatingSystem.indexOf("nux") == 0;
    public static final boolean isWindows = operatingSystem.indexOf("win") == 0;
    public static final boolean isMac = operatingSystem.indexOf("mac") == 0;

    private static final boolean SUPPRESS_CONSOLE_OUTPUT = true;

    /**
     * Don't let anyone instantiate this class.
     */
    private BASSLoader() {
    }

    /**
     * Initialize NativeBASS. This method loads all necessary DLLs by modifying the java.library.path property.<br>
     * Yes, it's a dirty hack. But it works.
     */
    static void initializeBASS() {
        try {
            if (isUnix) {
                appendToPath("./libraries/dlls/linux" + is64bit + "/");
            } else if (isMac) {
                appendToPath("./libraries/dlls/mac/");
            } else if (isWindows) {
                appendToPath(".\\libraries\\dlls\\win" + is64bit + "\\");
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored) {
        }

        init();
    }

    //private static final String workingDirectory = System.getProperty("user.dir");

    /**
     * Append to path.
     *
     * @param s the s
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
    private static void appendToPath(final String s) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        System.setProperty("java.library.path", System.getProperty("java.library.path") + (isWindows ? ";" : ":") + s);

        final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(null, null);
    }

    @SuppressWarnings("unused")
    private static void init() {
        /*
         * NativeBass Init
         */
        try {
            BassInit.DEBUG = !SUPPRESS_CONSOLE_OUTPUT;
            BassInit.loadLibraries();
        } catch (final BassException e) {
            printfExit("NativeBass error! %s\n", e.getMessage());
            return;
        }

        /*
         * Checking NativeBass version
         */
        if (BassInit.NATIVEBASS_LIBRARY_VERSION() != BassInit.NATIVEBASS_JAR_VERSION()) {
            printfExit("Error!  NativeBass library version (%08x) is different to jar version (%08x)\n", BassInit.NATIVEBASS_LIBRARY_VERSION(), BassInit.NATIVEBASS_JAR_VERSION());
            return;
        }

        /*==================================================*/

        RadicalBASS.init = true;
    }

    /**
     * Printf exit.
     *
     * @param format the format
     * @param args the args
     */
    private static void printfExit(final String format, final Object... args) {
        final String s = String.format(format, args);
        System.out.println(s);
    }
}
