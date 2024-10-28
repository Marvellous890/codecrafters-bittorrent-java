package Tracker;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
public class Tracker {
    private TrackerRequest request;
    private TrackerResponse response;
    public Tracker(TrackerRequest request) {
        this.request = request;
        response = this.sendRequest();
    }
    public TrackerResponse getResponse() {
        return response;
    }
    private TrackerResponse sendRequest(){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(request.getUri());
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            return new TrackerResponse(httpResponse.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                assert httpResponse != null;
                httpResponse.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}