package Domain;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
 * Analiza la página principal Recolecta todos los links directos del mismo
 * dominio Por cada link: extrae URLs de productos Por cada producto: entra a su
 * página individual y extrae nombre, descripción y precio
 */
public class AnalizarProducto extends AnalizadorWeb {

    private ExtraerPrecioProducto extraerPrecioProducto;
    private ArrayList<Producto> productos;
    private String terminoBusqueda;
    private int totalProductosEncontrados;
    private int productosFiltrados;
    private static final int MAX_LINKS_DIRECTOS = 30;
    private static final int MAX_PRODUCTOS_X_PAG = 30;
    private static final int TIMEOUT_MS = 15000;
    private Set<String> urlsGlobalesProcesadas = new HashSet<>();
    private Set<String> urlsProductosEncontradas = new HashSet<>();
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    // Selectores CSS utilizados para localizar información de productos
    // en diferentes tipos de tiendas web.
    private static final String SEL_CONTENEDOR
            = "li.product, .woocommerce-loop-product, .product-type-simple, "
            + ".product-card, .grid-product, .product__card, .product-item, "
            + ".product-item-info, .product-item-details, "
            + ".product-thumb, .product-layout, "
            + "article.product, div.product, div.producto, "
            + "[itemtype*='schema.org/Product']";

    //Selectores de nombre
    private static final String[] SEL_NOMBRE = {
        "[itemprop=name]",
        ".woocommerce-loop-product__title", ".product_title",
        ".product__title", ".product-single__title", ".product-title",
        ".product-item-name", ".product-name",
        "h1.title", "h2.title", "h3.title", "h1", "h2", "h3",
        ".name", ".nombre", "[class*=product-name]", "[class*=product-title]"
    };

    // Selectores de descripción 
    private static final String[] SEL_DESCRIPCION = {
        "[itemprop=description]",
        ".woocommerce-product-details__short-description",
        ".woocommerce-tabs .panel p",
        "#tab-description",
        ".product__description", ".product-single__description",
        ".product-info-main .overview", ".product.attribute.description",
        ".short-description", ".description", ".descripcion",
        ".product-description", "[class*=short-desc]", "[class*=description]",
        "p.description", ".summary p", "p"
    };

    public AnalizarProducto(String url) {
        this(url, "");
    }

    public AnalizarProducto(String url, String terminoBusqueda) {
        super(url);
        this.productos = new ArrayList<>();
        this.terminoBusqueda = terminoBusqueda != null
                ? terminoBusqueda.toLowerCase().trim() : "";
        this.totalProductosEncontrados = 0;
        this.productosFiltrados = 0;
        this.extraerPrecioProducto = new ExtraerPrecioProducto();
    }

    //Getters
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

    //Utilidades generales
    private String userAgentAleatorio() {
        return USER_AGENTS[(int) (Math.random() * USER_AGENTS.length)];
    }

