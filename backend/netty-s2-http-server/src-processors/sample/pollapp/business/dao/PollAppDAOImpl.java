package sample.pollapp.business.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import rfx.server.util.DatabaseDomainUtil;
import rfx.server.util.cache.Cachable;
import rfx.server.util.cache.CacheConfig;
import rfx.server.util.sql.CommonSpringDAO;
import rfx.server.util.sql.SqlTemplateString;
import rfx.server.util.sql.SqlTemplateUtil;
import sample.pollapp.model.Choice;
import sample.pollapp.model.Poll;

@CacheConfig( type = CacheConfig.LOCAL_CACHE_ENGINE, keyPrefix = "poll:", expireAfter = 6 )
public class PollAppDAOImpl extends CommonSpringDAO implements PollAppDAO {
	
	@SqlTemplateString
	String SQL_getAllPolls = SqlTemplateUtil.getSql("SQL_getAllPolls");

	@Override
	public List<Poll> getAllPolls() {
		Map<Integer, Poll> polls = new HashMap<>();
		SqlRowSet rowSet = jdbcTpl.queryForRowSet(SQL_getAllPolls);		
		while (rowSet.next()) {
			int poll_id = rowSet.getInt("poll_id");
			Poll poll = polls.get(poll_id);
			if(poll == null){
				poll = new Poll();
				poll.setId(poll_id);
				poll.setQuestion(rowSet.getString("question"));			
				polls.put(poll_id,poll);
			}
			Choice choice = new Choice();
			choice.setId(rowSet.getInt("choice_id"));
			choice.setPollId(poll_id);
			choice.setText(rowSet.getString("choice_text"));
			choice.setVotes(rowSet.getInt("votes"));
			poll.addChoice(choice);
		}
		return new ArrayList<Poll>(polls.values());
	}
	
	@Cachable
	public Poll getPoll(int id) {
		Poll poll = new Poll();
		poll.setId(id);
		poll.setPublishedDate(new Date());
		poll.setQuestion("what do you want to do ?");
		List<Choice> choices = new ArrayList<>();
		choices.add(new Choice(1, id, "eat", 1));
		choices.add(new Choice(2, id, "sleep", 22));
		poll.setChoices(choices );
		System.out.println(poll);
		rfx.server.util.Utils.sleep(2000);
		return poll;
	}
	
	public static void main(String[] args) {
		ApplicationContext context = DatabaseDomainUtil.getContext();
		PollAppDAO pollAppDAO = context.getBean(PollAppDAO.class);
		List<Poll> polls = pollAppDAO.getAllPolls();
		polls.parallelStream().forEach(new Consumer<Poll>(){
			@Override
			public void accept(Poll t) {
				System.out.println(t);
			} 
		});
		//System.out.println(new Gson().toJson(polls));
	}

}
