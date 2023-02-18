package cimmaintainer.converter;

import cimmaintainer.dto.*;
import cimmaintainer.writer.RdfWriter;
import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SldToCimConverter { // конвертация из данной модели в RDF XML

    private final String cimNamespace = "https://iec.ch/TC57/2013/CIM-schema-cim16#"; // обозначение пространства имен

    @Getter
    private ModelBuilder modelBuilder = new ModelBuilder(); // используя это создаем ресурс (у этого объекта есть метод subject, который инициализирует субъект - ресурс)

    public SldToCimConverter() { // указали, что у создаваемого файла RDF есть 2 пространства
        modelBuilder
                .setNamespace(RDF.PREFIX, RDF.NAMESPACE)
                .setNamespace("cim", cimNamespace)
                .subject("cim:BV")
                .add(RDF.TYPE, "cim:BaseVoltage");
    }

    // в cim модели
    // обычно описан набор первичного оборудования, набор связей между оборудованием, координаты на графической схеме и т.д.

    public void convert(SingleLineDiagram sld, Map<String, String> devices, Map<String, String> voltages) { // здесь для каждого метода преобразовываем элемент в ресурс
        sld.getElements().forEach(e -> convertElementToRdfResource(e, devices, voltages));
        List<List<Element>> groupsOfConnectivity = groupConnectivityElements(sld);
        ConnectivityNode(groupsOfConnectivity);
        Map<String, Element> elementsMap = new HashMap<>();
        sld.getElements().stream() // 336  - 54 con - 6 bus = 276
                .filter(element -> element.getType().equals("directory"))
                .forEach(element -> {
                    elementsMap.put(element.getId(), element);});

        List<Link> linksList = new ArrayList<>(sld.getLinks());
        linksToRDFResource(linksList, elementsMap);


        System.out.println();
    }

    private void linksToRDFResource(List<Link> linksList, Map<String, Element> elementsMap){
        linksList.stream()
                .filter(link -> elementsMap.containsKey(link.getSourceId()))
                .filter(link -> elementsMap.containsKey(link.getTargetId()))
                .forEach(link -> {
                    modelBuilder
                            .subject("cim:_" + link.getId())
                            .add("cim:IdentifiedObject.mRID", link.getId())
                            .add("cim:IdentifiedObject.name", "Link")
                            .add("cim:IdentifiedObject.projectID", "Substation Viezdnoe")
                            .add(RDF.TYPE, "cim:ConnectivityNode")
                            .add("cim:ConnectivityNode.BaseVoltage", elementsMap.get(link.getSourceId()).getVoltageLevel());
                    modelBuilder
                            .subject("cim:_" + link.getSourcePortId())
                            .add("cim:IdentifiedObject.mRID", link.getSourcePortId())
                            .add("cim:IdentifiedObject.name", elementsMap.get(link.getSourceId()).getOperationName())
                            .add(RDF.TYPE, "cim:Terminal")
                            .add("cim:Terminal.ConnectivityNode", "cim:_" + link.getId())
                            .add("cim:Terminal.ConductingEquipment", "cim:_" + link.getSourceId());
                    modelBuilder
                            .subject("cim:_" + link.getTargetPortId())
                            .add("cim:IdentifiedObject.mRID", link.getTargetPortId())
                            .add("cim:IdentifiedObject.name", elementsMap.get(link.getTargetId()).getOperationName())
                            .add(RDF.TYPE, "cim:Terminal")
                            .add("cim:Terminal.ConnectivityNode", "cim:_" + link.getId())
                            .add("cim:Terminal.ConductingEquipment", "cim:_" + link.getTargetId());
                });
    }

    private void ConnectivityNode(List<List<Element>> groupsOfConnectivity){
        int i = 0;
        while (i < groupsOfConnectivity.size()){
            modelBuilder
                    .subject("cim:_" + groupsOfConnectivity.get(i).get(0).getId())
                    .add("cim:IdentifiedObject.mRID", groupsOfConnectivity.get(i).get(0).getId())
                    .add("cim:IdentifiedObject.name", i)
                    .add("cim:IdentifiedObject.projectID", "Substation Viezdnoe")
                    .add(RDF.TYPE, "cim:ConnectivityNode")
                    .add("cim:ConnectivityNode.BaseVoltage", groupsOfConnectivity.get(i).get(0).getVoltageLevel());

            for (int z = 0; z < groupsOfConnectivity.get(i).size(); z++){
                int j = 0;
                while (j < groupsOfConnectivity.get(i).get(z).getPorts().size()) {
                    if (groupsOfConnectivity.get(i).get(z).getPorts().get(j).getLinks().size() != 0){
                        if (groupsOfConnectivity.get(i).get(z).getPorts().get(j).getLink().getSourceport().getElement().getType().equals("directory")){
                            modelBuilder
                                    .subject("cim:_" + groupsOfConnectivity.get(i).get(z).getPorts().get(j).getId())
                                    .add("cim:IdentifiedObject.mRID", groupsOfConnectivity.get(i).get(z).getPorts().get(j).getId())
                                    .add("cim:IdentifiedObject.name", groupsOfConnectivity.get(i).get(z).getPorts().get(j).getName())
                                    .add(RDF.TYPE, "cim:Terminal")
                                    .add("cim:Terminal.ConnectivityNode", "cim:_" + groupsOfConnectivity.get(i).get(z).getId())
                                    .add("cim:Terminal.ConductingEquipment", "cim:_" + groupsOfConnectivity.get(i).get(z).getPorts().get(j).getLink().getSourceport().getElement().getId());
                        }
                    }
                    j++;
                }
            }
            i++;
        }
    }

    private void Terminal(Element element) {
        for (Port port : element.getPorts()) {
            if (port.getLinks() == null || port.getLinks().size()==0) continue;
            modelBuilder
                    .subject("cim:_" + port.getId())
                    .add("cim:IdentifiedObject.mRID", port.getId())
                    .add("cim:IdentifiedObject.name", port.getName())
                    .add(RDF.TYPE, "cim:Terminal")
                    .add("cim:Terminal.ConnectivityNode", "cim:" + port.getLinks().get(0))
                    .add("cim:Terminal.ConductingEquipment", "cim:" + element.getId());
        }

    }

    private void convertElementToRdfResource(Element element, Map<String, String> devices, Map<String, String> voltages) {
        if (element.getType().equals("directory")) {
            modelBuilder
                    .subject("cim:_" + element.getId()) // тут добавили субъекты
                    .add("cim:IdentifiedObject.mRID", element.getId()) // здесь предикаты
                    .add("cim:ConductingEquipment.BaseVoltage", voltages.get(element.getVoltageLevel()));

            if (element.getProjectName() != null)
                modelBuilder.add("cim:IdentifiedObject.name", element.getProjectName());
            modelBuilder
                    .add(RDF.TYPE, "cim:".concat(devices.get(element.getDirectoryEntryId())));

            if (devices.get(element.getDirectoryEntryId()).equals("Three-winding power transformer with tap changer")) {
                for (Port port: element.getPorts()) {
                    modelBuilder
                            .add("cim:PowerTansformer.PowerTransformerEnd", "cim:" + port.getId());

                }
                for (Fields fields : element.getFields()) {
                    modelBuilder
                            .add("cim:ApparentPower", fields.getValue());

                    for (Port port: element.getPorts()) {

                        ModelBuilder y = modelBuilder
                                .subject("cim:PowerTransformerEnd" + port.getId())
                                .add("cim:PowerTransformerEnd.Powertransformer", element.getProjectName())
                                .add("cim:PowerTransformerEnd.connectionKind", "Y");
//                        ElPorts(element);
                        int i = 0;
                        for (Fields field: port.getFields()) {

                            if (field.getDirectoryId().equals("14")) {
                                modelBuilder
                                        .add(RDF.TYPE, "cim:_PTE_HV")
                                        .add("cim: TransformerEnd.endNumber",i)
                                        .add("cim:ConductingEquipment.BaseVoltage", voltages.get(field.getDirectoryId()))
                                        .add("cim:ConductingEquipment.Terminal", "cim:" + port.getId());

                            }
                            if (field.getDirectoryId().equals("13")) {
                                modelBuilder
                                        .add(RDF.TYPE, "cim:_PTE_MV")
                                        .add("cim: TransformerEnd.endNumber",i)
                                        .add("cim:ConductingEquipment.BaseVoltage", voltages.get(field.getDirectoryId()))
                                        .add("cim:ConductingEquipment.Terminal", "cim:" + port.getId());

                            }
                            if (field.getDirectoryId().equals("11")) {
                                modelBuilder
                                        .add(RDF.TYPE, "cim:_PTE_LV")
                                        .add("cim: TransformerEnd.endNumber",i)
                                        .add("cim:ConductingEquipment.BaseVoltage", voltages.get(field.getDirectoryId()))
                                        .add("cim:ConductingEquipment.Terminal", "cim:" + port.getId());
                            }
                            i+=1;
                        }
                    }
                }
            }

        }

    }

    private List<List<Element>> groupConnectivityElements(SingleLineDiagram sld){
        Set<String> visitedElements = new HashSet<>();
        List<List<Element>> groupsOfConnectivity = new LinkedList<>();
        sld.getElements().stream()
                .filter(element -> "connectivity".equals(element.getType()))
                .filter(element -> !visitedElements.contains(element.getId()))
                .forEach(element -> {
                    Deque<Element> connectivityElements = new LinkedList<>(){{add(element);}};
                    List<Element> groupOfConnectivity = new LinkedList<>();

                    walkThroughSLD(connectivityElements, visitedElements, groupOfConnectivity);
                    groupsOfConnectivity.add(groupOfConnectivity);
                });
        return groupsOfConnectivity;
    }

    private void walkThroughSLD(Deque<Element> connectivityElements, Set<String> visitedElements, List<Element> groupOfConnectivity){

        Element connectivity = connectivityElements.pop();
        visitedElements.add(connectivity.getId());
        groupOfConnectivity.add(connectivity);
        connectivity.getPorts().forEach(port -> {
            Link link = port.getLink();
            if (link == null) return;
            Element sibling = link.getSourcePortId().equals(port.getId()) ? link.getTarget() : link.getSource();

            if ("connectivity".equals(sibling.getType()) && !visitedElements.contains(sibling.getId())){
                connectivityElements.add(sibling);
                walkThroughSLD(connectivityElements, visitedElements, groupOfConnectivity);
            }
        });
    }

    public String getResult(RDFFormat rdfFormat) {
        Model model = modelBuilder.build(); //getModel от объекта modelBuilder

        if (rdfFormat.equals(RDFFormat.RDFXML)) {
            RdfWriter rdfWriter = new RdfWriter();
            return rdfWriter.writeXml(model);
        } else {
            OutputStream out = null;
            String cim;
            try {
                File tempFile = File.createTempFile("file", ".txt");
                out = new FileOutputStream(tempFile);
                Rio.write(model, out, cimNamespace, rdfFormat);
                cim = Files.readString(Path.of(tempFile.getPath()));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } finally {
                try {
                    assert out != null;
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return cim;
        }
    }
}
