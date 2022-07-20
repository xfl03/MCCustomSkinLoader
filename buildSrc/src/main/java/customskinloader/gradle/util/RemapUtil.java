package customskinloader.gradle.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;
import net.minecraftforge.srg2source.ast.RangeExtractor;
import net.minecraftforge.srg2source.util.io.InputSupplier;
import org.apache.commons.io.FileUtils;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.srg.SrgReader;
import org.cadixdev.lorenz.io.srg.SrgWriter;
import org.cadixdev.lorenz.io.srg.tsrg.TSrgReader;
import org.cadixdev.mercury.Mercury;
import org.cadixdev.mercury.remapper.MercuryRemapper;
import org.gradle.api.Project;

/**
 * Gradle Utils for Converting Mappings
 * Designed for Converting Mappings.
 * Converting Mappings: tsrg->srg
 */
public class RemapUtil {
    public static void tsrg2srg(File tsrg, File srg) throws IOException {
        if (!tsrg.exists()) return;
        if (!srg.getParentFile().exists()) srg.getParentFile().mkdirs();

        MappingSet set = MappingSet.create();

        try (
            Reader reader = Files.newBufferedReader(tsrg.toPath(), StandardCharsets.UTF_8);
            Writer writer = Files.newBufferedWriter(srg.toPath(), StandardCharsets.UTF_8)
        ) {
            new TSrgReader(reader).read(set);
            new SrgWriter(writer).write(set);
        }
        srg.deleteOnExit();
    }

    public static void remapSources(Project project) {
        Optional.ofNullable(project.getTasks().findByName("extractRangemapReplacedMain")).ifPresent(task -> task.doFirst(task0 -> {
            try {
                // Because of https://github.com/MinecraftForge/ForgeGradle/blob/62f37569f3afc044489e7606d2eb4c2509a85fb8/build.gradle#L152-L245,
                // ForgeGradle modified Eclipse JDT and Mercury will call related methods,
                // so here must use some hacks.
                RangeExtractor instance = new RangeExtractor();
                instance.setInput(new InputSupplier() {
                    @Override public String getRoot(String resource) { return null; }
                    @Override public InputStream getInput(String relPath) { try { return Files.newInputStream(Paths.get(relPath)); } catch (IOException e) { throw new RuntimeException(e); } }
                    @Override public List<String> gatherAll(String endFilter) { return null; }
                    @Override public void close() { }
                });

                Field instanceField = RangeExtractor.class.getDeclaredField("INSTANCE");
                instanceField.setAccessible(true);
                instanceField.set(null, instance);

                // We use Mercury to remap source jar because srg2source have a few issues.
                Mercury mercury = new Mercury();

                Set<File> set = Sets.newHashSet(project.getConfigurations().getByName("compileClasspath").getFiles());
                set.addAll(project.getConfigurations().getByName("forgeGradleMc").getFiles());
                set.addAll(project.getConfigurations().getByName("forgeGradleMcDeps").getFiles());
                for (File dependencies : set) {
                    mercury.getClassPath().add(dependencies.toPath());
                }

                mercury.getProcessors().add(MercuryRemapper.create(new SrgReader(Files.newBufferedReader(project.file("build/reobf.srg").toPath(), StandardCharsets.UTF_8)).read(MappingSet.create())));

                File source = project.file("build/sources/main/java"), dest = project.file("build/sources/main/java_tmp");
                if (source.renameTo(dest)) {
                    mercury.rewrite(dest.toPath(), source.toPath());
                    FileUtils.deleteDirectory(dest);
                }

                instanceField.set(null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
