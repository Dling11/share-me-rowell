import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.jar.JarFile;

/** Verify every AAB content entry against the existing application signing certificate. */
public final class VerifyAndroidBundle {
    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !args[1].matches("[a-f0-9]{64}")) throw new IllegalArgumentException("AAB path and certificate SHA256 required");
        int count = 0;
        try (var jar = new JarFile(Path.of(args[0]).toFile(), true)) {
            if (jar.getEntry("BundleConfig.pb") == null || jar.getEntry("base/manifest/AndroidManifest.xml") == null)
                throw new SecurityException("Missing bundle structure");
            var entries = jar.entries();
            var buffer = new byte[256 * 1024];
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().startsWith("META-INF/")) continue;
                try (InputStream input = jar.getInputStream(entry)) { while (input.read(buffer) != -1) {} }
                var certificates = entry.getCertificates();
                if (certificates == null || certificates.length == 0) throw new SecurityException("Unsigned bundle entry");
                String fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificates[0].getEncoded()));
                if (!fingerprint.equals(args[1])) throw new SecurityException("Bundle signing identity changed");
                count++;
            }
        }
        if (count == 0) throw new SecurityException("Empty bundle");
        System.out.println("AAB signature verified; original signing certificate; " + count + " signed content entries.");
    }
}
