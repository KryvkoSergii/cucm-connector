package ua.com.smiddle;

/**
 * Added by A.Osadchuk on 28.09.2016 at 16:36.
 * Project: SmiddleRecording
 */
public class CRMCUCMException extends CRMException {
    public CRMCUCMException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "CRMCUCMException:" + super.getMessage();
    }
}
