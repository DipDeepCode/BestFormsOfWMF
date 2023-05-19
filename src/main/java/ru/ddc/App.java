package ru.ddc;

import ru.ddc.csvparser.BestWMFCalculator;
import ru.ddc.csvparser.CSVParser;

import java.util.*;

public class App {

    public static void main(String[] args) {
        List<List<Float>> EurUsd =
                CSVParser.parseSpecificColumns("src/main/resources/EURUSD_15min_2022.csv",
                        "high", "low", "middle");
        List<Float> highList = EurUsd.get(0);
        List<Float> lowList = EurUsd.get(1);
        List<Float> middleList = EurUsd.get(2);
//        System.out.println(normalize(highList));
        List<List<Float>> wmf = CSVParser.parseAllColumns("src/main/resources/wmf_full.csv");
//        System.out.println(wmf.get(1));

//        System.out.print("cmo_1(normalize(EurUsd_np_high), normalize(wmf_np[1]), 100) = ");
//        System.out.println(cmo_1(normalize(highList), normalize(wmf.get(1)), 100));

//        System.out.print("case_1 = ");
//        System.out.println(identification_1(middleList, wmf.get(1), 100, 25));

//        System.out.print("case1_1 = ");
//        System.out.println(best_wmf_1(middleList, wmf, 100, 20));

//        System.out.print("case2_1 = ");
//        List<List<Float>> x = predict_1(middleList, highList, lowList, wmf, 254);
//        x.forEach(System.out::println);

        System.out.println("case3 = ");
        List<List<List<Float>>> y = predict_dataset(middleList, highList, lowList, wmf, 4420, 4560);
        y.forEach(lists -> lists.forEach(System.out::println));
    }

    private static List<List<Float>> predict(List<Float> price, List<Float> priceHigh, List<Float> priceLow, List<List<Float>> wmf, int point_price) {
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

    private static List<List<List<Float>>> predict_dataset(List<Float> price, List<Float> priceHigh, List<Float> priceLow, List<List<Float>> wmf, int point_price, int poin_end) {
        List<List<List<Float>>> result_list = new ArrayList<>();
        for (int i = point_price; i < poin_end; i += 4) {
            List<List<Float>> result = predict(price, priceHigh, priceLow, wmf, i);
            result_list.add(result);
        }
        return result_list;
    }

}
