/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import org.w3c.dom.Document;

/**
 *
 * @author saray
 */
public class AnalizadorWeb {
    private String URL;
    private Tarea tarea;
    private Document document;
    private ResultadoAnalisis resultado;
    private boolean conectado;

    public AnalizadorWeb(String URL, Tarea tarea) {
        this.URL = URL;
        this.tarea = tarea;
    }
    
    public ResultadoAnalisis analizar(){
        return resultado;
    }
    
    public void cargarPagina(){
        
    }

    public Document getDocument() {
        return document;
    }

    public ResultadoAnalisis getResultado() {
        return resultado;
    }

    @Override
    public String toString() {
        return "AnalizadorWeb{" + "URL=" + URL + ", tarea=" + tarea + ", document=" + document + ", resultado=" + resultado + ", conectado=" + conectado + '}';
    }
       
}
