package cz.snyll.Sunny.services.automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.services.EventEntryManagerService;
import org.springframework.beans.factory.annotation.Autowired;

public class Shelly1ResponseProcessor implements APIResponseProcessor {
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    @Override
    public boolean ProcessResponse(String response, boolean expectedResponse)  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response);
            if (jsonResponse.get("ison").toString().equals("false") && expectedResponse == false) {
                return true;
            }
            if (jsonResponse.get("ison").toString().equals("true") && expectedResponse == true) {
                return true;
            }
            return false;
        } catch (Exception e) {
            eventEntryManagerService.raiseEvent("API Error: there was exception thrown during API command: " + e.getMessage(), EventEntry.EventType.ERROR);
            return false;
        }
    }
}
