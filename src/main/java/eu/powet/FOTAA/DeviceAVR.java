package eu.powet.FOTAA;

import eu.powet.FOTAA.utils.FotaaError;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 06/02/12
 * Time: 10:04
 */
public class DeviceAVR
{
    private  String port ="";
    private  int target =0;
    private int size=0;
    private String type= "";
    private String tag;
    /**
     * DeviceAVR device firmware
     * @param _port    eg : /dev/ttyUSB0
     * @param avrDevice         eg : ATMEGA328
     * @param tag       eg : NODE0
     */


    public DeviceAVR(String _type,String _tag)
    {
        type = _type;
        try
        {
            tag = _tag;
            if(tag.length()  != 4)
            {
                System.out.println("The name of the node is wrong");
            }
            target = Integer.parseInt(Constants.boards.get(_type).toString());
        }catch (Exception e){
            System.out.println("avr device not available");
        }
    }

    public DeviceAVR(String _port,String _type,String _tag)
    {
        this.port  = _port;
        type = _type;
        try
        {
            tag = _tag;
            if(tag.length()  != 4)
            {
                System.out.println("The name of the node is wrong");
            }
            target = Integer.parseInt(Constants.boards.get(_type).toString());

        }catch (Exception e){
            System.out.println("avr device not available");
        }
    }


    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
