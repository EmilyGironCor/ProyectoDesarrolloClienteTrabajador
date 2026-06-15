/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Domain;

import Utility.GestionXML;
import java.util.ArrayList;
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
            int idTarea = tarea.getIdTarea();

            System.out.println("URL recibida para analizar: " + urlObjetivo);

            AnalizadorLinks buscadorDeLinks = new AnalizadorLinks(urlObjetivo);
            AnalizadorImagenes buscadorDeImagenes = new AnalizadorImagenes(urlObjetivo);
            AnalizarVideo buscadorDeVideos = new AnalizarVideo(urlObjetivo);

            String terminoBusqueda = tarea.getDescripcion();

            AnalizarProducto buscadorDeProductos
                    = new AnalizarProducto(urlObjetivo, terminoBusqueda);

            try {
                buscadorDeLinks.conectar();
                buscadorDeImagenes.conectar();
                buscadorDeVideos.conectar();
                buscadorDeProductos.conectar();

                buscadorDeLinks.start();
                buscadorDeImagenes.start();
                buscadorDeVideos.start();
                buscadorDeProductos.start();

                buscadorDeLinks.join();
                buscadorDeImagenes.join();
                buscadorDeVideos.join();
                buscadorDeProductos.join();

                int links = buscadorDeLinks.getCantidadDeLinks();
                int imagenes = buscadorDeImagenes.getCantidadDeImagenes();
                int videos = buscadorDeVideos.getCantidadDeVideos();
                int productos = buscadorDeProductos.getCantidadProductos();

                System.out.println("Links: " + links + " | Imágenes: " + imagenes + " | Videos: " + videos + " | Productos: " + productos);

                Element eDato = new Element("resultado");
                eDato.addContent(new Element("idTarea").setText(String.valueOf(idTarea)));
                eDato.addContent(new Element("totalEnlaces").setText(String.valueOf(links)));
                eDato.addContent(new Element("totalImagenes").setText(String.valueOf(imagenes)));
                eDato.addContent(new Element("totalVideos").setText(String.valueOf(videos)));
                eDato.addContent(new Element("totalProductos").setText(String.valueOf(productos)));

                DataProtocolo protocolo = new DataProtocolo("GUARDAR_RESULTADO", eDato);
                String xml = GestionXML.xmlToSTring(protocolo.geteAccion());
                cliente.enviarDatos(xml);

                //aqui
                Element eListaProductos = new Element("listaProductos");
                eListaProductos.addContent(
                        new Element("idTarea").setText(String.valueOf(idTarea))
                );

                for (Producto p : buscadorDeProductos.getProductos()) {
                    eListaProductos.addContent(p.toXMLElement());
                }

                DataProtocolo protocoloProductos
                        = new DataProtocolo("GUARDAR_PRODUCTOS", eListaProductos);

                String xmlProductos
                        = GestionXML.xmlToSTring(protocoloProductos.geteAccion());

                cliente.enviarDatos(xmlProductos);

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
    },
    BUSCAR_PRODUCTOS {
        @Override
        public void accion(Cliente cliente, Element eDatos) {
            try {
                System.out.println(" BUSCAR_PRODUCTOS recibido");

                Element eBusqueda = eDatos.getChild("busqueda");

                if (eBusqueda == null) {
                    System.out.println("Error: no llegó el nodo busqueda");
                    return;
                }

                String idTarea = eBusqueda.getChildTextTrim("idTarea");
                String url = eBusqueda.getChildTextTrim("url");
                String termino = eBusqueda.getChildTextTrim("termino");

                System.out.println("ID Tarea: " + idTarea);
                System.out.println("URL: " + url);
                System.out.println("Termino: " + termino);

                if (url == null || url.isEmpty()) {
                    System.out.println("Error: la URL llegó vacía al trabajador");
                    return;
                }

                if (termino == null) {
                    termino = "";
                }
                // Analizar productos
                AnalizarProducto analizador = new AnalizarProducto(url, termino);
                analizador.conectar();
                //analizador.analizar();  // Usar analizar() directamente

                analizador.start();
                analizador.join();

                ArrayList<Producto> productos = analizador.getProductos();

                System.out.println("Worker encontró " + productos.size() + " productos");
                System.out.println("Total en página: " + analizador.getTotalProductosEncontrados());
                System.out.println("Filtrados por '" + termino + "': " + analizador.getProductosFiltrados());

                // Enviar resultados al servidor
                Element eProductos = new Element("listaProductos");
                for (Producto p : productos) {
                    eProductos.addContent(p.toXMLElement());
                }

                Element eResultado = new Element("resultado");
                eResultado.addContent(new Element("idTarea").setText(idTarea));
                eResultado.addContent(eProductos);

                DataProtocolo dp = new DataProtocolo("GUARDAR_PRODUCTOS", eResultado);
                cliente.enviarDatos(Utility.GestionXML.xmlToSTring(dp.geteAccion()));

                

            } catch (Exception ex) {
                ex.printStackTrace();
                Element eError = new Element("error").addContent(ex.getMessage());
                DataProtocolo dpError = new DataProtocolo("GUARDAR_PRODUCTOS_ERROR", eError);
                cliente.enviarDatos(Utility.GestionXML.xmlToSTring(dpError.geteAccion()));
            }
        }
    };

    public abstract void accion(Cliente cliente, Element eDato);
}
