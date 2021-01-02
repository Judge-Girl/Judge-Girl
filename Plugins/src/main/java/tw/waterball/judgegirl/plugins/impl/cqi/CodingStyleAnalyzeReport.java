/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.plugins.impl.cqi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import tw.waterball.judgegirl.entities.submission.Report;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

public class CodingStyleAnalyzeReport extends Report {
    public String rawString;
    private Document resultXml;
    private Element xmlRootElement;

    public CodingStyleAnalyzeReport(String result) {
        super("CSA-Report");
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


    @Override
    public Map<String, ?> getRawData() {
        var data = new HashMap<String, Object>();
        data.put("score", getScore());
        data.put("illegalName", getBadNamingStyleList());
        data.put("globalVariable", getGlobalVariableList());
        return data;
    }
}

