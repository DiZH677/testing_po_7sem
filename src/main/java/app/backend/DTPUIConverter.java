package app.backend;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DTPUIConverter {
    public static DTPUI fromJson(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        int id = jsonObject.get("id").getAsInt();
        String description = jsonObject.get("description").getAsString();
        String datetime = jsonObject.get("datetime").getAsString();
        double coordW = jsonObject.get("coordL").getAsDouble();
        double coordL = jsonObject.get("coordW").getAsDouble();
        String osv = jsonObject.get("osv").getAsString();
        int countTs = jsonObject.get("countTs").getAsInt();
        int countPart = jsonObject.get("countParts").getAsInt();

        return new DTPUI(id, description, datetime, coordW, coordL, "", osv, countTs, countPart);
    }
}
