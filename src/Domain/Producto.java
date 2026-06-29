/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.awt.image.BufferedImage;
import org.jdom.Element;

/**
 * Representa un producto obtenido durante el análisis web y permite convertir
 * su información entre objetos Java y formato XML.
 *
 * @author saray
 */
public class Producto implements XMLConvertible {

    private int idProducto;
    private double precio;
    private String descripcion;
    private BufferedImage imagen;
    private String URL;

    public Producto(int idProducto, double precio, String descripcion, BufferedImage imagen, String URL) {
        this.idProducto = idProducto;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.URL = URL;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BufferedImage getImagen() {
        return imagen;
    }

    public void setImagen(BufferedImage imagen) {
        this.imagen = imagen;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    @Override
    public String toString() {
        return "Producto{" + "idProducto=" + idProducto + ", precio=" + precio + ", descripcion=" + descripcion + ", imagen=" + imagen + ", URL=" + URL + '}';
    }

    @Override
    public Element toXMLElement() {
        Element eProducto = new Element("producto");

        eProducto.addContent(
                new Element("idProducto")
                        .setText(String.valueOf(idProducto)));

        eProducto.addContent(
                new Element("precio")
                        .setText(String.valueOf(precio)));

        eProducto.addContent(
                new Element("descripcion")
                        .setText(descripcion));

        eProducto.addContent(
                new Element("url")
                        .setText(URL));

        return eProducto;
    }

    @Override
    public void toObject(Element element) {
        this.idProducto = Integer.parseInt(
                element.getChildText("idProducto"));

        this.precio = Double.parseDouble(
                element.getChildText("precio"));

        this.descripcion
                = element.getChildText("descripcion");

        this.URL
                = element.getChildText("url");
    }

}
