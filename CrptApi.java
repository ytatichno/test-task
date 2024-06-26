import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.concurrent.TimedSemaphore;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ytati
 * on 26.06.2024.
 */
public class CrptApi {


    private final HttpClient client;
    private final String uri;

    private TimedSemaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new TimedSemaphore(1, timeUnit, requestLimit);
        this.client = HttpClient.newHttpClient();
        this.uri = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    }

    public String create(Document document, byte[] signature) {
        try {
            semaphore.acquire();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .POST(HttpRequest.BodyPublishers.ofString(document.toJson()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (InterruptedException interruptedException) {
            System.err.println("create operation interrupted: " + interruptedException.getMessage());
            return "FAILED";  // or something other, depends on external API response

        } catch (IOException ioException) {
            System.err.println("create operation parsing failed: " + ioException.getMessage());
            return "FAILED";
        }
    }

    @Data
    class Document {
        String docId;
        String docStatus;
        String docType = "LP_INTRODUCE_GOODS";
        boolean importRequest = true;
        Description description;

        String ownerInn;
        String participantInn;
        String producerInn;

        Date productionDate = Date.valueOf("2020-01-23");
        String productionType;

        Date regDate = Date.valueOf("2020-01-23");
        String regNumber;

        @Data
        class Description {
            String participantInn;
        }

        @Data
        class Product {
            String certificateDocument;
            Date certificateDocumentDate = Date.valueOf("2020-01-23");
            String certificateDocumentNumber;
            String ownerInn;
            String producerInn;
            Date productionDate = Date.valueOf("2020-01-23");
            String tnvedCode;
            String uitCode;
            String uituCode;
        public String toJson() throws JsonProcessingException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }

    }

}


