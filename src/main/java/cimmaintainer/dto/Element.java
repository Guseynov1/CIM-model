package cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class Element extends Identifier {

    private String directoryEntryId, voltageLevel, operationName, projectName, type;
    private List<Port> ports;
    private List<Fields> fields;


}
