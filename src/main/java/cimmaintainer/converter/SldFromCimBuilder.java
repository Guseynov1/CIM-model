package cimmaintainer.converter;

import cimmaintainer.JO.Terminal;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SldFromCimBuilder {

    public static void sparQL(Model model) {
        Terminal terminal = new Terminal();
        String log4jConfPath = "C:\\Users\\gamza\\IdeaProjects\\CIM\\src\\main\\resources\\log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
        Repository repository = new SailRepository(new MemoryStore());
        RepositoryConnection connection = repository.getConnection();
        connection.add(model);

        String queryString = "PREFIX cim: <" + "https://iec.ch/TC57/2013/CIM-schema-cim16#" + "> " +
                "SELECT ?tId ?name ?cnId ?ceId " +
                "WHERE { " +
                " ?t a cim:Terminal ; " +
                " cim:IdentifiedObject.mRID ?tId ; " +
                " cim:IdentifiedObject.name ?name ; " +
                " cim:Terminal.ConnectivityNode ?cn ; " +
                " cim:Terminal.ConductingEquipment ?ce . " +
                " ?ce cim:IdentifiedObject.mRID ?ceId ." +
                " ?cn cim:IdentifiedObject.mRID ?cnId ." +
                "}";

        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        try (TupleQueryResult result = query.evaluate()) {

            for (BindingSet solution : result) {
                    String tId = solution.getValue("tId").stringValue();
                    String name = solution.getValue("name").stringValue();
                    String cnId = solution.getValue("cnId").stringValue();
                    String ceId = solution.getValue("ceId").stringValue();
                    System.out.println(tId + cnId + ceId);
                }
            }
    }
}
