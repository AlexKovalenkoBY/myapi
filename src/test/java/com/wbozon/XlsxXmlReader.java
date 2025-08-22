package com.wbozon;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

public class XlsxXmlReader {

    private static final String SHARED_STRINGS = "xl/sharedStrings.xml";
    private static final String WORKSHEET_PATH = "xl/worksheets/sheet";

    public static void main(String[] args) {
        try (ZipFile zipFile = new ZipFile("ARMTEK_MAIN_40006905_202503140512.xlsx")) {
            // Читаем общие строки
            List<String> sharedStrings = parseSharedStrings(zipFile);
            
            // Обрабатываем все листы
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith(WORKSHEET_PATH)) {
                    System.out.println("\nProcessing sheet: " + entry.getName());
                    parseWorksheet(zipFile.getInputStream(entry), sharedStrings);
                }
                int rr=0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> parseSharedStrings(ZipFile zipFile) throws Exception {
        ZipEntry entry = zipFile.getEntry(SHARED_STRINGS);
        if (entry == null) return Collections.emptyList();

        List<String> strings = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (InputStream is = zipFile.getInputStream(entry)) {
            XMLEventReader reader = factory.createXMLEventReader(is);
            
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement() && 
                    event.asStartElement().getName().getLocalPart().equals("t")) {
                    String text = reader.nextEvent().asCharacters().getData();
                    strings.add(text.trim());
                }
            }
        }
        return strings;
    }

    private static void parseWorksheet(InputStream is, List<String> sharedStrings) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(is);
        
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String elementName = startElement.getName().getLocalPart();
                
                if ("row".equals(elementName)) {
                    processRow(reader, sharedStrings);
                }
            }
        }
    }

    private static void processRow(XMLEventReader reader, List<String> sharedStrings) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndElement() && 
                "row".equals(event.asEndElement().getName().getLocalPart())) {
                System.out.println(); // Новая строка
                break;
            }
            
            if (event.isStartElement() && 
                "c".equals(event.asStartElement().getName().getLocalPart())) {
                String cellValue = getCellValue(reader, sharedStrings);
                System.out.print(cellValue + "\t");
            }
        }
    }

    private static String getCellValue(XMLEventReader reader, List<String> sharedStrings) throws XMLStreamException {
        String valueType = "n"; // По умолчанию число
        String value = "";
        
        // Получаем тип ячейки
        Iterator<Attribute> attributes = reader.nextEvent().asStartElement().getAttributes();
        while (attributes.hasNext()) {
            Attribute attr = attributes.next();
            if ("t".equals(attr.getName().getLocalPart())) {
                valueType = attr.getValue();
            }
        }

        // Читаем значение
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement() && 
                "v".equals(event.asStartElement().getName().getLocalPart())) {
                value = reader.nextEvent().asCharacters().getData();
                break;
            }
        }

        // Обработка разных типов
        if ("s".equals(valueType)) { // Ссылка на общую строку
            int index = Integer.parseInt(value);
            return sharedStrings.get(index);
        }
        return value; // Возвращаем сырое значение
    }
}