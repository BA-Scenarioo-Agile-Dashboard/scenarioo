package org.scenarioo.model.docu.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocFile {

	private String name;
	private String url;
	private FileType type;

	private List<Link> links = new ArrayList<Link>();

	public DocFile(){}

	public DocFile(String name, String url, FileType type){
		this.setName(name);
		this.setUrl(url);
		this.setType(type);
	}

	public void addLink(Link link){
		getLinks().add(link);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}
}
