package core;

import Torrent.Torrent;
import Torrent.TorrentParser;
import Tracker.Tracker;
import Tracker.Peer;
import Tracker.TrackerRequest;
import Tracker.TrackerResponse;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class PieceDownloader {
    public static final String PEER_ID = "-MY0001-" + randomString(12);
    public static final int BLOCK_SIZE = 16 * 1024;
    public static final int MESSAGE_LENGTH_SIZE = 4;
    public static final int MESSAGE_ID_SIZE = 1;
    public static final byte BITFIELD = 5;
    public static final byte INTERESTED = 2;
    public static final byte UNCHOKE = 1;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;

    public static void downloadPiece(String outputPath, String torrentPath, int pieceIndex) throws NoSuchAlgorithmException, IOException {
        Torrent torrent = null;

        try (FileInputStream fis = new FileInputStream(torrentPath)) {
            TorrentParser parser = new TorrentParser(fis);
            torrent = parser.getTorrent();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (torrent == null) {
            throw new RuntimeException("Failed to parse torrent file");
        }

        TrackerRequest req = new TrackerRequest(torrent);
        Tracker tracker = new Tracker(req);
        TrackerResponse response = tracker.getResponse();

        Peer[] peers = response.getPeers();

        if (peers.length == 0) {
            throw new RuntimeException("No peers available");
        }
        byte[] downloadedPiece = null;
        for (Peer peer : peers) {
            try {
                downloadedPiece = downloadPieceFromPeer(peer.getIp().getHostAddress(), peer.getPort(), torrent, pieceIndex);
                break;
            } catch (Exception e) {
                System.err.println("Failed to download from peer " + peer + ": " + e.getMessage());
            }
        }
        if (downloadedPiece == null) {
            throw new RuntimeException("Failed to download piece from any peer");
        }
        byte[] expectedHash = torrent.getInfo().getPieces()[pieceIndex];
        byte[] actualHash = MessageDigest.getInstance("SHA-1").digest(downloadedPiece);
        if (!Arrays.equals(expectedHash, actualHash)) {
            throw new RuntimeException("Piece hash verification failed");
        }
        Files.write(new File(outputPath).toPath(), downloadedPiece);
    }

    private static byte[] downloadPieceFromPeer(String host, int port, Torrent torrent, int pieceIndex)
            throws Exception {
        try (Socket socket = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            byte[] handshake = createHandshakeMessage(torrent.getInfo().getHash());
            out.write(handshake);
            out.flush();

            byte[] response = new byte[68];

            in.readFully(response);

            validateHandshakeResponse(response, torrent.getInfo().getHash());

            PeerMessage bitfieldMsg = readPeerMessage(in);

            if (bitfieldMsg.messageId != BITFIELD) {
                throw new RuntimeException("Expected bitfield message, got: " +
                        bitfieldMsg.messageId);
            }

            sendMessage(out, INTERESTED, new byte[0]);

            PeerMessage unchokeMsg = readPeerMessage(in);

            if (unchokeMsg.messageId != UNCHOKE) {
                throw new RuntimeException("Expected unchoke message, got: " +
                        unchokeMsg.messageId);
            }

            int pieceLength = torrent.getInfo().getPieceLengthOfPiece(pieceIndex);

            int numBlocks = (int) Math.ceil((double) pieceLength / BLOCK_SIZE);

            byte[] pieceData = new byte[(int) pieceLength];

            int offset = 0;

            for (int blockIndex = 0; blockIndex < numBlocks; blockIndex++) {
                int blockLength = Math.min(BLOCK_SIZE, pieceLength - offset);

                byte[] requestPayload = createRequestPayload(pieceIndex, offset, blockLength);

                sendMessage(out, REQUEST, requestPayload);

                PeerMessage pieceMsg = readPeerMessage(in);

                if (pieceMsg.messageId != PIECE) {
                    throw new RuntimeException("Expected piece message, got: " +
                            pieceMsg.messageId);
                }

                System.arraycopy(pieceMsg.payload, 8, pieceData, offset,
                        blockLength);

                offset += blockLength;
            }
            return pieceData;
        }
    }

    private static byte[] createHandshakeMessage(byte[] infoHash) {
        byte[] handshake = new byte[68];
        int offset = 0;
        handshake[offset++] = 19;
        byte[] protocol =
                "BitTorrent protocol".getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(protocol, 0, handshake, offset, protocol.length);
        offset += protocol.length;
        offset += 8;
        System.arraycopy(infoHash, 0, handshake, offset, 20);
        offset += 20;
        byte[] peerId = ("-PD0001-" + randomString(12))
                .getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(peerId, 0, handshake, offset, 20);
        return handshake;
    }

    private static void validateHandshakeResponse(byte[] response, byte[] expectedInfoHash) {
        if (response[0] != 19) {
            throw new RuntimeException("Invalid protocol length: " + response[0]);
        }
        byte[] protocolBytes = Arrays.copyOfRange(response, 1, 20);
        String protocol =
                new String(protocolBytes, StandardCharsets.ISO_8859_1);
        if (!"BitTorrent protocol".equals(protocol)) {
            throw new RuntimeException("Invalid protocol: " + protocol);
        }
        byte[] receivedInfoHash = Arrays.copyOfRange(response, 28, 48);
        if (!Arrays.equals(expectedInfoHash, receivedInfoHash)) {
            throw new RuntimeException("Info hash mismatch");
        }
    }

    private static String randomString(int length) {
        String chars =
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static byte[] createRequestPayload(int index, int begin,
                                               int length) {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(index);
        buffer.putInt(begin);
        buffer.putInt(length);
        return buffer.array();
    }

    private static void sendMessage(DataOutputStream out, byte messageId,
                                    byte[] payload) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(
                MESSAGE_LENGTH_SIZE + MESSAGE_ID_SIZE + payload.length);
        buffer.putInt(MESSAGE_ID_SIZE + payload.length);
        buffer.put(messageId);
        buffer.put(payload);
        out.write(buffer.array());
        out.flush();
    }

    private static PeerMessage readPeerMessage(DataInputStream in)
            throws IOException {
        int length = in.readInt();
        if (length == 0) {
            return new PeerMessage(length, (byte) 0, new byte[0]);
        }
        byte messageId = in.readByte();
        byte[] payload = new byte[length - 1];
        in.readFully(payload);
        return new PeerMessage(length, messageId, payload);
    }

    private record PeerMessage(int length, byte messageId, byte[] payload) {
    }
}
