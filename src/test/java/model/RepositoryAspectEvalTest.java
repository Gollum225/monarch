package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryAspectEvalTest {

    @Test
    void testConstructorWithPoints() {
        RepositoryAspectEval eval = new RepositoryAspectEval(3);

        assertEquals(3, eval.getPoints());
        assertTrue(eval.isApplicable());
        assertNull(eval.getFailureMessage());
        assertEquals("3", eval.getResultString());
    }

    @Test
    void testConstructorWithNegativePoints() {
        RepositoryAspectEval eval = new RepositoryAspectEval(-5);

        assertEquals(0, eval.getPoints());
        assertTrue(eval.isApplicable());
        assertNull(eval.getFailureMessage());
        assertEquals("0", eval.getResultString());
    }

    @Test
    void testConstructorWithFailureMessage() {
        RepositoryAspectEval eval = new RepositoryAspectEval("network error");

        assertEquals(0, eval.getPoints());
        assertFalse(eval.isApplicable());
        assertEquals("network error", eval.getFailureMessage());
        assertEquals("Rule not applicable. Reason: network error", eval.getResultString());
    }
}
