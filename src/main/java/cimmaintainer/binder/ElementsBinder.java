package cimmaintainer.binder;

import cimmaintainer.dto.Link;
import cimmaintainer.dto.SingleLineDiagram;

import java.util.HashMap;
import java.util.Map;

public class ElementsBinder {

    public static void bind(SingleLineDiagram sld){

        /* Из sld берем весь список Elements и идем по каждому элементу
         *  в каждом элементе идем по всем портам.
         */

        Map<String, Link> linkIdToLinkMap = new HashMap<>();
        sld.getLinks().forEach(link -> linkIdToLinkMap.put(link.getId(), link));
        sld.getElements().forEach(element -> element.getPorts().forEach(port -> {
            port.setElement(element);
            if (port.getLinks().isEmpty()) return;
            String linkId = port.getLinks().get(0); // записываем ID линка каждого порта в linkId
            if (linkId == null) return;

            // связь порта с линком
            Link link = linkIdToLinkMap.get(linkId); // по свойству словарей берем линк (value) с нужным значением Id (key)
            port.setLink(link); // в Port загружаем адрес линка к которому от подключен (связываем)
            // связь линка с портом
            if (link.getSourcePortId().equals(port.getId())){ // смотрим, сходится ли Id порта с источником Линка
                link.setSourcePort(port); // Id порта элемента, SourceId это Id самого элемента (линии, шины ...)
                link.setSource(element);
            }
            else { // или сходится с целью (Target) линка (у линка есть цель и источник которые она связывает, это порты)
                link.setTargetPort(port);
                link.setTarget(element);
            }
        }));
    }
}
