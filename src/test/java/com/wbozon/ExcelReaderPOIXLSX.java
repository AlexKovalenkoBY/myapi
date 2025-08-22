package com.wbozon;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class ExcelReaderPOIXLSX {
    public static void main(String[] args) {
            StopWatch watch = new StopWatch();
        watch.start();
        try {
            FileInputStream file = new FileInputStream(new File("ARMTEK_MAIN_40006905_202503111407.xlsx"));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                    ArrayList<String> rowStringValues = new ArrayList<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING:
                            // System.out.print(cell.getStringCellValue() + " ");
                            rowStringValues.add(cell.getStringCellValue().trim());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                // System.out.print(cell.getDateCellValue() + " ");
                                rowStringValues.add(cell.getDateCellValue().toString().trim());
                            } else {
                                // System.out.print(cell.getNumericCellValue() + " ");
                                rowStringValues.add(String.valueOf(cell.getNumericCellValue()).trim());
                            }
                            break;
                        case BOOLEAN:
                            // System.out.print(cell.getBooleanCellValue() + " ");
                            break;
                        case FORMULA:
                            // System.out.print(cell.getCellFormula() + " ");
                            break;
                        default:
                            System.out.print(" ");
                    }
                }
                System.out.println();
            }
            file.close();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
         watch.stop();
        System.out.println("Processing time :: " + watch.getTime(TimeUnit.MILLISECONDS));
    }
}
