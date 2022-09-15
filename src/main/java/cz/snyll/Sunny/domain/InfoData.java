package cz.snyll.Sunny.domain;

import cz.snyll.Sunny.config.InfoDataConfiguration;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.util.Date;

@Entity
@Getter
@ToString
@EqualsAndHashCode
@Table(name = "info_data")
@NoArgsConstructor
public class InfoData {
    @Setter
    @Id
    @GeneratedValue
    private Long id;
    @Setter
    @Column(unique=true)
    private String dataKey;
    @Setter
    private String dataValue;
    @Setter
    private String units;
    @Setter
    private Date dataFreshness;

    @Transient
    @Autowired
    private InfoDataConfiguration infoDataConfig;

    public String prettyValue() {
        if (dataFreshness.before(new Date(System.currentTimeMillis() - 10000 * 1000))) {
            return "-";
        }
        if (units.equals("W"))
            return Integer.toString(Math.round(Float.parseFloat(dataValue))) + " W";
        if (units.equals("kWh")) {
            DecimalFormat df = new DecimalFormat("#.##");
            String formatted = df.format(Float.parseFloat(dataValue));
            return formatted + units;
        }
        return dataValue + " " + units;
    }
    public Float floatValue() {
        return Float.parseFloat(dataValue);
    }

    public String prettyKey() {
        String pretty = infoDataConfig.getHumanMap().get(dataKey);
        if (pretty.equals("")) {
            return dataKey;
        } else {
            return pretty;
        }
    }
}
