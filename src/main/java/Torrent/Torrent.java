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

}