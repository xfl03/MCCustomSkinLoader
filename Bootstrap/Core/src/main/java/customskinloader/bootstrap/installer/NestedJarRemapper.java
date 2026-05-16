package customskinloader.bootstrap.installer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.mapping.Mappings;
import org.apache.logging.log4j.Logger;

final class NestedJarRemapper {
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    private final BytecodeRemapper remapper;

    NestedJarRemapper(Mappings mappings) {
        this.remapper = new BytecodeRemapper(mappings);
    }

    public void remapJar(InputStream inputStream, OutputStream outputStream) throws Exception {
        List<JarEntryData> entries = new ArrayList<>();
        Manifest manifest;

        try (JarInputStream jarInputStream = new JarInputStream(inputStream)) {
            manifest = jarInputStream.getManifest();

            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.isDirectory() || this.isSignatureEntry(entry.getName())) {
                    continue;
                }

                byte[] entryBytes = this.readAllBytes(jarInputStream);
                entries.add(new JarEntryData(entry.getName(), entry.getTime(), entryBytes));
                if (entry.getName().endsWith(".class")) {
                    this.remapper.addClassInheritance(entryBytes);
                }
            }
        }

        LOGGER.debug("Read " + entries.size() + " entries from nested CustomSkinLoader Common jar");

        try (JarOutputStream jarOutputStream = manifest == null
            ? new JarOutputStream(outputStream)
            : new JarOutputStream(outputStream, this.copyManifest(manifest))) {
            for (JarEntryData entry : entries) {
                byte[] entryBytes = entry.bytes;
                if (entry.name.endsWith(".class")) {
                    entryBytes = this.remapper.remapClass(entryBytes);
                    this.writeEntry(jarOutputStream, this.toRemappedClassEntryName(entry.name, entryBytes), entry.time, entryBytes);
                    continue;
                }

                this.writeEntry(jarOutputStream, entry.name, entry.time, entryBytes);
            }
            jarOutputStream.finish();
        }

        LOGGER.debug("Remapped " + entries.size() + " entries from nested CustomSkinLoader Common jar");
    }

    private Manifest copyManifest(Manifest manifest) {
        Manifest copiedManifest = new Manifest();
        copiedManifest.getMainAttributes().putAll(manifest.getMainAttributes());

        for (Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet()) {
            Attributes copiedAttributes = new Attributes();
            copiedAttributes.putAll(entry.getValue());
            copiedManifest.getEntries().put(entry.getKey(), copiedAttributes);
        }

        return copiedManifest;
    }

    private boolean isSignatureEntry(String entryName) {
        String upperCaseEntryName = entryName.toUpperCase();
        return upperCaseEntryName.startsWith("META-INF/")
            && (upperCaseEntryName.endsWith(".SF") || upperCaseEntryName.endsWith(".DSA") || upperCaseEntryName.endsWith(".RSA") || upperCaseEntryName.endsWith(".EC"));
    }

    private String toRemappedClassEntryName(String sourceEntryName, byte[] remappedClassBytes) {
        String classEntryName = this.remapper.toClassEntryName(remappedClassBytes);
        String versionedPrefix = this.getMultiReleaseVersionedPrefix(sourceEntryName);
        return versionedPrefix == null ? classEntryName : versionedPrefix + classEntryName;
    }

    private String getMultiReleaseVersionedPrefix(String entryName) {
        String prefix = "META-INF/versions/";
        if (!entryName.startsWith(prefix)) {
            return null;
        }

        int versionEnd = entryName.indexOf('/', prefix.length());
        if (versionEnd < 0) {
            return null;
        }

        return entryName.substring(0, versionEnd + 1);
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int readBytes;

        while ((readBytes = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, readBytes);
        }

        return outputStream.toByteArray();
    }

    private void writeEntry(JarOutputStream jarOutputStream, String entryName, long sourceEntryTime, byte[] entryBytes) throws IOException {
        JarEntry jarEntry = new JarEntry(entryName);
        if (sourceEntryTime >= 0L) {
            jarEntry.setTime(sourceEntryTime);
        }

        jarOutputStream.putNextEntry(jarEntry);
        jarOutputStream.write(entryBytes);
        jarOutputStream.closeEntry();
    }

    private static final class JarEntryData {
        private final String name;
        private final long time;
        private final byte[] bytes;

        JarEntryData(String name, long time, byte[] bytes) {
            this.name = name;
            this.time = time;
            this.bytes = bytes;
        }
    }
}
