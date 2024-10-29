package Torrent;

import java.util.List;

public class Info {
    private String name;

    // usually 0 in some files. they instead put the length of each file in files list.
    private long length;

    private int pieceLength;

    private byte[][] pieces;

    private byte[] hash;

    private String tracker;

    private String source;

    private List<InfoFile> files;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTracker() {
        return tracker;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

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

    public List<InfoFile> getFiles() {
        return files;
    }

    public void setFiles(List<InfoFile> files) {
        this.files = files;
    }
}
