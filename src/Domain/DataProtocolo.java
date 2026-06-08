/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import org.jdom.Element;

/**
 *
 * @author saray
 */
public class DataProtocolo {

    private Element eAccion;
    private Element eDatos;

    public DataProtocolo(String accion, Element datos) {

        this.eAccion = new Element("accion");
        this.eAccion.setAttribute("metodo", accion);

        this.eDatos = new Element("datos");

        if (datos != null) {
            this.eDatos.addContent(datos);
        }

        this.eAccion.addContent(this.eDatos);
    }

    public Element geteAccion() {
        return eAccion;
    }

}