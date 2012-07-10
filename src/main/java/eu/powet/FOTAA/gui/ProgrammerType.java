package eu.powet.FOTAA.gui;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 29/02/12
 * Time: 09:30
 */
public class ProgrammerType {
    private  String  ProgrammerType;
    private String port;

    public ProgrammerType(String programmerType){
        this.ProgrammerType =  programmerType;
    }

    public String getProgrammerType() {
        return ProgrammerType;
    }

    public void setProgrammerType(String programmerType) {
        ProgrammerType = programmerType;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
