package com.wbozon;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReaderTEST2saxOptimized {

    public static void main(String[] args) {
        Map<Integer, List<String>> excelData = readExcelData("Прайс лист КАМА МАРКЕТ.xlsx");
        // Вывод результатов
        excelData.entrySet().stream().limit(25).forEach(entry ->
            System.out.println("Row " + entry.getKey() + ": " + entry.getValue()));
    }

    public static Map<Integer, List<String>> readExcelData(String filePath) {
        Map<Integer, List<String>> excelData = new HashMap<>();
        try (OPCPackage pkg = OPCPackage.open(filePath)) {
            XSSFReader reader = new XSSFReader(pkg);

            // Получаем таблицу текстовых строк
            SharedStringsTable sharedStringsTable = (SharedStringsTable) reader.getSharedStringsTable();
            InputStream sheet = ((SheetIterator) reader.getSheetsData()).next();

            SAXHandler handler = new SAXHandler(excelData, sharedStringsTable);

            // SAX-парсер
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(handler);

            xmlReader.parse(new org.xml.sax.InputSource(sheet));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return excelData;
    }

    private static class SAXHandler extends DefaultHandler {
        private final Map<Integer, List<String>> data;
        private final SharedStringsTable sharedStringsTable;
        private List<String> currentRow;
        private int currentRowIndex = 0;
        private String currentCellValue = "";
        private boolean isSharedString = false;

        public SAXHandler(Map<Integer, List<String>> data, SharedStringsTable sharedStringsTable) {
            this.data = data;
            this.sharedStringsTable = sharedStringsTable;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("row".equals(qName)) {
                currentRow = new ArrayList<>();
            } else if ("c".equals(qName)) {
                currentCellValue = "";
                // Проверяем, является ли ячейка текстовой через тип атрибута
                String cellType = attributes.getValue("t");
                isSharedString = "s".equals(cellType);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("v".equals(qName)) {
                if (isSharedString) {
                    // Если текст хранится в Shared Strings Table
                    int idx = Integer.parseInt(currentCellValue);
                    currentCellValue = sharedStringsTable.getItemAt(idx).getString();
                }
                currentRow.add(currentCellValue);
            } else if ("row".equals(qName)) {
                data.put(currentRowIndex++, new ArrayList<>(currentRow));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            currentCellValue += new String(ch, start, length);
        }
    }
}
