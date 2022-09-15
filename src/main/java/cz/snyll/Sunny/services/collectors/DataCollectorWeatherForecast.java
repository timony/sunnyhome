package cz.snyll.Sunny.services.collectors;
import cz.snyll.Sunny.config.WeatherConfiguration;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import cz.snyll.Sunny.services.EventEntryManagerService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/***
 * This service will collect information from weather forecast (service in-pocasi.cz) for Drasov location and save info about sun score for today and tomorrow.
 */
@Slf4j
@Setter
@Getter
@Service
public class DataCollectorWeatherForecast extends DataCollectorAbstractService {
    @Autowired
    private EventEntryManagerService eventEntryManagerService;

    @Autowired
    private InfoDataRepository infoDataRepository;
    @Autowired
    private WeatherConfiguration weatherConfiguration;

    private final String weatherXMLurl = "https://www.in-pocasi.cz/pocasi-na-web/xml.php?id=71d0b5103b";

    public DataCollectorWeatherForecast(InfoDataRepository infoDataRepository) {
        super(infoDataRepository);
    }

    @Scheduled(fixedDelay = 3600000)
    @Override
    public void CollectData() {
        HashMap<String, String> weatherStatusMap;
        try {
            weatherStatusMap = weatherConfiguration.getStatusMap();
            if (weatherStatusMap == null)
                return;
            HashMap<String, Map.Entry<String, String>> dataMap = new HashMap<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(weatherXMLurl);

            // normalize XML response
            doc.getDocumentElement().normalize();

            Node day1 = doc.getElementsByTagName("den").item(0);
            Node day2 = doc.getElementsByTagName("den").item(1);
            NodeList day1NodeList = day1.getChildNodes();
            NodeList day2NodeList = day2.getChildNodes();
            int day1sunPoints = 0;
            int day2sunPoints = 0;
            for (int i = 0; i < (day1NodeList.getLength()); i++) {
                if(day1NodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element dayChild = (Element) day1NodeList.item(i);
                    if (dayChild.getNodeName().equals("stav")) {
                        if (dayChild.getAttribute("id").equals("rano")) {
                            day1sunPoints += Integer.parseInt(weatherStatusMap.get(dayChild.getTextContent()));
                        }
                        if (dayChild.getAttribute("id").equals("dopoledne")) {
                            day1sunPoints += Integer.parseInt(weatherStatusMap.get(dayChild.getTextContent()));
                        }
                        if (dayChild.getAttribute("id").equals("odpoledne")) {
                            day1sunPoints += Integer.parseInt(weatherStatusMap.get(dayChild.getTextContent()));
                        }
                    }
                }
            }
            for (int i = 0; i < (day2NodeList.getLength()); i++) {
                if(day2NodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element dayChild = (Element) day2NodeList.item(i);
                    if (dayChild.getNodeName().equals("stav")) {
                        if (dayChild.getAttribute("id").equals("rano")) {
                            day2sunPoints += Integer.parseInt(weatherStatusMap.get(dayChild.getTextContent()));
                        }
                        if (dayChild.getAttribute("id").equals("dopoledne")) {
                            day2sunPoints += Integer.parseInt(weatherStatusMap.get(dayChild.getTextContent()));
                        }
                        if (dayChild.getAttribute("id").equals("odpoledne")) {
                            day2sunPoints += Integer.parseInt(weatherStatusMap.get(dayChild.getTextContent()));
                        }
                    }
                }
            }
            System.out.println("Saving sun score points, day1: " + day1sunPoints + "; day2: " + day2sunPoints);
            dataMap.put("weather_today_sunscore", new AbstractMap.SimpleEntry<String, String>(Integer.toString(day1sunPoints), "points"));
            dataMap.put("weather_tomorrow_sunscore", new AbstractMap.SimpleEntry<String, String>(Integer.toString(day2sunPoints), "points"));
            this.SaveInfoData(dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("WEATHER: Issue with loading in-pocasi xml file.");
            eventEntryManagerService.raiseEvent("WEATHER: Issue occured while trying to load in-pocasi xml file: " + e.getMessage(), EventEntry.EventType.ERROR);
        }
    }
}
