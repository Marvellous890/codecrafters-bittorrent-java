package B;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Decoder {
    private final Object decoded;
    private final PushbackInputStream in;
    private final boolean useBytes;
    public Decoder(InputStream in, boolean useBytes) {
        this.in = new PushbackInputStream(in);
        this.useBytes = useBytes;
        decoded = decodeNext();
    }

    public Decoder(InputStream in){
        this(in, false);
    }

    public Decoder(byte[] bytes, boolean useBytes){
        this(new ByteArrayInputStream(bytes), useBytes);
    }

    public Decoder(byte[] bytes){
        this(new ByteArrayInputStream(bytes));
    }

    public Object getDecoded() {
        return decoded;
    }

    private Object decodeNext()  {
        try {
            int next = peek();
            if (Character.isDigit(next)){
                return useBytes ? readStringBytes() : readString();
            }
            else if(next == 'i') return readInteger();
            else if(next == 'l') return readList();
            else if(next == 'd') return readDictionary();
            else throw new RuntimeException("Invalid Bencode.");
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    private String readString() throws IOException {
        return new String(readStringBytes());
    }

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

    private int peek() throws IOException {
        int next = in.read();
        if (next != -1) in.unread(next);
        return next;
    }

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