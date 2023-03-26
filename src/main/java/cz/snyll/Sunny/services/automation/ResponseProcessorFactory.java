package cz.snyll.Sunny.services.automation;

import org.springframework.stereotype.Service;

@Service
public class ResponseProcessorFactory {
    public static APIResponseProcessor createResponseProcessor (String controlDeviceName) {
        if (controlDeviceName == null)
            return null;
        if (controlDeviceName.equals("ShellyPlugS"))
            return new ShellyPlugSResponseProcessor();
        if (controlDeviceName.equals("Shelly1"))
            return new Shelly1ResponseProcessor();
        if (controlDeviceName.equals("Shelly1PM"))
            return new Shelly1ResponseProcessor();
        if (controlDeviceName.equals("Shelly1PMPlus"))
            return new Shelly1PMPlusResponseProcessor();
        return null;
    }
}