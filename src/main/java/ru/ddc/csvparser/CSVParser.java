package ru.ddc.csvparser;

import com.opencsv.CSVIterator;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    public static List<List<Float>> parseSpecificColumns(String filename, String... columnNames) {
        List<List<Float>> floats = new ArrayList<>();
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

            for (int i = 0; i < columnNames.length; i++) {
                floats.add(new ArrayList<>());
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
                    floats.get(i).add(Float.parseFloat(values[indexes[i]]));
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return floats;
    }

    public static List<List<Float>> parseAllColumns(String filename) {
        List<List<Float>> floats = new ArrayList<>();
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

            for (int i = 0; i < columnNames.length; i++) {
                floats.add(new ArrayList<>());
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
                    floats.get(i).add(Float.parseFloat(values[indexes[i]]));
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return floats;
    }
}
