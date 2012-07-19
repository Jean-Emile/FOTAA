package eu.powet.FOTAA;

import eu.powet.FOTAA.api.FotaEventListener;
import eu.powet.FOTAA.events.FotaEvent;
import eu.powet.FOTAA.jna.NativeLoader;
import eu.powet.FOTAA.utils.Board;
import eu.powet.FOTAA.utils.Helpers;


public class Test {

    /**
     * @param args
     * @throws Exception
     */


    public static void main(String[] args) throws Exception {



        Fota fota = new Fota("*", Board.ATMEGA328);



        Byte[] intel = Helpers.read_file(NativeLoader.class.getClassLoader().getResourceAsStream("programTest/test.hex"));

        fota.upload(intel);


        fota.addEventListener(new FotaEventListener() {
            // @Override
            public void progressEvent(FotaEvent evt) {
                System.out.println(" Uploaded " + evt.getSize_uploaded()+"/"+evt.getFota().getProgram_size() + " octets");
            }

            @Override
            public void completedEvent(FotaEvent evt) {
                System.out.println("Transmission completed successfully <" + evt.getFota().getProgram_size() + " octets "+evt.getFota().getDuree()+" secondes >");
                System.exit(0);
            }
        });

        Thread.currentThread().sleep(50000);



    }

}
