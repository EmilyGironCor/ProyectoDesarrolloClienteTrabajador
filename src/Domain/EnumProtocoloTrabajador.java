/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Domain;

import Utility.GestionXML;
import org.jdom.Element;

/**
 *
 * @author saray
 */
public enum EnumProtocoloTrabajador {
    ANALIZARURL {
        @Override
        public void accion(Cliente cliente, Element eDatos) {
            Element eTarea = eDatos.getChild("tarea");
            if (eTarea == null) {
                System.out.println("No llegó ninguna tarea para analizar");
                return;
            }

            AnalisisTarea tarea = new AnalisisTarea();
            tarea.toObject(eTarea);
            String urlObjetivo = tarea.getURL();
            int idTarea = tarea.getIdTarea(); // ✅ necesitas el id para guardar

            System.out.println("URL recibida para analizar: " + urlObjetivo);

            AnalizadorLinks buscadorDeLinks = new AnalizadorLinks(urlObjetivo);
            AnalizadorImagenes buscadorDeImagenes = new AnalizadorImagenes(urlObjetivo);
            AnalizarVideo buscadorDeVideos = new AnalizarVideo(urlObjetivo);

            try {
                buscadorDeLinks.conectar();
                buscadorDeImagenes.conectar();
                buscadorDeVideos.conectar();

                buscadorDeLinks.start();
                buscadorDeImagenes.start();
                buscadorDeVideos.start();

                buscadorDeLinks.join();
                buscadorDeImagenes.join();
                buscadorDeVideos.join();

                int links = buscadorDeLinks.getCantidadDeLinks();
                int imagenes = buscadorDeImagenes.getCantidadDeImagenes();
                int videos = buscadorDeVideos.getCantidadDeVideos();

                System.out.println("Links: " + links + " | Imágenes: " + imagenes + " | Videos: " + videos);

                // ✅ Construir XML con resultados y enviarlo al servidor
                Element eDato = new Element("resultado");
                eDato.addContent(new Element("idTarea").setText(String.valueOf(idTarea)));
                eDato.addContent(new Element("totalEnlaces").setText(String.valueOf(links)));
                eDato.addContent(new Element("totalImagenes").setText(String.valueOf(imagenes)));
                eDato.addContent(new Element("totalVideos").setText(String.valueOf(videos)));
                eDato.addContent(new Element("totalProductos").setText("0"));

                DataProtocolo protocolo = new DataProtocolo("GUARDAR_RESULTADO", eDato);
                String xml = GestionXML.xmlToSTring(protocolo.geteAccion());
                cliente.enviarDatos(xml); // ✅ solo un String

            } catch (Exception e) {
                System.err.println("Error durante el escaneo: " + e.getMessage());
            }
        }
    },
    /* Procesa la respuesta del servidor confirmando que el análisis fue enviado al trabajador.
     */
    ANALIZAR_PRODUCTOS {

        @Override
        public void accion(Cliente cliente, Element eDato) {
            String url = eDato.getChild("url").getValue();
            try {
                AnalizadorWeb analizador = new AnalizarProducto(url);
                analizador.conectar();
                analizador.start();
            } catch (Exception e) {
                System.err.println("Error en ANALIZAR_PRODUCTOS: " + e.getMessage());
            }
        }
    },
    ANALIZAR_TODO {

        @Override
        public void accion(Cliente cliente, Element eDato) {
            String url = eDato.getChild("url").getValue();
            try {
                // Lanza todos en paralelo, cada uno su hilo
                AnalizadorWeb links = new AnalizadorLinks(url);
                AnalizadorWeb imagenes = new AnalizadorImagenes(url);
                AnalizadorWeb videos = new AnalizarVideo(url);
                AnalizadorWeb productos = new AnalizarProducto(url);

                links.conectar();
                imagenes.conectar();
                videos.conectar();
                productos.conectar();

                links.start();
                imagenes.start();
                videos.start();
                productos.start();

            } catch (Exception e) {
                System.err.println("Error en ANALIZAR_TODO: " + e.getMessage());
            }
        }
    };

    public abstract void accion(Cliente cliente, Element eDato);
}
