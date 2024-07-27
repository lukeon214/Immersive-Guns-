package com.imguns.guns.client.resource.loader.asset;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.client.resource.ClientAssetManager;
import com.imguns.guns.client.resource.pojo.PackInfo;
import com.imguns.guns.util.IOReader;
import com.imguns.guns.util.TacPathVisitor;
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

public final class PackInfoLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("CreativeTabLoader");
    private static final Pattern PACK_INFO_PATTERN = Pattern.compile("^(\\w+)/pack\\.json$");

    public static boolean load(ZipFile zipFile, String zipPath) {
        Matcher matcher = PACK_INFO_PATTERN.matcher(zipPath);
        if (matcher.find()) {
            String namespace = TacPathVisitor.checkNamespace(matcher.group(1));
            ZipEntry entry = zipFile.getEntry(zipPath);
            if (entry == null) {
                GunMod.LOGGER.warn(MARKER, "{} file don't exist", zipPath);
                return false;
            }
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                PackInfo packInfo = GSON.fromJson(IOReader.toString(inputStream, StandardCharsets.UTF_8), PackInfo.class);
                ClientAssetManager.INSTANCE.putPackInfo(namespace, packInfo);
                return true;
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}, entry: {}", zipFile, entry);
                exception.printStackTrace();
            }
        }
        return false;
    }

    public static void load(File root) {
        Path packInfoFilePath = root.toPath().resolve("pack.json");
        if (Files.isRegularFile(packInfoFilePath)) {
            try (InputStream stream = Files.newInputStream(packInfoFilePath)) {
                PackInfo packInfo = GSON.fromJson(IOReader.toString(stream, StandardCharsets.UTF_8), PackInfo.class);
                ClientAssetManager.INSTANCE.putPackInfo(root.getName(), packInfo);
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read info json: {}", packInfoFilePath);
                exception.printStackTrace();
            }
        }
    }
}
