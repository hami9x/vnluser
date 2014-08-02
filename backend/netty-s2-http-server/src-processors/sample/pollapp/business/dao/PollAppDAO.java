package sample.pollapp.business.dao;

import java.util.List;

import sample.pollapp.model.Poll;

public interface PollAppDAO {
	public List<Poll> getAllPolls();
	
	public Poll getPoll(int id);
}
