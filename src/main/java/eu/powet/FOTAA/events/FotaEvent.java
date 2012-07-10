package eu.powet.FOTAA.events;




import eu.powet.FOTAA.Fota;
import eu.powet.FOTAA.jna.FotaaEvent;
import eu.powet.FOTAA.jna.NativeLoader;
import eu.powet.FOTAA.utils.FotaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventObject;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 11:44
 */
public class FotaEvent extends EventObject implements FotaaEvent {

    public final static int FINISH=3;
    public final static int OK=0;
    public final static int EVENT_WAITING_BOOTLOADER=2;
    public final static int ERROR_WRITE=-2;
    public final static int ERROR_READ=-3;
    public final static int RE_SEND_EVENT=4;
    public  int count_waiting = 0;
    private int size_uploaded;
    private Fota fota;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public FotaEvent(Fota src) {
        super(src);
        NativeLoader.getINSTANCE_Foa().register_FlashEvent(this);
        this.fota = src;
    }


    public void failover()
    {
        NativeLoader.getINSTANCE_Foa().close_flash();
        try
        {
            fota.upload(fota.getRaw_intel_hex_array());
        } catch (FotaException e) {
            logger.error("fail over ",e);
        }

    }
    @Override
    public void FlashEvent(int evt)
    {
        if(evt == FINISH)
        {
            fota.setFinished();
            fota.fireFlashEvent(new UploadedEvent(fota));
            NativeLoader.getINSTANCE_Foa().close_flash();
        } else if(evt == RE_SEND_EVENT)
        {
            //logger.warn("RE_SEND");

        }
        else if(evt == ERROR_WRITE || evt == ERROR_READ)
        {
            logger.error("ERROR_WRITE/ERROR_READ ");
            failover();
        }else if(evt == EVENT_WAITING_BOOTLOADER)
        {
            System.out.println("Waiting for target IC to boot into bootloader ");
            count_waiting++;
            if(count_waiting > 20)
            {
               // failover();
                count_waiting = 0;
            }
        }
        else if(evt > 0)
        {
            this.size_uploaded = evt;
            fota.fireFlashEvent(this);
        }
    }

    public int getSize_uploaded() {
        return size_uploaded;
    }

    public Fota getFota() {
        return fota;
    }


}
