package eu.powet.FOTAA.events;

import eu.powet.FOTAA.Fota;

import java.util.EventObject;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 10/07/12
 * Time: 14:42
 * To change this template use File | Settings | File Templates.
 */
public class WaitingBLEvent extends FotaEvent {
    public WaitingBLEvent(Fota src) {
        super(src);
    }
}
