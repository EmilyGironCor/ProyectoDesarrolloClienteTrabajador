/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.Date;
import org.jdom.Element;

/**
 *
 * @author saray
 */
public class AnalisisTarea implements XMLConvertible {

    private int idTarea;
    private String URL;
    private String estado;
    private int usuarioCreador;
    private String descripcion;
    private int prioridad;

    public AnalisisTarea(int idTarea, String URL, String estado, int usuarioCreador, int prioridad, String descripcion) {
        this.idTarea = idTarea;
        this.URL = URL;
        this.estado = estado;
        this.usuarioCreador = usuarioCreador;
        this.prioridad = prioridad;
        this.descripcion=descripcion;
    }//constructor

    public AnalisisTarea() {
        this.URL = "";
        this.descripcion="";
    }

    //Set y get
    public int getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(int idTarea) {
        this.idTarea = idTarea;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(int usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    @Override
    public String toString() {
        return "Tarea{" + "idTarea=" + idTarea + ", URL=" + URL + ", estado=" + estado + ", usuarioCreador=" + usuarioCreador + ", prioridad=" + prioridad + '}';
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    

    @Override
    public Element toXMLElement() {
        Element eAnalisis = new Element("analisis");
        Element eURL = new Element("url");
        eURL.addContent(this.URL);
        eAnalisis.addContent(eURL);
        Element eDescripcion = new Element("descripcion");
        eDescripcion.addContent(this.descripcion != null ? this.descripcion : "");
        eAnalisis.addContent(eDescripcion);
        return eAnalisis;
        
    }

    @Override
    public void toObject(Element element) {
      
        if (element.getChild("idTarea") != null) {
            this.idTarea = Integer.parseInt(element.getChild("idTarea").getValue());
        }
    
        if (element.getChild("URL") != null) {
            this.URL = element.getChild("URL").getValue();
        } else if (element.getChild("url") != null) {
            this.URL = element.getChild("url").getValue();
        } else {
            System.out.println("No se encontró URL en el XML");
            this.URL = "";
        }
        if (element.getChild("descripcion") != null) {
            this.descripcion = element.getChild("descripcion").getValue();
        }
    }

}
