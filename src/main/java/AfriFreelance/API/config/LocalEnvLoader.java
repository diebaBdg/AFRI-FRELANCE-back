package AfriFreelance.API.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads KEY=VALUE pairs from the local env file used for AfriFreelance
 * ({@code afrifreelance files/env} or {@code .env}) into system properties
 * so {@code application-*.properties} placeholders resolve without IDE env injection.
 * Existing OS / JVM variables are never overwritten.
 */
public final class LocalEnvLoader {

    private LocalEnvLoader() {}

    public static void apply() {
        Path envFile = resolveEnvFile();
        if (envFile == null) {
            System.out.println("Aucun fichier env local trouvé (AFRIFREELANCE_ENV_FILE, .env, afrifreelance files/env).");
            return;
        }
        int loaded = 0;
        try (BufferedReader reader = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = stripQuotes(line.substring(eq + 1).trim());
                if (key.isEmpty() || isAlreadySet(key)) {
                    continue;
                }
                System.setProperty(key, value);
                loaded++;
            }
        } catch (IOException e) {
            System.err.println("Impossible de lire le fichier env " + envFile + " : " + e.getMessage());
            return;
        }
        System.out.println("Fichier env chargé (" + loaded + " clés) : " + envFile.toAbsolutePath());
    }

    private static boolean isAlreadySet(String key) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return true;
        }
        String fromProps = System.getProperty(key);
        return fromProps != null && !fromProps.isBlank();
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static Path resolveEnvFile() {
        String override = System.getenv("AFRIFREELANCE_ENV_FILE");
        if (override == null || override.isBlank()) {
            override = System.getProperty("AFRIFREELANCE_ENV_FILE");
        }
        if (override != null && !override.isBlank()) {
            Path p = Path.of(override);
            return Files.isRegularFile(p) ? p : null;
        }

        for (Path dir : searchRoots()) {
            for (Path candidate : envCandidates(dir)) {
                if (Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static List<Path> searchRoots() {
        List<Path> roots = new ArrayList<>();
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (int i = 0; i < 6 && current != null; i++) {
            roots.add(current);
            current = current.getParent();
        }
        return roots;
    }

    private static List<Path> envCandidates(Path dir) {
        return List.of(
                dir.resolve(".env"),
                dir.resolve("env"),
                dir.resolve("afrifreelance files").resolve("env"),
                dir.resolve("afrifreelance files").resolve(".env")
        );
    }
}
