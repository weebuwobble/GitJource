package tamaized.gitjource;

import com.google.common.io.Resources;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GitJource {

	private static final String avatarLoc = ".git/avatars/";

	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				String username = args[0];
				String repo = args[1];
				String repoURL = "https://api.github.com/repos/" + username + "/" + repo + "/";

				decodeContributors(repoURL + "contributors?anon=1");

				ProcessBuilder builder = new ProcessBuilder("gource", "--user-image-dir", avatarLoc, "--seconds-per-day", args.length > 2 ? args[2] : "10");
				builder.start();
			} else {
				System.out.println("Not enough Args");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void decodeContributors(String url) throws IOException {
		BufferedReader reader = Resources.asCharSource(new URL(url), StandardCharsets.UTF_8).openBufferedStream();
		JsonReader json = new JsonReader(reader);
		{
			json.beginArray();
			{
				while (json.hasNext()) {
					json.beginObject();
					{
						String user = "";
						String alias = "";
						String avatar = "";
						while (json.hasNext()) {
							switch (json.nextName()) {
								case "login":
									user = json.nextString();
									break;
								case "avatar_url":
									avatar = json.nextString();
									break;
								case "url":
									alias = decodeUsername(json.nextString(), user);
									break;
								default:
									json.skipValue();
									break;
							}
						}
						System.out.println("Downloading Avatar for " + user);
						File f = new File(avatarLoc + user + ".png");
						FileUtils.copyURLToFile(new URL(avatar), f);
						if (!alias.isEmpty() && !alias.equalsIgnoreCase(user)) {
							FileUtils.copyFile(f, new File(avatarLoc + alias + ".png"));
							System.out.println("Copied for alias: " + alias);
						}
					}
					json.endObject();
				}
			}
			json.endArray();
		}
		json.close();
	}

	private static String decodeUsername(String url, String user) throws IOException {
		String username = user;
		BufferedReader reader = Resources.asCharSource(new URL(url), StandardCharsets.UTF_8).openBufferedStream();
		JsonReader json = new JsonReader(reader);
		{
			try {
				json.beginObject();
				{
					while (json.hasNext()) {
						switch (json.nextName()) {
							case "name":
								username = json.nextString();
								break;
							default:
								json.skipValue();
								break;
						}
					}
				}
				json.endObject();
			} catch (IllegalStateException e) {
				// NO-OP
			}
		}
		json.close();
		return username;
	}

}
