package com.sun.mail.imap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MockIMAPServer {
    private ServerSocket serverSocket;
    private Vector<Context> clients;
    private ExecutorService worker;
    private ExecutorService connector;

    static class Context {
        int port;
        Socket socket;
        volatile boolean run;

        public Context(Socket socket) {
            this.socket = socket;
            port = socket.getLocalPort();
            run = true;
        }

        public void stop() {
            run = false;
        }
    }

    public MockIMAPServer(int port, int maxClients) {
        try {
            clients = new Vector<>();
            connector = Executors.newSingleThreadExecutor();
            worker = Executors.newFixedThreadPool(maxClients);
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println(String.format("listening to %d\n", getListenPort()));
            System.out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getListenPort() {
        return serverSocket.getLocalPort();
    }

    public void start() {
        connector.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final Socket client = serverSocket.accept();
                        final Context context = new Context(client);
                        doWrite(client, "* OK IMAP4 server (MOCK) ready now");
                        clients.add(context);
                        worker.submit(new Runnable() {
                            @Override
                            public void run() {
                                processLoop(context);
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void doWrite(Socket socket, String contents) {
        System.out.println(String.format("<<<<<<<< %s", contents));
        System.out.flush();
        try {
            socket.getOutputStream().write(contents.getBytes());
            if (!contents.endsWith("\r\n")) {
                socket.getOutputStream().write("\r\n".getBytes());
            }
            socket.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processLoop(final Context context) {
        final Socket clientSocket = context.socket;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            while (context.run) {
                String request = reader.readLine();
                if (request != null) {
                    String response = handle(clientSocket, request);
                    doWrite(clientSocket, response);
                } else {
                    // end of stream reached, client closed connection
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String handle(final Socket clientSocket, String request) {
        System.out.println(String.format(">>>>>>>> %s", request));
        System.out.flush();
        StringBuilder sb = new StringBuilder();
        try (Scanner scan = new Scanner(request)) {
            String uniq = scan.next();
            String req = scan.nextLine().trim();

            sb.append(uniq).append(" ");
            if (req.startsWith("LOGIN")) {
                sb.append("OK LOGIN completed");
            } else if (req.startsWith("NOOP")) {
                sb.append("OK NOOP completed");
            } else if (req.startsWith("CAPABILITY")) {
                doWrite(clientSocket, "* CAPABILITY IMAP4rev1");
                sb.append("OK CAPABILITY completed");
            } else if (req.startsWith("LOGOUT")) {
                sb.append("OK LOGOUT completed");
                int index = getClientIndexByPort(clientSocket.getLocalPort());
                closeClientSocket(index);
            } else if (req.startsWith("NAMESPACE")) {
                doWrite(clientSocket, "* NAMESPACE ((\"\" \"/\")) NIL NIL");
                sb.append("OK NAMESPACE completed");
            } else if (req.startsWith("LIST")) {
                if (!req.endsWith("\"\" \"\"")) {
                    doWrite(clientSocket, "* LIST \"\" \"INBOX\"");
                }
                sb.append("OK LIST completed");
            } else if (req.startsWith("EXAMINE")) {
                doWrite(clientSocket, "* 4 Exists");
                doWrite(clientSocket, "* OK [UNSEEN 1] First unseen message");
                sb.append("OK [READ-ONLY] EXAMINE completed");
            } else if (req.startsWith("SELECT")) {
                doWrite(clientSocket, "* 4 Exists");
                doWrite(clientSocket, "* OK [UNSEEN 1] First unseen message");
                doWrite(clientSocket, "* OK [UIDVALIDITY 1568094579] UIDs valid");
                sb.append("OK [READ-WRITE] SELECT completed");
            } else if (req.startsWith("UID FETCH 1000 (UID)")) {
                doWrite(clientSocket, "* 1 FETCH (UID 1000)");
                sb.append("OK UID FETCH completed");
            } else if (req.startsWith("FETCH")) {
                if (req.endsWith("(BODYSTRUCTURE)")) {
                    doWrite(clientSocket, "* 1 FETCH (BODYSTRUCTURE (\"text\" \"plain\" (\"charset\" \"UTF-8\" \"format\" \"flowed\" \"delsp\" \"no\") NIL NIL \"7bit\" 0 0))");
                } else {
                    doWrite(clientSocket,
                        "* 4 FETCH (ENVELOPE (\"Wed, 18 Sep 2019 14:50:34 +0100\" \"C\" ((NIL NIL \"u1\" \"tls.thunder.dom\")) ((NIL NIL \"u1\" \"tls.thunder.dom\")) ((NIL NIL \"u1\" \"tls.thunder.dom\")) ((NIL NIL \"u1\" \"tls.thunder.dom\")) NIL NIL \"<86b6184.22.16d44a385fb.Webtop.23@tls.thunder.dom>\" \"<de03ac5.23.16d44a3994c.Webtop.23@tls.thunder.dom>\") INTERNALDATE \"18-Sep-2019 13:50:35 +0000\" RFC822.SIZE 994 FLAGS () BODYSTRUCTURE (\"text\" \"plain\" (\"charset\" \"UTF-8\" \"format\" \"flowed\" \"delsp\" \"no\") NIL NIL \"7bit\" 0 0) UID 1643 BODY[HEADER.FIELDS (X-PRIORITY IMPORTANCE X-WEBTOP-FORWARDED-MESSAGE-GUID X-CP-CAL-NOTIFICATION REFERENCES DISPOSITION-NOTIFICATION-TO MESSAGE-ID IN-REPLY-TO X-CP-WEBTOP-DRAFT)] {261}\n"
                                + "Message-ID: <de03ac5.23.16d44a3994c.Webtop.23@tls.thunder.dom>\n"
                                + "In-Reply-To: <86b6184.22.16d44a385fb.Webtop.23@tls.thunder.dom>\n"
                                + "References: <69b4d793.21.16d44a372b2.Webtop.23@tls.thunder.dom><86b6184.22.16d44a385fb.Webtop.23@tls.thunder.dom>\n"
                                + "X-Priority: 1\n" + "\n" + ")");
                }
                sb.append("OK FETCH completed");
            } else if (req.startsWith("CLOSE")) {
                sb.append("OK CLOSE completed");
            }
            return sb.toString();
        }
    }

    private int getClientIndexByPort(int port) {
        for (int i = 0; i < clients.size(); ++i) {
            if (clients.get(i).port == port) {
                return i;
            }
        }
        return -1;
    }

    public void closeClientSocket(int index) {
        if (index < 0 || index >= clients.size()) {
            throw new IllegalArgumentException();
        }
        Context context = clients.remove(index);
        try {
            doWrite(context.socket, "* BYE IMAP4 server terminating connection");
            context.socket.close();
            context.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            for (Context context : clients) {
                if (context.socket != null && context.socket.isConnected()) {
                    try {
                        context.socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            connector.shutdownNow();
            worker.shutdownNow();
        }
        clients.clear();
    }

    public int getClientCounts() {
        return clients.size();
    }
}