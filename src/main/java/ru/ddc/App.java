package ru.ddc;

import ru.ddc.csvparser.CSVParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class App {
    public static void main(String[] args) {
        List<List<Float>> EurUsd =
                CSVParser.parseSpecificColumns("src/main/resources/EURUSD_15min_2022.csv",
                        "high", "low", "middle");
        List<Float> highList = EurUsd.get(0);
        List<Float> lowList = EurUsd.get(1);
        List<Float> middleList = EurUsd.get(2);
        System.out.println(normalize(highList));
        List<List<Float>> wmf = CSVParser.parseAllColumns("src/main/resources/wmf_full.csv");
        System.out.println(wmf.get(1));

        System.out.print("cmo_1(normalize(EurUsd_np_high), normalize(wmf_np[1]), 100) = ");
        System.out.println(cmo_1(normalize(highList), normalize(wmf.get(1)), 100));

        System.out.print("case_1 = ");
        System.out.println(identification_1(middleList, wmf.get(1), 100, 25));

        System.out.print("case1_1 = ");
        System.out.println(best_wmf_1(middleList, wmf, 100, 20));

        System.out.print("case2_1 = ");
        List<List<Float>> x = predict_1(middleList, highList, lowList, wmf, 254);
        x.forEach(System.out::println);

        System.out.println("case3 = ");
        List<List<List<Float>>> y = predict_dataset(middleList, highList, lowList, wmf, 4420, 4560);
        y.forEach(lists -> lists.forEach(System.out::println));

    }

    private static List<Float> normalize(List<Float> series) {
        float max = Collections.max(series);
        float min = Collections.min(series);
        return series.stream().map(x -> (x - min) / (max - min)).toList();
    }

    private static float cmo_1(List<Float> price, List<Float> wmf, int length) {
        float cmo_mean = 0;
        for (int i = 0; i < length; i++) {
            cmo_mean += Math.abs(price.get(i) - wmf.get(i));
        }
        return cmo_mean / length;
    }

    private static List<Float> identification_1(List<Float> price, List<Float> wmf, int point_price, int m) {
        int start_price = point_price - m;
        List<Float> price_part = price.subList(start_price, point_price);
        float max_price = Collections.max(price_part);
        float min_price = Collections.min(price_part);
        List<Float> price_norm = normalize(price_part);
        int index = 0;
        int index_cmo_min = 1010;
        float cmo_min = 10;
        for (int i = 0; i <= wmf.size() - m; i++) {
            List<Float> row = wmf.subList(i, i + m);
            List<Float> wmf_norm = normalize(row);
            float cmo_value = cmo_1(price_norm, wmf_norm, m);
            if (cmo_value < cmo_min) {
                cmo_min = cmo_value;
                index_cmo_min = index;
            }
            index++;
        }

        List<Float> result_list = new ArrayList<>();
        result_list.add((float) m);
        result_list.add(cmo_min);
        result_list.add((float) index_cmo_min);
        result_list.add(min_price);
        result_list.add(max_price);
        return result_list;
    }

    private static List<Float> best_wmf_1(List<Float> price, List<List<Float>> wmf, int point_price, int m) {
        float cmo_min = 10;
        float point_wmf = 0;
        float min_price = 0;
        float max_price = 0;
        int sort_wmf = 0;

        for (int item = 1; item < 321; item++) {
            List<Float> result = identification_1(price, wmf.get(item), point_price, m);
            if (result.get(1) < cmo_min) {
                cmo_min = result.get(1);
                point_wmf = result.get(2);
                min_price = result.get(3);
                max_price = result.get(4);
                sort_wmf = item;
            }
        }

        List<Float> result_list = new ArrayList<>();
        result_list.add((float) m);
        result_list.add(cmo_min);
        result_list.add(point_wmf);
        result_list.add(min_price);
        result_list.add(max_price);
        result_list.add((float) sort_wmf);
        return result_list;
    }

    private static List<List<Float>> predict_1(List<Float> price, List<Float> priceHigh, List<Float> priceLow, List<List<Float>> wmf, int point_price) {
        List<List<Float>> result_list = new ArrayList<>();
        for (int m = 20; m < 105; m += 5) {
            List<Float> best_wmf_middle = best_wmf_1(price, wmf, point_price, m);
            List<Float> best_wmf_high = best_wmf_1(priceHigh, wmf, point_price, m);
            List<Float> best_wmf_low = best_wmf_1(priceLow, wmf, point_price, m);
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

    private static List<List<List<Float>>> predict_dataset(List<Float> price, List<Float> priceHigh, List<Float> priceLow, List<List<Float>> wmf, int point_price, int poin_end) {
        List<List<List<Float>>> result_list = new ArrayList<>();
        for (int i = point_price; i < poin_end; i += 4) {
            List<List<Float>> result = predict_1(price, priceHigh, priceLow, wmf, i);
            result_list.add(result);
        }
        return result_list;
    }

}
