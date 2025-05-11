package test;

import org.junit.jupiter.api.Test;
import taskmanager.Managers;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void testManagerFactories() {
        assertNotNull(Managers.getDefault());
        assertNotNull(Managers.getDefaultHistory());
    }
}