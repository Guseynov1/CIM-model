package cimmaintainer.mapper;

import cimmaintainer.dto.SingleLineDiagram;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

public class JsonMapper {
    private ObjectMapper objectMapper = new ObjectMapper() // чтобы не было ошибки UnrecognizedPropertyException в классе Link
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @SneakyThrows
    public SingleLineDiagram mapJsonToSld(String filePath) {
        return objectMapper.readValue(
                new File(filePath), SingleLineDiagram.class
        );
    }

    @SneakyThrows
    public static List<DeviceDirectory> mapJsonToDev(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper().
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CollectionType collectionType = typeFactory.constructCollectionType(
                List.class, DeviceDirectory.class);
        return objectMapper.readValue(new File(filePath), collectionType);

    }

    @SneakyThrows
    public static List<VoltageLevelDirectory> mapJsonToVol(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper().
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CollectionType collectionType = typeFactory.constructCollectionType(
                List.class, VoltageLevelDirectory.class);
        return objectMapper.readValue(new File(filePath), collectionType);

    }

}
