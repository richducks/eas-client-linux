import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.sf.cglib.asm.ClassReader;
import net.sf.cglib.asm.ClassVisitor;
import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.MethodVisitor;
import net.sf.cglib.asm.Opcodes;

/** Forces EAS cascading-menu painters to use a clear 14-point CJK font and AA hints. */
public final class FontBytecodePatcher {
    private static final String TARGET_METHOD = "paintComponent";
    private static final String TARGET_DESCRIPTOR = "(Ljava/awt/Graphics;)V";
    private static final String FONT_CLASS = "java/awt/Font";
    private static final String FONT_FAMILY = "Noto Sans CJK SC";
    private static final int FONT_SIZE = 14;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: FontBytecodePatcher <input.class> <output.class>");
        }

        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassReader reader = new ClassReader(Files.readAllBytes(input));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        final int[] replacements = new int[3];

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!TARGET_METHOD.equals(name) || !TARGET_DESCRIPTOR.equals(descriptor)) {
                    return delegate;
                }

                return new MethodVisitor(Opcodes.ASM4, delegate) {
                    private boolean constructingFont;

                    @Override
                    public void visitCode() {
                        super.visitCode();
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

                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        super.visitTypeInsn(opcode, type);
                        if (opcode == Opcodes.NEW && FONT_CLASS.equals(type)) {
                            constructingFont = true;
                        }
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        if (constructingFont && opcode == Opcodes.ACONST_NULL) {
                            super.visitLdcInsn(FONT_FAMILY);
                            replacements[0]++;
                            return;
                        }
                        if (constructingFont
                                && (opcode == Opcodes.ICONST_0 || opcode == Opcodes.ICONST_1)) {
                            super.visitInsn(Opcodes.ICONST_1);
                            replacements[1]++;
                            return;
                        }
                        super.visitInsn(opcode);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (constructingFont && value instanceof String) {
                            super.visitLdcInsn(FONT_FAMILY);
                            replacements[0]++;
                            return;
                        }
                        super.visitLdcInsn(value);
                    }

                    @Override
                    public void visitIntInsn(int opcode, int operand) {
                        if (constructingFont && opcode == Opcodes.BIPUSH && operand == 12) {
                            super.visitIntInsn(opcode, FONT_SIZE);
                            replacements[2]++;
                            return;
                        }
                        if (constructingFont && opcode == Opcodes.BIPUSH && operand == FONT_SIZE) {
                            super.visitIntInsn(opcode, FONT_SIZE);
                            replacements[2]++;
                            return;
                        }
                        super.visitIntInsn(opcode, operand);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
                        super.visitMethodInsn(opcode, owner, name, descriptor);
                        if (constructingFont && FONT_CLASS.equals(owner) && "<init>".equals(name)) {
                            constructingFont = false;
                        }
                    }
                };
            }
        };

        reader.accept(visitor, 0);
        if (replacements[0] != 1 || replacements[1] != 1 || replacements[2] != 1) {
            throw new IllegalStateException("Unexpected replacement count: family="
                    + replacements[0] + ", style=" + replacements[1]
                    + ", size=" + replacements[2]);
        }
        Files.write(output, writer.toByteArray());
    }
}
