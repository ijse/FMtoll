package cn.ijser.app;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

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

		// 转换dataModel为MAP
		JSONParser parser = new JSONParser();
		Map<String, Object> root = null;
		try {
			root = (Map<String, Object>) parser.parse(dataModel);
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}

		// 输出模板
		Writer out = new OutputStreamWriter(System.out);
		FreeMarkerUtil.processTemplate(templateName, root, out);
	}
}
