/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.awt.image.BufferedImage;

/**
 *
 * @author saray
 */
public class Producto {
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
    
    
}
