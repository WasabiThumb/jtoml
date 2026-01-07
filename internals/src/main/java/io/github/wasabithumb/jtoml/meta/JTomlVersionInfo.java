package io.github.wasabithumb.jtoml.meta;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@ApiStatus.Internal
public final class JTomlVersionInfo {

    private static Manifest MANIFEST;

    private static @NotNull InputStream openManifestStream() throws IOException {
        InputStream in = JTomlVersionInfo.class.getResourceAsStream("/META-INF/MANIFEST.MF");
        if (in == null) throw new IllegalStateException("Failed to locate manifest");
        return in;
    }

    private static synchronized @NotNull Manifest getManifest() {
        Manifest mf = MANIFEST;
        if (mf == null) {
            mf = new Manifest();
            try (InputStream in = openManifestStream()) {
                mf.read(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read version info", e);
            }
            MANIFEST = mf;
        }
        return mf;
    }

    private static @NotNull String getSingleAttribute(@NotNull String key) {
        Attributes attrs = getManifest().getMainAttributes();
        String value = attrs.getValue(key);
        if (value == null) throw new IllegalStateException("Manifest is missing attribute \"" + key + "\"");
        return value;
    }

    public static @NotNull String libraryVersion() {
        return getSingleAttribute("Library-Version");
    }

    public static @NotNull String gitCommit() {
        return getSingleAttribute("Git-Commit");
    }

    public static @NotNull String gitBranch() {
        return getSingleAttribute("Git-Branch");
    }

    public static @NotNull String derivedVersion() {
        String base = libraryVersion();
        String branch = gitBranch();
        if ("master".equals(branch)) return base;
        String sha = gitCommit();
        return base + "-" + sha.substring(0, 7);
    }

    //

    private JTomlVersionInfo() { }

}
