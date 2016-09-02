package focusedCrawler.target;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.target.repository.FilesTargetRepository.RepositoryIterator;


public class FilesTargetRepositoryTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	static String html;
	static String url;
	static Map<String, List<String>> responseHeaders;
	
	@BeforeClass
	static public void setUp() {
		url = "http://example.com";
		html = "<html><body>Hello World! Hello World! Hello World!</body></html>";
		responseHeaders = new HashMap<>();
		responseHeaders.put("content-type", asList("text/html"));
	}
	
	@Test
	public void shouldStoreAndIterageOverData() throws IOException {
		// given
	    String folder = tempFolder.newFolder().toString(); 
		Page target = new Page(new URL(url), html, responseHeaders);
		FilesTargetRepository repository = new FilesTargetRepository(folder);
		
		// when
		repository.insert(target);
		repository.close();
		// then
		RepositoryIterator it = repository.iterator();
		assertThat(it.hasNext(), is(true));
		TargetModelJson page = it.next();
        assertThat(page.getResponseBody(), is(html));
        assertThat(page.getUrl(), is(url));
        assertThat(page.getResponseHeaders().get("content-type").get(0), is("text/html"));
	}
	
	@Test
    public void shoudNotCreateFilesLargerThanMaximumSize() throws IOException {
        // given
        String folder = tempFolder.newFolder().toString(); 
        
        String url1 = "http://a.com";
        String url2 = "http://b.com";
        
        Page target1 = new Page(new URL(url1), html);
        Page target2 = new Page(new URL(url2), html);
        
        long maxFileSize = 200;
        FilesTargetRepository repository = new FilesTargetRepository(folder, maxFileSize);
        
        // when
        repository.insert(target1);
        repository.insert(target2);
        repository.close();
        
        Iterator<TargetModelJson> it = repository.iterator();
        
        // then
        TargetModelJson page;
        
        assertThat(it.hasNext(), is(true));
        page = it.next();
        
        assertThat(page, is(notNullValue()));
        assertThat(page.getResponseBody(), is(html));
        
        assertThat(it.hasNext(), is(true));
        page = it.next();
        
        assertThat(page, is(notNullValue()));
        assertThat(page.getResponseBody(), is(html));
        
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
        
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
        
        File[] files = new File(folder).listFiles();
        assertThat(files.length, is(2));
        assertThat(files[0].length(), is(lessThan(maxFileSize)));
        assertThat(files[1].length(), is(lessThan(maxFileSize)));
    }
	
	@Test
    public void sholdIterateOverEmptyFolder() throws IOException {
        // given
        String folder = tempFolder.newFolder().toString(); 
        
        FilesTargetRepository repository = new FilesTargetRepository(folder);
        
        // when
        Iterator<TargetModelJson> it = repository.iterator();
        
        // then
        assertThat(it.hasNext(), is(false));
        assertThat(it.next(), is(nullValue()));
    }

}