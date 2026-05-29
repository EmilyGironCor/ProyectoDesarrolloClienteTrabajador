/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

/**
 *
 * @author saray
 */
public class Proceso {
    private int idProceso;
    private int idUsuario;
    private int cantidadHilos;
    private String estado;
    private ResultadoAnalisis resultado;

    public Proceso(int idProceso, int idUsuario, int cantidadHilos, String estado, ResultadoAnalisis resultado) {
        this.idProceso = idProceso;
        this.idUsuario = idUsuario;
        this.cantidadHilos = cantidadHilos;
        this.estado = estado;
        this.resultado = resultado;
    }

    public int getIdProceso() {
        return idProceso;
    }

    public void setIdProceso(int idProceso) {
        this.idProceso = idProceso;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getCantidadHilos() {
        return cantidadHilos;
    }

    public void setCantidadHilos(int cantidadHilos) {
        this.cantidadHilos = cantidadHilos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public ResultadoAnalisis getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoAnalisis resultado) {
        this.resultado = resultado;
    }

    @Override
    public String toString() {
        return "Proceso{" + "idProceso=" + idProceso + ", idUsuario=" + idUsuario + ", cantidadHilos=" + cantidadHilos + ", estado=" + estado + ", resultado=" + resultado + '}';
    }
    
}
