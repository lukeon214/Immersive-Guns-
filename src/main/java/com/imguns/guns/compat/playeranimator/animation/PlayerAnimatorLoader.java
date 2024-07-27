package com.imguns.guns.compat.playeranimator.animation;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.util.TacPathVisitor;
import net.minecraft.util.Identifier;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PlayerAnimatorLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("PlayerAnimatorLoader");
    private static final Pattern ANIMATOR_PATTERN = Pattern.compile("^(\\w+)/player_animator/([\\w/]+)\\.json$");

    public static boolean load(ZipFile zipFile, String zipPath) {
        Matcher matcher = ANIMATOR_PATTERN.matcher(zipPath);
        if (matcher.find()) {
            String namespace = TacPathVisitor.checkNamespace(matcher.group(1));
            String path = matcher.group(2);
            ZipEntry entry = zipFile.getEntry(zipPath);
            if (entry == null) {
                GunMod.LOGGER.warn(MARKER, "{} file don't exist", zipPath);
                return false;
            }
            try (InputStream stream = zipFile.getInputStream(entry)) {
                Identifier registryName = new Identifier(namespace, path);
                PlayerAnimatorAssetManager.INSTANCE.putAnimation(registryName, stream);
                return true;
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read player animator file: {}, entry: {}", zipFile, entry);
                exception.printStackTrace();
            }
        }
        return false;
    }

    public static void load(File root) {
        Path playerAnimatorPath = root.toPath().resolve("player_animator");
        if (Files.isDirectory(playerAnimatorPath)) {
            TacPathVisitor visitor = new TacPathVisitor(playerAnimatorPath.toFile(), root.getName(), ".json", (id, file) -> {
                try (InputStream stream = Files.newInputStream(file)) {
                    PlayerAnimatorAssetManager.INSTANCE.putAnimation(id, stream);
                } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                    GunMod.LOGGER.warn(MARKER, "Failed to read player animator file: {}", file);
                    exception.printStackTrace();
                }
            });
            try {
                Files.walkFileTree(playerAnimatorPath, visitor);
            } catch (IOException e) {
                GunMod.LOGGER.warn(MARKER, "Failed to walk file tree: {}", playerAnimatorPath);
                e.printStackTrace();
            }
        }
    }
}
