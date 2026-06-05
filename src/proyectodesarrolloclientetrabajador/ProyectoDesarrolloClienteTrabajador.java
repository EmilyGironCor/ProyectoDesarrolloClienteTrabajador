/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectodesarrolloclientetrabajador;

import Domain.Cliente;
import java.io.IOException;

/**
 *
 * @author emily
 */
public class ProyectoDesarrolloClienteTrabajador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Cliente cliente;
        try {
            cliente = new Cliente(
                    "192.168.50.190",
                    5025
            );
            
            cliente.start();
        } catch (IOException ex) {
            System.getLogger(ProyectoDesarrolloClienteTrabajador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
}
