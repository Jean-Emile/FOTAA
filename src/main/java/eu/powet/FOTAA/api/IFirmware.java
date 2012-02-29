package eu.powet.FOTAA.api;

import eu.powet.FOTAA.FirmwareEventListener;
import eu.powet.FOTAA.utils.FotaaError;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 27/02/12
 * Time: 11:39
 */
public interface IFirmware {
    public abstract void upload_program(Byte[] raw_intel_hex_array) throws FotaaError;
    public abstract void addEventListener (FirmwareEventListener listener);
    public abstract void removeEventListener (FirmwareEventListener listener);
}
