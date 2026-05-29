/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.Date;

/**
 *
 * @author saray
 */
public class Tarea {
    private int idTarea;
    private String URL;
    private String estado;
    private int usuarioCreador;
    private int prioridad;
    private Date fechaDeCreacion;
    private int cantidadHilos;

    public Tarea(int idTarea, String URL, String estado, int usuarioCreador, int prioridad, Date fechaDeCreacion, int cantidadHilos) {
        this.idTarea = idTarea;
        this.URL = URL;
        this.estado = estado;
        this.usuarioCreador = usuarioCreador;
        this.prioridad = prioridad;
        this.fechaDeCreacion = fechaDeCreacion;
        this.cantidadHilos = cantidadHilos;
    }//constructor

    
    
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

    public Date getFechaDeCreacion() {
        return fechaDeCreacion;
    }

    public void setFechaDeCreacion(Date fechaDeCreacion) {
        this.fechaDeCreacion = fechaDeCreacion;
    }

    public int getCantidadHilos() {
        return cantidadHilos;
    }

    public void setCantidadHilos(int cantidadHilos) {
        this.cantidadHilos = cantidadHilos;
    }

    @Override
    public String toString() {
        return "Tarea{" + "idTarea=" + idTarea + ", URL=" + URL + ", estado=" + estado + ", usuarioCreador=" + usuarioCreador + ", prioridad=" + prioridad + ", fechaDeCreacion=" + fechaDeCreacion + ", cantidadHilos=" + cantidadHilos + '}';
    }
    
    
}
