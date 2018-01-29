package focusedCrawler.target.repository;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.DocWriteRequest.OpType;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.percolator.PercolateQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.TargetModelElasticSearch;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchClientFactory;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;

public class ElasticSearchTargetRepository implements TargetRepository {

	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	private Client client;
	private String typeName;
	private String indexName;

	public ElasticSearchTargetRepository(ElasticSearchConfig config, String indexName, String typeName) {
		this.client = ElasticSearchClientFactory.createClient(config);
		this.indexName = indexName;
		this.typeName = typeName;
		// this.createIndexMapping(indexName);
	}

	// private void createIndexMapping(String indexName) {
	//
	// boolean exists =
	// client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
	//
	// if (!exists) {
	// String targetMapping = "" + "{" + " \"properties\": {"
	// + " \"domain\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
	// + " \"words\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
	// + " \"wordsMeta\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
	// + " \"retrieved\": {\"type\": \"date\",\"format\":
	// \"dateOptionalTime\"},"
	// + " \"text\": {\"type\": \"string\"},"
	// + " \"title\": {\"type\": \"string\"},"
	// + " \"url\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
	// + " \"topPrivateDomain\": {\"type\": \"string\",\"index\":
	// \"not_analyzed\"},"
	// + " \"isRelevant\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
	// + " \"relevance\": {\"type\": \"double\"}" + " }" + "}";
	//
	// client.admin().indices().prepareCreate(indexName).addMapping(typeName,
	// targetMapping, XContentType.JSON)
	// .execute().actionGet();
	// }
	// }

	@Override
	public boolean insert(Page page) {

		TargetModelElasticSearch data = new TargetModelElasticSearch(page);
		boolean isCreated = false;

		// if (StringUtils.isNotBlank(data.getText())) {

		String docId = page.getURL().toString();

		// We use upsert to avoid overriding existing fields in previously
		// indexed documents
		UpdateResponse response = client.prepareUpdate(indexName, typeName, docId)
				.setDoc(serializeAsJson(data), XContentType.JSON).setDocAsUpsert(true).execute().actionGet();

		IndexRequestBuilder request = client.prepareIndex(indexName, typeName, docId);
		request.setSource(serializeAsJson(data), XContentType.JSON);
		request.setOpType(OpType.INDEX);
		request.setPipeline("langdetect-pipeline");

		request.execute().actionGet();
		isCreated = response.status().equals(RestStatus.OK) ? true : false;
		// }

		// Set<String> category = filterDocument(data);
		//
		// if (CollectionUtils.isNotEmpty(category)) {
		// data.setCategory(category);
		//
		// client.prepareUpdate(indexName, "filtered",
		// docId).setDoc(serializeAsJson(data), XContentType.JSON)
		// .setDocAsUpsert(true).execute().actionGet();
		// }

		return isCreated;
	}

	private Set<String> filterDocument(TargetModelElasticSearch data) {
		XContentBuilder docBuilder = null;

		try {
			docBuilder = XContentFactory.jsonBuilder();
			docBuilder.startObject();
			docBuilder.field("title", data.getTitle());
			docBuilder.field("text", data.getText());
			docBuilder.endObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PercolateQueryBuilder percolateQuery = new PercolateQueryBuilder("query", "articles", docBuilder.bytes(),
				XContentType.JSON);

		SearchRequestBuilder percolateRequest = client.prepareSearch(indexName);

		percolateRequest.setQuery(percolateQuery);
		percolateRequest.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
		SearchResponse response = percolateRequest.execute().actionGet();

		SearchHit[] searchHits = response.getHits().getHits();

		Set<String> category = new HashSet<String>();

		for (SearchHit hit : searchHits) {
			Map<String, Object> source = hit.getSource();

			category.add((String) source.get("category"));

			// System.out.println("PRINTING PROFILE CATEGORY " + category);
		}
		return category;
	}

	private String serializeAsJson(Object model) {
		String targetAsJson;
		try {
			targetAsJson = mapper.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize TargetModel to JSON.", e);
		}
		return targetAsJson;
	}

	@Override
	public void close() {
		client.close();
	}

}
