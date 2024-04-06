package de.org.mhahnc.app.notestash;

import java.io.OutputStream;
import java.util.Date;

public interface Stash {

    public OutputStream add(String name) throws Exception;
    public OutputStream add(String name, Date createdAt) throws Exception;
    public void finish() throws Exception;
}
