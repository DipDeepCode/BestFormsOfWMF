package ru.ddc;

import java.util.*;

public class BestWMFCalculator extends Thread {
    private Object[] best_wmf;
    private final List<Float> price;
    private final Map<String, List<Float>> wmf;
    private final int point_price;
    private final int m;

    public BestWMFCalculator(List<Float> price, Map<String, List<Float>> wmf, int point_price, int m) {
        this.price = price;
        this.wmf = wmf;
        this.point_price = point_price;
        this.m = m;
    }

    @Override
    public void run() {
        best_wmf = best_wmf_1();
    }

    private Object[] best_wmf_1() {
        float cmo_min = 10;
        int point_wmf = 0;
        float min_price = 0;
        float max_price = 0;
        String sort_wmf = null;

        for (String key : wmf.keySet()) {
            Object[] result = identification_1(price, wmf.get(key), point_price, m);
            if ((float) result[1] < cmo_min) {
                cmo_min = (float) result[1];
                point_wmf = (int) result[2];
                min_price = (float) result[3];
                max_price = (float) result[4];
                sort_wmf = key;
            }
        }

        return new Object[] {m, cmo_min, point_wmf, min_price, max_price, sort_wmf};
    }

    private Object[] identification_1(List<Float> price, List<Float> wmf, int point_price, int m) {
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

        return new Object[]{m, cmo_min, index_cmo_min, min_price, max_price};
    }

    private float cmo_1(List<Float> price, List<Float> wmf, int length) {
        float cmo_mean = 0;
        for (int i = 0; i < length; i++) {
            cmo_mean += Math.abs(price.get(i) - wmf.get(i));
        }
        return cmo_mean / length;
    }

    private List<Float> normalize(List<Float> series) {
        float max = Collections.max(series);
        float min = Collections.min(series);
        return series.stream().map(x -> (x - min) / (max - min)).toList();
    }

    public Object[] getBest_wmf() {
        return best_wmf;
    }
}
