package ua.com.smiddle;

/**
 * Added by A.Osadchuk on 29.06.2016 at 14:41.
 * Project: SmiddleRecording
 */
public class RecordingException extends Exception {
    public RecordingException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "RecordingException:" + super.getMessage();
    }
}
