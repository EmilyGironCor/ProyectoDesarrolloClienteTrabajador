/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Domain;

import org.jdom.Element;

/**
 * Define los métodos necesarios para convertir objetos del sistema entre su
 * representación en Java y formato XML.
 * @author Saray
 */
public interface XMLConvertible {
    
    public Element toXMLElement();
    public void toObject(Element element);
    
} // fin
