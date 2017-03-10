package stashhelper;

import java.util.Set;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.ccreanga.bitbucket.rest.client.ProjectClient;
import com.ccreanga.bitbucket.rest.client.model.Repository;

public class StashSpeechHandler implements Speechlet {
	
	private static final String ITEM_SLOT = "repo";
	
	@Override
	public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
		Intent intent= request.getIntent();
		String intentName=intent!=null?intent.getName():null;
        
        if("GetRepositoryIntent".equals(intentName)){
            return getRepoList();
        }else if("GetRepositoryStatusIntent".equals(intentName)){
            return isRepoPrivate(intent);
        }else if("GetPullUpdateIntent".equals(intentName)){
            return getPullUpdates(intent);
        }else if("GetIssueIntent".equals(intentName)){
            return getReportedIssue(intent);
        }else if("GetLastCommitIntent".equals(intentName)){
            return getLastCommit(intent);
        }else if("AMAZON.HelpIntent".equals(intentName)){
            return getHelp();
        }else if("AMAZON.StopIntent".equals(intentName)){
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        	outputSpeech.setText("Goodbye");
        	return SpeechletResponse.newTellResponse(outputSpeech);
        }else{
             throw new SpeechletException("Invalid Intent");
        }
		
	
    }

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
		
        String speechOutput ="Welcome to stash helper";
		String repromtText="For instruction on what you can say, please say help me";
		
		return newAskResponse(speechOutput,repromtText);
		
	}

	@Override
	public void onSessionEnded(SessionEndedRequest arg0, Session arg1) throws SpeechletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionStarted(SessionStartedRequest arg0, Session arg1) throws SpeechletException {
		// TODO Auto-generated method stub

	}
	
	
	private SpeechletResponse getRepoList(){
		String speechText = "Sorry get Repositroy is under construction";
		
		ProjectClient client = StashConfig.getClientFactory().getProjectClient();
		Set<Repository> repos = client.getAllRepositories();
		for (Repository repository : repos) {
			String repoName = repository.getName();
		}

        SimpleCard card = new SimpleCard();
        card.setTitle("GetRepository");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newTellResponse(speech, card);
	}
	
	private SpeechletResponse isRepoPrivate(Intent intent){
		Slot itemSlot = intent.getSlot(ITEM_SLOT);
		String speechText = "Sorry, get Repositroy "+itemSlot.getValue()+"'s status is under construction";
		
        SimpleCard card = new SimpleCard();
        card.setTitle("GetStatus");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
        
//        return SpeechletResponse.newAskResponse(speech, reprompt, card);
        return SpeechletResponse.newTellResponse(speech, card);
	}
	private SpeechletResponse getPullUpdates(Intent intent){
		String speechText = "Sorry, get pull update is under construction";

        SimpleCard card = new SimpleCard();
        card.setTitle("GetUpdate");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newTellResponse(speech, card);
	}
	private SpeechletResponse getReportedIssue(Intent intent){
		String speechText = "Sorry, get reported issue is under construction";

        SimpleCard card = new SimpleCard();
        card.setTitle("GetIssue");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newTellResponse(speech, card);
	}
	
	private SpeechletResponse getLastCommit(Intent intent){
		String speechText = "Sorry, get last commit  is under construction";

        SimpleCard card = new SimpleCard();
        card.setTitle("GetLastCommit");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);
        
        return SpeechletResponse.newTellResponse(speech, card);
	}
	private SpeechletResponse getHelp(){
		String speechText = "you can ask about your repository in future, not now";

        SimpleCard card = new SimpleCard();
        card.setTitle("Help");
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
    
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);

        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

}
