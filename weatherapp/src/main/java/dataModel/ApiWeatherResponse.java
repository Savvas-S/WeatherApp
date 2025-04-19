package dataModel;
import lombok.Data;
import java.util.List;

@Data
public class ApiWeatherResponse {
    private String name;
    private List<Weather> weather;
    private Main main;

}