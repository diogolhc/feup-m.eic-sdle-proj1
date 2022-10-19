import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// <filesystems>/<server_id>/topics/<topic>/<message_id>.extension
public class PersistentStorage {
    String DIRECTORY_PATH_BASE = "./filesystems/";
    String basePath;

    public PersistentStorage(String serverId) {
        this.basePath = this.DIRECTORY_PATH_BASE + serverId + "/";

        File directory = new File(this.basePath);
        if (!directory.exists()){
            if (!directory.mkdir()) {
                throw new RuntimeException("Could not create directory " + serverId + ".");
            }
        }
    }

    public void writeSync(String path, String content) throws IOException {
        File file = new File(this.basePath + path);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fr = new FileWriter(file, true);
        fr.write(content);
        fr.close();
    }

    public String readSync(String path) throws IOException {
        return Files.readString(Paths.get(this.basePath + path));
    }
}
