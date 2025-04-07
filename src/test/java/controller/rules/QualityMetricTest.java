package controller.rules;

import model.MockRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class QualityMetricTest {

    @Test
    void testQualityMetric() {
        // Create a mock repository
        MockRepository repo = new MockRepository("testRepo", "owner");

        QualityMetric qm = new QualityMetric(repo, new int[]{0, 1, 2, 3});
        assertEquals(0, qm.execute().getPoints());
    }
}
