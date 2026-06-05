/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utility;

import java.io.IOException;
import java.io.StringReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author Nelson
 */
public class GestionXML {
    
    public static Element stringToXML(String eString) throws JDOMException, IOException{
        
        SAXBuilder saxBuilder=new SAXBuilder();
        StringReader stringReader=new StringReader(eString);
        Document doc=saxBuilder.build(stringReader);
        return doc.getRootElement();
        
    } // stringToXML
    
    public static String xmlToSTring(Element element){
        XMLOutputter outputter=
                new XMLOutputter(Format.getCompactFormat());
        String xmlStringElement=outputter.outputString(element);
        xmlStringElement=xmlStringElement.replace("\n", "");
        return xmlStringElement; 
    } // xmlToSTring
    
} // fin clase
