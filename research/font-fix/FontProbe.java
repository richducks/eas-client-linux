import java.awt.GraphicsEnvironment;
import java.util.Arrays;

public class FontProbe {
    public static void main(String[] args) {
        Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames())
                .filter(name -> name.toLowerCase().contains("kai") || name.contains("楷"))
                .forEach(System.out::println);
    }
}
