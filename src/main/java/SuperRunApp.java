import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

/**
 * Created by Rafael on 24/05/2017.
 *
 * @author Rafael
 * @since 24/05/2017
 */
public class SuperRunApp {
    public static int datecnt = 0;

    public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
        BASSLoader.initializeBASS();

        try {
            CtClass cls = ClassPool.getDefault().get("GameSparker");
            CtMethod method = cls.getDeclaredMethod("run");
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
            CtClass cls2 = ClassPool.getDefault().get("xtGraphics");
//            CtField _xtg = new CtField(cls2, "_xtg", cls2);
//            _xtg.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.TRANSIENT);
//            cls2.addField(_xtg);

            cls2.getDeclaredConstructors()[0].insertBeforeBody("{ HMInterceptor.xtg = this; }");

            // remove performance fog adjust code
            CtClass cls3 = ClassPool.getDefault().get("Medium");
            cls3.getDeclaredMethod("adjstfade").setBody("{ }"); // might not be in nfm2?

            CtClass cls4 = ClassPool.getDefault().get("RadicalMod");
            CtField bassHolderField = new CtField(ClassPool.getDefault().get("RadicalBASS"), "_bass", cls4);
            bassHolderField.setModifiers(Modifier.PUBLIC | Modifier.TRANSIENT);
            cls4.addField(bassHolderField);
            cls4.getDeclaredConstructors()[0].setBody("{ _bass = new RadicalBASS(new java.io.File($1));" +
                    "stream = new SuperStream(new byte[0]); }"); // superstream required for nfm1 to pretend its loaded.
            cls4.getDeclaredMethod("play").setBody("{ _bass.play(); }");
            cls4.getDeclaredMethod("stop").setBody("{ _bass.stop(); }");
            try {
                cls4.getDeclaredMethod("outwithit").setBody("{ }");
            } catch (NotFoundException ignored) {
                System.err.println("RadicalMod does not contain `#outwithit()`; likely NFM2");
                cls4.getDeclaredMethod("unloadAll").setBody("{ }");
                cls4.getDeclaredMethod("unloadMod").setBody("{ }");
            }
            cls4.getDeclaredMethod("resume").setBody("{ _bass.resume(); }");
            try {
                cls4.getDeclaredMethod("posit").setBody("{ return 240001; }");
            } catch (NotFoundException ignored) {
                System.err.println("RadicalMod does not contain `#posit()`.");
            }

            cls.toClass();
            cls2.toClass();
            cls3.toClass();
            cls4.toClass();
        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
        RunApp.amain(args);
    }
}
