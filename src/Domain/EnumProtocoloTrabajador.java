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

                Element eBusqueda = eDatos.getChild("busqueda");

                if (eBusqueda == null) {
                    System.out.println("No llego el nodo busqueda");
                    return;
                }

                String url = eBusqueda.getChildText("url");
                String termino = eBusqueda.getChildText("termino");
                String idTarea = eBusqueda.getChildText("idTarea");

                if (url == null || termino == null || idTarea == null) {
                    System.out.println("Faltan datos para buscar productos");
                    return;
                }

                termino = termino.toLowerCase();

                System.out.println("Buscando: " + termino + " en " + url);

                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url).get();

                int cantidadLinks = doc.select("a[href]").size();
                int cantidadImagenes = doc.select("img").size();
                int cantidadVideos = doc.select(
                        "video, iframe[src*=youtube], iframe[src*=vimeo], iframe[src*=youtu], a[href$=.mp4], a[href$=.avi], a[href$=.mov], a[href$=.mkv]"
                ).size();

                ArrayList<Producto> productosEncontrados = new ArrayList<>();

                org.jsoup.select.Elements elementos = doc.select(
                        ".product, .producto, .product-card, li.product, "
                        + ".product-item, .product-item-info, .product-item-details, "
                        + "[class*=product]"
                );

                
                
                int id = 1;
                for (org.jsoup.nodes.Element el : elementos) {

                    String textoOriginal = el.text().trim();
                    if (textoOriginal.length() > 250) {
                        continue;
                    }
                    String texto = textoOriginal.toLowerCase();

                    if (texto.contains(termino)) {
                        String nombre = el.select("h1, h2, h3, .name, .nombre, .title, [class*=name]").text();
                        String descripcion = el.select("p, .description, .descripcion, [class*=desc]").text();
                        String precioTexto = el.select(".price, .precio, [class*=price], [class*=precio]").text();
                        String urlProducto = el.select("a[href]").attr("abs:href");

                        double precio = 0.0;
                        if (precioTexto != null && !precioTexto.isEmpty()) {
                            try {
                                precio = Double.parseDouble(precioTexto.replaceAll("[^0-9.]", ""));
                            } catch (NumberFormatException ex) {
                                precio = 0.0;
                            }
                        }

                        String descripcionFinal = !nombre.isEmpty() ? nombre : descripcion;

                        if (!descripcionFinal.isEmpty()) {
                            Producto p = new Producto(
                                    id,
                                    precio,
                                    descripcionFinal,
                                    null,
                                    urlProducto.isEmpty() ? url : urlProducto
                            );

                            productosEncontrados.add(p);
                            id++;
                        }
                    }
                }

                Element eListaProductos = new Element("listaProductos");
                eListaProductos.addContent(new Element("idTarea").setText(idTarea));

                eListaProductos.addContent(new Element("totalEnlaces").setText(String.valueOf(cantidadLinks)));
                eListaProductos.addContent(new Element("totalImagenes").setText(String.valueOf(cantidadImagenes)));
                eListaProductos.addContent(new Element("totalVideos").setText(String.valueOf(cantidadVideos)));

                for (Producto p : productosEncontrados) {
                    eListaProductos.addContent(p.toXMLElement());
                }

                DataProtocolo dp = new DataProtocolo("GUARDAR_PRODUCTOS", eListaProductos);
                cliente.enviarDatos(Utility.GestionXML.xmlToSTring(dp.geteAccion()));

            } catch (Exception ex) {
                System.err.println("Error en BUSCAR_PRODUCTOS: " + ex.getMessage());
            }
        }
    };

    public abstract void accion(Cliente cliente, Element eDato);
}
