package Torrent;

import B.Decoder;
import B.Encoder;
import com.google.gson.Gson;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;

public class TorrentParser {
    private Map<String, ?> obj;
    private Torrent torrent = new Torrent();
    public TorrentParser(InputStream in) {
        Decoder Decoder = new Decoder(in, true);
        this.obj = (Map<String, ?>) Decoder.getDecoded();
        torrent = parse();
    }

    public Torrent getTorrent() {
        return torrent;
    }

    public Torrent parse(){
        Torrent torrent = new Torrent();
        torrent.setAnnounce(parseAnnounce());
        torrent.setInfo(parseInfo());
        return torrent;
    }

    private URL parseAnnounce(){
        try{
            String announce = new String((byte[]) obj.get("announce"));
            return new URI(announce).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Torrent.Info parseInfo(){
        var info = new Torrent.Info();
        Map<String, ?> infoMap = (Map<String, ?>) obj.get("info");
        info.setName(new String((byte[]) infoMap.get("name")));
        info.setLength((long) infoMap.get("length"));
        info.setPieceLength((long) infoMap.get("pieceLength"));
        info.setHash(hashInfo());
        return info;
    }

    private String hashInfo(){
        Map<String, ?> infoMap = (Map<String, ?>) obj.get("info");
        Gson gson = new Gson();
        System.out.println(gson.toJson(infoMap));
        Encoder encoder = new Encoder(infoMap);
        Decoder Decoder = new Decoder(encoder.getEncoded(), true);
        System.out.println(gson.toJson(Decoder.getDecoded()));
        byte[] bytes = encoder.getEncoded();
        return sha1(bytes);
    }

    private String sha1(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input);
            return bytesToHex(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}