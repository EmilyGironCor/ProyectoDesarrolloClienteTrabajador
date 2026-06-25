/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import org.jdom.Element;

/**
 *
 * @author emily
 */
public class Servicio implements XMLConvertible {

    private int idServicio;
    private int idTarea;
    private String nombre;
    private String descripcion;
    private double precio;
    private String URL;

    public Servicio(int idServicio, String nombre, String descripcion, double precio, String URL) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.URL = URL;
    }

    public Servicio(int idServicio, int idTarea, String nombre, String descripcion, double precio, String URL) {
        this.idServicio = idServicio;
        this.idTarea = idTarea;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.URL = URL;
    }

    public Servicio() {
    }

    public int getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(int idTarea) {
        this.idTarea = idTarea;
    }

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    @Override
    public String toString() {
        return "Servicio{idServicio=" + idServicio + ", idTarea=" + idTarea + ", nombre=" + nombre
                + ", precio=" + precio + ", URL=" + URL + '}';
    }

    @Override
    public Element toXMLElement() {
        Element eServicio = new Element("servicio");
        eServicio.addContent(new Element("idServicio").setText(String.valueOf(idServicio)));
        eServicio.addContent(new Element("idTarea").setText(String.valueOf(idTarea)));  // ← NUEVO
        eServicio.addContent(new Element("nombre").setText(nombre != null ? nombre : ""));
        eServicio.addContent(new Element("descripcion").setText(descripcion != null ? descripcion : ""));
        eServicio.addContent(new Element("precio").setText(String.valueOf(precio)));
        eServicio.addContent(new Element("URL").setText(URL != null ? URL : ""));
        return eServicio;
    }

    @Override
    public void toObject(Element element) {
        String idStr = element.getChildText("idServicio");
        if (idStr != null) {
            this.idServicio = Integer.parseInt(idStr);
        }

        String idTareaStr = element.getChildText("idTarea"); 
        if (idTareaStr != null) {
            this.idTarea = Integer.parseInt(idTareaStr);
        }

        this.nombre = element.getChildText("nombre");
        this.descripcion = element.getChildText("descripcion");

        String precioStr = element.getChildText("precio");
        if (precioStr != null) {
            this.precio = Double.parseDouble(precioStr);
        }

        this.URL = element.getChildText("URL");
        if (this.URL == null) {
            this.URL = element.getChildText("url");
        }
    }
}//fin clase
