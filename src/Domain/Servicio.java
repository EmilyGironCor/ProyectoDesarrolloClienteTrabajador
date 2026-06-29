/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import org.jdom.Element;

/**
 * /**
 * Representa un servicio obtenido durante el análisis web y permite convertir
 * su información entre objetos Java y formato XML.
 *
 * @author saray
 */
public class Servicio implements XMLConvertible {

    private int idServicio;
    private String nombre;
    private String descripcion;
    private double precio;
    private String URL;
    private int idTarea;

    public Servicio() {
    }

    public Servicio(int idServicio, String nombre, String descripcion, double precio, String URL) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.URL = URL;
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

    public int getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(int idTarea) {
        this.idTarea = idTarea;
    }

    @Override
    public String toString() {
        return "Servicio{idServicio=" + idServicio + ", nombre=" + nombre
                + ", precio=" + precio + ", URL=" + URL + '}';
    }

    @Override
    public Element toXMLElement() {
        Element eServicio = new Element("servicio");
        eServicio.addContent(new Element("idServicio").setText(String.valueOf(idServicio)));
        eServicio.addContent(new Element("nombre").setText(nombre != null ? nombre : ""));
        eServicio.addContent(new Element("descripcion").setText(descripcion != null ? descripcion : ""));
        eServicio.addContent(new Element("precio").setText(String.valueOf(precio)));
        eServicio.addContent(new Element("URL").setText(URL != null ? URL : ""));
        return eServicio;
    }

    @Override
    public void toObject(Element element) {
        this.idServicio = Integer.parseInt(element.getChildText("idServicio"));
        this.nombre = element.getChildText("nombre");
        this.descripcion = element.getChildText("descripcion");
        this.precio = Double.parseDouble(element.getChildText("precio"));
        this.URL = element.getChildText("URL");
    }
}
