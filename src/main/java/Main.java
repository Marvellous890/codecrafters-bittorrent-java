import B.Decoder;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        String command = args[0];

        if("decode".equals(command)) {
            String bencodedValue = args[1];
            Object decoded;
            try {
                Decoder b = new Decoder(bencodedValue.getBytes());
                decoded = b.getDecoded();
            } catch(RuntimeException e) {
                System.out.println(e.getMessage());
                return;
            }
            System.out.println(gson.toJson(decoded));

        } else if (command.equals("info")) {
            File file = new File(args[1]);
            Object decoded = null;
            try (FileInputStream fis = new FileInputStream(file)) {
                Decoder b = new Decoder(fis, true);
                decoded = b.getDecoded();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            System.out.println(gson.toJson(decoded));
        } else {
            System.out.println("Unknown command: " + command);
        }

    }

}