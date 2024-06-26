import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by ytati
 * on 26.06.2024.
 */
class CrptApiTest {

    public static CrptApi.Document document;


    @BeforeAll
    static void init() {
        CrptApi.Document.Product.ProductBuilder productBuilderStub = CrptApi.Document.Product.builder()
                .certificateDocument("somedoc")
                .certificateDocumentNumber("someNumber")
                .ownerInn("ownerInn")
                .producerInn("producerInn")
                .tnvedCode("code")
                .uitCode("code")
                .uituCode("code");
        List<CrptApi.Document.Product> products = List.of(
                productBuilderStub.build(),
                productBuilderStub.uitCode("anotherCode").build(),
                productBuilderStub.build()
        );

        CrptApiTest.document = CrptApi.Document.builder()
                .docId("someid")
                .docStatus("somestatus")
                .description(new CrptApi.Document.Description("some participantInn"))
                .ownerInn("ownerInn")
                .participantInn("participantInn")
                .producerInn("producerInn")
                .productionType("someType")
                .regNumber("123")
                .products(products)
                .build();
    }

    @Test
    void testCreate() throws InterruptedException {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
        Runnable createDocumentTask = () -> crptApi.create(document, "some signature".getBytes());
        Instant start = Instant.now();
        List<CompletableFuture<?>> cfList = List.of(
                CompletableFuture.runAsync(createDocumentTask),
                CompletableFuture.runAsync(createDocumentTask),
                CompletableFuture.runAsync(createDocumentTask),
                CompletableFuture.runAsync(createDocumentTask)
        );
        Thread.sleep(100);
        CompletableFuture.runAsync(createDocumentTask).thenRun(() -> {
            for (CompletableFuture<?> cf : cfList) {
                assertTrue(cf.isDone());
            }
            assertTrue(Duration.between(Instant.now(), start).getSeconds() < 30);
        });


    }

    @Test
    void testRateLimiter() throws InterruptedException {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 2);
        AtomicLongArray measures = new AtomicLongArray(5);
        Runnable createDocumentTask = () -> crptApi.create(document, "some signature".getBytes());
        Consumer<Integer> saveMeasure = (Integer startOrder) -> measures.set(startOrder, System.nanoTime());
        long start = System.nanoTime();
        List<CompletableFuture<?>> cfList = List.of(
                CompletableFuture.runAsync(createDocumentTask).thenRun(() -> saveMeasure.accept(0)),
                CompletableFuture.runAsync(createDocumentTask).thenRun(() -> saveMeasure.accept(1)),
                CompletableFuture.runAsync(createDocumentTask).thenRun(() -> saveMeasure.accept(2)),
                CompletableFuture.runAsync(createDocumentTask).thenRun(() -> saveMeasure.accept(3)),
                CompletableFuture.runAsync(createDocumentTask).thenRun(() -> saveMeasure.accept(4))
        );
        assertEquals(measures.length(), cfList.size());
        Thread.sleep(2200);
        ArrayList<Long> measuresResult = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            measuresResult.add(
                    TimeUnit.MILLISECONDS.convert(
                            measures.get(i) - start,
                            TimeUnit.NANOSECONDS
                    )
            );
        }
        measuresResult.sort(Long::compare);
        System.out.println(measuresResult);
        assertTrue(measuresResult.get(0) < 800);
        assertTrue(measuresResult.get(1) < 800);
        assertTrue(measuresResult.get(2) > 1000 && measuresResult.get(3) < 1500);
        assertTrue(measuresResult.get(3) > 1000 && measuresResult.get(3) < 1500);
        assertTrue(measuresResult.get(4) > 1100 && measuresResult.get(4) < 2500);
    }

    @Test
    void testDocumentToJson() throws JsonProcessingException {


        assertEquals("""
                        {
                          "doc_id" : "someid",
                          "doc_status" : "somestatus",
                          "doc_type" : "LP_INTRODUCE_GOODS",
                          "import_request" : true,
                          "description" : {
                            "participantInn" : "some participantInn"
                          },
                          "owner_inn" : "ownerInn",
                          "participant_inn" : "participantInn",
                          "producer_inn" : "producerInn",
                          "production_date" : 1579726800000,
                          "production_type" : "someType",
                          "products" : [ {
                            "certificate_document" : "somedoc",
                            "certificate_document_date" : 1579726800000,
                            "certificate_document_number" : "someNumber",
                            "owner_inn" : "ownerInn",
                            "producer_inn" : "producerInn",
                            "production_date" : 1579726800000,
                            "tnved_code" : "code",
                            "uit_code" : "code",
                            "uitu_code" : "code"
                          }, {
                            "certificate_document" : "somedoc",
                            "certificate_document_date" : 1579726800000,
                            "certificate_document_number" : "someNumber",
                            "owner_inn" : "ownerInn",
                            "producer_inn" : "producerInn",
                            "production_date" : 1579726800000,
                            "tnved_code" : "code",
                            "uit_code" : "anotherCode",
                            "uitu_code" : "code"
                          }, {
                            "certificate_document" : "somedoc",
                            "certificate_document_date" : 1579726800000,
                            "certificate_document_number" : "someNumber",
                            "owner_inn" : "ownerInn",
                            "producer_inn" : "producerInn",
                            "production_date" : 1579726800000,
                            "tnved_code" : "code",
                            "uit_code" : "anotherCode",
                            "uitu_code" : "code"
                          } ],
                          "reg_date" : 1579726800000,
                          "reg_number" : "123"
                        }""",
                document.toJson().replaceAll("\\r\\n", "\n"));
        // to except CLRF LF difference
    }
}