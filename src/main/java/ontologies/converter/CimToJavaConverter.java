package ontologies.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import ontologies.package1.ObjectInSubstation;
import ontologies.package1.Terminal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Getter
@Setter
public class CimToJavaConverter {

    ObjectInSubstation objectInSubstation = new ObjectInSubstation();

    public Model readRDF() {
        try (FileReader fileReader = new FileReader(
                "src/test/resources/cimModel.ttl")) {
            return Rio.parse(fileReader, "http://iec.ch/TC57/2013/CIM-schema-cim16#", RDFFormat.TURTLE);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public  void converterCimToJava(Model model) {

        Repository repository = new SailRepository(new MemoryStore());
        RepositoryConnection connection = repository.getConnection();
        connection.add(model);
        getTerminals(connection);
//        getEquipment(connection);
//      getConnection();


    }

    public  void getTerminals(RepositoryConnection connection) {

        String queryString = "PREFIX cim: <" + "http://iec.ch/TC57/2013/CIM-schema-cim16#" + "> " +
                "SELECT ?tId ?name ?cn " +
                "WHERE { " +
                " ?t a cim:Terminal ; " +
                " cim:IdentifiedObject.mRID ?tId ; " +
                " cim:IdentifiedObject.name ?name; " +
                " cim:Terminal.ConnectivityNode ?cn . " +
                "}";
        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);


        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution : result) {

                String name = solution.getValue("name").stringValue();
                String tId = solution.getValue("tId").stringValue();
                String cn = solution.getValue("cn").stringValue();
//                String ceId = solution.getValue("ceId").stringValue();

                Terminal terminal = new Terminal(name, tId, cn);   //, ceId, cnId);
                objectInSubstation.getTerminals().add(terminal);
            }
            System.out.println();
        }
    }

//    public void getEquipment(RepositoryConnection connection) {
//        String queryString = "PREFIX cim: <" + "http://iec.ch/TC57/2013/CIM-schema-cim16#" + "> " +
//                "SELECT ?s ?id ?name ?ptId ?ap ?bv ?tId " +
//                "WHERE { " +
//                " ?a a ?s ;" +
//                " cim:IdentifiedObject.mRID ?id . " +
//                "OPTIONAL {?a cim:IdentifiedObject.name ?name .} " +
//                "OPTIONAL {?a cim:PowerTransformerEnd.PowerTransformer ?ptId .}" +
//                "OPTIONAL {?a cim:ConductingEquipment.BaseVoltage ?bv .}" +
//                "OPTIONAL {?a cim:ConductingEquipment.Terminal ?tId .}" +
//                "OPTIONAL {?a cim:PowerTransformer.ApparentPower ?ap .} " +
//                "}";
//
//        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL,
//                queryString);
//
//        try (TupleQueryResult result = query.evaluate()) {
//            for (BindingSet solution : result) {
//
//                /* Переменные, которые есть не у всех классов */
//                String name;
//                String ap;
//                String ptId;
//                String bv;
//                String tId;
//
//                /* Переменные, которые есть у всех классов */
//                String s = solution.getValue("s").stringValue();
//                String id = solution.getValue("id").stringValue();
//
//                s = s.replace("http://iec.ch/TC57/2013/CIM-schema-cim16#", "");
//
//                switch (s) {
//                    case "Terminal":
//                        break;
//                    case "ConnectivityNode":
//
//                        ConnectivityNode connectivityNode = new ConnectivityNode(id);
//                        objectInSubstation.getConnectivityNodes().add(connectivityNode);
//                        break;
//
//                    case "BaseVoltage":
//
//                        name = solution.getValue("name").stringValue();
//                        BaseVoltage baseVoltage = new BaseVoltage(name, id);
//                        objectInSubstation.getBaseVoltages().add(baseVoltage);
//                        break;
//                    case "PowerTransformerEnd":
//                        bv = solution.getValue("bv").stringValue()
//                                .replace("http://iec.ch/TC57/2013/CIM-schema-cim16#", "");
//                        tId = solution.getValue("tId").stringValue()
//                                .replace("http://iec.ch/TC57/2013/CIM-schema-cim16#", "");
//                        ptId = solution.getValue("ptId").stringValue()
//                                .replace("http://iec.ch/TC57/2013/CIM-schema-cim16#", "");
//                        name = solution.getValue("name").stringValue();
//
//                        PowerTransformerEnd powerTransformerEnd = new PowerTransformerEnd(tId, ptId, bv, id, name);
//                        objectInSubstation.getConductingEquipments().add(powerTransformerEnd);
//                        break;
//
//                    case "PowerTransformer":
//
//                        name = solution.getValue("name").stringValue();
//                        bv = solution.getValue("bv").stringValue()
//                                .replace("http://iec.ch/TC57/2013/CIM-schema-cim16#", "");
//                        ap = solution.getValue("ap").stringValue();
//
//                        PowerTransformer powerTransformer = new PowerTransformer(id, name, ap, bv);
//                        objectInSubstation.getConductingEquipments().add(powerTransformer);
//                        break;
//
//
//                    default:
//                        name = solution.getValue("name").stringValue();
//                        bv = solution.getValue("bv").stringValue()
//                                .replace("http://iec.ch/TC57/2013/CIM-schema-cim16#", "");
//                        ConductingEquipment conductingEquipment = new ConductingEquipment(bv, id, name, s);
//                        objectInSubstation.getConductingEquipments().add(conductingEquipment);
//                }
//
//            }
//        }
//        System.out.println();
//    }

//    public static void getConnection() {
//        for (Terminal terminal : terminals) {
//            terminal.setConductingEquipment(conductingEquipmentMap.get(terminal.getConductingEquipmentId()));
//            terminal.setConnectivityNode(connectivityNodeMap.get(terminal.getConnectivityNodeId()));
//            ConductingEquipment equipment = conductingEquipmentMap.get(terminal.getConductingEquipmentId());
//            equipment.setBaseVolt(baseVoltageMap.get(equipment.getBaseVoltage()));
//
//            if (equipment instanceof PowerTransformerEnd) {
//                /* Приводим типы для работы с переменной */
//                PowerTransformerEnd powerTransformerEnd = (PowerTransformerEnd) equipment;
//
//                /* Добавляем связь с терминалом */
//                powerTransformerEnd.setTerminal(terminal);
//
//                /*Получаем powerTransformer */
//                PowerTransformer powerTransformer = (PowerTransformer) conductingEquipmentMap
//                        .get(powerTransformerEnd.getPowerTransformerId());
//
//                /* Добавляем связь с напряжением у powerTransformer */
//                powerTransformer.setBaseVolt(baseVoltageMap.get(powerTransformer.getBaseVoltage()));
//                powerTransformerEnd.setPowerTransformer(powerTransformer);
//                conductingEquipmentMap.put(terminal.getConductingEquipmentId(), powerTransformerEnd);
//            }
//        }
//        System.out.println();
//    }



    public  void WriteClassesToJson() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(
                new File("src/test/resources/jsonModel.json")
                ,objectInSubstation);
        System.out.println();
    }
}
