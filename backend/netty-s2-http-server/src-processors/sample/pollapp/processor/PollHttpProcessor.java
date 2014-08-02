package sample.pollapp.processor;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;

import rfx.server.configs.ContentTypePool;
import rfx.server.http.HttpProcessor;
import rfx.server.http.HttpProcessorConfig;
import rfx.server.http.data.HttpRequestEvent;
import rfx.server.http.data.service.DataService;
import rfx.server.http.data.service.StringDataService;
import rfx.server.util.DatabaseDomainUtil;
import rfx.server.util.StringUtil;
import sample.pollapp.business.dao.PollAppDAO;
import sample.pollapp.model.Poll;

import com.google.gson.annotations.Expose;

/**
 * @author trieu
 * 
 * simple sample processor 
 *
 */
@HttpProcessorConfig(uriPath= "/poll", contentType = ContentTypePool.JSON)
public class PollHttpProcessor extends HttpProcessor {
	
	PollAppDAO pollAppDAO = DatabaseDomainUtil.getContext().getBean(PollAppDAO.class);
	
	@Override
	protected DataService process(HttpRequestEvent event) {		
	
		String method = event.param("method", "getAllPolls");
		if(method.equals("getAllPolls")){
			List<Poll> polls = pollAppDAO.getAllPolls();
			return new MyData(polls);
		} else if(method.equals("getPoll")){
			int id = StringUtil.safeParseInt(event.param("id"));
			List<Poll> polls = Arrays.asList(pollAppDAO.getPoll(id));
			return new MyData(polls);
		}
		return new StringDataService("Empty");		
	}

	static class MyData implements DataService{		
		
		@Expose
		List<Poll> polls;	
		
		static final String classpath = MyData.class.getName();

		public List<Poll> getPolls() {			
			return polls;
		}
		
		
		public MyData(List<Poll> polls) {
			super();
			this.polls = polls;
		}


		@Override
		public void freeResource() {
			
		}
		
		@Override
		public String getClasspath() {
			return classpath;
		}

		@Override
		public boolean isRenderedByTemplate() {
			return false;
		}


		@Override
		public List<HttpHeaders> getHttpHeaders() {
			return null;
		}
		
	}

}
