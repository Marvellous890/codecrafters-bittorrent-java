package Tracker;

import B.Decoder;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

public class TrackerResponse {
    private int interval;
    private Peer[] peers;
    public TrackerResponse(InputStream in) {
        Decoder decoder = new Decoder(in, true);
        Map<String, Object> decodedBody = (Map<String, Object>) decoder.getDecoded();
        peers = parsePeers((byte[]) decodedBody.get("peers"));
        interval = ((Long) decodedBody.get("interval")).intValue();
    }
    public int getInterval() {
        return interval;
    }
    public Peer[] getPeers() {
        return peers;
    }
    private Peer[] parsePeers(byte[] bytes){
        int PEER_SIZE = 6;
        int totalPeers = bytes.length / PEER_SIZE;
        Peer[] peers = new Peer[totalPeers];
        for (int i = 0; i < totalPeers; i++) {
            int offset = i * PEER_SIZE;
            byte[] ipBytes = Arrays.copyOfRange(bytes, offset, offset + 4);
            byte[] portBytes = Arrays.copyOfRange(bytes, offset + 4, offset + PEER_SIZE);
            InetAddress ip = null;
            try{
                ip = InetAddress.getByAddress(ipBytes);
            } catch (UnknownHostException e){
                e.printStackTrace();
            }
            int port = 0;
            for (byte portByte : portBytes) {
                port = (port << 8) | (portByte & 0xFF);
            }
            peers[i] = new Peer(ip, port);
        }
        return peers;
    }
}
