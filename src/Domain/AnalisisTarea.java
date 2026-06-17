/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.ArrayList;
import org.jdom.Element;
import java.util.List;

/**
 *
 * @author saray
 */
public class AnalisisTarea implements XMLConvertible {

    private int idTarea;
    private String URL;  
    private String estado;
    private int usuarioCreador;
    private String descripcion;
    private int prioridad;

    // NUEVOS CAMPOS
    private ArrayList<String> urls;  // Lista de múltiples URLs
    private boolean analizarImagenes;
    private boolean analizarVideos;
    private boolean analizarLinks;
    private boolean analizarProductos;
    private boolean analizarServicios;

    // Constructores
    public AnalisisTarea(int idTarea, String URL, String estado, int usuarioCreador, int prioridad, String descripcion) {
        this.idTarea = idTarea;
        this.URL = URL;
        this.estado = estado;
        this.usuarioCreador = usuarioCreador;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.urls = new ArrayList<>();
        if (URL != null && !URL.isEmpty()) {
            this.urls.add(URL);
        }
        this.analizarImagenes = true;
        this.analizarVideos = true;
        this.analizarLinks = true;
        this.analizarProductos = false;
        this.analizarServicios = false;
    }

    public AnalisisTarea() {
        this.URL = "";
        this.descripcion = "";
        this.urls = new ArrayList<>();
        this.analizarImagenes = true;
        this.analizarVideos = true;
        this.analizarLinks = true;
        this.analizarProductos = false;
        this.analizarServicios = false;
    }

    // Constructor completo con múltiples URLs y opciones
    public AnalisisTarea(int idTarea, ArrayList<String> urls, String estado,
            int usuarioCreador, int prioridad, String descripcion,
            boolean analizarImagenes, boolean analizarVideos,
            boolean analizarLinks, boolean analizarProductos,
            boolean analizarServicios) {
        this.idTarea = idTarea;
        this.urls = urls != null ? urls : new ArrayList<>();
        this.estado = estado;
        this.usuarioCreador = usuarioCreador;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.analizarImagenes = analizarImagenes;
        this.analizarVideos = analizarVideos;
        this.analizarLinks = analizarLinks;
        this.analizarProductos = analizarProductos;
        this.analizarServicios = analizarServicios;

        if (!this.urls.isEmpty()) {
            this.URL = this.urls.get(0);
        } else {
            this.URL = "";
        }
    }

