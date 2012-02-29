package eu.powet.FOTAA;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 11:49
 */
public interface FirmwareEventListener extends java.util.EventListener
{
    void progressEvent(FirmwareEvent evt);
    void completedEvent(FirmwareEvent evt);
}
