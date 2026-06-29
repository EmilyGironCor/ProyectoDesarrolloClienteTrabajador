/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Analiza una página web para localizar y contabilizar los enlaces disponibles
 * en su contenido.
 */

public class AnalizadorLinks extends AnalizadorWeb {

    private int cantidadDeLinks;

    public AnalizadorLinks(String url) {
        super(url);
    }

    public int getCantidadDeLinks() {
        return cantidadDeLinks;
    }

    public void setCantidadDeLinks(int cantidadDeLinks) {
        this.cantidadDeLinks = cantidadDeLinks;
    }

    @Override
    public void analizar() {
        if (this.documento == null) {
            System.out.println("Error: No hay conexión establecida para los links.");
            return;
        }

        System.out.println("ENLACES");

        Elements links = this.documento.select("a[href]");

        cantidadDeLinks = links.size();

        System.out.println("Cantidad de links encontrados: "
                + cantidadDeLinks);

        for (Element link : links) {
            String texto = link.text().trim();
            String urlDestino = link.attr("abs:href");

            System.out.println("URL: " + urlDestino);
        }

    }

    @Override
    public void run() {
        this.analizar();
    }
}
