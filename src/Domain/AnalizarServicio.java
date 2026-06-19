/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
/**
 *
 * @author saray
 */
public class AnalizarServicio extends AnalizadorWeb{
    private ArrayList<Servicio> servicios;
    private String terminoBusqueda;
    private int totalServiciosEncontrados;
    private int serviciosFiltrados;
 
    public AnalizarServicio(String url) {
        this(url, "");
    }
 
    public AnalizarServicio(String url, String terminoBusqueda) {
        super(url);
        this.servicios = new ArrayList<>();
        this.terminoBusqueda = terminoBusqueda != null
                ? terminoBusqueda.toLowerCase().trim()
                : "";
        this.totalServiciosEncontrados = 0;
        this.serviciosFiltrados = 0;
    }
 
    public ArrayList<Servicio> getServicios() {
        return servicios;
    }
 
    public int getCantidadServicios() {
        return servicios.size();
    }
 
    public int getTotalServiciosEncontrados() {
        return totalServiciosEncontrados;
    }
 
    public int getServiciosFiltrados() {
        return serviciosFiltrados;
    }
 
    private double extraerPrecio(String texto) {
        if (texto == null || texto.isEmpty()) {
            return 0.0;
        }
 
        String[] patrones = {
            "[¢₡][\\s\\u00A0]*[0-9]{1,3}(?:[,\\.][0-9]{3})*(?:[,\\.][0-9]{1,2})?", // ¢ 10,835 o ₡10,000
    "\\$\\s*[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})?",
    "[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})"
        };
 
        for (String patronStr : patrones) {
            Pattern patron = Pattern.compile(patronStr);
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                String precioStr = matcher.group();
                String limpio = precioStr.replaceAll("[^0-9.]", "");
                if (limpio.indexOf('.') != limpio.lastIndexOf('.')) {
                    limpio = limpio.substring(0, limpio.lastIndexOf('.'))
                            + limpio.substring(limpio.lastIndexOf('.')).replace(".", "");
                }
                try {
                    return Double.parseDouble(limpio);
                } catch (NumberFormatException e) {
                    limpio = precioStr.replaceAll("[^0-9,]", "").replace(",", ".");
                    try {
                        return Double.parseDouble(limpio);
                    } catch (NumberFormatException ex) {
                        continue;
                    }
                }
            }
        }
        return 0.0;
    }
 
    @Override
    protected void analizar() {
        if (this.documento == null) {
            System.out.println(" No hay conexión establecida para servicios.");
            return;
        }
 
        System.out.println(" Analizando servicios en: " + url);
        System.out.println(" Término de búsqueda: " + (terminoBusqueda.isEmpty() ? "ninguno" : terminoBusqueda));
 
        System.out.println("\n ESTADÍSTICAS DE LA PÁGINA:");
        System.out.println("   Título: " + this.documento.title());
        System.out.println("   Divs: " + this.documento.select("div").size());
        System.out.println("   Sections: " + this.documento.select("section").size());
        System.out.println("   Articles: " + this.documento.select("article").size());
 
        // Selectores orientados a servicios/paquetes
        String selectoresServicios = ".plan, .paquete, .package, .pricing, .plan-card, "
        + ".service-card, .service-item, .precio-plan, "
        + "[class*=plan], [class*=paquete], [class*=package], [class*=pricing], "
        + "[class*=service], [class*=servicio], "
        + ".offer, .oferta, [class*=offer], [class*=oferta], "
        + "section.services, .tarifa, [class*=tarifa]," 
        + ".product, .producto, .item, article, "
        + "[class*=product], [class*=item], [class*=card], "
        + "div.column, [class*=column], [class*=mcb-column], "
        + ".box, [class*=box], .entry-content p, li";
 
        Elements elementos = this.documento.select(selectoresServicios);
 
        System.out.println("\n Elementos encontrados con selectores: " + elementos.size());
 
        this.totalServiciosEncontrados = elementos.size();
        ArrayList<String> urlsProcesadas = new ArrayList<>();
        int id = 1;
 
        for (Element el : elementos) {
            String textoCompletoOriginal = el.text().trim();
            
            String tagName = el.tagName().toLowerCase();
if (tagName.equals("nav") || tagName.equals("header") || tagName.equals("footer")) {
    continue;
}

// Saltar solo clases muy específicas de menú
String claseEl = el.className().toLowerCase();
if (claseEl.contains("menu-item") || claseEl.contains("navbar-nav") 
        || claseEl.contains("splide__slide")) {
    continue;
}
            
            System.out.println("ELEM [" + el.tagName() + "] [" + el.className() + "] len=" + textoCompletoOriginal.length() + " | " + textoCompletoOriginal.substring(0, Math.min(80, textoCompletoOriginal.length())));
 
            // Saltar elementos con muy poco o demasiado texto
            if (textoCompletoOriginal.length() < 10 || textoCompletoOriginal.length() > 1500) {
                System.out.println("DESCARTADO POR LONGITUD: "
            + textoCompletoOriginal.length() + " caracteres");
                
                continue;
            }
 
            // Filtrar por término de búsqueda
            if (!terminoBusqueda.isEmpty()) {
                if (!textoCompletoOriginal.toLowerCase().contains(terminoBusqueda)) {
                    continue;
                }
            }
 
            // Extraer nombre del servicio
            String nombre = el.select("h1, h2, h3, h4, .name, .nombre, .title, "
                    + "[class*=name], [class*=title], [class*=plan-name]").text().trim();
 
            // Extraer descripción — más importante en servicios que en productos
            String descripcion = el.select("p, li, .description, .descripcion, "
                    + "[class*=desc], [class*=feature], [class*=detail], ul").text().trim();
 
            // Extraer precio
            String selectoresPrecio = ".price, .precio, .plan-price, .package-price, "
                    + "[class*=price], [class*=precio], .monthly, .mensual, "
                    + "span.price, div.price, [class*=amount], [class*=cost]";
 
            String precioTexto = el.select(selectoresPrecio).text();
            double precio = extraerPrecio(precioTexto);
 
            if (precio == 0.0) {
                precioTexto = el.select("[class*=precio], [class*=price], [class*=plan]").text();
                precio = extraerPrecio(precioTexto);
            }
            if (precio == 0.0) {
    precio = extraerPrecio(textoCompletoOriginal);
}
 
            // URL del servicio
            String urlServicio = el.select("a[href]").attr("abs:href");
            if (urlServicio.isEmpty()) {
                Element link = el.select("a").first();
                if (link != null) {
                    urlServicio = link.attr("abs:href");
                }
            }
 
            // Evitar duplicados
            if (!urlServicio.isEmpty() && urlsProcesadas.contains(urlServicio)) {
                continue;
            }
            if (!urlServicio.isEmpty()) {
                urlsProcesadas.add(urlServicio);
            }
 
            if (nombre.isEmpty()) {
                nombre = textoCompletoOriginal.substring(0, Math.min(100, textoCompletoOriginal.length()));
            }
 
            if (descripcion.isEmpty()) {
                descripcion = nombre;
            }
 
            // En servicios la descripción es lo más importante
            String descripcionFinal = "Servicio: " + nombre + " | Descripción: " + descripcion;
 
            if (precio > 0) {
                System.out.println("    Servicio: " + nombre.substring(0, Math.min(40, nombre.length())) + " | Precio: ₡" + precio);
            } else {
                System.out.println("    Servicio: " + nombre.substring(0, Math.min(40, nombre.length())) + " | Sin precio");
            }
 
            Servicio servicio = new Servicio(
                    id,
                    nombre,
                    descripcionFinal,
                    precio,
                    urlServicio.isEmpty() ? this.url : urlServicio
            );
 
            servicios.add(servicio);
            id++;
        }
 
        this.serviciosFiltrados = servicios.size();
 
        System.out.println("\n RESUMEN FINAL SERVICIOS:");
        System.out.println("   Total encontrados: " + totalServiciosEncontrados);
        System.out.println("   Después de filtro: " + serviciosFiltrados);
        System.out.println("   Con precio > 0: " + servicios.stream().filter(s -> s.getPrecio() > 0).count());
        System.out.println("   Guardados: " + servicios.size());
    }
 
    @Override
    public void run() {
        analizar();
    }
}