package Torrent;

import java.net.URL;

public class Torrent {
    private URL announce;
    private Info info;

    public URL getAnnounce() {
        return announce;
    }

    public void setAnnounce(URL announce) {
        this.announce = announce;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public static class Info{
        private long length;
        private String name;
        private long pieceLength;
        private Piece[] pieces;
        private String hash;

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getPieceLength() {
            return pieceLength;
        }

        public void setPieceLength(long pieceLength) {
            this.pieceLength = pieceLength;
        }

        public Piece[] getPieces() {
            return pieces;
        }

        public void setPieces(Piece[] pieces) {
            this.pieces = pieces;
        }
    }

    public static class Piece{}
}