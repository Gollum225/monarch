package controller.rules;

import model.MockRepository;
import model.RepositoryAspectEval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocFolderTest {

    @Test
    void testFindsDocumentationFolder() {
        MockRepository repo = new MockRepository("testRepo", "owner");

        int[] limits = {0, 1, 2};

        DocFolder rule = new DocFolder(repo, limits);
        RepositoryAspectEval result = rule.execute();

        assertEquals(1, result.getPoints());
    }
}
