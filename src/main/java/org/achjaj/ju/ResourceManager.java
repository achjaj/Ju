package org.achjaj.ju;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

public class ResourceManager {
    final private HashMap<String, String> env;
    final private FileSystem fs;
    final private Path root;

    public ResourceManager(URI uri) throws IOException {
        this.env = new HashMap<>();

        if (uri.getScheme().equals("jar")) {
            root = null;
            fs = FileSystems.newFileSystem(URI.create(uri.toString().split("!")[0]), env);
        } else {
            root = Path.of(uri).getParent();
            fs = null;
        }
    }

    public void close() throws IOException {
        if (isJar())
            fs.close();
    }

    public boolean isJar() {
        return fs != null;
    }

    public Path getResource(String path) {
        return isJar() ? fs.getPath(path) : root.resolve(path);
    }
}
