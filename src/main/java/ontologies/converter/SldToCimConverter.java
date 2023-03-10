package ontologies.converter;

import cimmaintainer.dto.Element;
import cimmaintainer.dto.SingleLineDiagram;
import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SldToCimConverter {

    private final String cimNamespace = "http://iec.ch/TC57/2013/CIM-schema-cim16#";

    @Getter
    private ModelBuilder modelBuilder = new ModelBuilder();

    public SldToCimConverter() {
        modelBuilder
                .setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                .setNamespace("cim", cimNamespace);
    }


    public void convert(SingleLineDiagram sld, List<DeviceDirectory> dev, List<VoltageLevelDirectory> vol) {

        Map<String, String> mapVol = mapingVol(vol);
        Map<String, DeviceDirectory> mapDev = mapingDev(dev);

        sld.getElements().stream()  // 336  - 54 con - 6 bus = 276
                .filter(e -> (!"connectivity".equals(e.getType())))
                .filter(e -> (!"bus".equals(e.getType())))
                .forEach(e -> convertElementToRdfResource(e, mapVol, mapDev));

        List<List<Element>> groupsOfConnectivityElements = groupConnectivityElementsByGraphAnalyzing(sld);

        Resource CN = buildConnectivityNode(groupsOfConnectivityElements);
        CN.getElements().forEach(e -> convertCNToRdfResource(e));

        Resource T = buildTerminal(CN);
        T.getElements().forEach(e -> convertTerminalToRdfResource(e));

    }


    private void convertCNToRdfResource(Element element) {
        modelBuilder
                .subject("cim:" + element.getId())
                .add("cim:IdentifiedObject.mRID", element.getId())
                .add("nti:ConnectivityNode.VoltageLevel",element.getVoltageLevel())
                .add(RDF.TYPE, "cim:ConnectivityNode");
    }

    private void convertTerminalToRdfResource(Element element) {
        modelBuilder
                .subject("cim:" + element.getId())
                .add("cim:IdentifiedObject.mRID", element.getId())
                .add("cim:IdentifiedObject.name", element.getType()) // добавили предикат, чтобы видеть имена объектов
                .add("cim:Terminal.ConnectivityNode",element.getConNod())
                .add(RDF.TYPE, "cim:Terminal");
    }

    private void convertElementToRdfResource(Element element, Map<String, String> mapVol, Map<String, DeviceDirectory> mapDev ) {

        DeviceDirectory DevType = mapDev.get(element.getDirectoryEntryId());
        String VolLev = mapVol.get(element.getVoltageLevel());

        modelBuilder
                .subject("cim:" + "_"+element.getId())
                .add("cim:IdentifiedObject.mRID", ""+element.getId())
                .add("cim:IdentifiedObject.ProjectName", element.getProjectName ())
                .add(RDF.TYPE, "cim:"+DevType.getDeviceType())
                .add("cim:ConductingEquipment.BaseVoltage", VolLev)
                .add("cim:IdentifiedObject.name", DevType.getName().getRu())
                .add("cim:IdentifiedObject.ProjectName", element.getProjectName ());
        if ("ThreeWindingPowerTransformerWithTapChanger".equals(DevType.getDeviceType()))
            modelBuilder.add("cim:PowerTransformerEnd.ApparentPower", element.getFields().get(0).getValue());

    }

    public String getResult(String suffix, RDFFormat format) {
        Model model = modelBuilder.build(); //getModel от объекта modelBuilder
        if (format.equals(RDFFormat.RDFXML)) {
            RdfWriter rdfWriter = new RdfWriter();
            return rdfWriter.writeXml(model);
        } else {
            OutputStream out = null;
            String cim;
            try {
                File temoFile = File.createTempFile("file", suffix);
                out = new FileOutputStream(temoFile);
                Rio.write(model, out, cimNamespace, format);
                cim = Files.readString(Path.of(temoFile.getPath()));

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return cim;
        }
    }

    // алгоритм, группирующий те коннективити объекты, которые мы преобразуем в один объект коннективитинод
    private List<List<Element>> groupConnectivityElementsByGraphAnalyzing(SingleLineDiagram sld) {
        Set<String> visitedElementIds = new HashSet<>();                        // множество для записи элементов, по которым прошли
        List<List<Element>> groupsOfConnectivityElements = new LinkedList<>();  // Массив для группировки коннективити

        sld.getElements().stream()
                .filter(e -> ("connectivity".equals(e.getType()) | "bus".equals(e.getType())))
                .filter(e -> !visitedElementIds.contains(e.getId()))    // проверка содержится ли элемент в множестве
                .forEach(e -> { //так как несколько выражений
                    // тут будет поиск в ширину
                    Deque<Element> connectivityElements = new LinkedList<>(){{add(e);}};        // добавляем элемент в очередь
                    List<Element> groupOfConnectivityElements = new LinkedList<>();             // создаем группу конективити
                    walkThroughSingleLineDiagram(connectivityElements, visitedElementIds, groupOfConnectivityElements);
                    groupsOfConnectivityElements.add(groupOfConnectivityElements);
                });
        // как работает stream filter - elements это коллекция (list список) и в нем находится множество элементов и используя фильтр мы фильтруем
        // элементы этого списка и дальше используем отфильтрованные значения в другом метод, т.е. это функциональное программирование

        sld.getLinks().stream()                     // тут создаем из линков ConnectivityNode
                .filter(l -> !visitedElementIds.contains(l.getSource().getId()))
                .filter(l -> !visitedElementIds.contains(l.getTarget().getId()))
                .forEach(l -> {
                    List<Element> groupOfConnectivityElements = new LinkedList<>();
                    groupLinks(l, groupOfConnectivityElements);
                    groupsOfConnectivityElements.add(groupOfConnectivityElements);
                });

        System.out.println();
        return groupsOfConnectivityElements;
    }


    private Map<String, String>  mapingVol(List<VoltageLevelDirectory> vol) {
        Map<String, String> mapVol = new HashMap<>();
        vol.forEach(l -> mapVol.put(l.getDirectoryId(), l.getValue().getEn()));
        return mapVol;
    }

    private Map<String, DeviceDirectory>  mapingDev(List<DeviceDirectory> dev) {
        Map<String, DeviceDirectory> mapDev = new HashMap<>();
        dev.forEach(l -> mapDev.put(l.getId(), l));
        return mapDev;
    }


    private void groupLinks(Link l, List<Element> groupOfConnectivityElements) {
        Element element = new Element();
        element.setVoltageLevel(l.getSource().getVoltageLevel());
        element.getPorts().add(l.getSourcePort());
        element.getPorts().add(l.getTargetPort());
        groupOfConnectivityElements.add(element);
    }


    private void walkThroughSingleLineDiagram(
            Deque<Element> connectivityElements,
            Set<String> visitedElementIds,
            List<Element> groupOfConnectivityElements) {

        Element connectivity = connectivityElements.pop();          // достаем элемент из очереди
        visitedElementIds.add(connectivity.getId());                // добавляем в множество посещенных
        groupOfConnectivityElements.add(connectivity);              // добавляем в конективити узел

        connectivity.getPorts().forEach(p -> {              // смотрим каждый порт
            Link link = p.getLink();                        // у каждого порта берем линк
            if (link == null) return;                       // и находим соседний элемент
                                                            // если он connectivity, то добавляем в очередь и делаем рекурсивную функцию
            // соседний элемент
            Element sibling = link.getSourcePortId().equals(p.getId())?link.getTarget():link.getSource();

            if ( ("bus".equals(sibling.getType()) | "connectivity".equals(sibling.getType()))
                    && !visitedElementIds.contains(sibling.getId())) {

                connectivityElements.add(sibling);
                walkThroughSingleLineDiagram(connectivityElements, visitedElementIds, groupOfConnectivityElements);
            }
        });

    }

    private Resource buildTerminal(Resource CN) {
        Resource T = new Resource();

        CN.getElements().forEach(l -> {

            l.getPorts().forEach(p -> {

                Element element = new Element();
                element.getPorts().add(p);
                if (p.getId().equals(p.getLink().getSourcePortId())) {
                    element.setType(p.getLink().getTarget().getProjectName());
                    if (element.getType() == null)
                        element.setType(p.getLink().getSource().getProjectName());
                } else {
                    element.setType(p.getLink().getSource().getProjectName());
                    if (element.getType() == null)
                        element.setType(p.getLink().getTarget().getProjectName());
                }
                if (element.getType() == null)
                    System.out.println("null "+l.getId());
                element.setConNod(l.getId());
                element.setId(l.getId()+"_P_"+element.getType());
                T.getElements().add(element);
            });
        });
        return T;
    }


    private Resource buildConnectivityNode(List<List<Element>> groupsOfConnectivityElements) {
        Resource CN = new Resource();

        groupsOfConnectivityElements.forEach(l -> {     // проходим по первым листам в groupsOfConnectivityElements

            Set<String> addedPorts = new HashSet<>();
            Element element = new Element();
            element.setId( "CN_"+((CN.getElements().size())+1));
            element.setType("ConnectivityNode");
            element.setVoltageLevel(l.get(0).getVoltageLevel());

            l.forEach(e ->                            // проходим по листам содержащим элементы
                e.getPorts().forEach(p -> {
                    if (p.getLink() == null) return;

                    if ("connectivity".equals(p.getLink().getSource().getType()) & "connectivity".equals(p.getLink().getTarget().getType()))
                        return;                     // если с двух сторон коннективити, то эти порты не добавляем

                    if (!addedPorts.contains(p.getLink().getId())) {
                        addedPorts.add(p.getLink().getId());
                        element.getPorts().add(p);
                    }
                })
            );   // добавляем порты в Element

            CN.getElements().add(element);

        });
        return CN;
    }
}
