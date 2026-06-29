/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * /**
 * Extrae el precio de un producto utilizando distintos selectores y formatos
 * presentes en páginas web.
 *
 * @author saray
 */
public class ExtraerPrecioProducto {

    /**
     * Selectores CSS utilizados para localizar precios en diferentes
     * estructuras HTML de tiendas web.
     */
    private static final String[] SEL_PRECIO = {
        //
        "[itemprop=price]", "[itemprop=offers] [itemprop=price]",
        ".woocommerce-Price-amount",
        ".price ins .woocommerce-Price-amount",
        ".price > .woocommerce-Price-amount", ".price ins .amount",
        ".price > .amount", ".price__current", ".product__price",
        "[class*=price--sale]", ".price-item--sale",
        ".price-item--regular", ".special-price .price",
        ".price-box .price", ".price-wrapper .price",
        ".price-new", ".current-price", ".sale-price",
        ".offer-price", ".precio-oferta", ".precio-actual",
        ".precio", "span.price", "div.price", ".price"
    };

    /**
     * Extrae el precio de un producto buscando en distintos lugares del
     * documento: atributos HTML, texto visible, etiquetas meta, JSON-LD y texto
     * general.
     */
    public double extraerPrecio(Document doc) {

        // Buscar precios en atributos content de los elementos seleccionados.
        for (String sel : SEL_PRECIO) {
            try {
                for (Element el : doc.select(sel)) {
                    String content = el.attr("content").trim();
                    if (!content.isEmpty()) {
                        double v = parsearNumero(content);
                        if (v > 0) {
                            System.out.println("Precio: " + v);
                            return v;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Buscar precios en el texto visible de los elementos seleccionados
        for (String sel : SEL_PRECIO) {
            try {
                String texto = doc.select(sel).text().trim();
                if (!texto.isEmpty()) {
                    double v = parsearPrecioDeTexto(texto);
                    if (v > 0) {
                        System.out.println("Precio" + v);
                        return v;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Buscar precios definidos en etiquetas meta de la página.
        String metaPrice = doc.select(
                "meta[property='og:price:amount'], "
                + "meta[property='product:price:amount'], "
                + "meta[name='price']"
        ).attr("content").trim();

        if (!metaPrice.isEmpty()) {
            double v = parsearNumero(metaPrice);
            if (v > 0) {
                System.out.println("Precio: " + v);
                return v;
            }
        }
        // Buscar precios dentro de datos estructurados JSON-LD.
        for (Element script : doc.select("script[type='application/ld+json']")) {
            String json = script.html();
            if (!json.contains("\"Product\"") && !json.contains("\"Offer\"")) {
                continue;
            }

            Matcher m = Pattern.compile(
                    "\"price\"\\s*:\\s*\"?([0-9]+(?:[.,][0-9]+)?)\"?"
            ).matcher(json);

            if (m.find()) {
                double v = parsearNumero(m.group(1));
                if (v > 0) {
                    System.out.println("Precio: " + v);
                    return v;
                }
            }
        }

        String textoCompleto = doc.body() != null ? doc.body().text() : "";
        double v = parsearPrecioDeTexto(textoCompleto);
        if (v > 0) {
            System.out.println("Precio: " + v);
            return v;
        }

        return 0.0;
    }

    /**
     * Convierte un texto numérico en un valor double, considerando distintos
     * formatos de separadores de miles y decimales.
     */
    public double parsearNumero(String texto) {
        if (texto == null || texto.isEmpty()) {
            return 0.0;
        }
        // Limpiar caracteres que no son dígitos, punto ni coma
        String limpio = texto.replaceAll("[^0-9.,]", "").trim();
        if (limpio.isEmpty()) {
            return 0.0;
        }

        // Contar puntos y comas para identificar el formato del número.
        long puntos = limpio.chars().filter(c -> c == '.').count();
        long comas = limpio.chars().filter(c -> c == ',').count();

        // Interpretar el número según la combinación de puntos y comas encontrada.
        try {
            if (puntos == 0 && comas == 0) {
                // Número entero puro
                return Double.parseDouble(limpio);
            } else if (puntos == 1 && comas == 0) {
                // Puede ser decimal
                String[] partes = limpio.split("\\.");
                if (partes[partes.length - 1].length() <= 2) {
                    return Double.parseDouble(limpio);
                } else {
                    return Double.parseDouble(limpio.replace(".", ""));
                }
            } else if (comas == 1 && puntos == 0) {
                String[] partes = limpio.split(",");
                if (partes[partes.length - 1].length() <= 2) {
                    return Double.parseDouble(limpio.replace(",", "."));
                } else {
                    return Double.parseDouble(limpio.replace(",", ""));
                }
            } else if (puntos > 1 && comas == 0) {
                return Double.parseDouble(limpio.replace(".", ""));
            } else if (comas > 1 && puntos == 0) {
                return Double.parseDouble(limpio.replace(",", ""));
            } else if (puntos == 1 && comas == 1) {
                // Ambos presentes: el último es el decimal
                int ultimoPunto = limpio.lastIndexOf('.');
                int ultimaComa = limpio.lastIndexOf(',');
                if (ultimoPunto > ultimaComa) {
                    return Double.parseDouble(limpio.replace(",", ""));
                } else {
                    return Double.parseDouble(
                            limpio.replace(".", "").replace(",", "."));
                }
            } else if (puntos > 1 && comas == 1) {
                return Double.parseDouble(
                        limpio.replace(".", "").replace(",", "."));
            } else if (comas > 1 && puntos == 1) {
                return Double.parseDouble(limpio.replace(",", ""));
            }
        } catch (NumberFormatException ignored) {
        }

        return 0.0;
    }

    /**
     * Busca y extrae un posible precio dentro de un texto libre utilizando
     * expresiones regulares.
     */
    public double parsearPrecioDeTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return 0.0;
        }

        // Patrones que reconocen precios con colones, dólares y separadores numéricos.
        String[] patrones = {
            "₡\\s*[0-9]{1,3}(?:\\.[0-9]{3})+(?:,[0-9]{1,2})?",
            "₡\\s*[0-9]{1,3}(?:,[0-9]{3})+(?:\\.[0-9]{1,2})?",
            "₡\\s*[0-9]+(?:[.,][0-9]{1,2})?",
            "\\$\\s*[0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?", "[0-9]{1,3}(?:\\.[0-9]{3}){2,}",
            "[0-9]{1,3}(?:,[0-9]{3}){2,}", "[0-9]+[.,][0-9]{2}(?![0-9])"
        };

        // Probar cada patrón hasta encontrar un precio válido.
        for (String patron : patrones) {
            Matcher m = Pattern.compile(patron).matcher(texto);
            if (m.find()) {
                // Quitar símbolo de moneda y espacios, dejar solo dígitos y separadores
                String precioStr = m.group().replaceAll("[₡$\\s]", "");
                double v = parsearNumero(precioStr);
                if (v > 0) {
                    return v;
                }
            }
        }
        return 0.0;
    }

}
