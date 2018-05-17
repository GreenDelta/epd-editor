package epd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EpdProfile {

	public String id;

	public String name;

	public final List<Indicator> indicators = new ArrayList<>();

	/** Get the indicator with the given ID from the this profile. */
	public Indicator indicator(String uuid) {
		if (uuid == null)
			return null;
		for (Indicator i : indicators) {
			if (Objects.equals(uuid, i.uuid))
				return i;
		}
		return null;
	}
}
