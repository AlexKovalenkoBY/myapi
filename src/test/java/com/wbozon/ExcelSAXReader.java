package com.wbozon;

import org.apache.commons.lang3.time.StopWatch;
import java.util.concurrent.TimeUnit;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelSAXReader {

    public static void main(String[] args) {
        String filePath = "ARMTEK_MAIN_40006905_202503140512_.xlsx"; // Укажите путь к вашему файлу
        StopWatch watch = new StopWatch();
        watch.start();

        List<List<String>> allSheetData = new ArrayList<>(); // Список для хранения данных всех листов
        AtomicInteger totalRowCount = new AtomicInteger(0); // Общее количество строк во всех листах

        try (OPCPackage opcPackage = OPCPackage.open(filePath)) {
            XSSFReader reader = new XSSFReader(opcPackage);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();

            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();

            // Перебираем все листы
            while (sheets.hasNext()) {
                InputStream sheet = sheets.next();
                String sheetName = sheets.getSheetName();
                System.out.println("Reading sheet: " + sheetName);

                List<List<String>> sheetData = new ArrayList<>(); // Данные текущего листа
                AtomicInteger sheetRowCount = new AtomicInteger(0); // Количество строк в текущем листе

                XSSFSheetXMLHandler.SheetContentsHandler handler = new XSSFSheetXMLHandler.SheetContentsHandler() {
                    List<String> currentRow = new ArrayList<>();

                    @Override
                    public void startRow(int rowNum) {
                        System.out.println("Processing row: " + rowNum); // Лог текущей строки
                        currentRow = new ArrayList<>(); // Инициализируем новую строку
                    }

                    @Override
                    public void endRow(int rowNum) {
                        sheetData.add(currentRow); // Добавляем строку в список
                        sheetRowCount.incrementAndGet(); // Увеличиваем счетчик строк для текущего листа
                    }

                    @Override
                    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
                        System.out.println("Cell: " + cellReference + ", Value: " + formattedValue); // Лог каждой ячейки
                        currentRow.add(formattedValue); // Добавляем значение ячейки в текущую строку
                    }

                    @Override
                    public void headerFooter(String text, boolean isHeader, String tagName) {
                        // Игнорируем заголовки и футеры
                    }
                };

                xmlReader.setContentHandler(new XSSFSheetXMLHandler(reader.getStylesTable(), null, handler, false));
                xmlReader.parse(new InputSource(sheet));

                // Добавляем данные текущего листа в общий список
                allSheetData.addAll(sheetData);
                totalRowCount.addAndGet(sheetRowCount.get()); // Обновляем общее количество строк
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        System.out.println("Processing time :: " + watch.getTime(TimeUnit.SECONDS));
        System.out.println("Общее количество строк: " + totalRowCount.get());

        // Выводим данные всех листов
        for (List<String> row : allSheetData) {
            System.out.println(row);
        }
    }
}
