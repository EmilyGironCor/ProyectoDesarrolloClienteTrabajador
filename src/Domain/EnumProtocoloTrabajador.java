/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Domain;

import Utility.GestionXML;
import java.util.ArrayList;
import org.jdom.Element;

/**
 * Define las acciones que puede ejecutar el trabajador al recibir solicitudes
 * del servidor y coordina el proceso de análisis de las páginas web.
 *
 * @author saray
 */
public enum EnumProtocoloTrabajador {

    /**
     * Procesa una tarea de análisis recibida desde el servidor y ejecuta los
     * analizadores seleccionados para cada URL asociada.
     */
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

            // Obtener datos de la tarea
            int idTarea = tarea.getIdTarea();
            String descripcion = tarea.getDescripcion();

            // Obtener opciones de análisis
            boolean analizarImagenes = tarea.isAnalizarImagenes();
            boolean analizarVideos = tarea.isAnalizarVideos();
            boolean analizarLinks = tarea.isAnalizarLinks();
            boolean analizarProductos = tarea.isAnalizarProductos();
            boolean analizarServicios = tarea.isAnalizarServicios();

            // Obtener lista de URLs
            ArrayList<String> urls = tarea.getUrls();
            if (urls == null || urls.isEmpty()) {
                // Compatibilidad con versión antigua (URL única)
                String urlUnica = tarea.getURL();
                if (urlUnica != null && !urlUnica.isEmpty()) {
                    urls = new ArrayList<>();
                    urls.add(urlUnica);
                } else {
                    System.out.println("No hay URLs para analizar");
                    return;
                }
            }

            System.out.println("=== ANALIZANDO TAREA ID: " + idTarea + " ===");
            System.out.println("URLs a analizar: " + urls.size());
            System.out.println("Opciones seleccionadas:");
            System.out.println("  - Imágenes: " + analizarImagenes);
            System.out.println("  - Videos: " + analizarVideos);
            System.out.println("  - Enlaces: " + analizarLinks);
            System.out.println("  - Productos: " + analizarProductos);
            System.out.println("  - Servicios: " + analizarServicios);

            // Variables para acumular resultados totales
            int totalLinksGlobal = 0;
            int totalImagenesGlobal = 0;
            int totalVideosGlobal = 0;
            int totalProductosGlobal = 0;
            ArrayList<Producto> todosLosProductos = new ArrayList<>();

            // Analizar cada URL
            for (String url : urls) {
                System.out.println("\n--- Analizando URL: " + url + " ---");

                try {
                    // Crear analizadores según las opciones seleccionadas
                    AnalizadorLinks analizadorLinks = null;
                    AnalizadorImagenes analizadorImagenes = null;
                    AnalizarVideo analizadorVideos = null;
                    AnalizarProducto analizadorProductos = null;
                    AnalizarServicio analizadorServicios = null;

                    // Solo crear y analizar si la opción está activada
                    if (analizarLinks) {
                        analizadorLinks = new AnalizadorLinks(url);
                        analizadorLinks.conectar();
                        analizadorLinks.start();
                    }

                    if (analizarImagenes) {
                        analizadorImagenes = new AnalizadorImagenes(url);
                        analizadorImagenes.conectar();
                        analizadorImagenes.start();
                    }

                    if (analizarVideos) {
                        analizadorVideos = new AnalizarVideo(url);
                        analizadorVideos.conectar();
                        analizadorVideos.start();
                    }

                    if (analizarProductos) {
                        analizadorProductos = new AnalizarProducto(url, descripcion);
                        analizadorProductos.conectar();
                        analizadorProductos.start();
                    }

                    if (analizarServicios) {
                        analizadorServicios = new AnalizarServicio(url, descripcion);
                        analizadorServicios.conectar();
                        analizadorServicios.start();
                    }

                    // Esperar que terminen los hilos creados
                    if (analizadorLinks != null) {
                        analizadorLinks.join();
                        totalLinksGlobal += analizadorLinks.getCantidadDeLinks();
                        System.out.println("  Enlaces encontrados: " + analizadorLinks.getCantidadDeLinks());
                    }

                    if (analizadorImagenes != null) {
                        analizadorImagenes.join();
                        totalImagenesGlobal += analizadorImagenes.getCantidadDeImagenes();
                        System.out.println("  Imágenes encontradas: " + analizadorImagenes.getCantidadDeImagenes());
                    }

                    if (analizadorVideos != null) {
                        analizadorVideos.join();
                        totalVideosGlobal += analizadorVideos.getCantidadDeVideos();
                        System.out.println("  Videos encontrados: " + analizadorVideos.getCantidadDeVideos());
                    }

                    if (analizadorProductos != null) {
                        analizadorProductos.join();
                        int productosEncontrados = analizadorProductos.getCantidadProductos();
                        totalProductosGlobal += productosEncontrados;
                        todosLosProductos.addAll(analizadorProductos.getProductos());
                        System.out.println("  Productos encontrados: " + productosEncontrados);
                    }

                    if (analizadorServicios != null) {
                        analizadorServicios.join();

                        int serviciosEncontrados
                                = analizadorServicios.getCantidadServicios();

                        System.out.println(
                                "  Servicios encontrados: "
                                + serviciosEncontrados);
                    }

                } catch (Exception e) {
                    System.err.println("Error analizando URL " + url + ": " + e.getMessage());
                }
            }

