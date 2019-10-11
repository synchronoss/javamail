package com.sun.mail.imap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IMAPStoreTest {
    private IMAPStore store;
    private MockIMAPServer server;

    @Before
    public void setUp() throws MessagingException {
        final int port = 143;
        server = new MockIMAPServer(port, 20);
        server.start();

        String host = "localhost";
        String username = "u1@openwave.dom";
        String password = "p";
        Properties props = new Properties();

        Session session = Session.getInstance(props);
        store = (IMAPStore) session.getStore("imap");
        store.connect(host, port, username, password);

    }

    @After
    public void tearDown() throws MessagingException {
        store.close();
        server.close();
    }

    @Test
    public void serverDown() {
        server.close();
        assertTrue(store.isConnected() == false);
        assertTrue(server.getClientCounts() == 0);
    }

    @Test
    public void serverDown2() throws MessagingException {
        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        IMAPMessage msg = (IMAPMessage) inbox.getMessageByUID(1000);
        assertTrue(msg != null);
        assertEquals(true, store.isConnected());
        assertEquals(2, server.getClientCounts());

        server.close();
        assertTrue(store.isConnected() == false);
        assertTrue(server.getClientCounts() == 0);
    }

    @Test
    public void storeConnected() {
        assertTrue(store.isConnected());
        assertEquals(1, server.getClientCounts());
    }

    @Test
    public void openFolder() throws Exception {
        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        store.isConnected();
        assertEquals(2, server.getClientCounts());
    }

    @Test
    public void closeStore() throws MessagingException {
        assertEquals(1, server.getClientCounts());
        store.close();
        assertEquals(0, server.getClientCounts());
        assertEquals(false, store.isConnected());
        assertEquals(0, server.getClientCounts());
    }

    @Ignore
    @Test(expected = FolderClosedException.class)
    public void folderClosedExceptionNotFixed() throws Exception {
        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        IMAPMessage msg = (IMAPMessage) inbox.getMessageByUID(1000);
        assertTrue(msg != null);

        assertEquals(true, store.isConnected());
        assertEquals(2, server.getClientCounts());
        server.closeClientSocket(1);
        assertEquals(1, server.getClientCounts());

        /*
         * Upon seeing "* BYE IMAP4 server terminating connection",
         * java-mail close all connections and folders in its pool
         */
        assertEquals(false, store.isConnected());
        assertEquals(0, server.getClientCounts());
        msg.getContent();//throws FolderClosedException
    }

    @Test
    public void folderClosedExceptionFixed() throws Exception {
        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        IMAPMessage msg = (IMAPMessage) inbox.getMessageByUID(1000);
        assertTrue(msg != null);

        assertEquals(true, store.isConnected());
        assertEquals(2, server.getClientCounts());
        server.closeClientSocket(1);
        assertEquals(1, server.getClientCounts());

        assertEquals(true, store.isConnected());
        assertEquals(2, server.getClientCounts());
        assertTrue(msg.getContent() != null);
    }
}
