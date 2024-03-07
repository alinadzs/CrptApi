import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final int requestLimit;
    private final long intervalInMillis;
    private AtomicInteger requestCount = new AtomicInteger(0);
    private Lock lock = new ReentrantLock();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.intervalInMillis = timeUnit.toMillis(1);
    }

    public void createDocument(String document, String signature) {
        try {
            lock.lock();
            if (requestCount.get() >= requestLimit) {
                System.out.println("Request limit exceeded. Blocking the call.");
                return;
            }
            requestCount.incrementAndGet();
        } finally {
            lock.unlock();
        }

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/1k/documents/create");

            StringEntity entity = new StringEntity(document);
            httpPost.setEntity(entity);

            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + signature);

            HttpResponse response = httpClient.execute(httpPost);

            // Processing the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("Document creation successful.");
            } else {
                System.out.println("Document creation failed. Status code: " + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            requestCount.decrementAndGet();
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);

        // Simulating making multiple requests to test the request limit
        for (int i = 0; i < 10; i++) {
            String document = "{\"key\": \"value\"}";
            String signature = "abc123";
            crptApi.createDocument(document, signature);
        }
    }
}