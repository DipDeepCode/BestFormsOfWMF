package ru.ddc.utils;

import com.opencsv.CSVIterator;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class CSVParser {
    //TODO dry переработка

    public static String[] getHeader(String filename, char separator) {
        String[] header;
        try (FileReader reader = new FileReader(filename);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                     .build()) {
            CSVIterator iterator = new CSVIterator(csvReader);
            if (iterator.hasNext()) {
                header = iterator.next();
            } else {
                throw new RuntimeException("Ошибка чтения файла " + filename);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return header;
    }

    public static Map<String, List<Float>> parseSpecificColumns(String filename,
                                                                char separator,
                                                                LocalDateTime start,
                                                                LocalDateTime end,
                                                                String... columnNames) {
        //TODO добавить проверку что столбцов с таким именем не существует
        Map<String, List<Float>> result = new HashMap<>();
        try (FileReader reader = new FileReader(filename);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                     .build()) {

            String[] header;
            CSVIterator iterator = new CSVIterator(csvReader);
            if (iterator.hasNext()) {
                header = iterator.next();
            } else {
                throw new RuntimeException("Ошибка чтения файла " + filename);
            }

            for (String columnName : columnNames) {
                result.put(columnName, new ArrayList<>());
            }

            int[] indexes = new int[columnNames.length];
            for (int i = 0; i < header.length; i++) {
                for (int j = 0; j < columnNames.length; j++) {
                    if (columnNames[j].equalsIgnoreCase(header[i])) {
                        indexes[j] = i;
                    }
                }
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            while (iterator.hasNext()) {
                String[] values = iterator.next();
                LocalDateTime dateTime = LocalDateTime.parse(values[0], formatter);
                if (dateTime.isEqual(start) || dateTime.isAfter(start) && dateTime.isBefore(end)) {
                    for (int i = 0; i < columnNames.length; i++) {
                        float value;
                        try {
                            value = Float.parseFloat(values[indexes[i]]);
                        } catch (NumberFormatException ex) {
                            value = Float.NaN;
                        }
                        result.get(columnNames[i]).add(value);
                    }
                }            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static Map<String, List<Float>> parseAllColumns(String filename) {
        Map<String, List<Float>> floats = new HashMap<>();
        try (FileReader reader = new FileReader(filename);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withSeparator('\t').build())
                     .build()) {

            CSVIterator iterator = new CSVIterator(csvReader);
            String[] header;
            if (iterator.hasNext()) {
                header = iterator.next();
            } else {
                throw new RuntimeException("Ошибка чтения файла " + filename);
            }

            String[] columnNames = header;
            for (String columnName : columnNames) {
                floats.put(columnName, new ArrayList<>());
            }

            int[] indexes = new int[columnNames.length];
            for (int i = 0; i < header.length; i++) {
                for (int j = 0; j < columnNames.length; j++) {
                    if (columnNames[j].equalsIgnoreCase(header[i])) {
                        indexes[j] = i;
                    }
                }
            }

            while (iterator.hasNext()) {
                String[] values = iterator.next();
                for (int i = 0; i < columnNames.length; i++) {
                    float value;
                    try {
                        value = Float.parseFloat(values[indexes[i]]);
                    } catch (NumberFormatException ex) {
                        value = Float.NaN;
                    }
                    floats.get(columnNames[i]).add(value);
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return floats;
    }
}
