package poker5cardgame.network;

public interface ConnectionListener {
       
    public Network.Command receiveTCP(Network.Command packet);
    
}
