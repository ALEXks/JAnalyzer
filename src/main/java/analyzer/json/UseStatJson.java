package analyzer.json;

import com.google.gson.Gson;

public class UseStatJson {
    private static Gson gson = new Gson();

    public static StatJson GetStat(String json) {
        StatJson statJson = gson.fromJson(json, StatJson.class);
        for (IntervalJson intervalJson : statJson.inter) {
            intervalJson.times.prod_time = intervalJson.times.prod_cpu + intervalJson.times.prod_sys
                    + intervalJson.times.prod_io;
            intervalJson.calculateAdditionalMetrics();
        }

        return statJson;
    }

    public static String GetJson(StatJson stat) {
        return gson.toJson(stat);
    }

}