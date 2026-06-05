package Domain;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AnalizarVideo extends AnalizadorWeb {

    private int cantidadDeVideos;

   

    public AnalizarVideo(String url) {
        super(url);
    }
 public int getCantidadDeVideos() {
        return cantidadDeVideos;
    }
    @Override
    public void analizar() {
         Elements videosHTML = this.documento.select("video");
Elements iframes = this.documento.select("iframe[src]");
Elements links = this.documento.select("a[href]");

cantidadDeVideos = videosHTML.size();

for (Element iframe : iframes) {
    String urlIframe = iframe.attr("abs:src").toLowerCase();
    if (urlIframe.contains("youtube") || urlIframe.contains("vimeo") || urlIframe.contains("youtu.be")) {
        cantidadDeVideos++;
    }
}

for (Element link : links) {
    String urlLink = link.attr("abs:href").toLowerCase();
    if (urlLink.endsWith(".mp4") || urlLink.endsWith(".avi") ||
        urlLink.endsWith(".mov") || urlLink.endsWith(".mkv")) {
        cantidadDeVideos++;
    }
}

System.out.println("Cantidad de videos encontrados: " + cantidadDeVideos);
    }

    @Override
    public void run() {
        this.analizar();
    }
}
