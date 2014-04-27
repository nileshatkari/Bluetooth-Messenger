/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import javax.bluetooth.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;

/**
 * @author sony
 */
public class MyServer extends MIDlet implements Runnable,CommandListener{
    /*Declaration*/
    private Display display=null;
    public DataInputStream dis;
    private StreamConnection con;
    private DataOutputStream dos;
    
    /*var*/
    
    public boolean listening = false;
    StreamConnectionNotifier notifier = null ;
    String msg,msg1,msg2,url,url1;
    String sendmessage,address;
    int count=0;
    Form form;
    Ticker t;
    Thread t1;
    
    
    //declaration for second screen 
    public Form ChatForm = new Form("chat window/SERVER");
    private Command send,exit;
    private TextField textfield;
    private String sendmsg;
    /*Declaration OVER*/
    /*Declaration Complete*/
    
    
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
    
    
    
    

    public void startApp() 
    {
        
        form=new Form("Bluetooth device Address");
        display=Display.getDisplay(this);
        display.setCurrent(form);
        this.form.append("\n\nWaiting for Friend to connect for Chat.....");
        count=0;
        t1=new Thread(this);
        t1.start();
        
    }
    
     public void run() 
     {
        try
        {
            
              url="btspp://localhost:" +new UUID( 0x1101 ).toString() +";name=helloService";
              
              notifier=(StreamConnectionNotifier)Connector.open(url);
              
               con=(StreamConnection)notifier.acceptAndOpen();
               form.append("Starting...");
                dis=con.openDataInputStream();
                dos=con.openDataOutputStream();
               listening=true;
              
              getChatForm();
              
              setChat();
              
                     
                                       
        }
        catch(Exception ex)
        {
            System.out.println("Error in connection"+ex);
         //   ex.printStackTrace();
        }
    }
     
     public void setChat()
     {
         try
         {
           //  this.ChatForm.append("I am in servers listening mode");
           //  this.ChatForm.append("The Valu is :" +String.valueOf(listening));
             while(listening)
             {
                 
                 msg=dis.readUTF();
                 msg1=msg.substring(0,12);
                 msg2=msg.substring(12);
                 Display.getDisplay(this).setCurrent(ChatForm);
                 if(!msg2.equals("nil"))
                 {
                        url1="btspp://"+ msg1 + ":" + "3";
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
             ex.printStackTrace();
         }
     }
     
    
     public void setText(String s1)
     {
         this.ChatForm.append("\n"+"FRIEND: "+s1);
     }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable d) {
        if(c==send)
        {
            sendmsg=textfield.getString();
            textfield.setString("");
            this.ChatForm.append("\n"+"ME: "+sendmsg);
            try
            {
                
                dos.writeUTF(url1+sendmsg);
                dos.flush();
                
            }
            catch(IOException ex)
            {
                this.ChatForm.append("The Server Connection Error...");
                System.out.println("The Error :"+ex);
            }
            
             
        }
        
                
    }
    
    
   
}
