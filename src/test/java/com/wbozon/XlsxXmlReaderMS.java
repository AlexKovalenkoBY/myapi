package com.wbozon;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

public class XlsxXmlReaderMS {

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
                    System.out.println("\nОбрабатываем лист: " + entry.getName());
                    parseWorksheet(zipFile.getInputStream(entry), sharedStrings);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> parseSharedStrings(ZipFile zipFile) throws Exception {
        ZipEntry entry = zipFile.getEntry(SHARED_STRINGS);
        if (entry == null) {
            return Collections.emptyList();
        }

        List<String> strings = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (InputStream is = zipFile.getInputStream(entry)) {
            XMLEventReader reader = factory.createXMLEventReader(is);

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement() &&
                        "t".equals(event.asStartElement().getName().getLocalPart())) {
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
            if (event.isStartElement() &&
                    "row".equals(event.asStartElement().getName().getLocalPart())) {
                processRow(reader, sharedStrings);
            }
        }
    }

    private static void processRow(XMLEventReader reader, List<String> sharedStrings) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndElement() &&
                    "row".equals(event.asEndElement().getName().getLocalPart())) {
                System.out.println(); // Переход на новую строку после завершения обработки строки
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
        String valueType = "n";
        String value = "";
        boolean isInlineStr = false;
    
        // Читаем атрибуты ячейки
        StartElement cellStart = reader.nextEvent().asStartElement();
        Iterator<Attribute> attributes = cellStart.getAttributes();
        while (attributes.hasNext()) {
            Attribute attr = attributes.next();
            if ("t".equals(attr.getName().getLocalPart())) {
                valueType = attr.getValue();
            }
        }
    
        // Обработка inline-строк
        if ("inlineStr".equals(valueType)) {
            isInlineStr = true;
            valueType = "s"; // Обрабатываем как строку
        }
    
        // Читаем значение
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            
            if (event.isStartElement() && "v".equals(event.asStartElement().getName().getLocalPart())) {
                value = reader.nextEvent().asCharacters().getData();
                break;
            } else if (isInlineStr && event.isStartElement() 
                       && "t".equals(event.asStartElement().getName().getLocalPart())) {
                // Читаем inline-строку напрямую
                value = reader.nextEvent().asCharacters().getData();
                break;
            }
        }
    
        switch (valueType) {
            case "s": 
                return sharedStrings.get(Integer.parseInt(value));
            case "inlineStr":
                return value;
            case "str": 
                return value;
            case "n": 
                return value;
            case "b": 
                return value.equals("1") ? "TRUE" : "FALSE";
            case "e": 
                return "ERROR: " + value;
            default: 
                return value;
        }
    }}
