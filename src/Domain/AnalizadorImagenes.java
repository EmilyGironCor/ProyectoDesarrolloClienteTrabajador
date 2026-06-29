/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Analiza una página web para identificar y contabilizar las imágenes
 * presentes en su contenido.
 */
public class AnalizadorImagenes extends AnalizadorWeb {

    private int cantidadDeImagenes;

    public AnalizadorImagenes(String url) {
        super(url);
    }

    public int getCantidadDeImagenes() {
        return cantidadDeImagenes;
    }

    @Override
    public void analizar() {
        if (this.documento == null) {
            System.out.println("Error: No hay conexión establecida para las imágenes.");
            return;
        }

        Elements imagenes = this.documento.select("img");

        cantidadDeImagenes = imagenes.size();

        System.out.println("Cantidad de imágenes encontradas: " + cantidadDeImagenes);

        for (Element img : imagenes) {
            String urlImagen = img.attr("abs:src");
            String textoAlternativo = img.attr("alt");
            System.out.println("Imagen: " + urlImagen + " Texto: " + textoAlternativo);
        }
    }

    @Override
    public void run() {
        this.analizar();
    }
}
