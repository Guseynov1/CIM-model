package cimmaintainer.writer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.*;

public class RdfWriter {

    public String writeXml(Model model) {
        StringBuilder xml = new StringBuilder();
        Set<Namespace> namespaces = model.getNamespaces();
        this.setProlog(xml)
                .setRootElementWithNamespaces(namespaces, xml)
                .setResources(xml, model)
                .closeRootElement(xml);
        return xml.toString();
    }

    private RdfWriter setProlog(StringBuilder xml) {
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        return this;
    }

    private RdfWriter setRootElementWithNamespaces(Set<Namespace> namespaces, StringBuilder xml) {
        xml.append("<rdf:RDF");
        namespaces.forEach(namespace -> xml
                .append(" xmlns:")
                .append(namespace.getPrefix()).append("=\"").append(namespace.getName())
                .append("\""));
        xml.append(">\n");
        return this;
    }

    private RdfWriter setResources(StringBuilder xml, Model model) {
        Map<String, Resource> resourceMap = new HashMap<>();
        for (Statement statement : model) {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                if (statement.getSubject().isIRI() && statement.getObject().isIRI()) {
                    String id = ((IRI) statement.getSubject()).getLocalName();
                    String cimClassName = ((IRI) statement.getObject()).getLocalName();
                    resourceMap.put(id, new Resource(id, cimClassName));
                }
            }
        }

        for (Statement statement : model) {
            if (statement.getSubject().isIRI()) {
                String id = ((IRI) statement.getSubject()).getLocalName();
                Resource resource = resourceMap.get(id);
                if (!statement.getPredicate().isIRI()) continue;
                if (statement.getPredicate().equals(RDF.TYPE)) continue;
                if (statement.getObject().isIRI()) {
                    resource.getResourceProperties().add(
                            new Property(
                                    statement.getPredicate().getLocalName(),
                                    ((IRI) statement.getObject()).getLocalName()
                            )
                    );
                } else {
                    resource.getLiteralProperties().add(
                            new Property(
                                    statement.getPredicate().getLocalName(),
                                    statement.getObject().stringValue()
                            )
                    );
                }
            }
        }
        resourceMap.values().forEach(resource -> createXmlResource(resource, xml));
        return this;
    }
    private void createXmlResource(Resource resource, StringBuilder xml) {
        xml.append("\n")
                .append("\t").append("<cim:").append(resource.getCimClassName())
                .append(" rdf:ID=\"").append(resource.getId()).append("\">").append("\n");

        resource.getLiteralProperties().forEach(property -> xml
                .append("\t\t")
                .append("<cim:").append(property.getName()).append(">")
                .append(property.getValue())
                .append("</cim:").append(property.getName()).append(">\n"));

        resource.getResourceProperties().forEach(property -> xml
                .append("\t\t")
                .append("<cim:").append(property.getName()).append(" rdf:resource=\"#")
                .append(property.getValue())
                .append("\"").append("/>\n"));

        xml.append("\t").append("</cim:").append(resource.getCimClassName()).append(">").append("\n");
    }
    private RdfWriter closeRootElement(StringBuilder xml) {
        xml.append("</rdf:RDF>");
        return this;
    }
    @Getter @Setter
    class Resource {
        private String id, cimClassName;
        private List<Property> literalProperties = new LinkedList<>();
        private List<Property> resourceProperties = new LinkedList<>();

        public Resource(String id, String cimClassName) {
            this.id = id;
            this.cimClassName = cimClassName;
        }
    }
    @Getter @Setter
    @AllArgsConstructor
    class Property {
        private String name, value;
    }
}
