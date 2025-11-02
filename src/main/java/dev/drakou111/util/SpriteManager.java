package dev.drakou111.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SpriteManager {
    private final Map<String, Image> map = new LinkedHashMap<>();

    public void loadSprites() {
        try {
            ClassLoader cl = getClass().getClassLoader();
            URL spriteRoot = cl.getResource("sprites");

            if (spriteRoot == null) {
                System.err.println("sprites/ directory missing from resources");
                return;
            }

            switch (spriteRoot.getProtocol()) {
                case "file" -> {
                    // Running from IDE or unpacked folder
                    File dir = new File(spriteRoot.toURI());
                    loadRecursive(dir);
                }
                case "jar" -> {
                    // Running from inside a JAR
                    loadFromJar(spriteRoot);
                }
                default -> System.err.println("Unsupported protocol: " + spriteRoot.getProtocol());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromJar(URL spriteRoot) throws IOException, URISyntaxException {
        String path = spriteRoot.getPath();
        String jarPath = path.substring(5, path.indexOf("!")); // strip "file:" and everything after '!'
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // We only want entries under sprites/ ending with .png
                if (name.startsWith("sprites/") && name.toLowerCase().endsWith(".png")) {
                    String key = new File(name).getName().replaceFirst("\\.png$", "");
                    try {
                        // load using resource stream
                        Image img = new Image(Objects.requireNonNull(
                                getClass().getClassLoader().getResourceAsStream(name)
                                                                    ));
                        map.put(key, img);
                    } catch (Exception ex) {
                        System.err.println("Failed to load sprite from jar: " + name);
                    }
                }
            }
        }
    }

    /**
     * Handles resources on the filesystem (during dev time)
     */
    private void loadRecursive(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) loadRecursive(f);
            else if (f.getName().toLowerCase().endsWith(".png")) {
                try {
                    String key = f.getName().substring(0, f.getName().length() - 4);
                    Image img = new Image(f.toURI().toString());
                    map.put(key, img);
                } catch (Exception ex) {
                    System.err.println("Failed to load sprite: " + f.getAbsolutePath());
                }
            }
        }
    }

    public Image get(String key) {
        return map.get(key);
    }

    public Set<String> keys() {
        return map.keySet();
    }
}
