package com.imguns.guns.client.resource.loader.asset;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.client.resource.ClientAssetManager;
import com.imguns.guns.client.resource.pojo.model.BedrockModelPOJO;
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

import static com.imguns.guns.client.resource.ClientGunPackLoader.GSON;

public final class BedrockModelLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("BedrockModelLoader");
    private static final Pattern MODEL_PATTERN = Pattern.compile("^(\\w+)/models/([\\w/]+)\\.json$");

    public static boolean load(ZipFile zipFile, String zipPath) {
        Matcher matcher = MODEL_PATTERN.matcher(zipPath);
        if (matcher.find()) {
            String namespace = TacPathVisitor.checkNamespace(matcher.group(1));
            String path = matcher.group(2);
            ZipEntry entry = zipFile.getEntry(zipPath);
            if (entry == null) {
                GunMod.LOGGER.warn(MARKER, "{} file don't exist", zipPath);
                return false;
            }
            try (InputStream modelFileStream = zipFile.getInputStream(entry)) {
                Identifier registryName = new Identifier(namespace, path);
                BedrockModelPOJO modelPOJO = GSON.fromJson(IOReader.toString(modelFileStream, StandardCharsets.UTF_8), BedrockModelPOJO.class);
                ClientAssetManager.INSTANCE.putModel(registryName, modelPOJO);
                return true;
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read model file: {}, entry: {}", zipFile, entry);
                exception.printStackTrace();
            }
        }
        return false;
    }

    public static void load(File root) {
        Path modelPath = root.toPath().resolve("models");
        if (Files.isDirectory(modelPath)) {
            TacPathVisitor visitor = new TacPathVisitor(modelPath.toFile(), root.getName(), ".json", (id, file) -> {
                try (InputStream modelFileStream = Files.newInputStream(file)) {
                    BedrockModelPOJO modelPOJO = GSON.fromJson(IOReader.toString(modelFileStream, StandardCharsets.UTF_8), BedrockModelPOJO.class);
                    ClientAssetManager.INSTANCE.putModel(id, modelPOJO);
                } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                    GunMod.LOGGER.warn(MARKER, "Failed to read model file: {}", file);
                    exception.printStackTrace();
                }
            });
            try {
                Files.walkFileTree(modelPath, visitor);
            } catch (IOException e) {
                GunMod.LOGGER.warn(MARKER, "Failed to walk file tree: {}", modelPath);
                e.printStackTrace();
            }
        }
    }
}
