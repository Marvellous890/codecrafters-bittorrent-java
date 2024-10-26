import B.Decoder;

import Torrent.TorrentParser;
import Torrent.Torrent;
import Tracker.Tracker;
import Tracker.TrackerRequest;
import Tracker.TrackerResponse;
import Tracker.Peer;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String command = args[0];

        if ("decode".equals(command)) {
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

        } else if (command.equals("info")) {
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
                System.out.println("Piece Hashes:");

                for (byte[] piece : torrent.getInfo().getPieces()) {
                    System.out.println(Hex.encodeHex(piece));
                }
            }
        } else if (command.equals("peers")) {
            File file = new File(args[1]);
            Torrent torrent = null;
            try (FileInputStream fis = new FileInputStream(file)) {
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
        } else if ("handshake".equals(command)) {
            handshake(args[1], args[2]);
        } else {
            System.out.println("Unknown command: " + command);
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
                byteArrayOutputStream.write(
                        // rang
                        "00112233445566778899".getBytes(StandardCharsets.UTF_8));
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