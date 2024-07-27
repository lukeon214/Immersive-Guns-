package com.imguns.guns.resource.loader.index;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.resource.index.CommonAttachmentIndex;
import com.imguns.guns.resource.network.CommonGunPackNetwork;
import com.imguns.guns.resource.network.DataType;
import com.imguns.guns.resource.pojo.AttachmentIndexPOJO;
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

import static com.imguns.guns.resource.CommonGunPackLoader.ATTACHMENT_INDEX;
import static com.imguns.guns.resource.CommonGunPackLoader.GSON;

public final class CommonAttachmentIndexLoader {
    private static final Pattern ATTACHMENT_INDEX_PATTERN = Pattern.compile("^(\\w+)/attachments/index/(\\w+)\\.json$");
    private static final Marker MARKER = MarkerFactory.getMarker("CommonAttachmentIndexLoader");

    public static void loadAttachmentIndex(String path, ZipFile zipFile) throws IOException {
        Matcher matcher = ATTACHMENT_INDEX_PATTERN.matcher(path);
        if (matcher.find()) {
            String namespace = TacPathVisitor.checkNamespace(matcher.group(1));
            String id = matcher.group(2);
            ZipEntry entry = zipFile.getEntry(path);
            if (entry == null) {
                GunMod.LOGGER.warn(MARKER, "{} file don't exist", path);
                return;
            }
            try (InputStream stream = zipFile.getInputStream(entry)) {
                String json = IOReader.toString(stream, StandardCharsets.UTF_8);
                Identifier registryName = new Identifier(namespace, id);
                loadAttachmentFromJsonString(registryName, json);
                CommonGunPackNetwork.addData(DataType.ATTACHMENT_INDEX, registryName, json);
            } catch (IllegalArgumentException | JsonSyntaxException | JsonIOException exception) {
                GunMod.LOGGER.warn("{} index file read fail!", path);
                exception.printStackTrace();
            }
        }
    }

    public static void loadAttachmentIndex(File root) throws IOException {
        Path filePath = root.toPath().resolve("attachments/index");
        if (Files.isDirectory(filePath)) {
            TacPathVisitor visitor = new TacPathVisitor(filePath.toFile(), root.getName(), ".json", (id, file) -> {
                try (InputStream stream = Files.newInputStream(file)) {
                    String json = IOReader.toString(stream, StandardCharsets.UTF_8);
                    loadAttachmentFromJsonString(id, json);
                    CommonGunPackNetwork.addData(DataType.ATTACHMENT_INDEX, id, json);
                } catch (IllegalArgumentException | IOException | JsonSyntaxException | JsonIOException exception) {
                    GunMod.LOGGER.warn("{} index file read fail!", file);
                    exception.printStackTrace();
                }
            });
            Files.walkFileTree(filePath, visitor);
        }
    }

    public static void loadAttachmentFromJsonString(Identifier id, String json) {
        AttachmentIndexPOJO indexPOJO = GSON.fromJson(json, AttachmentIndexPOJO.class);
        ATTACHMENT_INDEX.put(id, CommonAttachmentIndex.getInstance(id, indexPOJO));
    }
}
