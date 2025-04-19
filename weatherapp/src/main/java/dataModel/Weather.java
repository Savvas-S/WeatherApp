package dataModel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Weather {
    private int id;
    private String main;
    private String description;
    private String icon;
}
