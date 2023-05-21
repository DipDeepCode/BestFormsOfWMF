package ru.ddc;

import ru.ddc.utils.CSVParser;

import java.util.*;

public class App {

    public static void main(String[] args) {
        String filename = "src/main/resources/vibro_ex_4.csv";
        String[] columns = new String[]{"ЭКСГАУСТЕР 4. ВИБРАЦИЯ НА ОПОРЕ 1"};
        Map<String, List<Float>> df = CSVParser.parseSpecificColumns(filename, columns);
        List<Float> df_1interv_1opora = df.get("ЭКСГАУСТЕР 4. ВИБРАЦИЯ НА ОПОРЕ 1").subList(0, 3900);

        List<Float> maxi_vibr = new ArrayList<>();
        List<Float> mini_vibr = new ArrayList<>();
        List<Float> midi_vibr = new ArrayList<>();
        int step = 30;
        for (int i = 0; i < df_1interv_1opora.size() - 1; i += step) {
            List<Float> df2 = df_1interv_1opora.subList(i, i + step);
            maxi_vibr.add(
                    (float) df2.stream()
                            .mapToDouble(value -> (double) value)
                            .filter(value -> !Double.isNaN(value))
                            .max()
                            .orElse(0.0)
            );
            mini_vibr.add(
                    (float) df2.stream()
                            .mapToDouble(value -> (double) value)
                            .filter(value -> !Double.isNaN(value))
                            .min()
                            .orElse(0.0)
            );
            midi_vibr.add(
                    (float) df2.stream()
                            .mapToDouble(value -> (double) value)
                            .filter(value -> !Double.isNaN(value))
                            .average()
                            .orElse(0.0)
            );
        }

        Map<String, List<Float>> wmf_np = CSVParser.parseAllColumns("src/main/resources/wmf_full.csv");

        Object[] dataset_identification = predict_1(midi_vibr, maxi_vibr, mini_vibr, wmf_np, 130)
                .stream()
                .min(Comparator.comparingDouble(value -> (double) value[1]))
                .orElse(new Object[]{});//TODO проработать выбрасывание исключения

        List<Float> result_list = line_predict(0, dataset_identification, wmf_np);
        result_list.forEach(value -> System.out.println(" " + value + ","));
    }

    private static List<Object[]> predict_1(List<Float> price, List<Float> priceHigh, List<Float> priceLow, Map<String, List<Float>> wmf, int point_price) {
        List<Object[]> result_list = new ArrayList<>();

        for (int m = 20; m < 105; m += 5) {

            BestWMFCalculator best_wmf_middle_object = new BestWMFCalculator(price, wmf, point_price, m);
            BestWMFCalculator best_wmf_high_object = new BestWMFCalculator(priceHigh, wmf, point_price, m);
            BestWMFCalculator best_wmf_low_object = new BestWMFCalculator(priceLow, wmf, point_price, m);

            Thread best_wmf_middle_thread = new Thread(best_wmf_middle_object);
            Thread best_wmf_high_thread = new Thread(best_wmf_high_object);
            Thread best_wmf_low_thread = new Thread(best_wmf_low_object);

            best_wmf_middle_thread.start();
            best_wmf_high_thread.start();
            best_wmf_low_thread.start();

            try {
                best_wmf_middle_thread.join();
                best_wmf_high_thread.join();
                best_wmf_low_thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Object[] best_wmf_middle = best_wmf_middle_object.getBest_wmf();
            Object[] best_wmf_high = best_wmf_high_object.getBest_wmf();
            Object[] best_wmf_low = best_wmf_low_object.getBest_wmf();

            if (best_wmf_low[2].equals(best_wmf_middle[2]) && best_wmf_middle[2].equals(best_wmf_high[2]) &&
                    best_wmf_low[5].equals(best_wmf_middle[5]) && best_wmf_middle[5].equals(best_wmf_high[5])) {
                result_list.add(new Object[]{
                        best_wmf_middle[0],
                        best_wmf_middle[1],
                        best_wmf_middle[2],
                        best_wmf_middle[3],
                        best_wmf_middle[4],
                        best_wmf_middle[5],
                        point_price});
            }
        }
        return result_list;
    }

    private static List<Float> normalize1(List<Float> serie, float max, float min) {
        return serie.stream().map(x -> (x - min) / (max - min)).toList();
    }

    private static List<Float> denormalize(List<Float> serie, float max, float min) {
        return serie.stream().map(x -> min + x * (max - min)).toList();
    }

    private static List<Float> line_predict(int line, Object[] dataset_identificated, Map<String, List<Float>> wmf_full) {
        int m = (int) dataset_identificated[0];
        int i = (int) dataset_identificated[2];
        String  l = (String) dataset_identificated[5];
        List<Float> wmf_part = wmf_full.get(l).subList(i, i + m);
        float wmf_max = Collections.max(wmf_part);
        float wmf_min = Collections.min(wmf_part);
        List<Float> x = wmf_full.get(l);
        List<Float> wmf_part_150 = x.subList(i, Math.min(i + 150, x.size()));
        List<Float> wmf_part_norm = normalize1(wmf_part_150, wmf_max, wmf_min);
        float min = (float) dataset_identificated[3];
        float max = (float) dataset_identificated[4];
        return denormalize(wmf_part_norm, max, min);
    }
}
