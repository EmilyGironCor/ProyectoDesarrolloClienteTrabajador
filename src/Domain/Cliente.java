/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import Utility.GestionXML;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 *
 * @author Saray
 */
public class Cliente extends Thread{

    private Socket socket;
    private PrintStream enviar;
    private BufferedReader recibir;

    public Cliente(String ip, int puerto) throws IOException {
        this.socket=new Socket(ip, puerto);
        this.enviar=new PrintStream(this.socket.getOutputStream());
        this.recibir=new BufferedReader(
                new InputStreamReader(this.socket.getInputStream())
        );
    } // constructor
    
    public String leerDatos() throws IOException{
        return this.recibir.readLine();
    } // leerDatos
    
    public void enviarDatos(String mensaje){
        this.enviar.println(mensaje);
    }
    
    
    public void run(){
        do{
            try {
                String xmlString=this.leerDatos();
                Element eAccion=GestionXML.stringToXML(xmlString);
                String accion=eAccion.getAttributeValue("metodo");
                System.out.println(xmlString);
                
                EnumProtocoloTrabajador enumProtocoloTrabajador=
                        EnumProtocoloTrabajador.valueOf(accion);
                enumProtocoloTrabajador.accion(this, eAccion.getChild("datos"));
            } catch (IOException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JDOMException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }while(true);
    } // run
    
} // fin clase
