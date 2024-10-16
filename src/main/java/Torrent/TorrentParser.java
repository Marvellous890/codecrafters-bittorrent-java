package Torrent;

import B.Decoder;
import B.Encoder;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
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

    private Info parseInfo(){
        Info info = new Info();

        Map<String, ?> infoMap = (Map<String, ?>) obj.get("info");

        Encoder bencoder = new Encoder(infoMap);

        info.setName(new String((byte[]) infoMap.get("name")));
        info.setLength((long) infoMap.get("length"));
        info.setHash(sha1(bencoder.getEncoded()));
        info.setPieces(parsePieces());

        return info;
    }

    private Piece[] parsePieces(){
        Map<String, ?> infoMap = (Map<String, ?>) obj.get("info");

        byte[] bytes = (byte[]) infoMap.get("pieces");
        int division = 20;
        int len = bytes.length/division;
        Piece[] pieces = new Piece[len];
        int length = ((Long) infoMap.get("pieceLength")).intValue();
        for (int i = 0; i < len; i++) {
            Piece piece = new Piece();
            int from = i * division;
            int to = from + division;
            piece.setHash(bytesToHex(Arrays.copyOfRange(bytes, from, to)));
            piece.setLength(length);
            pieces[i] = piece;
        }
        for (Piece p: pieces){
            System.out.println(p.getHash());
        }
        return pieces;
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