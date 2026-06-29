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
 * Extrae precios de servicios,datos estructurados y patrones de texto.
 *
 * @author saray
 */
public class ExtraerPrecioServicios {

    /**
     * Selectores CSS utilizados para localizar precios en servicios, planes,
     * paquetes o tarifas dentro de una página web.
     */
    private static final String[] SEL_PRECIO = {
        // Schema.org más confiable
        "[itemprop=price]",
        "[itemprop=offers] [itemprop=price]",
        // Específicos de pricing de servicios
        ".price-amount", ".plan-price", ".pricing-price",
        ".service-price", ".monthly-price", ".annual-price",
        ".per-month", ".per-year", ".per-hour",
        ".woocommerce-Price-amount",
        ".price ins .amount",
        ".price > .amount",
        ".price__current", ".product__price",
        ".current-price", ".sale-price", ".offer-price",
        ".precio", ".precio-servicio", ".precio-plan",
        "span.price", "div.price", ".price",
        ".price-from", ".starting-price",
        "[class*=price]", "[class*=precio]",
        "[class*=amount]", "[class*=cost]",
        ".monthly", ".mensual"
    };

    /**
     * Extrae precio de un ELEMENTO contenedor de servicio.
     */
    public double extraerPrecioDeElemento(Element el) {

        for (String sel : SEL_PRECIO) {
            try {
                for (Element precioEl : el.select(sel)) {
                    String content = precioEl.attr("content").trim();
                    if (!content.isEmpty()) {
                        double v = parsearNumero(content);
                        if (v > 0) {
                            return v;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (String sel : SEL_PRECIO) {
            try {
                String texto = el.select(sel).text().trim();
                if (!texto.isEmpty()) {
                    double v = parsearPrecioDeTexto(texto);
                    if (v > 0) {
                        return v;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (Element script : el.select("script[type='application/ld+json']")) {
            String json = script.html();
            if (!json.contains("\"Service\"") && !json.contains("\"Offer\"")
                    && !json.contains("\"Product\"")) {
                continue;
            }
            Matcher m = Pattern.compile(
                    "\"price\"\\s*:\\s*\"?([0-9]+(?:[.,][0-9]+)?)\"?"
            ).matcher(json);
            if (m.find()) {
                double v = parsearNumero(m.group(1));
                if (v > 0) {
                    return v;
                }
            }
        }

        return parsearPrecioDeTexto(el.text());
    }

    /**
     * Extrae precio de una página individual de servicio.
     */
    public double extraerPrecioDeDocumento(Document doc) {

        // Buscar el precio en el texto visible del contenedor del servicio.
        for (String sel : SEL_PRECIO) {
            try {
                for (Element el : doc.select(sel)) {
                    String content = el.attr("content").trim();
                    if (!content.isEmpty()) {
                        double v = parsearNumero(content);
                        if (v > 0) {
                            return v;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        for (String sel : SEL_PRECIO) {
            try {
                String texto = doc.select(sel).text().trim();
                if (!texto.isEmpty()) {
                    double v = parsearPrecioDeTexto(texto);
                    if (v > 0) {
                        return v;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String metaPrice = doc.select(
                "meta[property='og:price:amount'], "
                + "meta[property='product:price:amount'], "
                + "meta[name='price']"
        ).attr("content").trim();
        if (!metaPrice.isEmpty()) {
            double v = parsearNumero(metaPrice);
            if (v > 0) {
                return v;
            }
        }

        for (Element script : doc.select("script[type='application/ld+json']")) {
            String json = script.html();
            if (!json.contains("\"Service\"") && !json.contains("\"Offer\"")
                    && !json.contains("\"Product\"")) {
                continue;
            }
            Matcher m = Pattern.compile(
                    "\"price\"\\s*:\\s*\"?([0-9]+(?:[.,][0-9]+)?)\"?"
            ).matcher(json);
            if (m.find()) {
                double v = parsearNumero(m.group(1));
                if (v > 0) {
                    return v;
                }
            }
        }

        // Nivel 5: Regex texto completo
        return parsearPrecioDeTexto(doc.body() != null ? doc.body().text() : "");
    }

    /**
     * Convierte string numérico a double.
     */
    public double parsearNumero(String texto) {
        if (texto == null || texto.isEmpty()) {
            return 0.0;
        }
        String limpio = texto.replaceAll("[^0-9.,]", "").trim();
        if (limpio.isEmpty()) {
            return 0.0;
        }

        long puntos = limpio.chars().filter(c -> c == '.').count();
        long comas = limpio.chars().filter(c -> c == ',').count();

        try {
            if (puntos == 0 && comas == 0) {
                return Double.parseDouble(limpio);
            } else if (puntos == 1 && comas == 0) {
                String[] p = limpio.split("\\.");
                return p[p.length - 1].length() <= 2
                        ? Double.parseDouble(limpio)
                        : Double.parseDouble(limpio.replace(".", ""));
            } else if (comas == 1 && puntos == 0) {
                String[] p = limpio.split(",");
                return p[p.length - 1].length() <= 2
                        ? Double.parseDouble(limpio.replace(",", "."))
                        : Double.parseDouble(limpio.replace(",", ""));
            } else if (puntos > 1 && comas == 0) {
                return Double.parseDouble(limpio.replace(".", ""));
            } else if (comas > 1 && puntos == 0) {
                return Double.parseDouble(limpio.replace(",", ""));
            } else if (puntos == 1 && comas == 1) {
                return limpio.lastIndexOf('.') > limpio.lastIndexOf(',')
                        ? Double.parseDouble(limpio.replace(",", ""))
                        : Double.parseDouble(limpio.replace(".", "").replace(",", "."));
            } else if (puntos > 1 && comas == 1) {
                return Double.parseDouble(limpio.replace(".", "").replace(",", "."));
            } else if (comas > 1 && puntos == 1) {
                return Double.parseDouble(limpio.replace(",", ""));
            }
        } catch (NumberFormatException ignored) {
        }
        return 0.0;
    }

    /**
     * Busca patrones de precio en texto libre.
     */
    public double parsearPrecioDeTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return 0.0;
        }

        // Patrones para reconocer precios en colones, dólares y formatos numéricos.
        String[] patrones = {
            "[¢₡][\\s\\u00A0]*[0-9]{1,3}(?:\\.[0-9]{3})+(?:,[0-9]{1,2})?", // ₡1.250.000
            "[¢₡][\\s\\u00A0]*[0-9]{1,3}(?:,[0-9]{3})+(?:\\.[0-9]{1,2})?", // ₡1,250,000
            "[¢₡][\\s\\u00A0]*[0-9]+(?:[.,][0-9]{1,2})?", // ₡500 o ₡500.00
            "\\$\\s*[0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?", // $1,000.00
            "[0-9]{1,3}(?:\\.[0-9]{3}){2,}", // 1.250.000
            "[0-9]{1,3}(?:,[0-9]{3}){2,}", // 1,250,000
            "[0-9]+[.,][0-9]{2}(?![0-9])" // 59.99
        };

        for (String patron : patrones) {
            Matcher m = Pattern.compile(patron).matcher(texto);
            if (m.find()) {
                String precioStr = m.group().replaceAll("[¢₡$\\s\\u00A0]", "");
                double v = parsearNumero(precioStr);
                if (v > 0) {
                    return v;
                }
            }
        }
        return 0.0;
    }
}
