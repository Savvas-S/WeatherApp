package dataModel;
import lombok.Data;
import java.util.List;


@Data
public class ForecastResponse {
    private List<ForecastEntry> list;
    private City city;

    @Data
    public static class City {
        private String name;
        private String country;
    }
}
