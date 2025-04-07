package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepoListTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        java.lang.reflect.Field instanceField = RepoList.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void testAddSingleRepo() {
        RepoList list = RepoList.getInstance();
        Repository repo = new MockRepository("repo1", "owner");

        assertTrue(list.addSingleRepo(repo));
        assertEquals(1, list.size());
    }

    @Test
    void testAddDuplicateRepo() {
        RepoList list = RepoList.getInstance();
        Repository repo = new MockRepository("repo1", "owner");

        assertTrue(list.addSingleRepo(repo));
        assertFalse(list.addSingleRepo(repo)); // doppelt hinzufügen → false
        assertEquals(1, list.size());
    }

    @Test
    void testAddRepoAlreadyStarted() {
        RepoList list = RepoList.getInstance();
        Repository repo = new MockRepository("repo1", "owner");

        list.addSingleRepo(repo);
        list.getNext();

        assertFalse(list.addSingleRepo(repo));
    }

    @Test
    void testGetNext() {
        RepoList list = RepoList.getInstance();
        Repository repo1 = new MockRepository("repo1", "owner");
        Repository repo2 = new MockRepository("repo2", "owner");

        list.addSingleRepo(repo1);
        list.addSingleRepo(repo2);

        Repository next = list.getNext();
        assertEquals("repository: repo1 of owner: owner", next.getIdentifier());
        assertEquals(1, list.size());
    }

    @Test
    void testAddMultipleRepos() {
        RepoList list = RepoList.getInstance();
        Repository r1 = new MockRepository("a", "o");
        Repository r2 = new MockRepository("b", "o");

        boolean added = list.addMultipleRepos(new Repository[]{r1, r2});
        assertTrue(added);
        assertEquals(2, list.size());
    }

    @Test
    void testGetNextEmpty() {
        RepoList list = RepoList.getInstance();
        assertNull(list.getNext());
    }
}
