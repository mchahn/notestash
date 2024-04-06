package de.org.mhahnc.app.notestash;

import javax.mail.*;
import java.util.logging.Logger;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;

class Imap {
    public final static int DEFAULT_PORT = 993;
}

public class Note {

    private static int STREAM_BUFFER_SIZE = 256 * 1024;

    protected String host;
    protected String folderName;
    protected String username;
    protected String password;
    protected boolean delete;

    public Note(
        String host,
        String folderName,
        String username,
        String password,
        boolean delete
    ) {
        this.host = host;
        this.folderName = folderName;
        this.username = username;
        this.password = password;
        this.delete = delete;
    }

    public void stash(Stash stash, Logger logger) throws Exception
    {
        Properties props = new Properties();
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.ssl.protocols", "TLSv1.2");

        Session session = Session.getDefaultInstance(props, null);

        Store store = session.getStore("imap");
        store.connect(this.host, Imap.DEFAULT_PORT, this.username, this.password);

        Folder folder = store.getFolder(this.folderName);

        folder.open(this.delete ?
            Folder.READ_WRITE :
            Folder.READ_ONLY);

        Message[] messages = folder.getMessages();

        for (int i = 0, n = messages.length; i < n; i++) {

            Message message = messages[i];

            String subject = message.getSubject();

            logger.info(String.format("reading message %d/%d '%s' ...", i + 1, n, subject));

            String[] messageIds = message.getHeader("Message-Id");
            String messageId;
            if (messageIds == null || messageIds.length == 0) {
                messageId = UUID.randomUUID().toString();
                logger.warning("no message ID found, created: " + messageId);
            } else {
                messageId = messageIds[0];
            }

            byte[] data = message.getInputStream().readAllBytes();
            logger.info(data.length + " bytes (from: " + message.getFrom()[0] + "), stashing ...");

            try (OutputStream os = stash.add(messageId + " " + subject, message.getSentDate())) {
                OutputStream bos = new BufferedOutputStream(os, STREAM_BUFFER_SIZE);
                try {
                    message.writeTo(bos);
                } finally {
                    bos.flush();
                    bos.close();
                }
            }

            if (this.delete) {
                logger.info("marking message as deleted ...");
                message.setFlag(Flags.Flag.DELETED, true);
            }
        }

        folder.close(this.delete);
        store.close();

        logger.info(String.format("stashed %d message(s)", messages.length));
    }
}
