package poker5cardgame.network;

import java.net.Socket;

public class Server implements Runnable{

    // TODO DEFINE PARAMETERS

    public Server() {
    }
    
    public void bind(int port){
    }
    
    public void start(){
        new Thread(this).start();
    }

     @Override
    public void run() {
    }

    public void close() {
    }
    
    
    private class ServerThread implements Runnable{

        public ServerThread(Socket sock){
            
        }
        
        @Override
        public void run() {
        }
    
        
    }

}
