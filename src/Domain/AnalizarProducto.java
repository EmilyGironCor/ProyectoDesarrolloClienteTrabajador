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
public class AnalizarProducto extends AnalizadorWeb {

    private ArrayList<Producto> productos;
    private String terminoBusqueda;
    private int totalProductosEncontrados;
    private int productosFiltrados;

    public AnalizarProducto(String url) {
        this(url, "");
    }

    public AnalizarProducto(String url, String terminoBusqueda) {
        super(url);
        this.productos = new ArrayList<>();
        this.terminoBusqueda = terminoBusqueda != null
                ? terminoBusqueda.toLowerCase().trim()
                : "";
        this.totalProductosEncontrados = 0;
        this.productosFiltrados = 0;
    }

    public ArrayList<Producto> getProductos() {
        return productos;
    }

    public int getCantidadProductos() {
        return productos.size();
    }

    public int getTotalProductosEncontrados() {
        return totalProductosEncontrados;
    }

    public int getProductosFiltrados() {
        return productosFiltrados;
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

    private double obtenerPrecioDePagina(String urlProducto) {
        try {
            org.jsoup.nodes.Document docProducto = org.jsoup.Jsoup.connect(urlProducto)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            String selectoresPrecio = ".price, .precio, .product-price, .special-price, .regular-price, "
                    + "[class*=price], [class*=precio], [class*=Price], [class*=Precio], "
                    + ".offer-price, .sale-price, .current-price, [itemprop=price], "
                    + "span[class*=precio], div[class*=precio]";

            String precioTexto = docProducto.select(selectoresPrecio).text();
            return extraerPrecio(precioTexto);

        } catch (Exception ex) {
            System.out.println("No se pudo obtener precio desde producto: " + urlProducto);
            return 0.0;
        }
    }

    @Override
    protected void analizar() {
        if (this.documento == null) {
            System.out.println(" No hay conexión establecida para productos.");
            return;
        }

        System.out.println(" Analizando página: " + url);
        System.out.println(" Término de búsqueda: " + (terminoBusqueda.isEmpty() ? "ninguno" : terminoBusqueda));

        // Estadísticas de la página
        System.out.println("\n ESTADÍSTICAS DE LA PÁGINA:");
        System.out.println("   Título: " + this.documento.title());
        System.out.println("   Divs: " + this.documento.select("div").size());
        System.out.println("   Articles: " + this.documento.select("article").size());
        System.out.println("   Links: " + this.documento.select("a").size());
        System.out.println("   Imágenes: " + this.documento.select("img").size());

        // Selectores para productos
        String selectoresProductos = ".product, .producto, .product-card, .item, article, li.product, "
                + ".product-item, .product-item-info, .product-item-details, "
                + "[class*=product], [class*=item], [class*=card], "
                + ".prod, .producto-item, .product-container";

        Elements elementos = this.documento.select(selectoresProductos);

        System.out.println("\n Elementos encontrados con selectores: " + elementos.size());

        this.totalProductosEncontrados = elementos.size();
        ArrayList<String> urlsProcesadas = new ArrayList<>();
        int id = 1;

        for (Element el : elementos) {
            String textoCompletoOriginal = el.text().trim();

            // Saltar elementos con muy poco texto
            if (textoCompletoOriginal.length() < 10 || textoCompletoOriginal.length() > 500) {
                continue;
            }

            // Filtrar por término de búsqueda
            if (!terminoBusqueda.isEmpty()) {
                String textoCompleto = textoCompletoOriginal.toLowerCase();
                if (!textoCompleto.contains(terminoBusqueda)) {
                    continue;
                }
            }

            // Extraer información
            String nombre = el.select("h1, h2, h3, h4, .name, .nombre, .title, [class*=name], [class*=title]").text().trim();
            String descripcion = el.select("p, .description, .descripcion, [class*=desc]").text().trim();

            // Extraer precio con los selectores más comunes
            String selectoresPrecio = ".price, .precio, .product-price, .special-price, .regular-price, "
                    + "[class*=price], [class*=precio], .offer-price, .sale-price, "
                    + "span.price, div.price, .current-price";

            String precioTexto = el.select(selectoresPrecio).text();
            double precio = extraerPrecio(precioTexto);

            // Si no encontró precio, buscar en contenedores específicos
            if (precio == 0.0) {
                precioTexto = el.select("[class*=precio], [class*=price]").text();
                precio = extraerPrecio(precioTexto);
            }

            // Obtener URL del producto
            String urlProducto = el.select("a[href]").attr("abs:href");
            if (urlProducto.isEmpty()) {
                Element link = el.select("a").first();
                if (link != null) {
                    urlProducto = link.attr("abs:href");
                }
            }

            // Evitar URLs duplicadas
            if (!urlProducto.isEmpty() && urlsProcesadas.contains(urlProducto)) {
                continue;
            }
            if (!urlProducto.isEmpty()) {
                urlsProcesadas.add(urlProducto);
            }

            // Si no tenemos precio aún, intentar obtenerlo de la página del producto
            if (precio == 0.0 && !urlProducto.isEmpty() && !urlProducto.equals(this.url)) {
                System.out.println("   Buscando precio en página de producto: " + urlProducto.substring(0, Math.min(50, urlProducto.length())) + "...");
                precio = obtenerPrecioDePagina(urlProducto);
            }

            // Construir nombre y descripción
            if (nombre.isEmpty()) {
                nombre = textoCompletoOriginal.substring(0, Math.min(100, textoCompletoOriginal.length()));
            }

            if (descripcion.isEmpty()) {
                descripcion = nombre;
            }

            String descripcionFinal = "Nombre: " + nombre + " | Descripción: " + descripcion;

            if (precio > 0) {
                System.out.println("    Producto: " + nombre.substring(0, Math.min(40, nombre.length())) + " | Precio: ₡" + precio);
            }

            Producto producto = new Producto(
                    id,
                    precio,
                    descripcionFinal,
                    null,
                    urlProducto.isEmpty() ? this.url : urlProducto
            );

            productos.add(producto);
            id++;
        }

        this.productosFiltrados = productos.size();

        System.out.println("\n RESUMEN FINAL:");
        System.out.println("   Total productos encontrados: " + totalProductosEncontrados);
        System.out.println("   Productos después de filtro: " + productosFiltrados);
        System.out.println("   Productos con precio > 0: " + productos.stream().filter(p -> p.getPrecio() > 0).count());
        System.out.println("   Productos guardados: " + productos.size());
    }

    @Override
    public void run() {
        analizar();
    }
}
