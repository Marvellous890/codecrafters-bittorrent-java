package Torrent;

public class Info {
    private String name;
    private long length;
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
    public Piece[] getPieces() {
        return pieces;
    }
    public void setPieces(Piece[] pieces) {
        this.pieces = pieces;
    }
}
