package eu.powet.FOTAA;

import com.sun.jna.Memory;
import com.sun.jna.ptr.PointerByReference;
import eu.powet.FOTAA.api.FotaEventListener;
import eu.powet.FOTAA.api.IFota;
import eu.powet.FOTAA.events.FotaEvent;
import eu.powet.FOTAA.events.UploadedEvent;
import eu.powet.FOTAA.events.WaitingBLEvent;
import eu.powet.FOTAA.jna.NativeLoader;
import eu.powet.FOTAA.utils.Board;
import eu.powet.FOTAA.utils.Constants;
import eu.powet.FOTAA.utils.FotaException;
import eu.powet.FOTAA.utils.Helpers;


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 09:56
 */
public class Fota implements IFota {
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private FotaEvent fotaEvent =null;
    private String deviceport = "";
    private int devicetype=-1;
    private int program_size=-1;
    private Byte[] raw_intel_hex_array=null;
    private  long start;
    private  long duree;
    public Fota(String deviceport,Board type) throws FotaException
    {
        if(deviceport.equals("*"))
        {
            if(Helpers.getPortIdentifiers().size() == 0){ throw new FotaException("not board available");   }else
            {
                deviceport = Helpers.getPortIdentifiers().get(0);
            }
        }
        this.deviceport = deviceport;
        this.devicetype = Integer.parseInt(Constants.boards.get(type.toString()).toString());
        fotaEvent = new FotaEvent(this);
    }

    public void addEventListener (FotaEventListener listener) {
        listenerList.add(FotaEventListener.class, listener);
    }

    public void removeEventListener (FotaEventListener listener) {
        listenerList.remove(FotaEventListener.class, listener);
    }

    public void fireFlashEvent (FotaEvent evt)
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2)
        {
            if (evt instanceof UploadedEvent)
            {

                ((FotaEventListener) listeners[i + 1]).completedEvent((UploadedEvent) evt);
            }
            else
            {
                ((FotaEventListener) listeners[i + 1]).progressEvent((FotaEvent) evt);
            }
        }
    }

    @Override
    public void upload(Byte[] raw_intel_hex_array) throws FotaException
    {
        start= System.currentTimeMillis();
        this.raw_intel_hex_array = raw_intel_hex_array.clone();
        Memory mem = new Memory(Byte.SIZE * raw_intel_hex_array.length + 1);
        mem.clear();
        PointerByReference inipar = new PointerByReference();
        inipar.setPointer(mem);
        for (int i = 0; i < raw_intel_hex_array.length; i++) {
            inipar.getPointer().setByte(i * Byte.SIZE / 8, raw_intel_hex_array[i]);
        }
        byte c = '\n';
        inipar.getPointer().setByte((raw_intel_hex_array.length + 1) * Byte.SIZE / 8, c);

        program_size = NativeLoader.getINSTANCE_Foa().write_on_the_air_program(deviceport,devicetype,raw_intel_hex_array.length,inipar);
        if(program_size < 0)
        {
            throw new FotaException("Empty");
        }
    }

    public int getProgram_size() {
        return program_size;
    }

    public Byte[] getRaw_intel_hex_array() {
        return raw_intel_hex_array;
    }

    public void setRaw_intel_hex_array(Byte[] raw_intel_hex_array) {
        this.raw_intel_hex_array = raw_intel_hex_array;
    }

    /**
     * durée en seconde
     * @return
     */
    public long getDuree() {
        return duree  / 1000;
    }

    public void setFinished() {
        duree = System.currentTimeMillis() - start;
    }
}
