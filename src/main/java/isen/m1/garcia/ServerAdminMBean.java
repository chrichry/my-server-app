package isen.m1.garcia;
import java.io.IOException;
public interface ServerAdminMBean {
    public ServerStatus getServerStatus();
    public void setServerStatus(ServerStatus serverStatus);
    public void shutdown() throws IOException;
}