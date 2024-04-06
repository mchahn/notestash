package de.org.mhahnc.app.notestash;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "notestash", mixinStandardHelpOptions = true, version = "notestash")
public class Main implements Runnable {

    @Option(names = { "--delete" },
      description = "Delete the messages after they have been stashed, IRREVERSIBLY!")
    private boolean[] delete = new boolean[0];

    @Option(names = { "--keystore-password" },
    description = "Keystore password, to not get prompted for its input.")
    private String[] keystorePassword = new String[0];

    @Parameters(index = "0", paramLabel = "TARGET", description = "Target, either a path or a ZIP file.")
    private String target;

    @Parameters(index = "1", paramLabel = "SERVER", description = "IMAP server address.")
    private String server;

    @Parameters(index = "2", paramLabel = "FOLDER", description = "IMAP folder to read from.")
    private String folder;

    @Parameters(index = "3", paramLabel = "USERNAME", description = "IMAP user name.")
    private String username;

    private static String LOG_CONFIG = """
handlers= java.util.logging.ConsoleHandler
.level= DEBUG
java.util.logging.ConsoleHandler.level = DEBUG
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%1$tF %1$tT [%4$s] %5$s %n
""";

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    public void run() {
        try {
            this.runUNSAFE();
        } catch (Throwable err) {
            System.err.println("UNCAUGHT ERROR: " + err.getMessage());
            err.printStackTrace(System.err);
        }
    }

    public void runUNSAFE() throws Exception {

        char[] keystorePassword;
        if (this.keystorePassword.length == 0) {
            System.out.printf("keystore-password > ");
            keystorePassword = System.console().readPassword();
        } else {
            keystorePassword = this.keystorePassword[0].toCharArray();
        }
        String password = Main.loadPassword(username, keystorePassword);

        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(LOG_CONFIG.getBytes()));
        Logger logger = Logger.getLogger("main");

        Stash stash = target.endsWith(".zip") ?
            new ZipStash(new File(target), true, true) :
            new FileStash(new File(target));

        boolean delete = this.delete.length > 0 && this.delete[0] == true;

        try {
            new Note(server, folder, username, password, delete).stash(stash, logger);
        } finally {
            stash.finish();
        }
    }

    protected static String loadPassword(String username, char[] keystorePassword) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        String keystorePath = System.getenv().get("NOTESTASH_KEYSTORE_PATH");
        if (keystorePath == null) {
            keystorePath = "." + File.separator + "notestash.p12";
        }
        keystore.load(new FileInputStream(keystorePath), keystorePassword);
        SecretKey key = (SecretKey)keystore.getKey(username, keystorePassword);
        return new String(key.getEncoded());
    }
}
