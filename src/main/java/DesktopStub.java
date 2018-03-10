import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;

/**
 * An implementation of <code>AppletStub</code>, optimized for desktop apps.
 * It's not complete though, only the methods needed by Nfm2 are implemented.
 * @author DragShot
 */
public class DesktopStub implements AppletStub{

    private AppletContext context=new DesktopContext();

    /**
     * @inheritdoc
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * @inheritdoc
     */
    @Override
    public URL getDocumentBase() {
        return SuperRunApp.rootUrl;
    }

    /**
     * @inheritdoc
     */
    @Override
    public URL getCodeBase() {
        return SuperRunApp.rootUrl;
    }

    /**
     * This method is not implemented.
     */
    @Override
    public String getParameter(String name) {
        return null;
    }

    /**
     * @inheritdoc
     */
    @Override
    public AppletContext getAppletContext() {
        return context;
    }

    /**
     * @inheritdoc
     */
    @Override
    public void appletResize(int width, int height) {}
}