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

		// 初始化
		FreeMarkerUtil.initConfig(fmconfig);
		

		// 输出模板
		Writer out = new OutputStreamWriter(System.out);

		// 转换dataModel为MAP
		JSONParser parser = new JSONParser();
		Object parsedObject = null;
		try {
			 parsedObject = parser.parse(dataModel);

		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
		
		 if(parsedObject instanceof JSONObject) {
			 FreeMarkerUtil.processTemplate(templateName, (JSONObject) parsedObject, out);
		 } else if(parsedObject instanceof JSONArray) {
			 FreeMarkerUtil.processTemplate(templateName, (JSONArray) parsedObject, out);
		 }	
	}
}
