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
public class AnalizadorLinks {
    private Document document;
    private int totalLinks;
    private List<String> listaLinks;

    public AnalizadorLinks(Document document) {
        this.document = document;
        this.listaLinks = new ArrayList<>();
        this.totalLinks = totalLinks;
    }
    
    public void analizar(){
    }
    
    public int getTotalLinks(){
        return totalLinks;
    }

    public List<String> getListaLinks() {
        return listaLinks;
    }

    @Override
    public String toString() {
        return "AnalizadorLinks{" + "document=" + document + ", totalLinks=" + totalLinks + ", listaLinks=" + listaLinks + '}';
    }
    
    
}
