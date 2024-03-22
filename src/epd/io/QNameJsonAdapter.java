package epd.io;

import java.lang.reflect.Type;

import javax.xml.namespace.QName;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * QName instances cannot be directly serialized (it would throw
 * an illegal reflective access exception) thus we need a type
 * adapter for them. They are also used as keys in maps and Gson
 * serializes such keys by simply calling {@code toString()} on
 * them. Thus, our type adapter needs to produce and parse the
 * exact same format as {@code QName.toString()}.
 */
public class QNameJsonAdapter implements
	JsonSerializer<QName>, JsonDeserializer<QName> {

	@Override
	public QName deserialize(
		JsonElement elem, Type type, JsonDeserializationContext ctx
	) throws JsonParseException {
		if (!QName.class.equals(type))
			return new Gson().fromJson(elem, type);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		var prim = elem.getAsJsonPrimitive();
		if (!prim.isString())
			return null;

		var s = prim.getAsString();
		var parts = s.split("}");
		return parts.length == 2
			? new QName(parts[0].substring(1), parts[1])
			: new QName(s);
	}

	@Override
	public JsonElement serialize(
		QName qName, Type type, JsonSerializationContext ctx
	) {
		return qName != null
			? new JsonPrimitive(qName.toString())
			: null;
	}
}
