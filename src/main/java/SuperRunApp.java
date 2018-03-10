import holder.FrameHolder;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Handler;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rafael on 24/05/2017.
 *
 * @author Rafael
 * @since 24/05/2017
 */
public class SuperRunApp {
    public static int datecnt = 0;
    public static String jarPath;
    public static String rootDir;
    public static URL rootUrl;
    public static String originalRoot;

    public static PipedInputStream pipedInputStream = new PipedInputStream();
    public static PipedOutputStream pipedOutputStream;
    private static JTextArea textArea = new JTextArea(15, 30);
    public static String gameHash;

    static {
        try {
            pipedOutputStream = new PipedOutputStream(pipedInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class AnalyzingOutputStream extends FilterOutputStream {
        private final StringBuilder sb = new StringBuilder();

        public AnalyzingOutputStream (OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            apply(b);
            super.write(b);
        }

        private void apply(int b) {
            if (b == '\r')
                return;

            if (b == '\n') {
                final String text = sb.toString() + "\n";
                SwingUtilities.invokeLater(() -> textArea.append(text));
                sb.setLength(0);
                return;
            }

            sb.append((char) b);
        }
        // other overrides
    }

    public static void main(String[] args) throws URISyntaxException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        JFrame frame = new JFrame("Debug");
        frame.setLayout(new BorderLayout());

        frame.add(BorderLayout.CENTER, new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        frame.setVisible(true);

        ////Thread.currentThread().setContextClassLoader(MiscTools.addToClasspath());

        new Thread(() -> {
            while (FrameHolder.frame == null || !FrameHolder.frame.isEnabled() || !FrameHolder.frame.isShowing() || !FrameHolder.frame.isVisible()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            frame.toFront();
            FrameHolder.frame.toFront();
            frame.setSize(400, FrameHolder.frame.getHeight());
            frame.setLocation(FrameHolder.frame.getX() - frame.getWidth(), FrameHolder.frame.getY());

            FrameHolder.frame.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent e) {
                    frame.setSize(400, FrameHolder.frame.getHeight());
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    frame.setLocation(FrameHolder.frame.getX() - frame.getWidth(), FrameHolder.frame.getY());
                }

                @Override
                public void componentShown(ComponentEvent e) {

                }

                @Override
                public void componentHidden(ComponentEvent e) {

                }
            });
        }, "Frame size thread").start();

        runGame(args[0]);
    }

    private static String OS = System.getProperty("os.name").toLowerCase();

    private static boolean isWindows() {
        return OS.contains("win");
    }

    // add --illegal-access=deny to VM options
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
        System.setProperty("java.library.path", System.getProperty("java.library.path") + (isWindows() ? ";" : ":") + s);

        final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(null, null);
    }

