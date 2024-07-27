package com.imguns.guns.util;

import com.imguns.guns.GunMod;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.stream.Stream;

public final class GetJarResources {
    private GetJarResources() {
    }

    /**
     * Copy the files of this module to the specified folder. The original file will be forcibly overwritten.
     *
     * @param srcPath The address of the source file in the jar
     * @param root    The root directory to which you want to copy
     * @param path    Path after copying
     */
    public static void copyModFile(String srcPath, Path root, String path) {
        URL url = GunMod.class.getResource(srcPath);
        try {
            if (url != null) {
                FileUtils.copyURLToFile(url, root.resolve(path).toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy the folder of this module to the specified folder. The original folder will be forcibly overwritten.
     *
     * @param srcPath The address of the source file in the jar
     * @param root    The root directory to which you want to copy
     * @param path    Path after copying
     */
    public static void copyModDirectory(Class<?> resourceClass, String srcPath, Path root, String path) {
        URL url = resourceClass.getResource(srcPath);
        try {
            if (url != null) {
                copyFolder(url.toURI(), root.resolve(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copy the folder of this module to the specified folder. The original folder will be forcibly overwritten.
     *
     * @param srcPath The address of the source file in the jar
     * @param root    The root directory to which you want to copy
     * @param path    Path after copying
     */
    public static void copyModDirectory(String srcPath, Path root, String path) {
        copyModDirectory(GunMod.class, srcPath, root, path);
    }

    @Nullable
    public static InputStream readModFile(String filePath) {
        URL url = GunMod.class.getResource(filePath);
        try {
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void copyFolder(URI sourceURI, Path targetPath) throws IOException {
        if (Files.isDirectory(targetPath)) {
            // 删掉原文件夹，达到强行覆盖的效果
            deleteFiles(targetPath);
        }
        // 使用 Files.walk() 遍历文件夹中的所有内容
        try (Stream<Path> stream = Files.walk(Paths.get(sourceURI), Integer.MAX_VALUE)) {
            stream.forEach(source -> {
                Path target = getTargetPath(sourceURI, targetPath, source);
                try {
                    // 复制文件或文件夹
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    // 处理异常，例如权限问题等
                    e.printStackTrace();
                }
            });
        }
    }

    private static @NotNull Path getTargetPath(URI sourceURI, Path targetPath, Path source) {
        var relativize = sourceURI.relativize(source.toUri()).toString();
        // 生成目标路径
        if (Objects.equals(sourceURI.getScheme(), "jar")) {
            URI pSourceURI = URI.create(sourceURI.getSchemeSpecificPart().replace(" ", "%20"));
            URI pSource = URI.create(source.toUri().getSchemeSpecificPart().replace(" ", "%20"));
            relativize = pSourceURI.relativize(pSource).toString();
        }
        return targetPath.resolve(relativize);
    }

    private static void deleteFiles(Path targetPath) throws IOException {
        Files.walkFileTree(targetPath, new SimpleFileVisitor<>() {
            // Go ahead and traverse the deleted files.
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            // Iterate through the deleted directories again
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}