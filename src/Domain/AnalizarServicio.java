package Domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Analiza páginas web para identificar servicios y extraer información como
 * nombre, descripción, precio y enlace asociado.
 */
public class AnalizarServicio extends AnalizadorWeb {

    private ExtraerPrecioServicios precioServicios;
    private ArrayList<Servicio> servicios;
    private String terminoBusqueda;
    private int totalServiciosEncontrados;
    private int serviciosFiltrados;
    private static final int MAX_LINKS_DIRECTOS = 50;
    private static final int MAX_SERVICIOS_X_PAG = 30;
    private static final int TIMEOUT_MS = 15000;

    // Lista de User-Agent utilizados para simular diferentes navegadores
// al realizar solicitudes HTTP a los sitios web.
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    // Selectores CSS utilizados para localizar los contenedores que
// representan servicios, planes u ofertas en diferentes sitios web.
    private static final String SEL_CONTENEDOR
            = "[itemtype*='schema.org/Service'], [itemtype*='schema.org/Offer'], "
            + // Planes
            ".plan, .plan-card, .pricing-plan, .pricing-card, .pricing-box, .pricing-item, "
            + ".package, .package-card, .tier, .tier-card, "
            + // Servicios genéricos
            ".service, .servicio, .service-card, .service-item, .service-box, "
            + // Ofertas
            ".offer, .oferta, "
            + // Tarifa
            ".tarifa, "
            + // Columnas con precio 
            "div.column, "
            + // Artículos y secciones semánticos
            "article.service, article.plan, section.service, "
            + // Clases que contienen estas palabras
            "[class*=service]:not(script):not(style):not(nav):not(header):not(footer), "
            + "[class*=servicio]:not(script):not(style):not(nav):not(header):not(footer), "
            + "[class*=pricing]:not(script):not(style):not(nav):not(header):not(footer), "
            + "[class*=plan-]:not(script):not(style):not(nav):not(header):not(footer)";

    // Selectores de nombre 
    private static final String[] SEL_NOMBRE = {
        "[itemprop=name]",
        ".service-title", ".service-name", ".plan-name", ".plan-title",
        ".pricing-title", ".package-name", ".tier-name",
        "h1", "h2", "h3", "h4",
        ".title", ".name", ".nombre",
        "[class*=title]", "[class*=name]", "[class*=heading]"
    };

    // Selectores de descripción 
    private static final String[] SEL_DESCRIPCION = {
        "[itemprop=description]",
        ".service-description", ".service-desc", ".service-text",
        ".plan-description", ".plan-desc",
        ".package-description", ".tier-description",
        // Listas de características muy comunes en planes de servicios
        "ul.features", "ul.plan-features", "ul.service-features",
        ".features", ".feature-list", ".benefits",
        ".description", ".descripcion", ".summary",
        "[class*=desc]", "[class*=feature]", "[class*=detail]",
        "p", "ul", "li"
    };

    public AnalizarServicio(String url) {
        this(url, "");
    }

