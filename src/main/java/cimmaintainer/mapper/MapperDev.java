package cimmaintainer.mapper;

import cimmaintainer.dto.Device;
import  com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MapperDev {

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
        Map<String, String> device = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Device[] deviceType = objectMapper.readValue(new File(filePath), Device[].class);
        for (Device line : deviceType) {
            device.put(line.getId(), line.getDeviceType());
        }
        return device;
    }
}