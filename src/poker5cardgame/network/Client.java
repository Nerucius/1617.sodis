package poker5cardgame.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    ComUtils com;
    
    public Client() {
        
    }
    
    public void connect(String IP, int port){
        try {
            InetAddress address = InetAddress.getByName(IP);
            Socket sock = new Socket(address, port);
            com = new ComUtils(sock);
            
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    public void close(){        
    }

    public void writeMsg(String str) {
        try {
            com.write_string_variable(4, str);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
