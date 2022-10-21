package data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PersistentStorage {
    public final String DIRECTORY_PATH_BASE = "filesystems/";
    private final String basePath;

    public PersistentStorage(String baseFolder) {
        this.basePath = this.DIRECTORY_PATH_BASE + baseFolder + "/";

        File directory = new File(this.basePath);
        if (!directory.exists()){
            if (!directory.mkdirs()) {
                throw new RuntimeException("Could not create directory " + baseFolder + ".");
            }
        } else if (!directory.isDirectory()) {
            throw new RuntimeException(directory + " is not a directory.");

        }
    }

    private Path getPath(String... subFolders) {
        return Paths.get(this.basePath, subFolders);
    }

    public List<String> listFiles(String... subFolders) {
        String[] fileList = this.getPath(subFolders).toFile().list();
        return fileList == null ? List.of() : Arrays.asList(fileList);
    }

    public boolean exists(String... subFolders) {
        return this.getPath(subFolders).toFile().isFile();
    }

    public void makeDirectory(String... subFolders) throws IOException {
        Files.createDirectories(this.getPath(subFolders));
    }

    public FileWriter write(String path) throws IOException {
        return new FileWriter(this.getPath(path).toFile());
    }

    public void write(String path, String content) throws IOException {
        try (FileWriter writer = this.write(path)) {
            writer.write(content);
        }
    }

    public String read(String path) throws IOException {
        return Files.readString(Paths.get(this.basePath + path));
    }

    public List<String> readLines(String path) throws IOException {
        return Files.readAllLines(Paths.get(this.basePath + path));
    }

    public void delete(String path) throws IOException {
        Files.deleteIfExists(getPath(path));
    }
}
