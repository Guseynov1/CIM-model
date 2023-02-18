package cimmaintainer.converter;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileReader;
import java.io.IOException;

public class RdfMaintainer {
    public static Model rdfMaintainer(){
        try (
                FileReader fileReader = new FileReader(
                        "C:\\Users\\gamza\\IdeaProjects\\CIM\\src\\main\\resources\\cim-model.xml")) {
            return Rio.parse(fileReader, "https://iec.ch/TC57/2013/CIM-schema-cim16#", RDFFormat.RDFXML);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }
}
