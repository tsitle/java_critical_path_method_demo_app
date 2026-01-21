package org.tsitle.demo_cli_app_critical_path.json;

import com.google.gson.*;
import io.github.tsitle.criticalpath.exceptions.InvalidInputDataException;
import io.github.tsitle.criticalpath.rawdata.filereaders.AbstractReadRawData;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.lang.reflect.Type;

public final class Deserializer {
	private static class HolidayDeserializer implements JsonDeserializer<AppConfig.Holiday> {
		@Override
		public AppConfig.Holiday deserialize(JsonElement jElement, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			String stringValue = jElement.getAsString();
			return new AppConfig.Holiday(stringValue, HolidayTemplateParser.parse(stringValue));
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Read AppConfig from a JSON file.
	 * @param filename Input filename (may have 'rsc:' prefix to read from the 'resources' folder)
	 * @return Deserialized JSON data
	 * @throws InvalidInputDataException If the file contains invalid JSON
	 * @throws IOException If an I/O error occurs while reading the file
	 */
	public static @NonNull AppConfig readAppConfigFromFile(@NonNull String filename)
			throws InvalidInputDataException, IOException {
		if (! filename.startsWith("rsc:")) {
			File file = new File(filename);
			try (InputStream is = new FileInputStream(file)) {  // throws FileNotFoundException
				return readAppConfigFromStream(is);
			} catch (FileNotFoundException e) {
				throw new IOException("file '" + filename + "' not found");
			}
		} else {
			return readAppConfigFromResourcesFile(filename.substring(4));
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Read AppConfig from a JSON stream.
	 * @param stream Input stream
	 * @return Deserialized JSON data
	 * @throws InvalidInputDataException If the file contains invalid JSON
	 * @throws IOException If an I/O error occurs while reading the file
	 */
	private static @NonNull AppConfig readAppConfigFromStream(@NonNull InputStream stream)
			throws InvalidInputDataException, IOException {
		final String errorMsgPrefix = "Error in input stream: ";

		return internalAppConfigRead(errorMsgPrefix, stream);
	}

	/**
	 * Read AppConfig from a JSON file in the resource folder.
	 * @param rscFilename Input filename
	 * @return Deserialized JSON data
	 * @throws InvalidInputDataException If the file contains invalid JSON
	 * @throws IOException If an I/O error occurs while reading the file
	 */
	private static @NonNull AppConfig readAppConfigFromResourcesFile(@NonNull String rscFilename)
			throws InvalidInputDataException, IOException {
		final String errorMsgPrefix = "Error in file: '" + rscFilename + "': ";

		try (InputStream is = AbstractReadRawData.class.getClassLoader().getResourceAsStream(rscFilename)) {
			if (is == null) {
				throw new IOException("file '" + rscFilename + "' not found");
			}
			return internalAppConfigRead(errorMsgPrefix, is);
		}
	}

	private static @NonNull AppConfig internalAppConfigRead(
				String errorMsgPrefix,
				@NonNull InputStream stream
			) throws InvalidInputDataException, IOException {
		try {
			GsonBuilder gsonBldr = new GsonBuilder();
			gsonBldr.registerTypeAdapter(AppConfig.Holiday.class, new HolidayDeserializer());
			Reader reader = new InputStreamReader(stream);
			return gsonBldr.create().fromJson(reader, AppConfig.class);
		} catch (JsonSyntaxException e) {
			throw new InvalidInputDataException(errorMsgPrefix + "could not parse JSON: " + e.getMessage());
		} catch (JsonIOException e) {
			throw new IOException(errorMsgPrefix + "error while parsing JSON: " + e.getMessage());
		}
	}
}
