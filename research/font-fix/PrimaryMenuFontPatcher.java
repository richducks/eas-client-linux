import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.sf.cglib.asm.ClassReader;
import net.sf.cglib.asm.ClassVisitor;
import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.MethodVisitor;
import net.sf.cglib.asm.Opcodes;

/** Makes the first-level EAS menu use the same font and rendering as cascading menus. */
public final class PrimaryMenuFontPatcher {
    private static final String OWNER =
            "com/kingdee/eas/base/uiframe/client/MainMenuListCellRenderer";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PrimaryMenuFontPatcher <input.class> <output.class>");
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassReader reader = new ClassReader(Files.readAllBytes(input));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        final int[] patched = new int[1];

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"paintComponent".equals(name) || !"(Ljava/awt/Graphics;)V".equals(descriptor)) {
                    return delegate;
                }
                patched[0]++;
                return new MethodVisitor(Opcodes.ASM4, delegate) {
                    @Override
                    public void visitCode() {
                        super.visitCode();

                        super.visitVarInsn(Opcodes.ALOAD, 0);
                        super.visitTypeInsn(Opcodes.NEW, "java/awt/Font");
                        super.visitInsn(Opcodes.DUP);
                        super.visitLdcInsn("Noto Sans CJK SC");
                        super.visitInsn(Opcodes.ICONST_1);
                        super.visitIntInsn(Opcodes.BIPUSH, 14);
                        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/awt/Font", "<init>",
                                "(Ljava/lang/String;II)V");
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, OWNER, "setFont",
                                "(Ljava/awt/Font;)V");

                        setRenderingHint("KEY_TEXT_ANTIALIASING", "VALUE_TEXT_ANTIALIAS_ON");
                        setRenderingHint("KEY_FRACTIONALMETRICS", "VALUE_FRACTIONALMETRICS_ON");
                        setRenderingHint("KEY_RENDERING", "VALUE_RENDER_QUALITY");
                    }

                    private void setRenderingHint(String key, String value) {
                        super.visitVarInsn(Opcodes.ALOAD, 1);
                        super.visitTypeInsn(Opcodes.CHECKCAST, "java/awt/Graphics2D");
                        super.visitFieldInsn(Opcodes.GETSTATIC, "java/awt/RenderingHints", key,
                                "Ljava/awt/RenderingHints$Key;");
                        super.visitFieldInsn(Opcodes.GETSTATIC, "java/awt/RenderingHints", value,
                                "Ljava/lang/Object;");
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/awt/Graphics2D",
                                "setRenderingHint",
                                "(Ljava/awt/RenderingHints$Key;Ljava/lang/Object;)V");
                    }
                };
            }
        };

        reader.accept(visitor, 0);
        if (patched[0] != 1) {
            throw new IllegalStateException("Expected one paintComponent method, found " + patched[0]);
        }
        Files.write(output, writer.toByteArray());
    }
}
