package de.org.mhahnc.app.notestash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStash extends FileLikeStash {

    protected ZipOutputStream zos;
    protected boolean autoClose;

    public ZipStash(File zipFile, boolean forceDirectory, boolean autoClose) throws IOException {
        if (forceDirectory) {
            Files.createDirectories(Paths.get(zipFile.getParent()));
        }
        this.zos = new ZipOutputStream(new FileOutputStream(zipFile));
        this.zos.setLevel(ZipEntry.DEFLATED);
        this.autoClose = autoClose;
    }

    public void finish() throws IOException {
        if (this.autoClose) {
            this.zos.close();
        }
    }

    @Override
    protected OutputStream addFile(String fileName, FileTime fileTime) throws IOException {
        ZipEntry ze = new ZipEntry(fileName);
        ze.setLastAccessTime(fileTime);
        ze.setLastModifiedTime(fileTime);
        ze.setCreationTime(fileTime);
        this.zos.putNextEntry(ze);
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                ZipStash.this.zos.write(b);
            }
            @Override
            public void write(byte[] buf, int ofs, int len) throws IOException {
                ZipStash.this.zos.write(buf, ofs, len);
            }
            @Override
            public void close() {
            }
        };
    }
}
