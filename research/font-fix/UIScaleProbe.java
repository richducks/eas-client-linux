import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import javax.swing.UIManager;

public class UIScaleProbe {
    public static void main(String[] args) {
        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
        System.out.println("dpi=" + Toolkit.getDefaultToolkit().getScreenResolution());
        System.out.println("transform=" + gc.getDefaultTransform());
        System.out.println("Label.font=" + UIManager.getFont("Label.font"));
        System.out.println("Table.font=" + UIManager.getFont("Table.font"));
    }
}
