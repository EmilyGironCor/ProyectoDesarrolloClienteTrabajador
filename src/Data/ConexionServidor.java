/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import java.io.DataInputStream;
import java.net.Socket;

/**
 *
 * @author saray
 */
public class ConexionServidor {
    private String ip;
    private int puerto;
    private Socket socket;
    private DataInputStream entrada;
    private DataInputStream salida;
    private boolean conectado;

    public ConexionServidor(String ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
        this.conectado = false;
    }
    
    public void conectar(){
        
    }
    
    public void desconectar(){
        
    }
    
    public void enviarMensaje(String mensaje){
        
    }
    
    public String recibirMensaje(){
        return "";
    }
    
    public boolean isConectado(){
        return conectado;
    }
    
    public Socket getSocket(){
        return socket;
    }
    
}
