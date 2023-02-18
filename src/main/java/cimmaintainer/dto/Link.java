package cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Link extends Identifier {
    private String sourceId, targetId, sourcePortId, targetPortId;
    private Port sourcePort;
    private Port targetPort;
    private Element source;
    private Element target;

}
