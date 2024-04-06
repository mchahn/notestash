package de.org.mhahnc.app.notestash;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.Date;

public abstract class FileLikeStash implements Stash {

    protected final static String EXT = ".eml";
    protected final static int MAX_NAME_LENGTH = 224 - EXT.length();

    public OutputStream add(String name) throws IOException {
        return this.addFile(name, FileTime.fromMillis(System.currentTimeMillis()));
    }

    public OutputStream add(String name, Date createdAt) throws IOException {
        name = URLEncoder.encode(name, StandardCharsets.UTF_8);
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }
        String fileName = name + ".eml";
        FileTime fileTime = FileTime.fromMillis(createdAt.getTime());
        return this.addFile(fileName, fileTime);
    }

    protected abstract OutputStream addFile(String fileName, FileTime fileTime) throws IOException;
}
