package org.tsitle.demo_cli_app_critical_path.json;

import com.google.gson.*;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class Serializer {
	@SuppressWarnings("unused")
	private static class HolidaySerializer implements JsonSerializer<AppConfig.Holiday> {
		@Override
		public JsonElement serialize(AppConfig.Holiday holiday, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(holiday.templateStr());
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	public static @NonNull String appConfigToJsonString(@NonNull AppConfig appConfig) throws IOException {
		try {
			return buildGsonObj().toJson(appConfig);
		} catch (JsonIOException e) {
			throw new IOException("JsonIOException while producing JSON: " + e.getMessage());
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static @NonNull Gson buildGsonObj() {
		GsonBuilder gsonBldr = new GsonBuilder();
		gsonBldr.registerTypeAdapter(AppConfig.Holiday.class, new HolidaySerializer());
		return gsonBldr.setPrettyPrinting().disableHtmlEscaping().create();
	}
}
