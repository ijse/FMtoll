package cn.ijser.bean;

import java.io.File;
import java.util.Map;

public class ConfigBean {
	private String encoding;
	private File viewFolder;
	private Map<String, String> sharedVariables;

	public Map<String, String> getSharedVariables() {
		return sharedVariables;
	}

	public void setSharedVariables(Map<String, String> sharedVariables) {
		this.sharedVariables = sharedVariables;
	}

	public String getEncoding() {
		return encoding;
	}

	public File getViewFolder() {
		return viewFolder;
	}

	public void setViewFolder(File viewFolder) {
		this.viewFolder = viewFolder;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
