package dataModel;
import lombok.Data;
import java.util.List;

// ForecastEntry.java
@Data
public class ForecastEntry {
    private Main main;
    private List<Weather> weather;
    private String dt_txt;
    private Wind wind;

    @Data
    public static class Wind {
        private double speed;
    }
}