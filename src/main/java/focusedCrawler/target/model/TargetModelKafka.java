package focusedCrawler.target.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import focusedCrawler.target.classifier.TargetRelevance;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TargetModelKafka implements Serializable {

    @JsonProperty("url")
    private String url;

    @JsonProperty("redirected_url")
    private String redirectedUrl;

    @JsonProperty("html")
    private String html;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("response_headers")
    private Map<String, List<String>> responseHeaders;

    @JsonProperty("fetch_time")
    private long fetchTime;

    @JsonProperty("relevance")
    private TargetRelevance relevance;

    @JsonProperty("crawler_id")
    private String crawlerId;

    public TargetModelKafka() {
    }

    public TargetModelKafka(Page page) {
        if (page.getURL() != null) {
            this.url = page.getURL().toString();
        }

        if (page.getRedirectedURL() != null) {
            this.redirectedUrl = page.getRedirectedURL().toString();
        }

        if (page.isHtml()) {
            String contentAsString = page.getContentAsString();
            this.html = contentAsString;
        }

        this.responseHeaders = page.getResponseHeaders();
        this.fetchTime = page.getFetchTime();
        this.contentType = page.getContentType();
        this.relevance = page.getTargetRelevance();
        this.crawlerId = page.getCrawlerId();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRedirectedUrl() {
        return redirectedUrl;
    }

    public void setRedirectedUrl(String redirectedUrl) {
        this.redirectedUrl = redirectedUrl;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public String getContentType() {
        return this.contentType;
    }

    public TargetRelevance getRelevance() {
        return relevance;
    }

    public void setRelevance(TargetRelevance relevance) {
        this.relevance = relevance;
    }

    public String getCrawlerId() {
        return crawlerId;
    }

    public void setCrawlerId(String crawlerId) {
        this.crawlerId = crawlerId;
    }
}
