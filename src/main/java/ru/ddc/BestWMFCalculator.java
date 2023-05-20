package ru.ddc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BestWMFCalculator extends Thread {
    private List<Float> best_wmf;
    private List<Float> price;
    private List<List<Float>> wmf;
    private int point_price;
    private int m;

    public BestWMFCalculator(List<Float> price, List<List<Float>> wmf, int point_price, int m) {
        this.price = price;
        this.wmf = wmf;
        this.point_price = point_price;
        this.m = m;
    }

    @Override
    public void run() {
        best_wmf = best_wmf_1();
    }

    private List<Float> best_wmf_1() {
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

    private List<Float> identification_1(List<Float> price, List<Float> wmf, int point_price, int m) {
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

    public List<Float> getBest_wmf() {
        return best_wmf;
    }

    public void setBest_wmf(List<Float> best_wmf) {
        this.best_wmf = best_wmf;
    }

    public List<Float> getPrice() {
        return price;
    }

    public void setPrice(List<Float> price) {
        this.price = price;
    }

    public List<List<Float>> getWmf() {
        return wmf;
    }

    public void setWmf(List<List<Float>> wmf) {
        this.wmf = wmf;
    }

    public int getPoint_price() {
        return point_price;
    }

    public void setPoint_price(int point_price) {
        this.point_price = point_price;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }
}
