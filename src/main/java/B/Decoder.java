package B;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Decoder class is responsible for decoding Bencode encoded data.
 */
public class Decoder {
    private final Object decoded;
    private final PushbackInputStream in;
    private final boolean useBytes;

    /**
     * Constructs a Decoder with the given InputStream and a flag indicating whether to use bytes.
     *
     * @param in the InputStream to read from
     * @param useBytes whether to use bytes for string decoding
     */
    public Decoder(InputStream in, boolean useBytes) {
        this.in = new PushbackInputStream(in);
        this.useBytes = useBytes;
        decoded = decodeNext();
    }

    /**
     * Constructs a Decoder with the given InputStream.
     *
     * @param in the InputStream to read from
     */
    public Decoder(InputStream in){
        this(in, false);
    }

    /**
     * Constructs a Decoder with the given byte array and a flag indicating whether to use bytes.
     *
     * @param bytes the byte array to read from
     * @param useBytes whether to use bytes for string decoding
     */
    public Decoder(byte[] bytes, boolean useBytes){
        this(new ByteArrayInputStream(bytes), useBytes);
    }

    /**
     * Constructs a Decoder with the given byte array.
     *
     * @param bytes the byte array to read from
     */
    public Decoder(byte[] bytes){
        this(new ByteArrayInputStream(bytes));
    }

    /**
     * Returns the decoded object.
     *
     * @return the decoded object
     */
    public Object getDecoded() {
        return decoded;
    }

    /**
     * Decodes the next Bencode element from the input stream.
     *
     * @return the decoded object
     */
    private Object decodeNext()  {
        try {
            int next = peek();
            if (Character.isDigit(next)) return useBytes ? readStringBytes() : readString();
            else if(next == 'i') return readInteger();
            else if(next == 'l') return readList();
            else if(next == 'd') return readDictionary();
            else throw new RuntimeException("Invalid Bencode.");
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Reads a Bencode encoded string from the input stream.
     *
     * @return the decoded string
     * @throws IOException if an I/O error occurs
     */
    private String readString() throws IOException {
        return new String(readStringBytes());
    }

    /**
     * Reads a Bencode encoded byte array from the input stream.
     *
     * @return the decoded byte array
     * @throws IOException if an I/O error occurs
     */
    private byte[] readStringBytes() throws IOException {
        int next = in.read();
        StringBuilder sb = new StringBuilder();
        while (next != ':'){
            sb.append((char) next);
            next = in.read();
        }
        int length = Integer.parseInt(sb.toString());
        byte[] b = new byte[length];
        in.read(b);
        return b;
    }

    /**
     * Reads a Bencode encoded integer from the input stream.
     *
     * @return the decoded integer
     * @throws IOException if an I/O error occurs
     */
    private Long readInteger() throws IOException {
        int next = in.read();
        if (next != 'i') throw new RuntimeException("");
        StringBuilder sb = new StringBuilder();
        next = in.read();
        while (next != 'e'){
            sb.append((char) next);
            next = in.read();
        }
        return Long.parseLong(sb.toString());
    }

    /**
     * Reads a Bencode encoded list from the input stream.
     *
     * @return the decoded list
     * @throws IOException if an I/O error occurs
     */
    private List<?> readList() throws IOException {
        List<Object> list = new ArrayList<>();
        int next = in.read();
        if (next != 'l') throw new RuntimeException("");
        while (true){
            next = peek();
            if (next == 'e') break;
            list.add(decodeNext());
        }
        next = in.read();
        return list;
    }

    /**
     * Reads a Bencode encoded dictionary from the input stream.
     *
     * @return the decoded dictionary
     * @throws IOException if an I/O error occurs
     */
    private Map<String, ?> readDictionary() throws IOException {
        Map<String, Object> map = new HashMap<>();
        int next = in.read();
        if (next != 'd') throw new RuntimeException("");
        while (true){
            next = peek();
            if (next == 'e') break;
            String k = toCamelCase(readString());
            Object v = decodeNext();
            map.put(k, v);
        }
        next = in.read();
        return map;
    }

    /**
     * Peeks the next byte in the input stream without consuming it.
     *
     * @return the next byte
     * @throws IOException if an I/O error occurs
     */
    private int peek() throws IOException {
        int next = in.read();
        if (next != -1) in.unread(next);
        return next;
    }

    /**
     * Converts a string to camel case.
     *
     * @param str the string to convert
     * @return the camel case string
     */
    private String toCamelCase(String str){
        StringBuilder sb = new StringBuilder();
        boolean nextUp = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ' ') {
                nextUp = true;
            }
            else{
                if (nextUp) sb.append(Character.toUpperCase(c));
                else sb.append(c);
                nextUp = false;
            }
        }
        return sb.toString();
    }
}