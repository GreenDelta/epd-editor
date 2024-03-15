package epd.profiles;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "profile", namespace = EpdProfile2.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class EpdProfile2 {

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




}
