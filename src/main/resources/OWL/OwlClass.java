package OWL;

import lombok.SneakyThrows;
import org.nfunk.jep.function.Add;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semarglproject.vocab.OWL;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OwlClass {

    @SneakyThrows
    public static void main(String[] args) {
        File file = new File("src/test/resources/ont1.owl");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLDataFactory df = manager.getOWLDataFactory();        // factory
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        //создали пространство имен
        String namespace = ontology.getOntologyID().getOntologyIRI().get().toString() + "#";

        OWLDataProperty dataProperty1 = df.getOWLDataProperty(IRI.create(namespace + "datProp1"));

        System.out.println("Load ont document");



        // достали класс из онтологии
        OWLClass class1 = df.getOWLClass(IRI.create(namespace + "class1"));
        // тут будут индивидуалы класса
        Set<OWLNamedIndividual> class1Individual = new HashSet<>();
        class1Individual = getIndividualsOfClass(class1, reasoner);


        System.out.println("Индивиды классов");
        class1Individual.forEach(x -> {         // выводим индивидуумов первого класса
            String name = x.getIRI().getShortForm();
            System.out.println(name);
        });


        // добавление нового индивида в класс
        String newInd = "ind7";
        setNewIndividualToOntology(df, namespace, ontology, manager, class1, newInd);

        // добавление DataProperty в индивидов класса
        class1Individual.forEach(x -> {
                    setDataOWLProperty(df, ontology, manager, dataProperty1, x, 10);
                });

        File file2 = new File("src/test/resources/ont2.owl");
        OutputStream out = new FileOutputStream(file2);
        manager.saveOntology(ontology, out);


    }


    public static Set<OWLNamedIndividual> getIndividualsOfClass(OWLClass cls, OWLReasoner reasoner) {
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true);
        Set<OWLNamedIndividual> instancesFlattened = instances.getFlattened();
        return instancesFlattened;
    }

    public static Collection<OWLIndividual> getIndividualsFromProperty(OWLIndividual ind, OWLOntology ont,
                                                                       OWLObjectProperty property) {
        Collection<OWLIndividual> objectPropertyValues = EntitySearcher.getObjectPropertyValues(ind, property, ont);
        return objectPropertyValues;
    }

    public static Collection<OWLLiteral> getDataPropertyValue(OWLIndividual ind, OWLOntology ont,
                                                              OWLDataProperty property) {
        Collection<OWLLiteral> dataPropertyValue = null;
        dataPropertyValue = EntitySearcher.getDataPropertyValues(ind, property, ont);
        return dataPropertyValue;
    }

    public static void setNewIndividualToOntology(OWLDataFactory dataFactory, String ontNameSpace, OWLOntology ontology,
                                                  OWLOntologyManager manager, OWLClass owlClass, String individualName) {
        OWLNamedIndividual owlNamedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ontNameSpace+individualName));
        OWLAxiom classDeclaration = dataFactory.getOWLClassAssertionAxiom(owlClass, owlNamedIndividual);
        AddAxiom addAxiom = new AddAxiom(ontology, classDeclaration);
        manager.applyChange(addAxiom);
    }

    public static void setObjectOWLProperty(OWLDataFactory dataFactory, String ontNameSpace, OWLOntology ontology, //OWLClass owlClass,
                                            OWLOntologyManager manager, String toIndividualName, OWLObjectProperty objectProperty,
                                            OWLNamedIndividual owlNamedIndividual) {

        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(IRI.create(ontNameSpace+toIndividualName));
        OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                objectProperty, owlNamedIndividual, individual);
        AddAxiom addAxiom = new AddAxiom(ontology, owlObjectPropertyAssertionAxiom);
        manager.applyChange(addAxiom);
    }

    public static void setDataOWLProperty(OWLDataFactory dataFactory, OWLOntology ontology, OWLOntologyManager manager,
                                          OWLDataProperty dataProperty, OWLNamedIndividual namedIndividual,int val) {
        OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty,
                namedIndividual, val);
        AddAxiom addAxiom = new AddAxiom(ontology, dataPropertyAssertionAxiom);
        manager.applyChange(addAxiom);
    }

}
