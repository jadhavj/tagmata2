package com.gauronit.tagmata.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

public class SettingsUtil {

	private static Properties props;

	static {
		init();
	}

	private static void init() {
		props = new Properties();
		try {
			props.load(new BufferedReader(new FileReader(new File(
					"tagmata.properties"))));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane
					.showMessageDialog(
							null,
							"Your Tagmata installation seems to have been corrupted.\nPlease reinstall.",
							"Corrupted Installation", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void saveSearchFields(boolean[] values) {
		props.setProperty("searchFields", values[0] + ", " + values[1] + ", "
				+ values[2] + ", " + values[3] + ", " + values[4] + ", "
				+ values[5] + ", " + values[6]);
		saveSettings();
	}

	public static boolean[] getSearchFields() {
		String[] tokens = props.getProperty("searchFields",
				"true, true, true, true, true, true, true").split(",");
		return new boolean[] { Boolean.parseBoolean(tokens[0].trim()),
				Boolean.parseBoolean(tokens[1].trim()),
				Boolean.parseBoolean(tokens[2].trim()),
				Boolean.parseBoolean(tokens[3].trim()),
				Boolean.parseBoolean(tokens[4].trim()),
				Boolean.parseBoolean(tokens[5].trim()),
				Boolean.parseBoolean(tokens[6].trim()) };
	}
	
	private static void saveSettings() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File("tagmata.properties")));
			props.store(out, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveSuperFuzzy(Boolean val) {
		props.setProperty("superFuzzy", val.toString());
		saveSettings();
	}
	
	public static boolean getSuperFuzzy() {
		return Boolean.parseBoolean(props.getProperty("superFuzzy"));
	}
	
	public static void saveSelectedIndexes(String[] selIndexes) {
		String selIndStr = "";
		for (int i = 0; i < selIndexes.length; i++) {
			selIndStr += selIndexes[i] + ", ";
		}
		selIndStr.substring(0, selIndStr.length() - 1);
		props.setProperty("selectedIndexes", selIndStr);
		saveSettings();
	}
	
	public static String[] getSelectedIndexes() {
		return props.getProperty("selectedIndexes", "all, General").split(",");
	}
}
