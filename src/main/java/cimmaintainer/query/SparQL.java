package cimmaintainer.query;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class SparQL {

    private final String cimUri = "http://iec.ch/TC57/2013/CIM-schema-cim16#";

    public void RDFtoJavaClass(Model model) {
        Repository repository = new SailRepository(new MemoryStore());
        RepositoryConnection connection = repository.getConnection();
        connection.add(model);

        String queryString = "PREFIX cim: <" + cimUri + "> " +
                "SELECT ?tId ?cnId " +
                "WHERE { " +
                " ?t a cim:ConnectivityNode ; " +
                " cim:IdentifiedObject.mRID ?tId ; " +
                " cim:ConnectivityNode.VoltageLevel ?cnId" +
                "}";
        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                queryString);
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution : result) {
                String tId = solution.getValue("tId").stringValue();
                String cnId = solution.getValue("cnId").stringValue();
//                String ceId = solution.getValue("ceId").stringValue();

            }
        }
    }

}
//<cim:Terminal rdf:ID="CN_131_P_CTR66">
//<cim:IdentifiedObject.mRID>CN_131_P_CTR66</cim:IdentifiedObject.mRID>
//<cim:IdentifiedObject.name>CTR66</cim:IdentifiedObject.name>
//<cim:Terminal.ConnectivityNode>CN_131</cim:Terminal.ConnectivityNode>
//</cim:Terminal>
//
//<cim:Terminal rdf:ID="CN_43_P_DIS68">
//<cim:IdentifiedObject.mRID>CN_43_P_DIS68</cim:IdentifiedObject.mRID>
//<cim:IdentifiedObject.name>DIS68</cim:IdentifiedObject.name>
//<cim:Terminal.ConnectivityNode>CN_43</cim:Terminal.ConnectivityNode>
//</cim:Terminal>
