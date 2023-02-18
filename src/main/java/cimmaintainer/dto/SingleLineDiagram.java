package cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SingleLineDiagram {

    private List<Link> links; // их тип список - из библиотеки jackson
    private List<Element> elements;
}
