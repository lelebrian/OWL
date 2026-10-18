package it.emanuelebriano.owl;

/** Decides only whether the OWL snooze gate suppresses this notification attempt. */
final class SnoozePolicy {
    private SnoozePolicy() { }

    static boolean shouldSuppress(boolean snoozeActive, boolean bypassEnabled,
                                  Integer localReadingValue) {
        return snoozeActive && (!bypassEnabled || localReadingValue == null
                || localReadingValue >= 99);
    }
}
