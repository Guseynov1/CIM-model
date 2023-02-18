package ru.mpei.cimmaintainer;

import lombok.SneakyThrows;
import ontologies.bin.ElementsBinder;
import ontologies.converter.CimToJavaConverter;
import ontologies.converter.SldToCimConverter;
import ontologies.mapper.JsonMapper;
import ontologies.package1.DeviceDirectory;
import ontologies.package1.SingleLineDiagram;
import ontologies.package1.VoltageLevelDirectory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import org.eclipse.rdf4j.rio.Rio;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class TestClass1 { // здесь проверяем подключенные зависимости
    @Test @SneakyThrows
    public void test()  {
        /// создание объектов Java
        JsonMapper jsonMapper = new JsonMapper();

        SingleLineDiagram sld = jsonMapper.mapJsonToSld("src/test/resources/Viezdnoe.json");
        List<DeviceDirectory> dev =  JsonMapper.mapJsonToDev("src/test/resources/DeviceDirectory.json");
        List<VoltageLevelDirectory> vol =  JsonMapper.mapJsonToVol("src/test/resources/VoltageLevelDirectory.json");

        ElementsBinder.bind(sld);

        /// конвертирование. Внутри converter есть сам modelBuilder
        SldToCimConverter converter = new SldToCimConverter();
        converter.convert(sld, dev, vol);

        /// создаем файл сим модели в формате xml
        String cimModelXml = converter.getResult(".xml", RDFFormat.RDFXML);

        /* ЛР2 */
        File initialFile = new File("src/test/resources/LR1_cimModel2.xml");
        InputStream targetStream = new FileInputStream(initialFile);


        Model model = Rio.parse(targetStream,"http://iec.ch/TC57/2013/CIM-schema-cim16#", RDFFormat.RDFXML);

        CimToJavaConverter cimToJavaConverter = new CimToJavaConverter();
        cimToJavaConverter.converterCimToJava(model);

        System.out.println(cimModelXml);
    }
}

