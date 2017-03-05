package com.zgeorg03.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zgeorg03 on 3/4/17.
 */
public class Calculations {

    /**
     * Return average of an Integer list
     * @param list
     * @return
     */
    public static double averageInt(List<Integer> list){
        if(list.size()==0)
            return 0;
        return list.stream().mapToInt(x->x).average().getAsDouble();
    }

    /**
     * Return average of a Long list
     * @param list
     * @return
     */
    public static double averageLong(List<Long> list){
        if(list.size()==0)
            return 0;
        return list.stream().mapToLong(x->x).average().getAsDouble();
    }
    /**
     * Return median of an Integer list
     * @param list
     * @return
     */
    public static int medianInt(List<Integer> list){
        if(list.size()==0)
            return 0;
        List<Integer> sorted = list.stream().sorted().collect(Collectors.toList());
        return sorted.get(sorted.size()/2);
    }

    /**
     * Return median of a Long list
     * @param list
     * @return
     */
    public static Long medianLong(List<Long> list){
        if(list.size()==0)
            return 0L;
        List<Long> sorted = list.stream().sorted().collect(Collectors.toList());
        return sorted.get(sorted.size()/2);
    }

    /**
     * Return std of an Integer list
     * @param list
     * @return
     */
    public static double stdInt(List<Integer> list,double avg){
        if(list.size()==0)
            return 0;
        return Math.sqrt(list.stream().mapToDouble(v -> (v-avg)*(v-avg)).sum()/list.size());
    }
    /**
     * Return std of an Integer list
     * @param list
     * @return
     */
    public static double stdLong(List<Long> list,double avg){
        if(list.size()==0)
            return 0;
        return Math.sqrt(list.stream().mapToDouble(v -> (v-avg)*(v-avg)).sum()/list.size());
    }
}
