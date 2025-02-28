package controller.rules;

import model.MockRepository;
import model.Repository;
import model.RuleReturn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyWordTest {

    private Repository repository;
    private KeyWord keyWordRule;

    @BeforeEach
    void setUp() {
        repository = new MockRepository("name123", "repository123");
        keyWordRule = new KeyWord(repository);
    }

    @Test
    void executeRule() {
        RuleReturn result = keyWordRule.execute();
        assertEquals(1, result.getPoints());
    }
}