package Tracker;

import Torrent.Torrent;
import core.PieceDownloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TrackerRequest {
    private String announce;
    private byte[] infoHash;
    private byte[] peerId;
    private int port;
    private int uploaded;
    private int downloaded;
    private long left;
    private int compact;
    public TrackerRequest(Torrent torrent) {
        announce = torrent.getAnnounce();
        infoHash = torrent.getInfo().getHash();
        peerId = PieceDownloader.PEER_ID.getBytes();
        port = 6881;
        uploaded = 0;
        downloaded = 0;
        left = torrent.getInfo().getLength();
        compact = 1;
    }
    public URI getUri(){
        StringBuilder uri = new StringBuilder(announce + '/');
        uri.append("?info_hash=").append(urlEncode(infoHash));
        uri.append("&peer_id=").append(urlEncode(peerId));
        uri.append("&port=").append(port);
        uri.append("&uploaded=").append(uploaded);
        uri.append("&downloaded=").append(downloaded);
        uri.append("&left=").append(left);
        uri.append("&compact=").append(compact);
        try{
            return new URI(uri.toString());
        } catch (URISyntaxException e){
            e.printStackTrace();
            return null;
        }
    }
    private String urlEncode(byte[] bytes){
        StringBuilder encoded = new StringBuilder();
        encoded.append("%");
        for (int i = 0; i < bytes.length; i++) {
            encoded.append(URLEncoder.encode(String.format("%02X", bytes[i]), StandardCharsets.UTF_8));
            if (i < bytes.length - 1) {
                encoded.append("%");
            }
        }
        return encoded.toString();
    }
}