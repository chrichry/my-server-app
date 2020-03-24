package isen.m1.garcia;

import javax.management.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private static ExecutorService executor = Executors.newFixedThreadPool(100);

    public static void main(String[] args) throws IOException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        ServerSocket server = null;
        Socket client = null;
        try {
            server = new ServerSocket(8888);
            ServerAdmin serverAdmin = new ServerAdmin();
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(serverAdmin, new ObjectName("com.garcia:type=ServerAdmin"));
            serverAdmin.setServerStatus(ServerStatus.RUNNING);
            serverAdmin.getServerStatus();
            System.out.println("Serveur démarré");
            while (true) {
                client = server.accept();
                ExecutionClient executionClient = new ExecutionClient(client);
                executor.execute(executionClient);
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        } finally {
            try {
                executor.shutdown();
                server.close();
            } catch (Exception exceptionClose) {
                System.out.println(exceptionClose.getMessage());
            }
        }
    }
}
class ExecutionClient implements Runnable {
    Socket client;
    public ExecutionClient(Socket client) {
        this.client = client;
    }
    public DiskBookDAO createLibrary() {
        DiskBookDAO books = DiskBookDAO.getInstance();
        try {
            Book book = Library.getInstance().newBook("Titre");
            books.insertBook(book);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            books.close();
        }
        return books;
    }
    public void run() {
        DiskBookDAO books = createLibrary();
        try {
            InputStream inputStream = client.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                if (line.isEmpty()) {
                    break;
                }
            }
            OutputStream outputStream = client.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            // Création de la page web
            printWriter.print("HTTP/1.1 200 \r\n");
            printWriter.print("Content-Type: text/html\r\n");
            printWriter.print("Connection: close\r\n");
            printWriter.print("\r\n");
            printWriter.print("<html><body><h1> Ca Marche !!! </h1><br>");
            Iterator<Book> it = books.findAll().iterator();
            while (it.hasNext()) {
                Book bookToPrint = it.next();
                printWriter.print("Title: " + bookToPrint.getTitle());
            }
            printWriter.print("</body></html>" + "\r\n");
            //fin page web
            Thread.sleep(1000);
            printWriter.flush();
            bufferedReader.close();
            outputStream.close();
            inputStream.close();
        } catch (IOException | InterruptedException e) {
            e.getStackTrace();
        }
        finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}