package eu.powet.FOTAA.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 03/02/12
 * Time: 16:14
 */
public class Helpers {

    public static List<String> getPortIdentifiers() {
        List<String> ports = new ArrayList<String>();
        File file = new File("/dev");
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory() == false)
                {
                    if(files[i].getName().startsWith("tty") || files[i].getName().startsWith("cu"))
                    {
                        if(files[i].getName().contains("USB") || files[i].getName().contains("usb") || files[i].getName().contains("AC") )
                        {
                            String device_name =   "/dev/"+files[i].getName();
                            ports.add(device_name);
                        }
                    }
                }
            }
        }
        return ports;
    }

    public static Byte[] read_file(InputStream reader) throws IOException {
        int c;
        ArrayList<Byte> tab = new ArrayList<Byte>();
        while((c = reader.read()) != -1) {
            tab.add((byte)c);
        }
        if (reader!=null)
            reader.close();

        Byte[] data = tab.toArray(new Byte[tab.size()]);

        return data;
    }
}
