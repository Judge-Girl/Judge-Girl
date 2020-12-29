package tw.waterball.judgegirl.entities.submission;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import lombok.Value;

@Value
public class CodingStyleAnalyzeReport {
    public String rawString;
    private Document resultXml;
    private Element xmlRootElement;

    public CodingStyleAnalyzeReport(String result) {
        rawString = result;
        resultXml = convertStringToXMLDocument(result);
        xmlRootElement = resultXml.getDocumentElement();
    }

    public int getScore() {
        return Integer.parseInt(xmlRootElement.getAttribute("score"));
    }

    public List<String> getBadNamingStyleList() {
        return Arrays.asList(xmlRootElement.getAttribute("bad_naming_style_list").split(","));
    }

    public List<String> getGlobalVariableList() {
        return Arrays.asList(xmlRootElement.getAttribute("global_variable_list").split(","));
    }

    private Document convertStringToXMLDocument(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