    /**
     * Extrae el dominio principal de una URL para verificar que los enlaces
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
     * Recorre una lista de selectores CSS y devuelve el primer texto
     * encontrado.
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
     * externos, duplicados o que no sean útiles para el análisis.
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

            if (href.isEmpty()
                    || href.startsWith("#")
                    || href.startsWith("mailto:")
                    || href.startsWith("javascript:")
                    || href.startsWith("tel:")) {
                continue;
            }
            if (!href.contains(dominio)) {
                continue;
            }
            if (href.matches(".*\\.(pdf|jpg|jpeg|png|gif|svg|zip|rar)$")) {
                continue;
            }

            if (urlsGlobalesProcesadas.contains(href)) {
                continue;
            }

            if (vistos.contains(href)) {
                continue;
            }
            vistos.add(href);
            // Priorizar links cuya URL ya contiene el término buscado
            if (!terminoBusqueda.isEmpty()) {
                String hrefLower = href.toLowerCase();
                String textoLink = a.text().toLowerCase();

                boolean esRelevante = hrefLower.contains(terminoBusqueda)
                        || textoLink.contains(terminoBusqueda);

                if (esRelevante) {
                    links.add(0, href);
                } else {
                    links.add(href);
                }
            } else {
                links.add(href);
            }
            if (links.size() >= MAX_LINKS_DIRECTOS) {
                break;
            }
        }
        return links;
    }

    /**
     * Extrae las URLs de los productos presentes en una página de listado
     * utilizando selectores HTML o información estructurada JSON-LD.
     */
    private ArrayList<String> extraerURLsDeProductos(Document doc, String urlBase) {
        ArrayList<String> urls = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        String dominio = extraerDominio(urlBase);

        // Primero intentar con contenedores estándar
        Elements contenedores = doc.select(SEL_CONTENEDOR);

        // Si no encontró, intentar con JSON-LD ItemList
        if (contenedores.isEmpty()) {
            for (Element script : doc.select("script[type='application/ld+json']")) {
                String json = script.html();
                if (!json.contains("\"Product\"") && !json.contains("\"ItemList\"")) {
                    continue;
                }
                Matcher m = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
                while (m.find()) {
                    String u = m.group(1);
                    if (!vistas.contains(u) && u.contains(dominio)) {
                        urls.add(u);
                        vistas.add(u);
                    }
                }
            }
        }

        for (Element el : contenedores) {
            String urlProducto = el.select("a[href]").attr("abs:href");
            if (urlProducto.isEmpty()) {
                Element link = el.select("a").first();
                if (link != null) {
                    urlProducto = link.attr("abs:href");
                }
            }
            if (!urlProducto.isEmpty()
                    && !vistas.contains(urlProducto)
                    && urlProducto.contains(dominio)) {
                urls.add(urlProducto);
                vistas.add(urlProducto);
            }
            if (urls.size() >= MAX_PRODUCTOS_X_PAG) {
                break;
            }
        }
        return urls;
    }

