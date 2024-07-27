package com.imguns.guns.client.resource.loader.asset;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.client.resource.ClientAssetManager;
import com.imguns.guns.client.resource.ClientGunPackLoader;
import com.imguns.guns.client.resource.pojo.display.attachment.AttachmentDisplay;
import com.imguns.guns.util.IOReader;
import com.imguns.guns.util.TacPathVisitor;
import net.minecraft.util.Identifier;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AttachmentDisplayLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("AttachmentDisplayLoader");
    private static final Pattern DISPLAY_PATTERN = Pattern.compile("^(\\w+)/attachments/display/([\\w/]+)\\.json$");

    public static boolean load(ZipFile zipFile, String zipPath) {
        Matcher matcher = DISPLAY_PATTERN.matcher(zipPath);
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
                AttachmentDisplay display = ClientGunPackLoader.GSON.fromJson(IOReader.toString(stream, StandardCharsets.UTF_8), AttachmentDisplay.class);
                ClientAssetManager.INSTANCE.putAttachmentDisplay(registryName, display);
                return true;
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read display file: {}, entry: {}", zipFile, entry);
                exception.printStackTrace();
            }
        }
        return false;
    }

    public static void load(File root) {
        Path displayPath = root.toPath().resolve("attachments/display");
        if (Files.isDirectory(displayPath)) {
            TacPathVisitor visitor = new TacPathVisitor(displayPath.toFile(), root.getName(), ".json", (id, file) -> {
                try (InputStream stream = Files.newInputStream(file)) {
                    AttachmentDisplay display = ClientGunPackLoader.GSON.fromJson(IOReader.toString(stream, StandardCharsets.UTF_8), AttachmentDisplay.class);
                    ClientAssetManager.INSTANCE.putAttachmentDisplay(id, display);
                } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                    GunMod.LOGGER.warn(MARKER, "Failed to read display file: {}", file);
                    exception.printStackTrace();
                }
            });
            try {
                Files.walkFileTree(displayPath, visitor);
            } catch (IOException e) {
                GunMod.LOGGER.warn(MARKER, "Failed to walk file tree: {}", displayPath);
                e.printStackTrace();
            }
        }
    }
}
