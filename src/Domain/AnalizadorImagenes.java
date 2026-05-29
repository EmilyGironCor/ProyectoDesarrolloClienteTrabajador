/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;

/**
 *
 * @author saray
 */
public class AnalizadorImagenes {
    private Document document;
    private int totalImagenes;
    private List<String> listaImagenes;

    public AnalizadorImagenes(Document document) {
        this.document = document;
        this.listaImagenes = new ArrayList<>();
    }
    
    public void analizar(){
    }

    public int getTotalImagenes() {
        return totalImagenes;
    }

    public List<String> getListaImagenes() {
        return listaImagenes;
    }

    @Override
    public String toString() {
        return "AnalizadorImagenes{" + "document=" + document + ", totalImagenes=" + totalImagenes + ", listaImagenes=" + listaImagenes + '}';
    }
      
}
