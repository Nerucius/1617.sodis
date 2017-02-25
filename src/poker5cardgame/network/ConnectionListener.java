package poker5cardgame.network;

public interface ConnectionListener {
       
    public Network.Packet receiveTCP(Network.Packet packet);
    
}
