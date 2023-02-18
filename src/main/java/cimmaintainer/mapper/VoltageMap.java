package cimmaintainer.mapper;

import cimmaintainer.dto.Device;
import cimmaintainer.dto.Voltage;
import  com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VoltageMap {

    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @SneakyThrows
    public Device[] mapJsonToSld(String filePath) {
        return objectMapper.readValue(
                new File(filePath), Device[].class
        );
    }

    @SneakyThrows
    public Map<String, String> mapJsonToType(String filePath) {
        Map<String, String> voltage = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Voltage[] voltLevel = objectMapper.readValue(new File(filePath), Voltage[].class);
        for (Voltage line : voltLevel) {
            voltage.put(line.getDirectoryId(), line.getValue().getEn());
        }
        return voltage;
    }
}