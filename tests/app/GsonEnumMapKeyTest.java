package app;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;

public class GsonEnumMapKeyTest {

	public enum AB {
		A("B"),
		B("A");

		private final String s;

		AB(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}

	public static class Wrapper {
		public final HashMap<AB, String> idx = new HashMap<>();
	}

	public static class ABAdapter implements JsonDeserializer<AB> {

		@Override
		public AB deserialize(
			JsonElement elem, Type type, JsonDeserializationContext ctx
		) throws JsonParseException {
			if (!AB.class.equals(type))
				return new Gson().fromJson(elem, type);
			if (!elem.isJsonPrimitive())
				return null;
			var prim = elem.getAsJsonPrimitive();
			if (!prim.isString())
				return null;
			var s = prim.getAsString();
			for (var ab : AB.values()) {
				if (s.equals(ab.toString()))
					return ab;
			}
			return null;
		}
	}

	/**
	 * This is a quite confusing aspect of the Gson-API: when you use enumerations
	 * as keys in maps, the {@code toString} method of the respective enumeration
	 * item is used in the serialization but the item {@code name} is used for
	 * deserialization. Thus, you can get unexpected results when {@code toString}
	 * returns something else than {@code name} (like in the example below, you
	 * get B for A and A for B). You can also get the error: JsonSyntaxException:
	 * "duplicate key: null" if Gson does not know how to infer the enum items
	 * in this case.
	 */
	@Test
	public void testEnumKeyProblem() {
		var w = new Wrapper();
		w.idx.put(AB.A, "A");
		w.idx.put(AB.B, "B");
		var gson = new Gson();
		var json = gson.toJson(w);
		var copy = gson.fromJson(json, Wrapper.class);
		assertEquals("B", copy.idx.get(AB.A));
		assertEquals("A", copy.idx.get(AB.B));
	}

	/**
	 * Solves the problem described above with a type adapter.
	 */
	@Test
	public void testEnumKeyAdapter() {
		var w = new Wrapper();
		w.idx.put(AB.A, "A");
		w.idx.put(AB.B, "B");
		var json = new Gson().toJson(w);
		var copy = new GsonBuilder()
			.registerTypeAdapter(AB.class, new ABAdapter())
			.create()
			.fromJson(json, Wrapper.class);
		assertEquals("A", copy.idx.get(AB.A));
		assertEquals("B", copy.idx.get(AB.B));
	}
}
