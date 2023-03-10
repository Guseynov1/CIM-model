package cimmaintainer.converter;

import java.io.FileOutputStream;
import java.io.IOException;

public class CimToFileSaver {
    public static void writeXML(String cimModel){
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\gamza\\IdeaProjects\\CIM\\src\\main\\resources\\cim-model.xml")){
            byte[] buffer = cimModel.getBytes();
            fos.write(buffer, 0, buffer.length);
            System.out.println("The file is recorded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
