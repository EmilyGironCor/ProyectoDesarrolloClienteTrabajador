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

        // ✅ Patrones mejorados para diferentes formatos
        String[] patrones = {
            "[¢₡]\\s*[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})?",
            "[¢₡]\\s+[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})?",
            "\\$\\s*[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})?",
            "[0-9]{1,3}(?:[\\.\\,][0-9]{3})*(?:[\\.\\,][0-9]{1,2})",
            "[0-9]+(?:\\.[0-9]{2})?"
        };

        for (String patronStr : patrones) {
            Pattern patron = Pattern.compile(patronStr);
            Matcher matcher = patron.matcher(texto);
            if (matcher.find()) {
                String precioStr = matcher.group().trim();
                System.out.println("  🔍 Precio raw encontrado: '" + precioStr + "'");
                
                String limpio = precioStr.replaceAll("[¢₡\\$\\s]", "");
                
                if (limpio.contains(",") && limpio.contains(".")) {
                    if (limpio.lastIndexOf(".") > limpio.lastIndexOf(",")) {
                        limpio = limpio.replace(",", "");
                    } else {
                        limpio = limpio.replace(".", "").replace(",", ".");
                    }
                } else if (limpio.contains(",") && !limpio.contains(".")) {
                    limpio = limpio.replace(",", "");
                } else if (limpio.contains(".")) {
                    if (limpio.endsWith(".")) {
                        limpio = limpio.substring(0, limpio.length() - 1);
                    }
                }
                
                try {
                    double precio = Double.parseDouble(limpio);
                    System.out.println("  ✅ Precio extraído: " + precio);
                    return precio;
                } catch (NumberFormatException e) {
                    System.out.println("  ⚠️ Error parseando: " + limpio);
                    continue;
                }
            }
        }
        
        System.out.println("  ❌ No se encontró precio");
        return 0.0;
    }

    @Override
    protected void analizar() {
        if (this.documento == null) {
            System.out.println("❌ No hay conexión establecida para servicios.");
            return;
        }

        System.out.println("🔍 Analizando servicios en: " + url);
        System.out.println("📝 Término de búsqueda: " + (terminoBusqueda.isEmpty() ? "ninguno" : terminoBusqueda));

        // ✅ Selectores específicos para servicios (incluyendo enlaces)
        String selectoresServicios = 
            // Contenedores de servicios
            ".service, .servicio, .service-card, .servicio-card, "
            + ".plan, .paquete, .package, .pricing, .plan-card, "
            + ".tarifa, .offer, .oferta, "
            + "[class*=service], [class*=servicio], [class*=plan], [class*=paquete], "
            + "[class*=package], [class*=pricing], [class*=offer], [class*=oferta], "
            // Especialidades médicas/clínicas
            + ".especialidad, .specialty, .especialidades, "
            + "[class*=especialidad], [class*=specialty], "
            + ".clinica, .clinic, .medico, .medical, "
            + "[class*=clinica], [class*=clinic], [class*=medico], [class*=medical], "
            // Elementos genéricos
            + ".item, article, li, "
            + "[class*=item], [class*=card]";

        Elements elementos = this.documento.select(selectoresServicios);

        System.out.println("\n📦 Elementos encontrados con selectores: " + elementos.size());

        this.totalServiciosEncontrados = elementos.size();
        ArrayList<String> urlsProcesadas = new ArrayList<>();
        int id = 1;

        for (Element el : elementos) {
            String textoCompletoOriginal = el.text().trim();

            // Saltar elementos con muy poco texto (menos de 10 caracteres) o demasiado (más de 500)
            if (textoCompletoOriginal.length() < 10 || textoCompletoOriginal.length() > 500) {
                continue;
            }

            // Filtrar por término de búsqueda (igual que en productos)
            if (!terminoBusqueda.isEmpty()) {
                String textoCompleto = textoCompletoOriginal.toLowerCase();
                if (!textoCompleto.contains(terminoBusqueda)) {
                    continue;
                }
            }

            // Extraer información del servicio
            String nombre = el.select("h1, h2, h3, h4, .name, .nombre, .title, .service-name, .plan-name, [class*=name], [class*=title]").text().trim();
            String descripcion = el.select("p, .description, .descripcion, .detail, .feature, [class*=desc], [class*=detail]").text().trim();

            // ✅ EXTRAER PRECIO - Intentar diferentes formas para servicios
            double precio = 0.0;
            
            // 1. Buscar en selectores de precio específicos
            String selectoresPrecio = ".price, .precio, .plan-price, .package-price, .service-price, "
                    + "[class*=price], [class*=precio], .offer-price, .sale-price, "
                    + "span.price, div.price, .current-price, "
                    + ".amount, .cost, .value, .precio-final, .precio-ahora";

            String precioTexto = el.select(selectoresPrecio).text();
            System.out.println("  📝 Texto de precio encontrado: '" + precioTexto + "'");
            precio = extraerPrecio(precioTexto);

            // 2. Si no hay precio, buscar en contenedores específicos
            if (precio == 0.0) {
                precioTexto = el.select("[class*=precio], [class*=price]").text();
                precio = extraerPrecio(precioTexto);
            }

            // 3. Si aún no hay precio, buscar en todo el texto del elemento
            if (precio == 0.0) {
                System.out.println("  🔍 Buscando precio en todo el texto del elemento");
                precio = extraerPrecio(textoCompletoOriginal);
            }

            // 4. Para servicios, a veces el precio está en el texto de un enlace
            if (precio == 0.0) {
                String textoEnlaces = el.select("a").text();
                precio = extraerPrecio(textoEnlaces);
            }

            // Obtener URL del servicio
            String urlServicio = el.select("a[href]").attr("abs:href");
            if (urlServicio.isEmpty()) {
                Element link = el.select("a").first();
                if (link != null) {
                    urlServicio = link.attr("abs:href");
                }
            }

            // Si no hay URL, usar la URL base
            if (urlServicio.isEmpty()) {
                urlServicio = this.url;
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
                System.out.println("    ✅ Servicio con precio: " + nombre.substring(0, Math.min(40, nombre.length())) + " | Precio: ₡" + precio);
            } else {
                System.out.println("    📋 Servicio: " + nombre.substring(0, Math.min(40, nombre.length())) + " | Sin precio");
            }

            // ✅ Crear objeto Servicio
            Servicio servicio = new Servicio(
                    id,
                    nombre,
                    descripcionFinal,
                    precio,
                    urlServicio
            );

            servicios.add(servicio);
            id++;
        }

        this.serviciosFiltrados = servicios.size();

        System.out.println("\n📊 RESUMEN FINAL SERVICIOS:");
        System.out.println("   Total servicios encontrados: " + totalServiciosEncontrados);
        System.out.println("   Servicios después de filtro: " + serviciosFiltrados);
        System.out.println("   Servicios con precio > 0: " + servicios.stream().filter(s -> s.getPrecio() > 0).count());
        System.out.println("   Servicios guardados: " + servicios.size());
    }

    @Override
    public void run() {
        analizar();
    }
}