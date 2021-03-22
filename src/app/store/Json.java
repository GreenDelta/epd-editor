package app.store;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Json {

	private Json() {
	}

	public static void write(Object obj, File file) {
		try (var fos = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				 var buffer = new BufferedWriter(writer)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String string = gson.toJson(obj);
			buffer.write(string);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Json.class);
			log.error("failed to write " + obj + " to " + file, e);
		}
	}

	public static <T> T read(File file, Class<T> clazz) {
		try (FileInputStream fis = new FileInputStream(file)) {
			return read(fis, clazz);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Json.class);
			log.error("failed to read " + clazz + " from file " + file, e);
			return null;
		}
	}

	public static <T> T read(InputStream stream, Class<T> clazz) {
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			Gson gson = new Gson();
			return gson.fromJson(reader, clazz);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Json.class);
			log.error("failed to read " + clazz, e);
			return null;
		}
	}
}