            // Mostrar resumen total
            System.out.println("\n=== RESUMEN TOTAL PARA TAREA " + idTarea + " ===");
            System.out.println("Total URLs analizadas: " + urls.size());
            System.out.println("Total enlaces: " + totalLinksGlobal);
            System.out.println("Total imágenes: " + totalImagenesGlobal);
            System.out.println("Total videos: " + totalVideosGlobal);
            System.out.println("Total productos: " + totalProductosGlobal);

            // Enviar resultado al servidor
            Element eDato = new Element("resultado");
            eDato.addContent(new Element("idTarea").setText(String.valueOf(idTarea)));
            eDato.addContent(new Element("totalEnlaces").setText(String.valueOf(totalLinksGlobal)));
            eDato.addContent(new Element("totalImagenes").setText(String.valueOf(totalImagenesGlobal)));
            eDato.addContent(new Element("totalVideos").setText(String.valueOf(totalVideosGlobal)));
            eDato.addContent(new Element("totalProductos").setText(String.valueOf(totalProductosGlobal)));

            DataProtocolo protocolo = new DataProtocolo("GUARDAR_RESULTADO", eDato);
            String xml = GestionXML.xmlToSTring(protocolo.geteAccion());
            cliente.enviarDatos(xml);

            // Enviar lista de productos encontrados
            if (!todosLosProductos.isEmpty()) {
                Element eListaProductos = new Element("listaProductos");
                eListaProductos.addContent(new Element("idTarea").setText(String.valueOf(idTarea)));

                int idProducto = 1;
                for (Producto p : todosLosProductos) {
                    p.setIdProducto(idProducto++);
                    eListaProductos.addContent(p.toXMLElement());
                }

                DataProtocolo protocoloProductos = new DataProtocolo("GUARDAR_PRODUCTOS", eListaProductos);
                String xmlProductos = GestionXML.xmlToSTring(protocoloProductos.geteAccion());
                cliente.enviarDatos(xmlProductos);
                System.out.println(todosLosProductos.size() + " productos enviados al servidor");
            }
        }
    },
    /**
     * Ejecuta el análisis completo de múltiples URLs utilizando un hilo
     * independiente para cada una de ellas.
     */
    ANALIZAR_URL_COMPLETO {
        @Override
        public void accion(Cliente cliente, Element eDatos) {
            AnalisisTarea tarea = new AnalisisTarea();
            tarea.toObject(eDatos);

            int idTarea = tarea.getIdTarea();
            String terminoBusqueda = tarea.getDescripcion();

            boolean analizarImagenes = tarea.isAnalizarImagenes();
            boolean analizarVideos = tarea.isAnalizarVideos();
            boolean analizarLinks = tarea.isAnalizarLinks();
            boolean analizarProductos = tarea.isAnalizarProductos();
            boolean analizarServicios = tarea.isAnalizarServicios();

            ArrayList<String> urls = tarea.getUrls();
            if (urls == null || urls.isEmpty()) {
                String urlUnica = tarea.getURL();
                if (urlUnica != null && !urlUnica.isEmpty()) {
                    urls = new ArrayList<>();
                    urls.add(urlUnica);
                } else {
                    System.out.println("No hay URLs para analizar");
                    return;
                }
            }

            System.out.println(" ANALIZANDO " + urls.size() + " URL(s) PARA TAREA " + idTarea + " ===");

            // lanzar un hilo por URL simultáneamente
            ArrayList<Thread> hilos = new ArrayList<>();
            for (String url : urls) {
                // Copiar variables para usarlas dentro del hilo (deben ser effectively final)
                final String urlFinal = url;
                final int idTareaFinal = idTarea;
                final String terminoFinal = terminoBusqueda;
                final boolean imgFinal = analizarImagenes;
                final boolean vidFinal = analizarVideos;
                final boolean lnkFinal = analizarLinks;
                final boolean prodFinal = analizarProductos;
                final boolean servFinal = analizarServicios;

                Thread hilo = new Thread(() -> {
                    System.out.println("\nAnalizando: " + urlFinal + " ---");

                    int totalImagenes = 0;
                    int totalVideos = 0;
                    int totalLinks = 0;
                    ArrayList<Producto> productos = new ArrayList<>();

                    try {
                        if (lnkFinal) {
                            AnalizadorLinks analizadorLinks = new AnalizadorLinks(urlFinal);
                            analizadorLinks.conectar();
                            analizadorLinks.start();
                            analizadorLinks.join();
                            totalLinks = analizadorLinks.getCantidadDeLinks();
                            System.out.println("  [" + urlFinal + "] Enlaces: " + totalLinks);
                        }

                        if (imgFinal) {
                            AnalizadorImagenes analizadorImagenes = new AnalizadorImagenes(urlFinal);
                            analizadorImagenes.conectar();
                            analizadorImagenes.start();
                            analizadorImagenes.join();
                            totalImagenes = analizadorImagenes.getCantidadDeImagenes();
                            System.out.println("  [" + urlFinal + "] Imágenes: " + totalImagenes);
                        }

                        if (vidFinal) {
                            AnalizarVideo analizadorVideos = new AnalizarVideo(urlFinal);
                            analizadorVideos.conectar();
                            analizadorVideos.start();
                            analizadorVideos.join();
                            totalVideos = analizadorVideos.getCantidadDeVideos();
                            System.out.println("  [" + urlFinal + "] Videos: " + totalVideos);
                        }

                        if (prodFinal) {
                            AnalizarProducto analizadorProductos = new AnalizarProducto(urlFinal, terminoFinal);
                            analizadorProductos.conectar();
                            analizadorProductos.start();
                            analizadorProductos.join();
                            productos = analizadorProductos.getProductos();
                            System.out.println("  [" + urlFinal + "] Productos: " + productos.size());
                        }

                        ArrayList<Servicio> servicios = new ArrayList<>();
                        if (servFinal) {
                            AnalizarServicio analizadorServicios = new AnalizarServicio(urlFinal, terminoFinal);
                            analizadorServicios.conectar();
                            analizadorServicios.start();
                            analizadorServicios.join();
                            servicios = analizadorServicios.getServicios();
                            System.out.println("  [" + urlFinal + "] Servicios: " + servicios.size());
                        }

                        // Enviar resultado de esta URL al servidor
                        Element eResultado = new Element("resultado");
                        eResultado.addContent(new Element("idTarea").setText(String.valueOf(idTareaFinal)));
                        eResultado.addContent(new Element("url").setText(urlFinal));
                        eResultado.addContent(new Element("totalEnlaces").setText(String.valueOf(totalLinks)));
                        eResultado.addContent(new Element("totalImagenes").setText(String.valueOf(totalImagenes)));
                        eResultado.addContent(new Element("totalVideos").setText(String.valueOf(totalVideos)));
                        eResultado.addContent(new Element("totalProductos").setText(String.valueOf(productos.size())));

                        DataProtocolo dp = new DataProtocolo("GUARDAR_RESULTADO_URL", eResultado);
                        synchronized (cliente) {
                            cliente.enviarDatos(Utility.GestionXML.xmlToSTring(dp.geteAccion()));
                        }

                        if (!productos.isEmpty()) {
                            Element eProductos = new Element("listaProductos");
                            eProductos.addContent(new Element("idTarea").setText(String.valueOf(idTareaFinal)));
                            for (Producto p : productos) {
                                eProductos.addContent(p.toXMLElement());
                            }
                            DataProtocolo dpProductos = new DataProtocolo("GUARDAR_PRODUCTOS", eProductos);
                            synchronized (cliente) {
                                cliente.enviarDatos(Utility.GestionXML.xmlToSTring(dpProductos.geteAccion()));
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("Error analizando URL " + urlFinal + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });

                hilos.add(hilo);
                hilo.start();
                System.out.println("Hilo lanzado para: " + url);
            }

            //esperar que todos terminen
            for (Thread hilo : hilos) {
                try {
                    hilo.join();
                } catch (InterruptedException e) {
                    System.err.println("Hilo interrumpido: " + e.getMessage());
                }
            }
            System.out.println("TAREA " + idTarea + " COMPLETADA ===");

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
    /**
     * Ejecuta simultáneamente todos los analizadores disponibles sobre una
     * misma página web.
     */
    ANALIZAR_TODO {
        @Override
        public void accion(Cliente cliente, Element eDato) {
            String url = eDato.getChild("url").getValue();
            try {
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
    /**
     * Busca productos relacionados con un término específico dentro de una
     * página web y envía los resultados al servidor.
     */
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

                AnalizarProducto analizador = new AnalizarProducto(url, termino);
                analizador.conectar();
                analizador.start();
                analizador.join();

                ArrayList<Producto> productos = analizador.getProductos();

                System.out.println("Worker encontró " + productos.size() + " productos");
                System.out.println("Total en página: " + analizador.getTotalProductosEncontrados());
                System.out.println("Filtrados por '" + termino + "': " + analizador.getProductosFiltrados());

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
