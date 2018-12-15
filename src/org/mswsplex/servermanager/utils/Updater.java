package org.mswsplex.servermanager.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Updater {
	public String getSpigotVersion() {
		try {
			HttpsURLConnection con = (HttpsURLConnection) new URL(
					"https://api.spigotmc.org/legacy/update.php?resource=53281").openConnection();
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				return buffer.readLine();
			} catch (Exception ex) {
			}
		} catch (Exception e) {
		}
		return null;
	}

}
