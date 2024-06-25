import lombok.Data;

import java.util.concurrent.TimeUnit;
import java.sql.Date;

/**
 * Created by ytati
 * on 26.06.2024.
 */
public class CrptApi {
    private final int requestLimit;
    private final TimeUnit timeUnit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeUnit = timeUnit;
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
        }

    }

}


