package tamaized.gitjource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Resources;
import com.google.gson.stream.JsonReader;

public class GitJource {

	private static final String avatarLoc = ".git/avatars/";

	public static void main(String[] args) {
		try {
			if (args.length > 1) {

				String username = args[0];
				String repo = args[1];
				String repoURL = "https://api.github.com/repos/" + username + "/" + repo + "/";

				decodeJSON(repoURL + "contributors");

				ProcessBuilder builder = new ProcessBuilder(new String[] { "gource", "--user-image-dir", avatarLoc, "--seconds-per-day", args.length > 2 ? args[2] : "10" });
				builder.start();
			} else {
				System.out.println("Not enough Args");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void decodeJSON(String url) throws MalformedURLException, IOException {
		BufferedReader reader = Resources.asCharSource(new URL(url), StandardCharsets.UTF_8).openBufferedStream();
		JsonReader json = new JsonReader(reader);
		{
			json.beginArray();
			{
				while (json.hasNext()) {
					json.beginObject();
					{
						String user = "";
						String avatar = "";
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "login":
									user = json.nextString();
									break;
								case "avatar_url":
									avatar = json.nextString();
									break;
								default:
									json.skipValue();
									break;
							}
						}
						System.out.println("Downloading Avatar for " + user);
						FileUtils.copyURLToFile(new URL(avatar), new File(avatarLoc + user + ".png"));
					}
					json.endObject();
				}
			}
			json.endArray();
		}
		json.close();
	}

}
