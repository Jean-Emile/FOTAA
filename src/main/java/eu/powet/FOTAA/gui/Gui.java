package eu.powet.FOTAA.gui;

import eu.powet.FOTAA.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 29/02/12
 * Time: 09:39
 */
public class Gui extends JFrame {


    private JButton btflash=null;

        private   JTextArea logs=null;

    private void generateFrame()
    {
        setTitle("Flash BOOTLOADER");
        setSize(200, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel lb_ProgrammerType = new JLabel("Programmer Type");
        JLabel lb_AVRdevice = new JLabel("AVR device ");
        JPanel panel = new JPanel(new GridLayout(8, 0));
        btflash = new JButton();
        logs = new JTextArea();
        JScrollPane zoneScrolable = new JScrollPane(logs);
        zoneScrolable.setAutoscrolls(true);
        logs.setAutoscrolls(true);
        btflash.setText("Burn Bootloader");
        JComboBox jlist_devices = new JComboBox(Constants.avrDevices);
        JComboBox jlist_programmerType = new JComboBox(Constants.programmerType);
        panel.add(lb_AVRdevice);
        panel.add(jlist_devices);
        panel.add(lb_ProgrammerType);
        panel.add(jlist_programmerType);
        panel.add(btflash);
        panel.add(zoneScrolable);
        add(panel);
        setVisible(true);
      btflash.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            //    setTarget("usbtiny","m328p");
              //  burnBootloader();
                btflash.setEnabled(false);
            }
        });
    }

    /*
           if(logs !=null){
                                logs.setText(incommingMessage.toString());
                                logs.setCaretPosition(logs.getDocument().getLength());
                            }
     */
}
