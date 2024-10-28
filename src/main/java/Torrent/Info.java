package Torrent;

public class Info {
    private String name;

    private long length;

    private int pieceLength;

    private byte[][] pieces;

    private byte[] hash;

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
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

    public byte[][] getPieces() {
        return pieces;
    }

    public void setPieces(byte[][] pieces) {
        this.pieces = pieces;
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public int getPieceLengthOfPiece(int pieceIndex) {
        if (pieceIndex == pieces.length - 1) {
            return (int) (length % pieceLength);
        }
        return pieceLength;
    }

    public void setPieceLength(int pieceLength) {
        this.pieceLength = pieceLength;
    }
}
