package eu.powet.FOTAA;

import com.sun.jna.Memory;
import com.sun.jna.ptr.PointerByReference;
import eu.powet.FOTAA.api.IFirmware;
import eu.powet.FOTAA.jna.NativeLoader;
import eu.powet.FOTAA.utils.FotaaError;


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 09:56
 */
public class Firmware implements IFirmware {
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private FirmwareEvent flashFirmwareEvent=null;
    private DeviceAVR avr=null;
    private int program_size=0;

    public Firmware(DeviceAVR avr){
        this.avr = avr;
        flashFirmwareEvent = new FirmwareEvent(this);
    }

    public void addEventListener (FirmwareEventListener listener) {
        listenerList.add(FirmwareEventListener.class, listener);
    }

    public void removeEventListener (FirmwareEventListener listener) {
        listenerList.remove(FirmwareEventListener.class, listener);
    }

    void fireFlashEvent (FirmwareEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2)
        {
            if (evt instanceof UploadedEvent)
            {
                ((FirmwareEventListener) listeners[i + 1]).completedEvent((UploadedEvent) evt);
            }
            else if(evt instanceof  FirmwareEvent)
            {
                ((FirmwareEventListener) listeners[i + 1]).progressEvent((FirmwareEvent) evt);
            }
        }
    }

    @Override
    public void upload_program(Byte[] raw_intel_hex_array) throws FotaaError
    {
        int fd;
        Memory mem = new Memory(Byte.SIZE * raw_intel_hex_array.length + 1);
        mem.clear();
        PointerByReference inipar = new PointerByReference();
        inipar.setPointer(mem);
        for (int i = 0; i < raw_intel_hex_array.length; i++) {
            inipar.getPointer().setByte(i * Byte.SIZE / 8, raw_intel_hex_array[i]);
        }
        byte c = '\n';
        inipar.getPointer().setByte((raw_intel_hex_array.length + 1) * Byte.SIZE / 8, c);
        program_size = raw_intel_hex_array.length;
        fd = NativeLoader.getINSTANCE_Foa().write_on_the_air_program(avr.getPort(),avr.getTarget(),avr.getTag(),raw_intel_hex_array.length,inipar);
        if(fd < 0)
        {
            throw new FotaaError(""+Constants.messages.get(fd));
        }
    }


    public DeviceAVR getAvr() {
        return avr;
    }

    public void setAvr(DeviceAVR avr) {
        this.avr = avr;
    }

    public int getProgram_size() {
        return program_size;
    }


}
