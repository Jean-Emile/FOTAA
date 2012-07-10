package eu.powet.FOTAA.api;

import eu.powet.FOTAA.utils.FotaException;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 27/02/12
 * Time: 11:39
 */
public interface IFota {
    public abstract void upload(Byte[] raw_intel_hex_array) throws FotaException;
    public abstract void addEventListener (FotaEventListener listener);
    public abstract void removeEventListener (FotaEventListener listener);
}
