package eu.powet.FOTAA;

import eu.powet.FOTAA.utils.Helpers;
import eu.powet.FOTAA.jna.NativeLoader;


public class Test {

    /**
     * @param args
     * @throws Exception
     */


    public static void main(String[] args) throws Exception {

        Byte[] intel = Helpers.read_file(NativeLoader.class.getClassLoader().getResourceAsStream("programTest/test.hex"));

        Firmware flash = new Firmware(new DeviceAVR("/dev/ttyACM0","ATMEGA328","K000"));

        flash.upload_program(intel);


        flash.addEventListener(new FirmwareEventListener() {
            // @Override
            public void progressEvent(FirmwareEvent evt) {
                System.out.println(" Uploaded "+evt.getSize_uploaded()+" octets");
            }

            @Override
            public void completedEvent(FirmwareEvent evt) {
                System.out.println("Transmission completed successfully <"+evt.getFlashFirmware().getAvr().getTag()+">");
                System.exit(0);
            }
        });

        Thread.currentThread().sleep(1000000);



    }

}
