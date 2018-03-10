
import jouvieje.bass.BassInit;
import jouvieje.bass.exceptions.BassException;

import java.lang.reflect.Field;

/**
 * The Class BASSLoader. This class loads the BASS library. <br>
 * <br>
 * This is a utility class, so it can't be inherited.
 */
final class BASSLoader {

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
        System.out.println("%PATH% is " + System.getProperty("java.library.path"));

        System.setProperty("org.lwjgl.librarypath", ".\\libraries\\dlls\\win" + System.getProperty("sun.arch.data.model") + "\\");

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
        System.setProperty("java.library.path", System.getProperty("java.library.path") + (SuperRunApp.isWindows() ? ";" : ":") + s);

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

        //Bass.BASS_Init(-1, forceFrequency(44100), 0, null, null);
        RadicalBASS.init = true;
    }

    /**
     * Printf exit.
     *
     * @param format the format
     * @param args the args
     */
    private static void printfExit(final String format, final Object... args) {
        System.out.println(String.format(format, args));
    }
}
