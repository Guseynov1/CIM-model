package cimmaintainer.dto;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Device extends Identifier {
    private String deviceType;
    private Name name;

}
