package eu.powet.FOTAA;

import eu.powet.FOTAA.API_FOTAA.FlashFirmware;
import eu.powet.FOTAA.API_FOTAA.FlashFirmwareEvent;
import eu.powet.FOTAA.API_FOTAA.FlashFirmwareEventListener;
import eu.powet.FOTAA.Utils.KHelpers;


public class Test {

    /**
     * @param args
     * @throws Exception
     */


    public static void main(String[] args) throws Exception {


        FlashFirmware flash = new FlashFirmware("/dev/tty.usbserial-A400g2wl","ATMEGA328","NODE0");

        Byte[] intel = KHelpers.read_file("/Users/oxyss35/kevoree-extra/org.kevoree.extra.kserial/src/main/c/FlashOvertheair/program_test/test.hex");
        if(flash.write_on_the_air_program(intel) >= 0){
            flash.addEventListener(new FlashFirmwareEventListener() {
                // @Override
                public void FlashEvent(FlashFirmwareEvent evt) {
                    System.out.println("Callback Event received :  "+evt.getSize_uploaded());
                }
            });

            Thread.currentThread().sleep(1000000);

        }


    }

}
