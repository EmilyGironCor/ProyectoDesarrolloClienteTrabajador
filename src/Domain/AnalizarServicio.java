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
public class AnalizarServicio extends AnalizadorWeb {

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

        // Patrones para diferentes formatos de moneda
        String[] patrones = {
            "₡\\s*[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})?", // Colones ₡10,000.00
            "\\$\\s*[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})?", // Dólares $10,000.00
            "[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})" // Números 10,000.00
        };

        for (String patronStr : patrones) {
            Pattern patron = Pattern.compile(patronStr);
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                String precioStr = matcher.group();
                // Limpiar caracteres no numéricos excepto punto decimal
                String limpio = precioStr.replaceAll("[^0-9.]", "");
                // Si hay múltiples puntos, solo mantener el último (decimales)
                if (limpio.indexOf('.') != limpio.lastIndexOf('.')) {
                    limpio = limpio.substring(0, limpio.lastIndexOf('.'))
                            + limpio.substring(limpio.lastIndexOf('.')).replace(".", "");
                }
                try {
                    return Double.parseDouble(limpio);
                } catch (NumberFormatException e) {
                    // Intentar con coma como decimal
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

        // Estadísticas de la página
       

        // ✅ Selectores para servicios (similar a productos pero orientado a servicios)
        String selectoresServicios = ".service, .servicio, .service-card, .servicio-card, "
                + ".plan, .paquete, .package, .pricing, .plan-card, "
                + ".tarifa, .offer, .oferta, "
                + "[class*=service], [class*=servicio], [class*=plan], [class*=paquete], "
                + "[class*=package], [class*=pricing], [class*=offer], [class*=oferta], "
                + ".especialidad, .specialty, .especialidades, "
                + "[class*=especialidad], [class*=specialty], "
                + ".clinica, .clinic, .medico, .medical, "
                + "[class*=clinica], [class*=clinic], [class*=medico], [class*=medical], "
                + ".item, article, li, "
                + "[class*=item], [class*=card]";

        Elements elementos = this.documento.select(selectoresServicios);

        System.out.println("\n Elementos encontrados con selectores: " + elementos.size());

        this.totalServiciosEncontrados = elementos.size();
        ArrayList<String> urlsProcesadas = new ArrayList<>();
        int id = 1;

        for (Element el : elementos) {
            String textoCompletoOriginal = el.text().trim();

            // Saltar elementos con muy poco texto (menos de 10 caracteres) o demasiado (más de 500)
            if (textoCompletoOriginal.length() < 10 || textoCompletoOriginal.length() > 500) {
                continue;
            }

            //  Filtrar por término de búsqueda (igual que en productos)
            if (!terminoBusqueda.isEmpty()) {
                String textoCompleto = textoCompletoOriginal.toLowerCase();
                if (!textoCompleto.contains(terminoBusqueda)) {
                    continue;
                }
            }

            // Extraer información del servicio
            String nombre = el.select("h1, h2, h3, h4, .name, .nombre, .title, .service-name, .plan-name, [class*=name], [class*=title]").text().trim();
            String descripcion = el.select("p, .description, .descripcion, .detail, .feature, [class*=desc], [class*=detail]").text().trim();

            // Extraer precio con los selectores más comunes
            String selectoresPrecio = ".price, .precio, .plan-price, .package-price, .service-price, "
                    + "[class*=price], [class*=precio], .offer-price, .sale-price, "
                    + "span.price, div.price, .current-price";

            String precioTexto = el.select(selectoresPrecio).text();
            double precio = extraerPrecio(precioTexto);

            // Si no encontró precio, buscar en contenedores específicos
            if (precio == 0.0) {
                precioTexto = el.select("[class*=precio], [class*=price]").text();
                precio = extraerPrecio(precioTexto);
            }

            // Obtener URL del servicio
            String urlServicio = el.select("a[href]").attr("abs:href");
            if (urlServicio.isEmpty()) {
                Element link = el.select("a").first();
                if (link != null) {
                    urlServicio = link.attr("abs:href");
                }
            }

            // Evitar URLs duplicadas
            if (!urlServicio.isEmpty() && urlsProcesadas.contains(urlServicio)) {
                continue;
            }
            if (!urlServicio.isEmpty()) {
                urlsProcesadas.add(urlServicio);
            }

            // Construir nombre y descripción
            if (nombre.isEmpty()) {
                nombre = textoCompletoOriginal.substring(0, Math.min(100, textoCompletoOriginal.length()));
            }

            if (descripcion.isEmpty()) {
                descripcion = nombre;
            }

            String descripcionFinal = "Servicio: " + nombre + " | Descripción: " + descripcion;

            if (precio > 0) {
                System.out.println("    Servicio: " + nombre.substring(0, Math.min(40, nombre.length())) + " | Precio: ₡" + precio);
            }

            // ✅ Crear objeto Servicio (igual que Producto pero con Servicio)
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

       
    }

    @Override
    public void run() {
        analizar();
    }
}