package ua.com.smiddle;

/**
 * Added by A.Osadchuk on 16.09.2016 at 13:12.
 * Project: SmiddleRecording
 */
public class CRMException extends RecordingException {
    public CRMException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "CRMException:" + super.getMessage();
    }
}