    /**
     * Analiza la página individual de un producto para obtener su nombre,
     * descripción y precio, y posteriormente almacenarlo.
     */
    private void analizarPaginaDeProducto(String urlProducto, int id) {
        try {
            if (urlsProductosEncontradas.contains(urlProducto)) {
                return;
            }
            urlsGlobalesProcesadas.add(urlProducto);
            // System.out.println(urlProducto.substring(0, Math.min(65, urlProducto.length())) + "...");

            Document doc = Jsoup.connect(urlProducto)
                    .userAgent(userAgentAleatorio())
                    .referrer(this.url)
                    .timeout(TIMEOUT_MS)
                    .ignoreHttpErrors(true)
                    .get();

            if (doc.connection().response().statusCode() != 200) {
                return;
            }

            //  Nombre
            String nombre = extraerTexto(doc, SEL_NOMBRE);
            if (nombre.isEmpty()) {
                nombre = doc.title();
            }
            if (nombre.isEmpty()) {
                nombre = "Producto sin nombre";
            }

            //Filtrar por término de búsqueda
            if (!terminoBusqueda.isEmpty()
                    && !nombre.toLowerCase().contains(terminoBusqueda)
                    && !doc.text().toLowerCase().contains(terminoBusqueda)) {
                return;
            }

            // Descripción
            String descripcion = extraerTexto(doc, SEL_DESCRIPCION);
            if (descripcion.isEmpty()) {
                descripcion = "Sin descripción";
            }
            if (descripcion.length() > 200) {
                descripcion = descripcion.substring(0, 200) + "...";
            }

            //Precio
            double precio = extraerPrecioProducto.extraerPrecio(doc);

            // Construir descripción final
            String descripcionFinal = "Nombre: " + nombre
                    + " | Descripción: " + descripcion;

            System.out.println(nombre.substring(0, Math.min(40, nombre.length()))
                    + " | Precio: " + (precio > 0 ? +precio : "no encontrado"));

            if (urlsGlobalesProcesadas.contains(urlProducto)) {
                return;
            }
            urlsGlobalesProcesadas.add(urlProducto);
            productos.add(new Producto(id, precio, descripcionFinal, null, urlProducto));
            totalProductosEncontrados++;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Analiza una página de listado y procesa cada uno de los productos
     * encontrados en ella.
     */
    private void analizarPaginaListado(Document doc, String urlPagina) {
        System.out.println("Buscando productos en listado...");
        ArrayList<String> urls = extraerURLsDeProductos(doc, urlPagina);
        System.out.println("Productos en listado: " + urls.size());

        if (urls.isEmpty()) {
            // Puede ser una página de producto individual directamente
            analizarPaginaDirecta(doc, urlPagina);
            return;
        }

        for (String urlProducto : urls) {
            analizarPaginaDeProducto(urlProducto, productos.size() + 1);
            pausaCorta();
        }
    }

    /**
     * Analiza una página que corresponde directamente a un único producto, sin
     * necesidad de recorrer un listado.
     */
    private void analizarPaginaDirecta(Document doc, String urlPagina) {
        String nombre = extraerTexto(doc, SEL_NOMBRE);
        if (nombre.isEmpty()) {
            return;
        }

        if (!terminoBusqueda.isEmpty()
                && !nombre.toLowerCase().contains(terminoBusqueda)
                && !doc.text().toLowerCase().contains(terminoBusqueda)) {
            return;
        }

        String descripcion = extraerTexto(doc, SEL_DESCRIPCION);
        if (descripcion.isEmpty()) {
            descripcion = "Sin descripción";
        }
        if (descripcion.length() > 200) {
            descripcion = descripcion.substring(0, 200) + "...";
        }

        double precio = extraerPrecioProducto.extraerPrecio(doc);

        String descripcionFinal = "Nombre: " + nombre + " | Descripción: " + descripcion;

        System.out.println("[Directo] "
                + nombre.substring(0, Math.min(40, nombre.length()))
                + " | Precio: " + (precio > 0 ? +precio : "no encontrado"));

        productos.add(new Producto(
                productos.size() + 1, precio, descripcionFinal, null, urlPagina));
        totalProductosEncontrados++;
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
     * Ejecuta el proceso completo de análisis del sitio web recorriendo la
     * página principal, los enlaces internos y las páginas de productos.
     */
    @Override
    protected void analizar() {
        if (this.documento == null) {
            System.out.println("No hay conexión establecida.");
            return;
        }
        long inicio = System.currentTimeMillis();
        System.out.println("         ANÁLISIS PROFUNDO DE PRODUCTOS             ");
        System.out.println("URL    : " + url.substring(0, Math.min(50, url.length())));
        System.out.println("Término: " + (terminoBusqueda.isEmpty() ? "todos" : terminoBusqueda));

        //página principal
        System.out.println("\n ESTADÍSTICAS:");
        System.out.println("   Título  : " + this.documento.title());
        System.out.println("   Links   : " + this.documento.select("a[href]").size());
        System.out.println("   Imágenes: " + this.documento.select("img").size());
        System.out.println("   Videos  : " + this.documento.select(
                "video, iframe[src*=youtube], iframe[src*=vimeo], iframe[src*=youtu.be]").size());
        analizarPaginaListado(this.documento, this.url);
        ArrayList<String> links = recolectarLinksDirectos();
        int errores = 0;
        for (int i = 0; i < links.size(); i++) {
            String linkUrl = links.get(i);

            try {
                Document docLink = Jsoup.connect(linkUrl)
                        .userAgent(userAgentAleatorio())
                        .referrer(this.url)
                        .timeout(TIMEOUT_MS)
                        .ignoreHttpErrors(true)
                        .get();

                if (docLink.connection().response().statusCode() == 200) {
                    analizarPaginaListado(docLink, linkUrl);
                } else {
                    errores++;
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                errores++;
            }
        }
        this.productosFiltrados = productos.size();
        long segundos = (System.currentTimeMillis() - inicio) / 1000;
        System.out.println("                  RESUMEN FINAL");
        System.out.printf("Tiempo total     : %d min %d seg%n",
                segundos / 60, segundos % 60);
        System.out.println("Páginas analizadas: " + (links.size() + 1));
        System.out.println("Páginas con error : " + errores);
        System.out.println("Productos totales : " + totalProductosEncontrados);
        System.out.println("Con precio > 0    : "
                + productos.stream().filter(p -> p.getPrecio() > 0).count());
    }

    @Override
    public void run() {
        analizar();
    }
}
