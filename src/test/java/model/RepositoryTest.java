package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private Repository repository;

    @BeforeEach
    void setUp() {
        repository = new Repository("name123", "repository123");
    }

    @Test
    void getRepositoryName() {
        assertEquals("name123", repository.getRepositoryName());
    }

    @Test
    void getOwner() {
        assertEquals("repository123", repository.getOwner());
    }

    @Test
    void getCreationDate() {
        assertNotNull(repository.getCreationDate());
        assert (repository.getCreationDate().getTime() > 0);
    }
}