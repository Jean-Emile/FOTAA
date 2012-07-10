package eu.powet.FOTAA.utils;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 27/02/12
 * Time: 11:37
 */

import eu.powet.FOTAA.api.IBootloader;
import eu.powet.FOTAA.gui.ProgrammerType;
import eu.powet.FOTAA.jna.NativeLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

public class Bootloader  implements Runnable,IBootloader
{
    private Process process=null;

    private ProgrammerType programmer=null;
    private StringBuilder logging=null;
    private Thread t = null;
    private Semaphore lock = new Semaphore(0);

    public Bootloader(ProgrammerType _programmerType){
        t = new Thread(this);
        this.programmer = _programmerType;

    }
    public void burnBootloader()
    {
        logging = new StringBuilder();
        t.start();
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread() {
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = "";
                    try {
                        while((line = reader.readLine()) != null) {
                            logging.append(line + "\n");
                            System.out.println(line);
                        }
                    } finally {
                        reader.close();
                    }
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line = "";
                    try
                    {
                        while((line = reader.readLine()) != null)
                        {
                            if(!line.contains("avrdude: error: usbtiny_receive: No error") && line.length() > 0)
                            {
                                if(line.contains("#")){
                                    logging.append("#");
                                    System.out.print("#");
                                }
                                else
                                {
                                    logging.append(line + "\n");
                                    System.out.println(line);
                                }
                            }
                        }
                    } finally {
                        reader.close();
                    }
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void run()
    {
        try
        {
            File folder = new File(System.getProperty("java.io.tmpdir") + File.separator + "Bootloader");
            if (folder.exists())
            {
                NativeLoader.deleteOldFile(folder);
            }
            folder.mkdirs();

            String pathcodesource = NativeLoader.copyFileFromStream( "Bootloaders/atmega328p/Wireless_Bootloader_ATmega328.c", folder.getAbsolutePath(), "Wireless_Bootloader_ATmega328.c");
            String pathmakefile = NativeLoader.copyFileFromStream( "Bootloaders/atmega328p/Makefile", folder.getAbsolutePath(), "Makefile");

            File dir = new File ( folder.getPath()) ;
            String [] envp = {"" } ;

            process = Runtime.getRuntime().exec("make clean",envp,dir);
            process = Runtime.getRuntime().exec("make all",envp,dir);


            lock.release();
            process.waitFor ( ) ;



            /*
            String pathavrdude =  Bootloader.class.getClassLoader().getResource(NativeLoader.getPath("avrdude")).getPath();


            String fuse = pathavrdude+" -c "+programmer.getProgrammerType()+" -p "+avr.getType()+" -e -u -U lock:w:0x3f:m -U efuse:w:0x00:m -U hfuse:w:0xDA:m -U lfuse:w:0xFF:m";

            Process p =  Runtime.getRuntime().exec(fuse);

            Runtime.getRuntime().exec();




            String flashbootloader=    pathavrdude+" -p "+avr.getType()+" -c "+programmer.getProgrammerType()+" -U flash:w:"+Bootloader.class.getClassLoader().getResource("Bootloaders/atmega328p/Wireless_Bootloader_ATmega328.hex").toURI().getPath()+" -U lock:w:0x0f:m";
            System.out.println(flashbootloader);
            process = Runtime.getRuntime().exec(flashbootloader);
            lock.release();
            process.waitFor();   */
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        //Bootloader burn = new Bootloader(new ProgrammerType("usbtiny"),new DeviceAVR("m328p","K000"));
        //burn.burnBootloader();
    }
}
