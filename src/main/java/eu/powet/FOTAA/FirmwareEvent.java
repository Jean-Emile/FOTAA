package eu.powet.FOTAA;




import eu.powet.FOTAA.jna.FotaaEvent;
import eu.powet.FOTAA.jna.NativeLoader;

import java.util.EventObject;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 11:44
 */
public class FirmwareEvent extends EventObject implements FotaaEvent {
    private int size_uploaded;
    private Firmware flashFirmware;
    public FirmwareEvent(Firmware src) {
        super(src);
        NativeLoader.getINSTANCE_Foa().register_FlashEvent(this);
        this.flashFirmware = src;
    }

    @Override
    public void FlashEvent(int taille)
    {
        // TODO implements timeout is the taille don't increase
        if(taille == -38)
        {
            flashFirmware.fireFlashEvent(new UploadedEvent(flashFirmware));
            NativeLoader.getINSTANCE_Foa().close_flash();
        }else  if(taille == -35)
        {
            System.out.println(Constants.messages.get(taille));
        }else if(taille == -29)
        {
            System.out.println(Constants.messages.get(taille));
        }
        else if(taille == -36)
        {
            //System.out.println("Ready..");
        }
        else if (taille <0)
        {
            System.out.println("ERROR = "+taille+" "+Constants.messages.get(taille));
        }
        else
        {
            this.size_uploaded = taille;
            flashFirmware.fireFlashEvent(this);
        }
    }

    public int getSize_uploaded() {
        return size_uploaded;
    }

    public Firmware getFlashFirmware() {
        return flashFirmware;
    }
}
