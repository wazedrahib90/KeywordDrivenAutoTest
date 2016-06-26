package com.keyword.automation.base.browser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取Firefox浏览器的安装参数
 * 
 * @author airpy
 *
 */
public class FirefoxBrowserProfile {
	private Map<String, HashMap<String, String>> sectionsMap = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, String> itemsMap = new HashMap<String, String>();
	private File strProfilesIniPath = null;
	private String currentSection = "";
	public String strDefaultProfilePath = "";
	private String strFirefoxPath = "";

	public FirefoxBrowserProfile() {
		this.strProfilesIniPath = getProfilesIni();
		loadData(this.strProfilesIniPath);
	}

	public String getProfilePath() {
		return this.strProfilesIniPath.getAbsolutePath();
	}

	private String getDefaultProfilesPath() {
		if (this.strDefaultProfilePath.equals("")) {
			this.strDefaultProfilePath = getDefaultProfile();
		}
		return this.strDefaultProfilePath;
	}

	/**
	 * 寻找profiles.ini文件，返回File对象
	 * 
	 * @return File对象
	 */
	private File getProfilesIni() {
		File rtn = null;
		String strAppDataPath = System.getenv("AppData");
		if (strAppDataPath == null) {
			return null;
		}
		String strProfile = strAppDataPath + "/Mozilla/Firefox";
		File fireFoxProfile = new File(strProfile);
		if (fireFoxProfile.exists()) {
			this.strFirefoxPath = strProfile;
			File[] childFiles = fireFoxProfile.listFiles();
			for (int i = 0; i < childFiles.length; i++) {
				if (childFiles[i].getName().contains("profiles.ini")) {
					return childFiles[i];
				}
			}
		}
		return rtn;
	}

	/**
	 * 读取profiles.ini文件
	 * 
	 * @param file
	 *            文件句柄
	 */
	private void loadData(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim(); // 去字符串前后空格
				if (!"".equals(line)) {
					if ((line.startsWith("[")) && (line.endsWith("]"))) {
						if ((this.itemsMap.size() > 0) && (!"".equals(this.currentSection.trim()))) {
							this.sectionsMap.put(this.currentSection, this.itemsMap);
						}
						this.currentSection = "";
						this.itemsMap = null;

						this.currentSection = line.substring(1, line.length() - 1);
						this.itemsMap = new HashMap<String, String>();
					} else {
						int index = line.indexOf("=");
						if (index != -1) {
							String key = line.substring(0, index);
							String value = line.substring(index + 1, line.length());
							this.itemsMap.put(key, value);
						}
					}
				}
			}
			this.sectionsMap.put(this.currentSection, this.itemsMap);
			reader.close();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从Map中读取profiles.ini配置项
	 * 
	 * @param section
	 * @param item
	 * @return
	 */
	public String getItemValue(String section, String item) {
		if (this.sectionsMap.isEmpty()) {
			return null;
		}
		HashMap<String, String> map = (HashMap<String, String>) this.sectionsMap.get(section);
		if (map == null) {
			return "No such section:" + section;
		}
		String value = (String) map.get(item);
		if (value == null) {
			return "No such item:" + item;
		}
		return value;
	}

	/**
	 * 获取firefox默认安装路径
	 * 
	 * @return
	 */
	public String getDefaultProfile() {
		if (this.sectionsMap.isEmpty()) {
			return null;
		}
		for (String itr : this.sectionsMap.keySet()) {
			if (itr.contains("Profile")) {
				HashMap<String, String> item = (HashMap<String, String>) this.sectionsMap.get(itr);
				if (item.containsKey("Default")) {
					String strDefault = (String) item.get("Default");
					String isRelative = (String) item.get("IsRelative");
					String path = (String) item.get("Path");
					if (strDefault.equals("1")) {
						if (isRelative.equals("1")) {
							this.strDefaultProfilePath = (this.strFirefoxPath + "/" + path);
						} else {
							this.strDefaultProfilePath = path;
						}
					}
				} else if (item.containsKey("IsRelative")) {
					String isRelative = (String) item.get("IsRelative");
					String path = (String) item.get("Path");
					if (isRelative.equals("1")) {
						this.strDefaultProfilePath = (this.strFirefoxPath + "/" + path);
					} else {
						this.strDefaultProfilePath = path;
					}
				}
			}
		}
		return this.strDefaultProfilePath;
	}

	/**
	 * 获取firefox安装路径
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getFirefoxBinInstallPath() throws Exception {
		if (this.strProfilesIniPath == null) {
			this.strProfilesIniPath = getProfilesIni();
			loadData(this.strProfilesIniPath);
		}
		String sParentPath = getDefaultProfilesPath();
		File compatibilityFile = new File(new File(sParentPath), "compatibility.ini");

		String firefoxPath = null;
		try {
			if ((compatibilityFile.isFile())) {
				BufferedReader br = new BufferedReader(new FileReader(compatibilityFile));

				String s = null;
				while ((s = br.readLine()) != null) {
					if (s.startsWith("LastPlatformDir=")) {
						firefoxPath = s.split("=", 2)[1];
					}
				}
				br.close();
			} else {
				throw new Exception("compatibility.ini获取失败");
			}
		} catch (Exception e) {
		}
		return firefoxPath + "/firefox.exe";
	}

	public static void main(String[] args) {
		FirefoxBrowserProfile test = new FirefoxBrowserProfile();
		try {
			System.out.println(test.getFirefoxBinInstallPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
