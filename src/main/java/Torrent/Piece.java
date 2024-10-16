package Torrent;

public class Piece{
    private int length;
    private String hash;
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
}
