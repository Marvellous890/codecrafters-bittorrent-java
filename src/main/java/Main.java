import B.Decoder;

import Torrent.TorrentParser;
import Torrent.Torrent;

import Tracker.Tracker;
import Tracker.TrackerRequest;
import Tracker.TrackerResponse;
import Tracker.Peer;

import com.google.gson.Gson;
import core.Downloader;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String command = args[0];

        switch (command) {
            case "decode" -> {
                String bencodedValue = args[1];
                Object decoded;

                try {
                    Decoder b = new Decoder(bencodedValue.getBytes());
                    decoded = b.getDecoded();
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                    return;
                }

                System.out.println(gson.toJson(decoded));
            }
            case "info" -> {
                File file = new File(args[1]);

                Torrent torrent = null;

                try (FileInputStream fis = new FileInputStream(file)) {
                    TorrentParser parser = new TorrentParser(fis);
                    torrent = parser.getTorrent();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

                if (torrent != null) {
                    System.out.println("Tracker URL: " + torrent.getAnnounce());
                    System.out.println("Length: " + torrent.getInfo().getLength());

                    System.out.println("Info Hash: " + new String(Hex.encodeHex(torrent.getInfo().getHash())));
                    System.out.println("Piece Length: " + torrent.getInfo().getPieceLength());

                    /*System.out.println("Info Files");
                    for (InfoFile infoFile : torrent.getInfo().getFiles()) {
                        System.out.println("Paths:");
                        for (String path : infoFile.getPath()) {
                            System.out.println(path);
                        }

                        System.out.println("Length: " + infoFile.getLength());
                    }*/

                    System.out.println("Piece Hashes:");
                    for (byte[] piece : torrent.getInfo().getPieces()) {
                        System.out.println(Hex.encodeHex(piece));
                    }
                }
            }
            case "peers" -> {
                Torrent torrent = null;
                try (FileInputStream fis = new FileInputStream(args[1])) {
                    TorrentParser parser = new TorrentParser(fis);
                    torrent = parser.getTorrent();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                assert torrent != null;
                TrackerRequest req = new TrackerRequest(torrent);
                Tracker tracker = new Tracker(req);
                TrackerResponse response = tracker.getResponse();
                for (Peer p : response.getPeers()) {
                    System.out.println(p);
                }
            }
            case "handshake" -> handshake(args[1], args[2]);
            case "download_piece" -> {
                if (args.length < 5 || !"-o".equals(args[1])) {
                    System.out.println(
                            "Usage: download_piece -o <output_file> <torrent_file> <piece_index>"
                    );
                    return;
                }
                String outputPath = args[2];
                String torrentPath = args[3];
                int pieceIndex = Integer.parseInt(args[4]);

                try {
                    Downloader.downloadPiece(outputPath, torrentPath, pieceIndex);
                } catch (IOException | NoSuchAlgorithmException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            case "download" -> {
                if (args.length < 4 || !"-o".equals(args[1])) {
                    System.out.println(
                            "Usage: download -o <output_file> <torrent_file>"
                    );
                    return;
                }

                var outputPath = args[2];
                var torrentPath = args[3];

                try {
                    Downloader.download(outputPath, torrentPath);
                } catch (Exception ex) {
                    System.out.println(ex.getClass() + ": " + ex.getMessage());
                }
            }
            case null, default -> System.out.println("Unknown command: " + command);
        }

    }

    private static void handshake(String torrentFile, String hostPort) {
        try {
            File file = new File(torrentFile);
            FileInputStream fis = new FileInputStream(file);
            TorrentParser parser = new TorrentParser(fis);

            Torrent torrent = parser.getTorrent();

            String[] split = hostPort.split(":");
            String host = split[0], port = split[1];
            try (Socket clientSocket = new Socket(host, Integer.parseInt(port))) {
                OutputStream outputStream = clientSocket.getOutputStream();
                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream();
                byteArrayOutputStream.write(19);
                byteArrayOutputStream.write(
                        "BitTorrent protocol".getBytes(StandardCharsets.UTF_8));
                byteArrayOutputStream.write(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
                byteArrayOutputStream.write(torrent.getInfo().getHash());

                SecureRandom secureRandom = new SecureRandom();
                byte[] randomBytes = new byte[20]; // my peer_id
                secureRandom.nextBytes(randomBytes);

                byteArrayOutputStream.write(randomBytes);
                outputStream.write(byteArrayOutputStream.toByteArray());
                InputStream inputStream = clientSocket.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int length = inputStream.read(buf);
                baos.write(buf, 0, length);
                inputStream.close();
                byte[] responseBytes = baos.toByteArray();
                byte[] peerId = Arrays.copyOfRange(responseBytes, 48, 68);
                String peerIdHex = new String(Hex.encodeHex(peerId));
                System.out.println("Peer ID: " + peerIdHex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}