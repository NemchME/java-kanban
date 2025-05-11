package tests;

import org.junit.jupiter.api.Test;
import taskManager.Managers;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void testManagerFactories() {
        assertNotNull(Managers.getDefault());
        assertNotNull(Managers.getDefaultHistory());
    }
}