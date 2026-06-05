/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Domain;

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

        System.out.println("URL recibida para analizar: " + urlObjetivo);

        AnalizadorLinks buscadorDeLinks =
                new AnalizadorLinks(urlObjetivo);

        AnalizadorImagenes buscadorDeImagenes =
                new AnalizadorImagenes(urlObjetivo);

        AnalizarVideo buscadorDeVideos =
                new AnalizarVideo(urlObjetivo);

        try {
            System.out.println("Iniciando análisis modular en: " + urlObjetivo);

            buscadorDeLinks.conectar();
            buscadorDeImagenes.conectar();
            buscadorDeVideos.conectar();

            buscadorDeLinks.start();
            buscadorDeImagenes.start();
            buscadorDeVideos.start();

            buscadorDeLinks.join();
            buscadorDeImagenes.join();
            buscadorDeVideos.join();

            System.out.println("Links encontrados: "
                    + buscadorDeLinks.getCantidadDeLinks());

            System.out.println("Imágenes encontradas: "
                    + buscadorDeImagenes.getCantidadDeImagenes());

            System.out.println("Videos encontrados: "
                    + buscadorDeVideos.getCantidadDeVideos());

        } catch (Exception e) {
            System.err.println("Ocurrió un error durante el escaneo: "
                    + e.getMessage());
        }
    }
},
//    ANALIZAR_IMAGENES {
//        
//        @Override
//        public void accion(Cliente cliente, Element eDato) {
//            String url = eDato.getChild("url").getValue();
//            try {
//                AnalizadorWeb analizador = new AnalizadorImagenes(url);
//                analizador.conectar();
//                analizador.start();
//            } catch (Exception e) {
//                System.err.println("Error en ANALIZAR_IMAGENES: " + e.getMessage());
//            }
//        }
//    },
//    ANALIZAR_VIDEOS {
//        
//        @Override
//        public void accion(Cliente cliente, Element eDato) {
//            String url = eDato.getChild("url").getValue();
//            try {
//                AnalizadorWeb analizador = new AnalizarVideo(url);
//                analizador.conectar();
//                analizador.start();
//            } catch (Exception e) {
//                System.err.println("Error en ANALIZAR_VIDEOS: " + e.getMessage());
//            }
//        }
//    },
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
