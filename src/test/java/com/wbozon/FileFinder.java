package com.wbozon;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.StreamSupport;
import java.util.Locale;
import java.util.regex.Pattern;
public class FileFinder {
    public static Path findLatestFile(String dir, String filemask) {
        Path directory = Paths.get(dir);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filemask);
        Optional<Path> latestFile = Optional.empty();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            latestFile = StreamSupport.stream(stream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .filter(matcher::matches)
                    .max(Comparator.comparingLong(p -> {
                        try {
                            return Files.readAttributes(p, BasicFileAttributes.class).creationTime().toMillis();
                        } catch (IOException e) {
                            return Long.MIN_VALUE;
                        }
                    }));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return latestFile.orElse(null);
    }
    public static Path findLatestFileWithMask(String dir, String filemask) {
        Path directory = Paths.get(dir);
        // Преобразование маски файла в регулярное выражение
        String regexMask = filemask
        .replaceAll("\\.", "\\\\.")
        .replaceAll("\\*", ".*")
        .replaceAll("\\?", ".")
        .toLowerCase(Locale.ROOT);
        Pattern pattern = Pattern.compile(regexMask);
        
        Optional<Path> latestFile = Optional.empty();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
        latestFile = StreamSupport.stream(stream.spliterator(), false)
        .filter(Files::isRegularFile)
        .filter(path -> pattern.matcher(path.getFileName().toString().toLowerCase(Locale.ROOT)).matches())
        .max(Comparator.comparingLong(p -> {
        try {
        return Files.readAttributes(p, BasicFileAttributes.class).creationTime().toMillis();
        } catch (IOException e) {
        return Long.MIN_VALUE;
        }
        }));
        } catch (IOException e) {
        e.printStackTrace();
        }
        
        return latestFile.orElse(null);
        }
    public static Path findLatestFileIgnoreCase(String dir, String filemask) {
        Path directory = Paths.get(dir);
        String regexMask = filemask.replaceAll("\\*", ".*").toLowerCase(Locale.ROOT);
        Pattern pattern = Pattern.compile(regexMask);
        
        Optional<Path> latestFile = Optional.empty();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
        latestFile = StreamSupport.stream(stream.spliterator(), false)
        .filter(Files::isRegularFile)
        .filter(path -> pattern.matcher(path.getFileName().toString().toLowerCase(Locale.ROOT)).matches())
        .max(Comparator.comparingLong(p -> {
        try {
        return Files.readAttributes(p, BasicFileAttributes.class).creationTime().toMillis();
        } catch (IOException e) {
        return Long.MIN_VALUE;
        }
        }));
        } catch (IOException e) {
        e.printStackTrace();
        }
        
        return latestFile.orElse(null);
        }
    public static void main(String[] args) {
        // Path latestFilePath = findLatestFileWithMask("C:\\Users\\kovalenko\\Downloads", "polidrev.XLSX");
        Path latestFilePath = findLatestFileWithMask("C:\\Users\\kovalenko\\Downloads", "price.??-??-????*.xls");
        if (latestFilePath != null) {
            System.out.println("Самый свежий файл: " + latestFilePath);
        } else {
            System.out.println("Файлы не найдены.");
        }
    }
}
