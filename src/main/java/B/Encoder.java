package B;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class Encoder {
    private final ArrayList<Byte> encoded = new ArrayList<>();

    public Encoder(Object obj, OutputStream ou) {
        encode(obj);
        try {
            ou.write(getEncoded());
            ou.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Encoder(Object obj) {
        encode(obj);
    }

    public byte[] getEncoded() {
        byte[] bytes = new byte[encoded.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = encoded.get(i);
        }
        return bytes;
    }

    private void encode(Object obj){
        try {
            switch (obj) {
                case String s -> writeString(s);
                case byte[] bytes -> writeStringBytes(bytes);
                case Integer i -> writeInteger((long) obj);
                case Long l -> writeInteger((long) obj);
                case List list -> writeList(list);
                case Map map -> writeDictionary((Map<String, ?>) obj);
                case null, default -> throw new RuntimeException("Unsupported Type.");
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private void writeString(String str) throws IOException {
        writeStringBytes(str.getBytes());
    }

    private void writeStringBytes(byte[] bytes) throws IOException {
        int len = bytes.length;
        String s = String.valueOf(len);
        for (int i = 0; i < s.length(); i++) {
            encoded.add((byte) s.charAt(i));
        }
        encoded.add((byte) ':');
        for (byte b : bytes) {
            encoded.add(b);
        }
    }

    private void writeInteger(long n) throws IOException {
        encoded.add((byte) 'i');
        String s = String.valueOf(n);
        for (int i = 0; i < s.length(); i++) {
            encoded.add((byte) s.charAt(i));
        }
        encoded.add((byte) 'e');
    }

    private void writeList(List<?> obj) throws IOException {
        encoded.add((byte) 'l');
        for (Object o : obj) {
            encode(o);
        }
        encoded.add((byte) 'e');
    }

    private void writeDictionary(Map<String, ?> obj) throws IOException {
        Map<String, ?> orderedMap = new TreeMap<>(obj);
        encoded.add((byte) 'd');
        for (Map.Entry<String, ?> entry : orderedMap.entrySet()) {
            String k = camelCaseToSpaces(entry.getKey());
            Object v = entry.getValue();
            encode(k);
            encode(v);
        }
        encoded.add((byte) 'e');
    }

    private String camelCaseToSpaces(String s){
        String spacedString = s.replaceAll("(\\p{Lu})", " $1");
        if (!Character.isUpperCase(spacedString.charAt(0))) {
            spacedString = spacedString.charAt(0) + spacedString.substring(1);
        }
        return spacedString.toLowerCase();
    }
}