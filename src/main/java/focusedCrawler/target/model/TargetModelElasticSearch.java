package focusedCrawler.target.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.net.InternetDomainName;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.readability.Readability;

public class TargetModelElasticSearch {

	private String domain;
	private String url;
	private String host;
	private String title;
	private String text;
	private Date retrieved;
	private String[] words;
	private String[] wordsMeta;
	private String topPrivateDomain;
	private String html;
	private Map<String, List<String>> responseHeaders;
	private String isRelevant;
	private double relevance;

	private Set<String> category;

	public TargetModelElasticSearch() {
		// mandatory for object unserialization
	}

	public TargetModelElasticSearch(Page page) {
		this.url = page.getURL().toString();
		this.host = page.getURL().getProtocol().concat("://").concat(page.getURL().getHost()).concat("/");
		this.retrieved = new Date();
		this.domain = page.getDomainName();
		this.html = page.getContentAsString();

		this.responseHeaders = page.getResponseHeaders();
		this.topPrivateDomain = LinkRelevance.getTopLevelDomain(page.getDomainName());
		this.isRelevant = page.getTargetRelevance().isRelevant() ? "relevant" : "irrelevant";
		if (page.isHtml()) {
			try {
				Readability readability = new Readability(page.getContentAsString());
				readability.init();
				this.words = page.getParsedData().getWords();
				this.wordsMeta = page.getParsedData().getWordsMeta();
				this.title = page.getParsedData().getTitle();

				this.text = DefaultExtractor.getInstance().getText(readability.html());

				// this.html = readability.html();
			} catch (BoilerpipeProcessingException | ArrayIndexOutOfBoundsException e) {
				this.text = "";
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.relevance = page.getTargetRelevance().getRelevance();
		}
	}

	public TargetModelElasticSearch(TargetModelCbor model) {

		URL url;
		try {
			url = new URL(model.url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("page has an invalid URL: " + model.url);
		}
		String rawContent = (String) model.response.get("body");

		Page page = new Page(url, rawContent);
		page.setParsedData(new ParsedData(new PaginaURL(url, rawContent)));

		// this.html = rawContent;
		this.url = model.url;
		this.retrieved = new Date(model.timestamp * 1000);
		this.words = page.getParsedData().getWords();
		this.wordsMeta = page.getParsedData().getWordsMeta();
		this.title = page.getParsedData().getTitle();
		this.domain = url.getHost();

		try {
			this.text = DefaultExtractor.getInstance().getText(page.getContentAsString());
		} catch (Exception e) {
			this.text = "";
		}

		InternetDomainName domainName = InternetDomainName.from(page.getDomainName());
		if (domainName.isUnderPublicSuffix()) {
			this.topPrivateDomain = domainName.topPrivateDomain().toString();
		} else {
			this.topPrivateDomain = domainName.toString();
		}
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getRetrieved() {
		return retrieved;
	}

	public void setRetrieved(Date retrieved) {
		this.retrieved = retrieved;
	}

	public String[] getWords() {
		return words;
	}

	public void setWords(String[] words) {
		this.words = words;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String[] getWordsMeta() {
		return wordsMeta;
	}

	public void setWordsMeta(String[] wordsMeta) {
		this.wordsMeta = wordsMeta;
	}

	public String getTopPrivateDomain() {
		return topPrivateDomain;
	}

	public void setTopPrivateDomain(String topPrivateDomain) {
		this.topPrivateDomain = topPrivateDomain;
	}

	public String getIsRelevant() {
		return isRelevant;
	}

	public void setIsRelevant(String isRelevant) {
		this.isRelevant = isRelevant;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

	public Set<String> getCategory() {
		return category;
	}

	public void setCategory(Set<String> category) {
		this.category = category;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

}