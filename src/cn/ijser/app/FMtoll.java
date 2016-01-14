package cn.ijser.app;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import cn.ijser.util.FreeMarkerUtil;

/**
 * @author liyi_nad
 *
 */
public class FMtoll {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        String fmconfig = args[0];
        String templateName = args[1];
        String dataModel = args[2];
        String deps = args[3];
        String nodes = args[4];

        // initialisation
        FreeMarkerUtil.initConfig(fmconfig);
        // stream to output Template to
        Writer out = new OutputStreamWriter(System.out);

        // dataModel converted to JSON object
        Object parsedData = parseJSON(dataModel);

        JSONArray parsedDeps = (JSONArray) parseJSON(deps);
        JSONArray parsedNodes = (JSONArray) parseJSON(nodes);

        if (parsedData instanceof JSONObject) {
            FreeMarkerUtil.processTemplate(templateName, parsedDeps, (JSONObject) parsedData, out);
        } else if (parsedData instanceof JSONArray) {
            FreeMarkerUtil.processTemplate(templateName, parsedDeps, (JSONArray) parsedData, (JSONArray) parsedNodes, out);
        }
    }

    private static Object parseJSON(String data) {
        Object parsedObject = null;
        if (data != null && !data.equals("undefined")) {
            JSONParser parser = new JSONParser();
            try {
                parsedObject = parser.parse(data);
            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
            }
        }
        return parsedObject;
    }
}
