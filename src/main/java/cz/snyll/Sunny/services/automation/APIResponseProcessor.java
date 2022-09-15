package cz.snyll.Sunny.services.automation;

public interface APIResponseProcessor {
    boolean ProcessResponse(String response, boolean expectedResponse);
}
