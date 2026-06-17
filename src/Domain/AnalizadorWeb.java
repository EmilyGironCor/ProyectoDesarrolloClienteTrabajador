/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;


public abstract class AnalizadorWeb extends Thread{ 
    protected String url; 
    protected Document documento;

    // Constructor
    public AnalizadorWeb(String url) {
        this.url = url;
    }

  
    public void conectar() throws IOException {
        this.documento = Jsoup.connect(this.url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeout(15000)
            .followRedirects(true)
            .get();
    }


    protected abstract void analizar(); 
    public abstract void run();

    // Getters y Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
}