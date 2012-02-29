package eu.powet.FOTAA.jna;

import com.sun.jna.Callback;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 11:32
 */
public interface FotaaEvent extends Callback {
    void FlashEvent (int msg);
}
