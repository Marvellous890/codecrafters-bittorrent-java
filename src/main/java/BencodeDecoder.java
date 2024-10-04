import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BencodeDecoder {
    private final String encodedValue;
    private int current = 0;

    public BencodeDecoder(String encodedValue) {
        this.encodedValue = encodedValue;
    }

    public Object decode(){
        if (Character.isDigit(encodedValue.charAt(current))) return decodeString();
        if (encodedValue.charAt(current) == 'i') return decodeInteger();
        if (encodedValue.charAt(current) == 'l') return decodeList();
        if (encodedValue.charAt(current) == 'd') return decodeDict();
        return null;
    }

    private String decodeString(){
        int delimeterIndex = 0;
        for (int i = current; i < encodedValue.length(); i++) {
            if (encodedValue.charAt(i) == ':') {
                delimeterIndex = i;
                break;
            }
        }
        int length = Integer.parseInt(encodedValue.substring(current, delimeterIndex));
        int start = delimeterIndex+1, end = start + length;
        current = end;
        return encodedValue.substring(start, end);
    }

    private Long decodeInteger(){
        int start = current+1, end = 0;
        for (int i = start; i < encodedValue.length(); i++) {
            if (encodedValue.charAt(i) == 'e') {
                end = i;
                break;
            }
        }
        current = end + 1;
        return Long.parseLong(encodedValue.substring(start, end));
    }

    private List<Object> decodeList(){
        List<Object> list = new ArrayList<>();
        current++;
        while (encodedValue.charAt(current) != 'e'){
            list.add(decode());
        }
        current++;
        return list;
    }

    private Map<String, Object> decodeDict() {
        Bencode bencode = new Bencode();
        return bencode.decode(encodedValue.getBytes(), Type.DICTIONARY);
    }
}