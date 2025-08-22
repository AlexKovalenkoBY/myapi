package com.wbozon;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExcelReaderTEST2 {
    public static void main(String[] args) {
        StopWatch watch = new StopWatch();
        watch.start();
        Long rows = 0L;
        
        // Создаем Map для хранения данных
        Map<Integer, List<String>> excelData = new HashMap<>();

        try (FileInputStream file = new FileInputStream("ARMTEK_MAIN_40006905_202503111407.xlsx");
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0); // Первый лист

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                
                for (Cell cell : row) {
                    // Обработка ячейки в зависимости от её типа
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            rowData.add(String.valueOf(cell.getNumericCellValue()));
                            break;
                        case STRING:
                            rowData.add(handleStringCell(cell));
                            break;
                        case BOOLEAN:
                            rowData.add(String.valueOf(cell.getBooleanCellValue()));
                            break;
                        case FORMULA:
                            rowData.add(cell.getCellFormula());
                            break;
                        default:
                            rowData.add("UNKNOWN_TYPE");
                    }
                }
                
                // Добавляем строку в Map
                excelData.put(row.getRowNum(), rowData);
                rows++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        watch.stop();
        
        // Выводим результаты
        System.out.println("Processing time :: " + watch.getTime(TimeUnit.MILLISECONDS) + " строк: " + rows);
        System.out.println("Total rows in map: " + excelData.size());
        
        // Пример вывода первых 5 строк для проверки
        int printCount = Math.min(5, excelData.size());
        for (int i = 0; i < printCount; i++) {
            System.out.println("Row " + i + ": " + excelData.get(i));
        }
    }

    private static String handleStringCell(Cell cell) {
        String value = cell.getStringCellValue().trim();
        try {
            // Пытаемся преобразовать строку в число
            double number = Double.parseDouble(value);
            return String.valueOf(number);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}