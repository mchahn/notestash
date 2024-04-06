package de.org.mhahnc.app.notestash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStash extends FileLikeStash {

    protected File dir;

    public FileStash(File dir) throws IOException {
        this.dir = dir;
        Files.createDirectories(Paths.get(this.dir.toString()));
    }

    public void finish() {
    }

    @Override
    protected OutputStream addFile(String fileName, FileTime fileTime) throws IOException {
        String filePath = new File(this.dir, fileName).toString();
        FileOutputStream fos = new FileOutputStream(filePath);
        return new OutputStream() {
            public void write(int c) throws IOException {
                fos.write(c);
            }
            public void write(byte[] buf) throws IOException {
                fos.write(buf);
            }
            public void close() throws IOException {
                fos.close();
                Path path = FileSystems.getDefault().getPath(filePath);
                Files.getFileAttributeView(path, BasicFileAttributeView.class).setTimes(fileTime, fileTime, fileTime);
            }
        };
    }
}