    public AnalizarServicio(String url, String terminoBusqueda) {
        super(url);
        this.servicios = new ArrayList<>();
        this.terminoBusqueda = terminoBusqueda != null
                ? terminoBusqueda.toLowerCase().trim() : "";
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

    /**
     * Selecciona aleatoriamente un User-Agent para realizar las solicitudes
     * HTTP y reducir la posibilidad de ser bloqueado por el sitio web.
     */
    private String userAgentAleatorio() {
        return USER_AGENTS[(int) (Math.random() * USER_AGENTS.length)];
    }

    /**
     * Extrae el dominio principal de una URL para validar que los enlaces
     * pertenezcan al mismo sitio web.
     */
    private String extraerDominio(String urlCompleta) {
        try {
            String sin = urlCompleta.replaceAll("https?://", "").replaceAll("^www\\.", "");
            int slash = sin.indexOf('/');
            return slash > 0 ? sin.substring(0, slash) : sin;
        } catch (Exception e) {
            return urlCompleta;
        }
    }

    /**
     * Recorre una lista de selectores CSS y devuelve el primer texto encontrado
     * dentro del elemento indicado.
     */
    private String extraerTexto(Element contexto, String[] selectores) {
        for (String sel : selectores) {
            try {
                String texto = contexto.select(sel).text().trim();
                if (!texto.isEmpty()) {
                    return texto;
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    /**
     * Obtiene los enlaces internos de la página principal descartando enlaces
     * externos, archivos y enlaces duplicados.
     */
    private ArrayList<String> recolectarLinksDirectos() {
        ArrayList<String> links = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        String dominio = extraerDominio(this.url);

        if (this.documento == null) {
            return links;
        }

        Elements anchors = this.documento.select("a[href]");
        System.out.println("   Links totales en página principal: " + anchors.size());

        for (Element a : anchors) {
            String href = a.attr("abs:href").trim();

            if (href.isEmpty() || href.startsWith("#") || href.startsWith("mailto:")
                    || href.startsWith("javascript:") || href.startsWith("tel:")) {
                continue;
            }
            if (!href.contains(dominio)) {
                continue;
            }
            if (href.matches(".*\\.(pdf|jpg|jpeg|png|gif|svg|zip|rar)$")) {
                continue;
            }
            if (vistos.contains(href)) {
                continue;
            }
            vistos.add(href);

            // Priorizar links que en su URL mencionen servicios, planes, pricing
            String hrefLower = href.toLowerCase();
            boolean prioritario = hrefLower.contains("service") || hrefLower.contains("servicio")
                    || hrefLower.contains("plan") || hrefLower.contains("pricing")
                    || hrefLower.contains("precio") || hrefLower.contains("paquete")
                    || hrefLower.contains("tarifa") || hrefLower.contains("oferta")
                    || (!terminoBusqueda.isEmpty() && hrefLower.contains(terminoBusqueda));

            if (prioritario) {
                links.add(0, href);
            } else {
                links.add(href);
            }

            if (links.size() >= MAX_LINKS_DIRECTOS) {
                break;
            }
        }

        System.out.println("   Links directos válidos: " + links.size());
        return links;
    }

    //Extraer servicios de una página 
    /**
     * Extrae los servicios encontrados en una página utilizando los
     * contenedores HTML definidos para diferentes sitios web.
     */
    private void extraerServiciosDePagina(Document doc, String urlPagina) {
        Elements contenedores = doc.select(SEL_CONTENEDOR);

        System.out.println("   ?Contenedores encontrados: " + contenedores.size());

        // Si no encontró contenedores, intentar con JSON-LD
        if (contenedores.isEmpty()) {
            extraerServiciosDeJsonLD(doc, urlPagina);
            return;
        }

        Set<String> nombresVistos = new HashSet<>();
        int procesados = 0;

        for (Element el : contenedores) {

            if (procesados >= MAX_SERVICIOS_X_PAG) {
                break;
            }

            // Ignorar elementos de navegación, header y footer
            String tag = el.tagName().toLowerCase();
            if (tag.equals("nav") || tag.equals("header") || tag.equals("footer")) {
                continue;
            }

            String clase = el.className().toLowerCase();
            if (clase.contains("menu-item") || clase.contains("navbar")
                    || clase.contains("nav-item") || clase.contains("splide__slide")) {
                continue;
            }

            String textoCompleto = el.text().trim();

            // Sin texto mínimo no es un contenedor útil
            if (textoCompleto.length() < 10) {
                continue;
            }

            String nombre = extraerTexto(el, SEL_NOMBRE);
            if (nombre.isEmpty()) {
                nombre = textoCompleto.substring(0, Math.min(80, textoCompleto.length()));
            }

            // Filtrar por término de búsqueda
            if (!terminoBusqueda.isEmpty()
                    && !textoCompleto.toLowerCase().contains(terminoBusqueda)) {
                continue;
            }

            // Evitar duplicados por nombre
            String nombreKey = nombre.toLowerCase().trim();
            if (nombresVistos.contains(nombreKey)) {
                continue;
            }
            nombresVistos.add(nombreKey);

            // Descripción
            String descripcion = extraerTexto(el, SEL_DESCRIPCION);
            if (descripcion.isEmpty()) {
                descripcion = textoCompleto;
            }
            if (descripcion.length() > 300) {
                descripcion = descripcion.substring(0, 300) + "...";
            }

            //Precio
            double precio = precioServicios.extraerPrecioDeElemento(el);

            // Si no encontró precio en el contenedor,
            // entrar a esa URL a buscarlo 
            String urlServicio = el.select("a[href]").attr("abs:href");
            if (urlServicio.isEmpty()) {
                Element link = el.select("a").first();
                if (link != null) {
                    urlServicio = link.attr("abs:href");
                }
            }
            if (urlServicio.isEmpty()) {
                urlServicio = urlPagina;
            }

            if (precio == 0.0 && !urlServicio.equals(urlPagina)
                    && urlServicio.contains(extraerDominio(urlPagina))) {
                System.out.println("      Buscando precio en: "
                        + urlServicio.substring(0, Math.min(60, urlServicio.length())) + "...");
                try {
                    Document docServicio = Jsoup.connect(urlServicio)
                            .userAgent(userAgentAleatorio())
                            .referrer(this.url)
                            .timeout(TIMEOUT_MS)
                            .ignoreHttpErrors(true)
                            .get();
                    if (docServicio.connection().response().statusCode() == 200) {
                        precio = precioServicios.extraerPrecioDeDocumento(docServicio);
                    }
                    pausaCorta();
                } catch (Exception ignored) {
                }
            }

            System.out.println("      listo" + nombre.substring(0, Math.min(40, nombre.length()))
                    + " | Precio: " + (precio > 0 ? +precio : "sin precio"));

            servicios.add(new Servicio(
                    servicios.size() + 1, nombre, descripcion, precio, urlServicio));
            totalServiciosEncontrados++;
            procesados++;
        }
    }

    /**
     * Extrae la información de los servicios a partir de datos estructurados en
     * formato JSON-LD.
     */
    private void extraerServiciosDeJsonLD(Document doc, String urlPagina) {
        for (Element script : doc.select("script[type='application/ld+json']")) {
            String json = script.html();
            if (!json.contains("\"Service\"") && !json.contains("\"Offer\"")) {
                continue;
            }

            String nombre = extraerCampoJson(json, "name");
            String descripcion = extraerCampoJson(json, "description");
            String precioStr = extraerCampoJson(json, "price");

            if (nombre.isEmpty()) {
                continue;
            }

            if (!terminoBusqueda.isEmpty()
                    && !nombre.toLowerCase().contains(terminoBusqueda)
                    && !descripcion.toLowerCase().contains(terminoBusqueda)) {
                continue;
            }

            double precio = 0.0;
            try {
                precio = Double.parseDouble(precioStr);
            } catch (NumberFormatException e) {
                precio = precioServicios.parsearPrecioDeTexto(precioStr);
            }

            if (descripcion.isEmpty()) {
                descripcion = "Sin descripción";
            }

            System.out.println("       [JSON-LD] "
                    + nombre.substring(0, Math.min(40, nombre.length()))
                    + " | Precio: " + (precio > 0 ? +precio : "sin precio"));

            servicios.add(new Servicio(
                    servicios.size() + 1, nombre, descripcion, precio, urlPagina));
            totalServiciosEncontrados++;
        }
    }

    /**
     * Obtiene el valor de un campo específico dentro de un documento JSON-LD
     * utilizando expresiones regulares.
     */
    private String extraerCampoJson(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1).trim() : "";
    }

    /**
     * Realiza una pausa entre solicitudes para evitar enviar múltiples
     * peticiones consecutivas al servidor.
     */
    private void pausaCorta() {
        try {
            Thread.sleep(500 + (int) (Math.random() * 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ejecuta el proceso completo de análisis del sitio web, recorriendo los
     * enlaces internos y extrayendo la información de los servicios
     * encontrados.
     */
    @Override
    protected void analizar() {
        if (this.documento == null) {
            System.out.println(" No hay conexión establecida.");
            return;
        }
        long inicio = System.currentTimeMillis();
        System.out.println("ANÁLISIS PROFUNDO DE SERVICIOS             ");
        System.out.println("URL    : " + url.substring(0, Math.min(50, url.length())));
        System.out.println("Término: " + (terminoBusqueda.isEmpty() ? "todos" : terminoBusqueda));

        //Estadísticas de la página principal
        System.out.println("\n ESTADÍSTICAS:");
        System.out.println("   Título   : " + this.documento.title());
        System.out.println("   Links    : " + this.documento.select("a[href]").size());
        System.out.println("   Imágenes : " + this.documento.select("img").size());
        System.out.println("   Videos   : " + this.documento.select(
                "video, iframe[src*=youtube], iframe[src*=vimeo], iframe[src*=youtu.be]").size());
        System.out.println("   Sections : " + this.documento.select("section").size());
        System.out.println("   Articles : " + this.documento.select("article").size());

        //links directos
        ArrayList<String> links = recolectarLinksDirectos();
        int errores = 0;

        for (int i = 0; i < links.size(); i++) {
            String linkUrl = links.get(i);
            System.out.println(" [" + (i + 1) + "/" + links.size() + "] "
                    + linkUrl.substring(0, Math.min(70, linkUrl.length())));
            try {
                Document docLink = Jsoup.connect(linkUrl)
                        .userAgent(userAgentAleatorio())
                        .referrer(this.url)
                        .timeout(TIMEOUT_MS)
                        .ignoreHttpErrors(true)
                        .get();

                if (docLink.connection().response().statusCode() == 200) {
                    extraerServiciosDePagina(docLink, linkUrl);
                } else {
                    System.out.println("     Status "
                            + docLink.connection().response().statusCode());
                    errores++;
                }

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
                errores++;
            }
        }

        //Resumen final 
        this.serviciosFiltrados = servicios.size();
        long segundos = (System.currentTimeMillis() - inicio) / 1000;

        System.out.println("                  RESUMEN FINAL                     ");
        System.out.printf("║ Tiempo total      : %d min %d seg%n",
                segundos / 60, segundos % 60);
        System.out.println("║ Páginas analizadas: " + (links.size() + 1));
        System.out.println("║ Servicios totales : " + totalServiciosEncontrados);
        System.out.println("║ Con precio > 0    : "
                + servicios.stream().filter(s -> s.getPrecio() > 0).count());
        System.out.println("║ Sin precio        : "
                + servicios.stream().filter(s -> s.getPrecio() == 0).count());
    }

    @Override
    public void run() {
        analizar();
    }
}
