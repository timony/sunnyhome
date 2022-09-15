package cz.snyll.Sunny.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;

@ConfigurationProperties(prefix = "shelly")
public class ShellyConfiguration {
    private HashMap<String, String> shellyHtMap;

    public ShellyConfiguration(HashMap<String, String> shellyHtMap) {
        this.shellyHtMap = shellyHtMap;
    }

    public HashMap<String, String> getShellyHtMap() {
        return this.shellyHtMap;
    }

    public void setShellyHtMap(HashMap<String, String> shellyHtMap) {
        this.shellyHtMap = shellyHtMap;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ShellyConfiguration)) return false;
        final ShellyConfiguration other = (ShellyConfiguration) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$shellyHtMap = this.getShellyHtMap();
        final Object other$shellyHtMap = other.getShellyHtMap();
        if (this$shellyHtMap == null ? other$shellyHtMap != null : !this$shellyHtMap.equals(other$shellyHtMap))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ShellyConfiguration;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $shellyHtMap = this.getShellyHtMap();
        result = result * PRIME + ($shellyHtMap == null ? 43 : $shellyHtMap.hashCode());
        return result;
    }

    public String toString() {
        return "ShellyConfiguration(shellyHtMap=" + this.getShellyHtMap() + ")";
    }
}