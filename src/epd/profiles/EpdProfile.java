package epd.profiles;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "profile", namespace = EpdProfile.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class EpdProfile {

	static final String NS = "http://greendelta.com/epd-editor";

	@XmlElement(name = "id", namespace = NS)
	private String id;

	@XmlElement(name = "name", namespace = NS)
	private String name;

	@XmlElement(name = "description", namespace = NS)
	private String description;

	@XmlElement(name="dataUrl", namespace = NS)
	private String dataUrl;

	@XmlElementWrapper(name="modules", namespace = NS)
	@XmlElement(name ="module", namespace = NS)
	private List<Module> modules;

	@XmlElementWrapper(name = "indicators", namespace = NS)
	@XmlElement(name="indicator", namespace = NS)
	private List<Indicator> indicators;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public List<Module> getModules() {
		return modules != null ? modules : Collections.emptyList();
	}

	public List<Indicator> getIndicators() {
		return indicators;
	}

	public EpdProfile withId(String id) {
		this.id = id;
		return this;
	}

	public EpdProfile withName(String name) {
		this.name = name;
		return this;
	}

	public EpdProfile withDescription(String description) {
		this.description = description;
		return this;
	}

	public EpdProfile withDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
		return this;
	}

	public EpdProfile withModules(List<Module> modules) {
		this.modules = modules;
		return this;
	}

	public List<Module> withModules() {
		if (modules == null) {
			modules = new ArrayList<>();
		}
		return modules;
	}

	public EpdProfile withIndicators(List<Indicator> indicators) {
		this.indicators = indicators;
		return this;
	}

	public List<Indicator> withIndicators() {
		if (indicators == null) {
			indicators = new ArrayList<>();
		}
		return indicators;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof EpdProfile other))
			return false;
		return Objects.equals(this.id, other.id);
	}

	@Override
	public int hashCode() {
		return id != null
			? id.hashCode()
			: super.hashCode();
	}

}