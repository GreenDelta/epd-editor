package epd.model;

import java.util.ArrayList;
import java.util.List;

public class EpdProfile {

	public String id;

	public String name;

	public final List<Indicator> indicators = new ArrayList<>();
}
