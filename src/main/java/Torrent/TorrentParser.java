package Torrent;

import B.Decoder;
import B.Encoder;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private Torrent parse(){
        Torrent torrent = new Torrent();

        torrent.setAnnounce(parseString("announce"));
        torrent.setCreatedBy(parseString("created by"));
        torrent.setAnnounceList(parseAnnounceList());
        torrent.setInfo(parseInfo());

        return torrent;
    }

    private String parseString (String s) {
        if (obj.containsKey(s)){
            return new String((byte[]) obj.get(s));
        }

        return null;
    }

    private List<String> parseAnnounceList(){
        if (obj.containsKey("announce-list")){
            return (List<String>) obj.get("announce-list");
        }

        return null;
    }

    private Info parseInfo(){
        Info info = new Info();

        Map<String, ?> infoMap = (Map<String, ?>) obj.get("info");

        Encoder bencoder = new Encoder(infoMap);

        info.setName(parseInfoString(infoMap, "name"));
        info.setSource(parseInfoString(infoMap, "source"));
        info.setTracker(parseInfoString(infoMap, "tracker"));
        info.setHash(sha1(bencoder.getEncoded()));
        info.setPieces(parsePieces());
        info.setLength(parseInfoLong(infoMap, "length"));
        info.setPieceLength(((int) parseInfoLong(infoMap, "pieceLength")));
        info.setFiles(parseFiles(infoMap));

        return info;
    }

    private String parseInfoString(Map<String, ?> infoMap, String str){
        if (infoMap.containsKey(str)){
            return new String((byte[]) infoMap.get(str));
        }

        return null;
    }

    private long parseInfoLong(Map<String, ?> infoMap, String str){
        if (infoMap.containsKey(str)){
            return (long) infoMap.get(str);
        }

        return 0;
    }

    private List<InfoFile> parseFiles(Map<String, ?> infoMap){
        if (infoMap.containsKey("files")) {
            List<Map<String, ?>> files = (List<Map<String, ?>>) infoMap.get("files");
            List<InfoFile> infoFiles = new ArrayList<>();

            for (Map<String, ?> file : files) {
                InfoFile infoFile = new InfoFile();
                infoFile.setLength(parseInfoLong(file, "length"));

                List<String> path = new ArrayList<>();
                List<?> filePath = (List<?>) file.get("path");
                for (Object _path : filePath) {
                    path.add(new String((byte[]) _path));
                }

                infoFile.setPath(path);
                infoFiles.add(infoFile);
            }

            return infoFiles;
        }

        return null;
    }

    private byte[][] parsePieces(){
        Map<String, ?> infoMap = (Map<String, ?>) obj.get("info");

        byte[] bytes = (byte[]) infoMap.get("pieces");
        int division = 20;
        int len = bytes.length/division;
        byte[][] pieces = new byte[len][];
        for (int i = 0; i < len; i++) {
            int from = i * division;
            int to = from + division;
            byte[] piece = Arrays.copyOfRange(bytes, from, to);
            pieces[i] = piece;
        }
        return pieces;
    }

    private byte[] sha1(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}