    public static void runGame(String jarPath) throws IOException, URISyntaxException {
        SuperRunApp.jarPath = jarPath;

        gameHash = new CrockfordBase32().encodeToString(
                Hashing.goodFastHash(256)
                        .hashBytes(Files.readAllBytes(Paths.get(jarPath))).asBytes()).toLowerCase();
        System.out.println("Hash: " + gameHash);

        {
            System.setOut(new PrintStream(new AnalyzingOutputStream(System.out)));
            System.setErr(new PrintStream(new AnalyzingOutputStream(System.err)));
        }

        System.out.println(System.getProperty("java.class.path"));
//        try {
//            appendToPath(jarPath);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }

        try {
            ClassPool classPool = ClassPool.getDefault();
            classPool.insertClassPath(jarPath);
            CtClass gameSparker = classPool.get("GameSparker");

            CtMethod method = gameSparker.getDeclaredMethod("run");
            method.instrument(new ExprEditor() {
                @Override
                public void edit(NewExpr newExpr) throws CannotCompileException {
                    if (newExpr.getClassName().equals("java.util.Date")) {
                        newExpr.replace("{ HMInterceptor.newDate(); $_ = new java.util.Date(); }");
                        datecnt++;
                    }
                }

                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    try {
                        if (methodCall.getClassName().equals("java.lang.Thread") && Modifier.isStatic(methodCall.getMethod().getModifiers())) {
                            if (methodCall.getMethodName().equals("sleep")) {
                                methodCall.replace("{ HMInterceptor.sleep($1); }");
                            }
                        }
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

            // for fase detect to disable bad sleep in menus
            CtClass xtg = classPool.get("xtGraphics");
//            CtField _xtg = new CtField(cls2, "_xtg", cls2);
//            _xtg.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.TRANSIENT);
//            cls2.addField(_xtg);

            CtConstructor xtgConstructor = xtg.getDeclaredConstructors()[0];
            xtgConstructor.insertBeforeBody("{ HMInterceptor.xtg = this; }");
            xtgConstructor.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    if (methodCall.getMethodName().equals("getResourceAsStream")) {
                        methodCall.replace("{ $_ = HMInterceptor.getStreamFromJar($1); }");
                    }
                }
            });

            // remove performance fog adjust code
            CtClass medium = classPool.get("Medium");
            medium.getDeclaredMethod("adjstfade").setBody("{ }"); // might not be in nfm2?

            CtClass rdmod = classPool.get("RadicalMod");
//            CtField bassHolderField = new CtField(classPool.get("RadicalBASS"), "_bass", cls4);
//            bassHolderField.setModifiers(Modifier.PUBLIC | Modifier.TRANSIENT);
//            cls4.addField(bassHolderField);
            rdmod.getDeclaredConstructors()[0].setBody("{ }");
            rdmod.getDeclaredMethod("play").setBody("{ }");
            rdmod.getDeclaredMethod("stop").setBody("{ }");
            try {
                rdmod.getDeclaredMethod("outwithit").setBody("{ }");
            } catch (NotFoundException ignored) {
                System.err.println("RadicalMod does not contain `#outwithit()`; likely NFM2");
                rdmod.getDeclaredMethod("unloadAll").setBody("{ }");
                rdmod.getDeclaredMethod("unloadMod").setBody("{ }");
            }
            rdmod.getDeclaredMethod("resume").setBody("{ }");
            try {
                rdmod.getDeclaredMethod("posit").setBody("{ return 240001; }");
            } catch (NotFoundException ignored) {
                System.err.println("RadicalMod does not contain `#posit()`.");
            }

            CtClass co = classPool.get("ContO");
            hookLogging(co);
            hookLogging(gameSparker);
            hookLogging(xtg);
            hookLogging(medium);
            hookLogging(rdmod);

            // inject classes
            gameSparker.toClass(SuperRunApp.class.getClassLoader(), SuperRunApp.class.getProtectionDomain());
            xtg.toClass(SuperRunApp.class.getClassLoader(), SuperRunApp.class.getProtectionDomain());
            medium.toClass(SuperRunApp.class.getClassLoader(), SuperRunApp.class.getProtectionDomain());
            rdmod.toClass(SuperRunApp.class.getClassLoader(), SuperRunApp.class.getProtectionDomain());
            co.toClass(SuperRunApp.class.getClassLoader(), SuperRunApp.class.getProtectionDomain());

            // inject all the referenced classes
            // TODO: could possibly read the classes names/paths from the jar, and then inject just them?
            //       because this WILL break if something tries to load a class from the jar by name
            List<String> loaded = new ArrayList<>();
            // don't want to load class twice
            loaded.add("GameSparker");
            loaded.add("xtGraphics");
            loaded.add("Medium");
            loaded.add("RadicalMod");
            loaded.add("ContO");
            loadRefs(loaded, classPool, gameSparker);
            loadRefs(loaded, classPool, xtg);
            loadRefs(loaded, classPool, medium);
            loadRefs(loaded, classPool, rdmod);
            loadRefs(loaded, classPool, co);
        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }

        {
            originalRoot = System.getProperty("user.dir");
            System.setProperty("user.dir", rootDir = new File(jarPath).getParentFile().getAbsolutePath());
            rootUrl = new File(rootDir).toURI().toURL();
            System.out.println(rootUrl);
        }

        RunApp.amain();
    }

    private static void loadRefs(List<String> loadedClasses, ClassPool classPool, CtClass clsRef) throws NotFoundException, CannotCompileException {
        for (Object o : clsRef.getRefClasses()) {
            if (o.equals("HMInterceptor")) continue;
            String o1 = (String) o;
            if (loadedClasses.contains(o1)) continue;
            loadedClasses.add(o1);
            clsRef = classPool.get(o1);
            System.out.println("Loading " + o + "/" + clsRef.getPackageName());
            if (clsRef.getPackageName() != null && clsRef.getPackageName().startsWith("java")) continue;
            clsRef.toClass(SuperRunApp.class.getClassLoader(), SuperRunApp.class.getProtectionDomain());
            loadRefs(loadedClasses, classPool, clsRef);
        }
    }

    private static void hookLogging(CtClass cls) throws CannotCompileException {
        for (CtConstructor method : cls.getDeclaredConstructors()) {
            method.instrument(new ExprEditor() {
                public void edit(Handler h) throws CannotCompileException {
                    if (!h.isFinally()) {
                        h.insertBefore("{ HMInterceptor.log($1); }");
                    }
                }
            });
        }
        for (CtMethod method : cls.getDeclaredMethods()) {
            System.out.println("Hanndling " + cls.getName() + "," + method.getName());
            method.instrument(new ExprEditor() {
                public void edit(Handler h) throws CannotCompileException {
                    if (!h.isFinally()) {
                        h.insertBefore("{ HMInterceptor.log($1); }");
                    }
                }
            });
        }
    }
}
