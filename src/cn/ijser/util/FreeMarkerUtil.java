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

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.ObjectConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

import cn.ijser.bean.ConfigBean;
import freemarker.ext.rhino.RhinoWrapper;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;


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
	 * @param deps
	 *            模板根 用于在模板内输出结果集
	 * @param out
	 *            输出对象 具体输出到哪里
	 */
	@SuppressWarnings("unchecked")
	public static void processTemplate(String templateName, JSONArray deps, JSONObject jsonDataModel,
			Writer out) {


		Map<String, Object> root = (Map<String, Object>) jsonDataModel;

		try {

			// 获得模板
			Template template = freemarkerConfig.getTemplate(templateName,
					configObj.getEncoding());

            // Import all the given deps into the processing environment.
            Environment processingEnvironment = template.createProcessingEnvironment(root, out);

            importLibs(processingEnvironment, deps);

            processingEnvironment.process();
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
	 * @param deps
	 *            模板根 用于在模板内输出结果集
	 * @param out
	 *            输出对象 具体输出到哪里
	 */
	public static void processTemplate(String templateName, JSONArray deps, JSONArray jsonDataModel, JSONArray nodes,
			Writer out) {

		Context cx = Context.enter();
		Scanner scanner = null;
		Object[] dataFiles = jsonDataModel.toArray();
		try {

			// 获得模板
			Template template = freemarkerConfig.getTemplate(templateName,
					configObj.getEncoding());

            Map<String, Object> root =  new HashMap<String, Object>();
	        Scriptable scope = cx.initStandardObjects();
			// executes all the js files in sequence
	        for(int i = 0; i < dataFiles.length; i++) {
                StringBuilder sb = new StringBuilder();
	        	File f = new File(dataFiles[i].toString());
                String fileName = f.getName().split("\\.")[0];
		        scanner = new Scanner(f);
		        while (scanner.hasNextLine()){
		        	sb.append(scanner.nextLine());
		        	sb.append(System.getProperty("line.separator"));
	            }
                String script = sb.toString();
                Object o = cx.evaluateString(scope, script, fileName, 0, null);
                root.put(fileName, o);
	        }


            Object[] nodeFiles = nodes.toArray();
            // Parse all the xml nodes
            for(int i = 0; i < nodeFiles.length; i++) {
                File f = new File(nodeFiles[i].toString());
                String fileName = f.getName().split("\\.")[0];
                root.put(fileName, freemarker.ext.dom.NodeModel.parse(f));
            }




            root.put("objectConstructor", new ObjectConstructor());
            Environment processingEnvironment = template.createProcessingEnvironment(root, out, new RhinoWrapper());

            // Import all the given deps into the processing environment.
            importLibs(processingEnvironment, deps);

            processingEnvironment.process();
			out.flush();
		} catch (IOException e) {
			System.out.println("读取模板文件IO异常！");
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch(EvaluatorException e) {
            System.err.println(e.getMessage() + " at line number: " + e.getLineNumber());
            System.err.println(e.getScriptStackTrace());
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally {
			try {
				out.close();
				Context.exit();
				if (scanner != null)
					scanner.close();
				out = null;
			} catch (IOException e) {
				System.out.println("读取模板文件IO异常！");
				e.printStackTrace();
			}
		}
	}

    private static void importLibs(Environment environment, JSONArray deps) throws IOException, TemplateException {
        if (deps != null) {
            for (Object depJSON : deps) {
                String dep = (String) depJSON;
                environment.importLib(dep, getNamespaceForDep(dep));
            }
        }
    }

    private static String getNamespaceForDep(String dep) {
        int slash = dep.lastIndexOf("/");
        int ftl = dep.lastIndexOf(".ftl");
        return dep.substring(slash > 0 ? slash + 1 : 0, ftl > 0 ? ftl : dep.length() - 1);
    }


    /**
	 * 初始化模板配置
	 *
	 * @param configFile
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
		//scanFolder(templateDir);

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
	 * @param configContent
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
			System.err.println("Config File does not exist!");
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
