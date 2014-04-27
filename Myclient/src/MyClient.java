/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.Vector;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


/**
 * @author sony
 */
public class MyClient extends MIDlet implements CommandListener,Runnable{
    /*Declaration*/
    private Display display=null;
    int i=1;
    private List deviceList;
    public String deviceName,address;
    private Command Refresh,connect,end,Exit;
    String s=null,url=null,msg,msg1,msg2;
    public  Vector  devicesDiscovered = new Vector();
    final Object inquiryCompletedEvent = new Object();
    Ticker t;
    Thread t1;
    private StreamConnection conn;
    private DataOutputStream dos;
    DataInputStream dis;
     
    
    /*var  */
   
    
    
   
     int flg1=0;
     boolean listening;
    
    
    
     //declaration for second screen 
    public Form ChatForm = new Form("chat window");
    private Command send,exit;
    private TextField textfield;
    private String sendmsg;
    
     
    /*Declaration OVER*/
   
    /*Method to assign the 2nd form*/
    public void getChatForm()
    {
        textfield = new TextField("type text ","",50,TextField.ANY);
         exit = new Command ("Exit", Command.EXIT,1);
         send = new Command ("send",Command.OK,1);
         ChatForm.append(textfield);
         ChatForm.append("---------------------");
         ChatForm.addCommand(exit);
         ChatForm.addCommand(send);
         ChatForm.setCommandListener(this);
  }
    
    /*end of the method*/
    
    public void MyClient()
    {
        
    }

    public void startApp()
    {
        
           /*Set the list to current Display and some initialization*/  
                 deviceList=new List("Device List",List.IMPLICIT);
                  Exit= new Command("Exit",Command.EXIT, 0);
                  connect = new Command("connect",Command.SCREEN, 1);
                  Refresh = new Command("Refresh",Command.SCREEN, 1);
                  end = new Command("End",Command.EXIT, 1);
                  deviceList.addCommand(Exit);
                  deviceList.addCommand(connect);
                  deviceList.addCommand(Refresh);
                  deviceList.setCommandListener(this);
                   t=new Ticker("...***...");
                                  
                   deviceList.setTicker(t);
                   getChatForm();
                   Display.getDisplay(this).setCurrent(deviceList);
                    
                   
                   
                   
                   
                   t1=new Thread(this);
               /*Code to search The devices*/      
                  DiscoveryListener listener = new DiscoveryListener() 
                  {
                           public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) 
                           {
                              // form.append("Device " + btDevice.getBluetoothAddress() + " found");
                               s="Device " + btDevice.getBluetoothAddress() + " found";
                                deviceList.insert(i, s, null);
                                i++;
                                devicesDiscovered.addElement( btDevice );
                                  try 
                                  {
                                   //  form.append("     name " + btDevice.getFriendlyName(false));
                                    s= "name :" + btDevice.getFriendlyName(false);
                                      deviceList.insert(i, s, null);
                                      i++;
                                  }
                                  catch (Exception cantGetDeviceName)
                                        {
                                        }
                            }  

                             public void inquiryCompleted(int discType) 
                             {
                                 //  form.append("Device Inquiry completed!");
                                // s="Device Inquiry Completed";
                               // deviceList.insert(1, s, null);
                                    t.setString(s);
                                    synchronized(inquiryCompletedEvent)
                                    {
                                       inquiryCompletedEvent.notifyAll();
                                    }
                             }

                                    public void serviceSearchCompleted(int transID, int respCode) {}
                                    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) { }
                   };
         
                     synchronized(inquiryCompletedEvent) {
                            boolean started;
                             try 
                               {
                                   started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                                 if (started) 
                                 {
                                      //form.append("wait for device inquiry to complete...");
                                           s="wait for device inquiry to complete...";
   /*Just Change Here*/                  //  s="Device 012345678900 found";
                                           deviceList.insert(0, s, null);
                                           t.setString("Wait For DEVICE inquiry to complete");
                                           
                                     try 
                                     {
                                         inquiryCompletedEvent.wait();
                                     }
                                     catch (InterruptedException ex)
                                     {
                                          ex.printStackTrace();
                                     }
                                        //form.append(devicesDiscovered.size() +  " device(s) found");
                                           s=devicesDiscovered.size() +  " device(s) found";
                                             //deviceList.insert(2, s, null);
                                           t.setString(s);
                                 }
                             }
                             catch (BluetoothStateException ex) 
                              {
                                   ex.printStackTrace();
                              }
            
                      }
                      /*End Of The Code To Search Device*/
                     
                     
                     
                     
                     
                     
         
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable d) 
    {
       /*hndle all events*/
        if(c==connect)
        {
            Display.getDisplay(this).setCurrent(ChatForm);
            String temp=deviceList.getString(deviceList.getSelectedIndex());
            s=temp.substring(7, 19);
            url="btspp://"+ s + ":" + "3" ;
           // ChatForm.append(s);   /*desabled ddress of selected device*/
            address=s;
            initialize();
         //t1.start();
         
          
        }
        if(c==send)
        {
            sendmsg=textfield.getString();
            textfield.setString("");
            this.ChatForm.append("\n"+"ME: "+sendmsg);
            try
            {
                dos.writeUTF(address+sendmsg);
                dos.flush();
             }
            catch(IOException ex)
            {
                ex.printStackTrace();;
            }
           t1.start();         
        }
        if(c==Exit)
        {
            try
            {
                dos.writeUTF("nil");
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
            notifyDestroyed();
        }
         
        
    }
    
    public void initialize()
    {
        try
               {
                   conn=(StreamConnection)Connector.open(url);
                   dos=conn.openDataOutputStream();
                   dis=conn.openDataInputStream();
                  // dos.writeUTF("Connected");
               }
        catch(Exception ex)
        {
            System.out.println("Error for Connection :"+ex);
            ex.printStackTrace();
        }
    }

    public void read()
    {
        
        try
         {
             listening=true;
           //  this.ChatForm.append("The Valu is :" +String.valueOf(listening));
          //   this.ChatForm.append("Client");
             while(listening)
             {
                  
                    msg=dis.readUTF();
                    msg1=msg.substring(8,22);
                    msg2=msg.substring(22);
                    Display.getDisplay(this).setCurrent(ChatForm);
                    if(!msg2.equals("nil") && !msg.equals(null))
                    {
                     //   url1="btspp://"+ msg1 + ":" + "3";
                        setText(msg2);
                        
                        
                     }   
                    else 
                     {
                         listening=false;
                         setText(msg1);
                         setText("Connection Ended"); 
                      }
                   
             }
             
         }
         catch(Exception ex)
         {
             System.out.println("The IO Error :"+ex);
         }
    }
    public void run()
    {
       /*  try{
          // this.ChatForm.append("waiting...");
          // Thread.sleep(10000);
           
       }catch(Exception e)
       {
           
       }*/
             
             
        
        read();
       
    }
    
    
    /*meThod to set The text Whatever received from client*/
    public void setText(String s)
    {
        this.ChatForm.append("\n"+"FRIEND :"+s);
    }
    
}
