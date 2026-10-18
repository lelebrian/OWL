package it.emanuelebriano.owl;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SnoozePolicyTest {
    @Test
    public void activeSnoozeBypassesOnlyCurrentLocalValuesUnder99WhenEnabled() {
        assertFalse(SnoozePolicy.shouldSuppress(true, true, 85));
        assertFalse(SnoozePolicy.shouldSuppress(true, true, 98));
        assertTrue(SnoozePolicy.shouldSuppress(true, true, 99));
        assertTrue(SnoozePolicy.shouldSuppress(true, true, 100));
    }

    @Test
    public void disabledBypassAndNonReadingNoticesStaySnoozed() {
        assertTrue(SnoozePolicy.shouldSuppress(true, false, 85));
        assertTrue(SnoozePolicy.shouldSuppress(true, true, null));
        assertTrue(SnoozePolicy.shouldSuppress(true, false, null));
    }

    @Test
    public void switchDoesNotAffectNotificationsWithoutActiveSnooze() {
        assertFalse(SnoozePolicy.shouldSuppress(false, false, 85));
        assertFalse(SnoozePolicy.shouldSuppress(false, true, 99));
        assertFalse(SnoozePolicy.shouldSuppress(false, true, null));
    }
}
