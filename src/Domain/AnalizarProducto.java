/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.ArrayList;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

/**
 *
 * @author saray
 */
public class AnalizarProducto extends AnalizadorWeb {

    private ArrayList<Producto> productos;
    private String terminoBusqueda;

    public AnalizarProducto(String url) {
        this(url, "");
    }

    public AnalizarProducto(String url, String terminoBusqueda) {
        super(url);
        this.productos = new ArrayList<>();
        this.terminoBusqueda = terminoBusqueda != null
                ? terminoBusqueda.toLowerCase().trim()
                : "";
    }

    public ArrayList<Producto> getProductos() {
        return productos;

    }

    public int getCantidadProductos() {
        return productos.size();
    }

    @Override
    protected void analizar() {

        if (this.documento == null) {
            System.out.println("No hay conexión establecida para productos.");
            return;
        }

        System.out.println("Buscando productos en: " + url);

        System.out.println("Titulo: " + this.documento.title());
        System.out.println("Cantidad div: " + this.documento.select("div").size());
        System.out.println("Cantidad article: " + this.documento.select("article").size());
        System.out.println("Cantidad .product: " + this.documento.select(".product").size());
        System.out.println("Cantidad .product-item: " + this.documento.select(".product-item").size());
        System.out.println("Cantidad .price: " + this.documento.select(".price").size());

        Elements elementos = this.documento.select(
                ".product, .producto, .product-card, .item, article, li.product, "
                + ".product-item, .product-item-info, .product-item-details, "
                + "[class*=product], [class*=item], [class*=card]"
        );

        System.out.println("Posibles productos encontrados: " + elementos.size());

        int id = 1;
        ArrayList<String> urlsProcesadas = new ArrayList<>();
        
        for (Element el : elementos) {

            String textoCompletoOriginal = el.text().trim();

            
            if (textoCompletoOriginal.length() > 250) {
                continue;
            }

            String textoCompleto = textoCompletoOriginal.toLowerCase();

            if (!terminoBusqueda.isEmpty()
                    && !textoCompleto.contains(terminoBusqueda)) {
                continue;
            }

            String nombre = el.select("h1, h2, h3, .name, .nombre, .title, [class*=name], [class*=title]").text().trim();
            String descripcion = el.select("p, .description, .descripcion, [class*=desc]").text().trim();
            String precioTexto = el.select(
                    ".price, .precio, .product-price, .special-price, .regular-price, "
                    + "[class*=price], [class*=precio], [class*=Price], [class*=Precio], "
                    + "span, div"
            ).text().trim();
            String urlProducto = el.select("a[href]").attr("abs:href");

            if (urlProducto.isEmpty()) {
                urlProducto = this.url;
            }

            if (urlsProcesadas.contains(urlProducto)) {
                continue;
            }

            urlsProcesadas.add(urlProducto);
            
            if (nombre.isEmpty()) {
                nombre = textoCompletoOriginal;
            }

            if (descripcion.isEmpty()) {
                descripcion = nombre;
            }

            double precio = 0.0;

            if (!precioTexto.isEmpty()) {
                try {
                    java.util.regex.Pattern patron = java.util.regex.Pattern.compile("(₡|¢)?\\s*[0-9]{2,3}(\\.?[0-9]{3})+");
                    java.util.regex.Matcher matcher = patron.matcher(precioTexto);

                    if (matcher.find()) {
                        String precioEncontrado = matcher.group();
                        String limpio = precioEncontrado.replaceAll("[^0-9]", "");
                        precio = Double.parseDouble(limpio);
                    }

                } catch (NumberFormatException e) {
                    System.out.println("No se pudo leer precio: " + precioTexto);
                }
            }

            
            if (precio == 0.0 && !urlProducto.isEmpty()) {
                try {
                    org.jsoup.nodes.Document docProducto
                            = org.jsoup.Jsoup.connect(urlProducto).get();

                    String precioPagina = docProducto.select(
                            ".price, .precio, .product-price, .special-price, .regular-price, "
                            + "[class*=price], [class*=precio], [class*=Price], [class*=Precio]"
                    ).text();

                    java.util.regex.Pattern patron
                            = java.util.regex.Pattern.compile("[0-9]{1,3}([.,][0-9]{3})+");

                    java.util.regex.Matcher matcher = patron.matcher(precioPagina);

                    if (matcher.find()) {
                        String limpio = matcher.group().replaceAll("[^0-9]", "");
                        precio = Double.parseDouble(limpio);
                    }

                } catch (Exception ex) {
                    System.out.println("No se pudo obtener precio desde producto: " + urlProducto);
                }
            }

            System.out.println("PRECIO FINAL: " + precio + " | " + nombre);
            
            String descripcionFinal
                    = "Nombre: " + nombre
                    + " | Descripción: " + descripcion;

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

        System.out.println("Productos encontrados: " + productos.size());
    }

    @Override
    public void run() {
        analizar();
    }

}
