package eu.powet.FOTAA.jna;


import com.sun.jna.Library;
import com.sun.jna.ptr.PointerByReference;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 09:42
 */
public interface FotaaJNA extends Library {
    int write_on_the_air_program(String port_device,int target,int msg, PointerByReference raw_intel_hex_array);
    int register_FlashEvent(FotaaEvent callback);
    void close_flash();
}
