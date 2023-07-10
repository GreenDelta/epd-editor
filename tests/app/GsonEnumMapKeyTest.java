package app;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import org.junit.Test;

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
}
