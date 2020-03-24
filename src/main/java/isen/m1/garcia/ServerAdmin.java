package isen.m1.garcia;
import java.io.IOException;
import java.net.ServerSocket;
public class ServerAdmin implements ServerAdminMBean {
    private ServerStatus serverStatus;
    private ServerSocket serverSocket;
    public ServerAdmin() {}
    public ServerStatus getServerStatus() {
        return serverStatus;
    }
    public void setServerStatus(ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public void shutdown() throws IOException{
        serverSocket.close();
    }
}