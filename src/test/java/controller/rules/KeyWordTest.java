package controller.rules;

import model.MockRepository;
import model.Repository;
import model.RepositoryAspectEval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyWordTest {

    private Repository repository;
    private KeyWord keyWordRule;

    @BeforeEach
    void setUp() {
        repository = new MockRepository("name123", "repository123");
        keyWordRule = new KeyWord(repository, new int[]{0, 1, 5, 20, 50, 200});
    }

    @Test
    void executeRule() {
        RepositoryAspectEval result = keyWordRule.execute();
        assertEquals(1, result.getPoints());
    }
}