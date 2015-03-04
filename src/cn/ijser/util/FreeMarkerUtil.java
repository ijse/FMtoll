package cn.ijser.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import cn.ijser.bean.ConfigBean;
import freemarker.ext.rhino.RhinoWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;


/**
 * @author liyi_nad
 */
public class FreeMarkerUtil {

	private static ConfigBean configObj = new ConfigBean();
	private static Configuration freemarkerConfig = new Configuration();

	static {
		configObj = new ConfigBean();
		configObj.setSharedVariables(new HashMap<String, String>());
	}

	/**
	 * @param templateName
	 *            模板名字
	 * @param root
	 *            模板根 用于在模板内输出结果集
	 * @param out
	 *            输出对象 具体输出到哪里
	 */
	@SuppressWarnings("unchecked")
	public static void processTemplate(String templateName, JSONObject jsonObject,
			Writer out) {
		

		Map<String, Object> root = new HashMap<String, Object>();
		root = (Map<String, Object>) jsonObject;
		
		try {
			
			// 获得模板
			Template template = freemarkerConfig.getTemplate(templateName,
					configObj.getEncoding());

	        
			template.process(root, out);
			out.flush();
		} catch (IOException e) {
			System.out.println("读取模板文件IO异常！");
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				out = null;
			} catch (IOException e) {
				System.out.println("读取模板文件IO异常！");
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param templateName
	 *            模板名字
	 * @param root
	 *            模板根 用于在模板内输出结果集
	 * @param out
	 *            输出对象 具体输出到哪里
	 */
	public static void processTemplate(String templateName, JSONArray jsonArray,
			Writer out) {

		Context cx = Context.enter();
		Scanner scanner = null;
		Object[] files = jsonArray.toArray();
		try {
			
			
			// 获得模板
			Template template = freemarkerConfig.getTemplate(templateName,
					configObj.getEncoding());


	        Scriptable scope = cx.initStandardObjects();
	        String script = "";
        	StringBuilder sb = new StringBuilder();
	        for(int i = 0; i < files.length; i++) {
	        	File f = new File(files[i].toString());
		        scanner = new Scanner(f);
		        while (scanner.hasNextLine()){
		        	sb.append(scanner.nextLine());
	            }      
	        }

	        script = sb.toString();
	        Object o = cx.evaluateString(scope, script, null, 0, null);
	        
			template.process(o, out, new RhinoWrapper());
			out.flush();
		} catch (IOException e) {
			System.out.println("读取模板文件IO异常！");
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				Context.exit();
				scanner.close();
				out = null;
			} catch (IOException e) {
				System.out.println("读取模板文件IO异常！");
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * 初始化模板配置
	 *
	 * @param servletContext
	 *            javax.servlet.ServletContext
	 * @param templateDir
	 *            模板位置
	 * @throws IOException
	 * @throws TemplateModelException
	 */
	public static void initConfig(String configFile) {

		// 获取配置信息

		//String configContent = readConfigFile(configFile);
		convertConfig(configFile);

		File templateDir = configObj.getViewFolder();

		freemarkerConfig.setLocale(Locale.ENGLISH);
		freemarkerConfig.setDefaultEncoding(configObj.getEncoding());
		freemarkerConfig.setEncoding(Locale.ENGLISH, configObj.getEncoding());
		freemarkerConfig.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		try {
			freemarkerConfig.setDirectoryForTemplateLoading(templateDir);
		} catch (IOException e) {
			System.out.println("设置视图目录时出现IO异常！");
			e.printStackTrace();
		}

		// addAutoImport
		scanFolder(templateDir);

		// setSharedVariable
		try {
			addSharedVariables(configObj.getSharedVariables());
		} catch (TemplateModelException e) {
			System.out.println("添加全局变量时出错！");
			e.printStackTrace();
		}
		
		
		
		freemarkerConfig.setSharedVariable("block", new BlockDirective());
		freemarkerConfig.setSharedVariable("override", new OverrideDirective());
		freemarkerConfig.setSharedVariable("extends", new ExtendsDirective());
	}

	/**
	 * 将配置文件内容转换为ConfigBean对象
	 *
	 * @param configFile
	 *            配置文件位置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static void convertConfig(String configContent) {
		JSONParser parser = new JSONParser();
		try {
			Map<String, Object> obj = (Map<String, Object>) parser
					.parse(configContent);

			configObj.setEncoding(obj.get("encoding").toString());
			configObj.setViewFolder(new File(obj.get("viewFolder").toString()));

			/*Map<String, String> sharedVariables = (Map<String, String>) obj
					.get("sharedVariables");
			Iterator<Map.Entry<String, String>> iter = sharedVariables
					.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				configObj.getSharedVariables().put(entry.getKey(),
						entry.getValue());
			}*/

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param configFile
	 * @return
	 */
	private static String readConfigFile(String configFile) {
		String output = "";

		File file = new File(configFile);

		if (file.exists() && file.isFile()) {
			BufferedReader input = null;
			try {
				input = new BufferedReader(new FileReader(file));
				StringBuffer buffer = new StringBuffer();
				String text;

				while ((text = input.readLine()) != null)
					buffer.append(text);
				input.close();
				output = buffer.toString();
			} catch (IOException ioException) {
				System.err.println("读取配置文件IO异常！");
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.err.println("Config File Does not exist!");
		}
		return output;
	}

	/**
	 * 扫描目录下所有文件
	 *
	 * @param templatePath
	 */
	private static void scanFolder(File templatePath) {
		File[] files = templatePath.listFiles();

		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				scanFolder(files[i]);
			} else {
				String strFileName = files[i].getAbsolutePath();
				addAutoes(strFileName);
			}
		}
	}

	private static void addAutoes(String file) {
		String relativeFilePath = file.replace(configObj.getViewFolder()
				.getAbsolutePath(), "");
		// addAutoImport
		freemarkerConfig.addAutoImport("", relativeFilePath);
		// addAutoInclude
		freemarkerConfig.addAutoInclude(relativeFilePath);
	}

	private static void addSharedVariables(Map<String, String> sharedVariables)
			throws TemplateModelException {
		Iterator<Map.Entry<String, String>> iter = sharedVariables.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			freemarkerConfig
					.setSharedVariable(entry.getKey(), entry.getValue());
		}
	}
}
