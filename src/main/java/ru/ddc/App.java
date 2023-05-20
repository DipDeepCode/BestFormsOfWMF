package ru.ddc;

import ru.ddc.csvparser.CSVParser;

import java.util.*;
import java.util.function.Consumer;

public class App {

    public static void main(String[] args) {
        String filename = "src/main/resources/vibro_ex_4.csv";
        String[] columns = new String[]{"ЭКСГАУСТЕР 4. ВИБРАЦИЯ НА ОПОРЕ 1"};
        Map<String, List<Float>> df = CSVParser.parseSpecificColumns(filename, columns);
        List<Float> df_1interv_1opora = df.get("ЭКСГАУСТЕР 4. ВИБРАЦИЯ НА ОПОРЕ 1").subList(0, 3901);

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

        List<List<Float>> wmf_np = CSVParser.parseAllColumns("src/main/resources/wmf_full.csv");

//        System.out.print("predict_param = ");
        List<List<Float>> predict_param = predict_1(midi_vibr, maxi_vibr, mini_vibr, wmf_np, 130);
//        predict_param.forEach(System.out::println);

        List<Float> dataset_identification;
        dataset_identification = predict_param.stream()
                .min(Comparator.comparingDouble(value -> (double) value.get(1)))
                .orElse(new ArrayList<>());//TODO проработать выбрасывание исключения
        System.out.println(dataset_identification);

        List<Float> result_list = line_predict(0, dataset_identification, wmf_np);
        result_list.forEach(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                System.out.println(" " + aFloat + ",");
            }
        });
    }

    private static List<List<Float>> predict_1(List<Float> price, List<Float> priceHigh, List<Float> priceLow, List<List<Float>> wmf, int point_price) {
        List<List<Float>> result_list = new ArrayList<>();
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

            List<Float> best_wmf_middle = best_wmf_middle_object.getBest_wmf();//best_wmf(price, wmf, point_price, m);
            List<Float> best_wmf_high = best_wmf_high_object.getBest_wmf();//best_wmf(priceHigh, wmf, point_price, m);
            List<Float> best_wmf_low = best_wmf_low_object.getBest_wmf();//best_wmf(priceLow, wmf, point_price, m);
            if (best_wmf_low.get(2).equals(best_wmf_middle.get(2)) && best_wmf_middle.get(2).equals(best_wmf_high.get(2)) &&
                    best_wmf_low.get(5).equals(best_wmf_middle.get(5)) && best_wmf_middle.get(5).equals(best_wmf_high.get(5))) {


                result_list.add(new ArrayList<>());
                result_list.get(result_list.size() - 1).add(best_wmf_middle.get(0));
                result_list.get(result_list.size() - 1).add(best_wmf_middle.get(1));
                result_list.get(result_list.size() - 1).add(best_wmf_middle.get(2));
                result_list.get(result_list.size() - 1).add(best_wmf_middle.get(3));
                result_list.get(result_list.size() - 1).add(best_wmf_middle.get(4));
                result_list.get(result_list.size() - 1).add(best_wmf_middle.get(5));
                result_list.get(result_list.size() - 1).add((float) point_price);
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

    private static List<Float> line_predict(int line, List<Float> dataset_identificated, List<List<Float>> wmf_full) {
        float m = dataset_identificated.get(0);
        float i = dataset_identificated.get(2);
        float l = dataset_identificated.get(5);
        List<Float> wmf_part = wmf_full.get((int) l).subList((int) i, (int) i + (int) m);
        float wmf_max = Collections.max(wmf_part);
        float wmf_min = Collections.min(wmf_part);
        List<Float> x = wmf_full.get((int) l);
        List<Float> wmf_part_150 = x.subList((int) i, Math.min((int) i + 150, x.size()));
        List<Float> wmf_part_norm = normalize1(wmf_part_150, wmf_max, wmf_min);
        float min = dataset_identificated.get(3);
        float max = dataset_identificated.get(4);
        List<Float> result = denormalize(wmf_part_norm, max, min);
        return result;
    }

}
