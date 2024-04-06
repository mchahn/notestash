# Intro

Simple command line tool written in Java to archive all notes of an IMAP backed account.

## Building

Happens with Maven:

    mvn install && mvn package

## Running

On the command line for the help screen just type:

    java -jar target/notestash-*-jar-with-dependencies.jar

This will prompt the help screen:

    Usage: notestash [-hV] [--delete]...
                    [--keystore-password=<keystorePassword>]... TARGET SERVER
                    FOLDER USERNAME
        TARGET      Target, either a path or a ZIP file.
        SERVER      IMAP server address.
        FOLDER      IMAP folder to read from.
        USERNAME    IMAP user name.
        --delete    Delete the messages after they have been stashed, IRREVERSIBLY!
    -h, --help      Show this help message and exit.
        --keystore-password=<keystorePassword>
                    Keystore password, to not get prompted for its input.
    -V, --version   Print version information and exit.

The IMAP folder is usually called _Notes_. The keystore password is optional (will be prompted if not given). To do the archiving it would look like this:

    java -jar target/notestash-*-jar-with-dependencies.jar archive.zip {IMAP-HOSTNAME} Notes {EMAIL-ADDRESS}

## Keystore

The account credentials are kept in a key store with the name _notestash.p12_ in the current directory (can be overriden by the environment variable `NOTESTASH_KEYSTORE_PATH` if needed). To create it:

    keytool -importpass -storetype pkcs12 -alias <IMAP-EMAIL> -keystore notestash.p12

To view it:

    keytool -list -v -keystore notestash.p12
