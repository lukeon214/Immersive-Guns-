package com.imguns.guns.client.resource.loader.asset;

import com.google.common.collect.Maps;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.client.resource.ClientAssetManager;
import com.imguns.guns.util.TacPathVisitor;
import net.minecraft.util.Language;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LanguageLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("LanguageLoader");
    private static final Pattern LANG_PATTERN = Pattern.compile("^\\w+/lang/(\\w+)\\.json$");

    public static boolean load(ZipFile zipFile, String zipPath) {
        Matcher matcher = LANG_PATTERN.matcher(zipPath);
        if (matcher.find()) {
            String languageCode = TacPathVisitor.checkNamespace(matcher.group(1));
            ZipEntry entry = zipFile.getEntry(zipPath);
            if (entry == null) {
                GunMod.LOGGER.warn(MARKER, "{} file don't exist", zipPath);
                return false;
            }
            try (InputStream zipEntryStream = zipFile.getInputStream(entry)) {
                Map<String, String> languages = Maps.newHashMap();
                Language.load(zipEntryStream, languages::put);
                ClientAssetManager.INSTANCE.putLanguage(languageCode, languages);
                return true;
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read language file: {}, entry: {}", zipFile, entry);
                exception.printStackTrace();
            }
        }
        return false;
    }

    public static void load(File root) {
        Path filePath = root.toPath().resolve("lang");
        if (!Files.isDirectory(filePath)) {
            return;
        }
        File[] subFiles = filePath.toFile().listFiles((dir, name) -> true);
        if (subFiles == null) {
            return;
        }
        for (File file : subFiles) {
            String name = file.getName();
            if (!name.endsWith(".json")) {
                continue;
            }
            String languageCode = name.substring(0, name.length() - 5);
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                Map<String, String> languages = Maps.newHashMap();
                Language.load(inputStream, languages::put);
                ClientAssetManager.INSTANCE.putLanguage(languageCode, languages);
            } catch (IOException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn(MARKER, "Failed to read language file: {}", file);
                exception.printStackTrace();
            }
        }
    }
}