    // Getters y Setters
    public int getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(int idTarea) {
        this.idTarea = idTarea;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
        if (this.urls == null) {
            this.urls = new ArrayList<>();
        }
        if (!this.urls.contains(URL) && URL != null && !URL.isEmpty()) {
            this.urls.add(0, URL);
        }
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(int usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // NUEVOS GETTERS Y SETTERS
    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
        if (urls != null && !urls.isEmpty()) {
            this.URL = urls.get(0);
        }
    }

    public void addUrl(String url) {
        if (this.urls == null) {
            this.urls = new ArrayList<>();
        }
        this.urls.add(url);
        if (this.URL == null || this.URL.isEmpty()) {
            this.URL = url;
        }
    }

    public boolean isAnalizarImagenes() {
        return analizarImagenes;
    }

    public void setAnalizarImagenes(boolean analizarImagenes) {
        this.analizarImagenes = analizarImagenes;
    }

    public boolean isAnalizarVideos() {
        return analizarVideos;
    }

    public void setAnalizarVideos(boolean analizarVideos) {
        this.analizarVideos = analizarVideos;
    }

    public boolean isAnalizarLinks() {
        return analizarLinks;
    }

    public void setAnalizarLinks(boolean analizarLinks) {
        this.analizarLinks = analizarLinks;
    }

    public boolean isAnalizarProductos() {
        return analizarProductos;
    }

    public void setAnalizarProductos(boolean analizarProductos) {
        this.analizarProductos = analizarProductos;
    }

    public boolean isAnalizarServicios() {
        return analizarServicios;
    }

    public void setAnalizarServicios(boolean analizarServicios) {
        this.analizarServicios = analizarServicios;
    }

    @Override
    public String toString() {
        return "AnalisisTarea{" + "idTarea=" + idTarea + ", urls=" + (urls != null ? urls.size() : 0)
                + ", estado=" + estado + ", usuarioCreador=" + usuarioCreador
                + ", prioridad=" + prioridad + '}';
    }

    @Override
    public Element toXMLElement() {
        Element eAnalisis = new Element("analisis");

        // URLs
        Element eUrls = new Element("urls");
        if (this.urls != null) {
            for (String url : this.urls) {
                if (url != null && !url.isEmpty()) {
                    eUrls.addContent(new Element("url").setText(url));
                }
            }
        } else if (this.URL != null && !this.URL.isEmpty()) {
            eUrls.addContent(new Element("url").setText(this.URL));
        }
        eAnalisis.addContent(eUrls);

        // Descripción
        Element eDescripcion = new Element("descripcion");
        eDescripcion.addContent(this.descripcion != null ? this.descripcion : "");
        eAnalisis.addContent(eDescripcion);

        // Opciones de análisis
        Element eOpciones = new Element("opcionesAnalisis");
        eOpciones.addContent(new Element("analizarImagenes").setText(String.valueOf(analizarImagenes)));
        eOpciones.addContent(new Element("analizarVideos").setText(String.valueOf(analizarVideos)));
        eOpciones.addContent(new Element("analizarLinks").setText(String.valueOf(analizarLinks)));
        eOpciones.addContent(new Element("analizarProductos").setText(String.valueOf(analizarProductos)));
        eOpciones.addContent(new Element("analizarServicios").setText(String.valueOf(analizarServicios)));
        eAnalisis.addContent(eOpciones);

        return eAnalisis;
    }

  @Override
public void toObject(Element element) {
    // Buscar el elemento raíz
    Element root = element.getName().equals("tarea") ? element : element.getChild("tarea");
    if (root == null) {
        root = element;
    }

    // Leer idTarea - con manejo de null
    Element idTareaElem = root.getChild("idTarea");
    if (idTareaElem != null) {
        try {
            this.idTarea = Integer.parseInt(idTareaElem.getValue());
        } catch (NumberFormatException e) {
            this.idTarea = 0;
        }
    }

    // LEER LISTA DE URLs
    Element eUrls = root.getChild("urls");
    if (eUrls != null) {
        this.urls = new ArrayList<>();
        List<Element> listaUrls = eUrls.getChildren("url");
        for (Element eUrl : listaUrls) {
            String urlValue = eUrl.getValue();
            if (urlValue != null && !urlValue.isEmpty()) {
                this.urls.add(urlValue);
            }
        }
        if (!this.urls.isEmpty()) {
            this.URL = this.urls.get(0);
        }
    } else {
        // Compatibilidad con formato antiguo
        String urlValue = null;
        Element urlElem = root.getChild("URL");
        if (urlElem == null) urlElem = root.getChild("url");
        if (urlElem == null) urlElem = element.getChild("URL");
        if (urlElem == null) urlElem = element.getChild("url");
        
        if (urlElem != null) {
            urlValue = urlElem.getValue();
        }

        if (urlValue != null && !urlValue.isEmpty()) {
            this.URL = urlValue;
            this.urls = new ArrayList<>();
            this.urls.add(urlValue);
        } else {
            System.out.println("No se encontró URL en el XML");
            this.URL = "";
            this.urls = new ArrayList<>();
        }
    }

    // Leer descripción
    Element descElem = root.getChild("descripcion");
    if (descElem == null) descElem = element.getChild("descripcion");
    if (descElem != null) {
        this.descripcion = descElem.getValue();
    }

    // LEER OPCIONES DE ANÁLISIS
    Element eOpciones = root.getChild("opcionesAnalisis");
    if (eOpciones != null) {
        Element imgElem = eOpciones.getChild("analizarImagenes");
        if (imgElem != null) this.analizarImagenes = Boolean.parseBoolean(imgElem.getValue());
        
        Element vidElem = eOpciones.getChild("analizarVideos");
        if (vidElem != null) this.analizarVideos = Boolean.parseBoolean(vidElem.getValue());
        
        Element linkElem = eOpciones.getChild("analizarLinks");
        if (linkElem != null) this.analizarLinks = Boolean.parseBoolean(linkElem.getValue());
         
        Element prodElem = eOpciones.getChild("analizarProductos");
        if (prodElem != null) this.analizarProductos = Boolean.parseBoolean(prodElem.getValue());
        
        Element servElem = eOpciones.getChild("analizarServicios");
        if (servElem != null) this.analizarServicios = Boolean.parseBoolean(servElem.getValue());
    }
}
}
