package hu.akarnokd.reactive;

import static org.junit.Assert.*;

import org.junit.Test;

public class FreelistItemManagerTest {

    @Test
    public void offer() {
        FreelistItemManager<Integer> mgr = new FreelistItemManager<>(128);

        for (int i = 0; i < 128; i++) {
            mgr.offer(i);
        }

        for (int i = 0; i < 128; i++) {
            assertEquals(i + 1, mgr.get(i).index, i + 1);
            assertEquals(i, mgr.get(i).item.intValue());
        }

        for (int i = 0; i < 128; i++) {
            mgr.remove(mgr.get(i));
        }

        for (int i = 0; i < 128; i++) {
            assertNull(mgr.get(i));
        }

        for (int i = 0; i < 128; i++) {
            mgr.offer(i);
        }

        for (int i = 0; i < 128; i++) {
            assertEquals(i + 1, mgr.get(i).index, i + 1);
            assertEquals(i, mgr.get(i).item.intValue());
        }
    }
}
