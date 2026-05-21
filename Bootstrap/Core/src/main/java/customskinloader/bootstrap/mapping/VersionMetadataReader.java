package customskinloader.bootstrap.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class VersionMetadataReader {
    private static final String VERSION_JSON = "version.json";
    private static final String NETWORK_PROTOCOL_VERSION_FIELD = "NETWORK_PROTOCOL_VERSION";
    private static final String[] VERSION_KEYS = {"protocol_version", "world_version"};
    private static final int[] VERSIONS = {0, 0};

    static {
        initializeVersions();
    }

    public static final int PROTOCOL_VERSION = VERSIONS[0];
    public static final int WORLD_VERSION = VERSIONS[1];

    private VersionMetadataReader() {
    }

    private static void initializeVersions() {
        InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(VERSION_JSON);
        if (resourceStream == null) {
            readProtocolVersionFromRealmsSharedConstants();
            return;
        }

        try (InputStream inputStream = resourceStream; InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            JsonElement rootElement = new JsonParser().parse(reader);
            if (rootElement == null || !rootElement.isJsonObject()) {
                return;
            }

            JsonObject rootObject = rootElement.getAsJsonObject();
            for (int i = 0; i < VERSION_KEYS.length; i++) {
                JsonElement versionElement = rootObject.get(VERSION_KEYS[i]);
                if (versionElement != null && !versionElement.isJsonNull()) {
                    VERSIONS[i] = versionElement.getAsInt();
                }
            }
        } catch (Exception ignored) {

        }
    }

    private static void readProtocolVersionFromRealmsSharedConstants() {
        InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream("net/minecraft/realms/RealmsSharedConstants.class");
        if (resourceStream == null) {
            return;
        }

        ClassNode classNode = new ClassNode();
        try (InputStream inputStream = resourceStream) {
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException ignored) {

        }

        for (FieldNode fieldNode : classNode.fields) {
            if (NETWORK_PROTOCOL_VERSION_FIELD.equals(fieldNode.name) && fieldNode.value instanceof Integer) {
                VERSIONS[0] = (int) fieldNode.value;
                return;
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            if (!"<clinit>".equals(methodNode.name)) {
                continue;
            }

            for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                if (instruction.getOpcode() != Opcodes.PUTSTATIC) {
                    continue;
                }

                FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                if (classNode.name.equals(fieldInsnNode.owner) && NETWORK_PROTOCOL_VERSION_FIELD.equals(fieldInsnNode.name) && "I".equals(fieldInsnNode.desc)) {
                    instruction = instruction.getPrevious();
                    int opcode = instruction.getOpcode();
                    if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) {
                        VERSIONS[0] = opcode - Opcodes.ICONST_0;
                    } else if (opcode >= Opcodes.BIPUSH && opcode <= Opcodes.SIPUSH) {
                        VERSIONS[0] = ((IntInsnNode) instruction).operand;
                    } else if (opcode == Opcodes.LDC) {
                        Object constant = ((LdcInsnNode) instruction).cst;
                        if (constant instanceof Integer) {
                            VERSIONS[0] = (int) constant;
                        }
                    }
                    break;
                }
            }
        }
    }
}
