import B.Decoder;

import Torrent.TorrentParser;
import Torrent.Torrent;
import Torrent.Piece;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
                System.out.println("Info Hash: " + torrent.getInfo().getHash());

                Piece[] pieces = torrent.getInfo().getPieces();

                System.out.println("Piece Length: " + pieces[0].getLength());
                System.out.println("Piece Hashes:");

                for (Piece piece: pieces){
                    System.out.println(piece.getHash());
                }
            }
        } else {
            System.out.println("Unknown command: " + command);
        }

    }

}