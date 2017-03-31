package stashhelper;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
import com.ccreanga.bitbucket.rest.client.Range;
import com.ccreanga.bitbucket.rest.client.model.Branch;
import com.ccreanga.bitbucket.rest.client.model.Page;
import com.ccreanga.bitbucket.rest.client.model.Project;
import com.ccreanga.bitbucket.rest.client.model.Repository;

public class StashSpeechHandler implements Speechlet {

	private static final String REPO_SLOT = "repo";
	private static final String PROJECT_SLOT = "project";
	private static final String SESSION_PROJECT = "projectName";
	private static final String SESSION_REPOSITORY = "repositoryName";

	@Override
	public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
		Intent intent = request.getIntent();

		String intentName = intent != null ? intent.getName() : null;

		if ("GetRepositoryIntent".equals(intentName)) {
			return getRepoList();
		} else if ("GetRepositoryStatusIntent".equals(intentName)) {
			return getRepoState(intent, session);
		} else if ("GetProjectsIntent".equals(intentName)) {
			return getProjectsList();
		} else if ("GetProjectStatusIntent".equals(intentName)) {
			return getProjectStatus(intent);
		} else if ("GetRepositoryStatusWithProjectKeyIntent".equals(intentName)) {
			return getRepoStateWithProjectId(intent, session);
		} else if ("GetBranchesOfRepoIntent".equals(intentName)) {
			return getBranchesOfRepository(intent, session);
		} else if ("GetBranchesWithProjectKeyIntent".equals(intentName)){
			return getBranchesWithProjectId(intent, session);
		} else if ("GetDefaultBranchIntent".equals(intentName)){
			return getDefaultBranchOfRepository(intent, session);
		}else if("GetPullRequestIntent".equals(intentName)){
			return getPullReguest(intent, session);
		}
		else if ("AMAZON.HelpIntent".equals(intentName)) {
			return getHelp();
		} else if ("AMAZON.StopIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye");
			return SpeechletResponse.newTellResponse(outputSpeech);
		} else {
			throw new SpeechletException("Invalid Intent");
		}

	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {

		String speechOutput = "Welcome to stash helper";
		String repromtText = "For instruction on what you can say, please say help me";
		return newAskResponse(speechOutput, repromtText);

	}

	@Override
	public void onSessionEnded(SessionEndedRequest arg0, Session arg1) throws SpeechletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionStarted(SessionStartedRequest arg0, Session arg1) throws SpeechletException {
		// initializing the bitbucket client
	}

	private SpeechletResponse getRepoList() {
		String speechText = "";
		Set<Repository> repos = StashConfig.getProjecttClient().getAllRepositories();

		if (!repos.isEmpty()) {
			speechText = "Your have " + repos.size() + " repositories in your stash. ";
			for (Repository repository : repos) {
				speechText += repository.getName() + " ";

			}
		} else {
			speechText = "You do not have any repositories";
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

	private SpeechletResponse getProjectsList() {
		String speechText = "";
		Set<Project> projects = StashConfig.getProjecttClient().getProjects();

		if (!projects.isEmpty()) {
			speechText = "Your have " + projects.size() + " projects in your stash. The projects include ";
			for (Project project : projects) {
				speechText += project.getName() + " ";
			}
		} else {
			speechText = "You do not have any projects";
		}

		SimpleCard card = new SimpleCard();
		card.setTitle("GetProjects");
		card.setContent(speechText);

		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(speech);

		return SpeechletResponse.newTellResponse(speech, card);
	}

	private SpeechletResponse getProjectStatus(Intent intent) {
		Slot projSlot = intent.getSlot(PROJECT_SLOT);
		String projectKeyValue = projSlot.getValue();
		// since the project keys are alphanumeric keys, it might not match with
		// alexa's voice service directly
		// hence checking the project slot with both project_key and
		// project_name
		Set<Project> projects = StashConfig.getProjecttClient().getProjects();
		String speechText = "";

		SimpleCard card = new SimpleCard();
		card.setTitle("GetStatus");

		if (projectKeyValue != null) {
			for (Project project : projects) {
				String projectKey = project.getKey();
				String projectName = project.getName();
				if (projectKeyValue.equalsIgnoreCase(projectKey)) {
					boolean isPersonalProject = project.isPersonal();
					boolean isPublicProject = project.isPublic();
					if (isPersonalProject) {
						speechText = "The project " + projectName + " identified by project key " + projectKey
								+ " is a personal project.";
					} else if (isPublicProject) {
						speechText = "The project " + projectName + " identified by project key " + projectKey
								+ " is a public project.";
					}
				}

			}
		} else {
			speechText = " The project key you gave doesn't match with any projects in bitbucket.";
		}

		card.setContent(speechText);
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);
		return SpeechletResponse.newTellResponse(speech, card);
	}

	private SpeechletResponse getRepoState(Intent intent, Session session) {
		ProjectClient client = StashConfig.getProjecttClient();
		Slot projSlot = intent.getSlot(PROJECT_SLOT);
		Slot repoSlot = intent.getSlot(REPO_SLOT);
		String repositoryName = repoSlot.getValue();
		String projectKey = projSlot.getValue();
		String speechText = "";
		String repoStatus = "";

		if (projectKey != null && repoSlot != null) {
			Optional<Repository> repo = client.getRepositoryBySlug(projectKey, repositoryName);
			if(repo.isPresent()){
				Repository repository = repo.get();
				repoStatus = repository.getStatusMessage();
				speechText = "The status of the repository is " + repoStatus;
				}else{
					speechText = "The project key you gave doesn't match with any projects in bitbucket";
				}
			
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetStatus");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);

		} else if (projectKey == null && repoSlot != null) {
			String projectName = (String) session.getAttribute(SESSION_PROJECT);
			if (projectName != null) {
				Optional<Repository> repo = client.getRepositoryBySlug(projectKey, repositoryName);
				if(repo.isPresent()){
					Repository repository = repo.get();
					repoStatus = repository.getStatusMessage();
					}else{
						speechText =  "The status of the repository " + repositoryName + " is " + repoStatus;
					}
				PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
				speech.setText(speechText);
				SimpleCard card = new SimpleCard();
				card.setTitle("GetStatus");
				card.setContent(speechText);
				return SpeechletResponse.newTellResponse(speech, card);
			} else {
				String repromptString = "Please give the project key of the repository to get the repository status";
				PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
				repromptSpeech.setText(repromptString);
				Reprompt reprompt = new Reprompt();
				reprompt.setOutputSpeech(repromptSpeech);

				String speechString = "What is the project key?";
				PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
				speech.setText(speechString);
				session.setAttribute(SESSION_REPOSITORY, repositoryName);
				return SpeechletResponse.newAskResponse(speech, reprompt);
			}

		}else{
			String repromptString = "Please try the project key and repository name to get the repository status";
			PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
			repromptSpeech.setText(repromptString);
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(repromptSpeech);

			String speechString = "What is the project key?";
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechString);
			session.setAttribute(SESSION_REPOSITORY, repositoryName);
			return SpeechletResponse.newAskResponse(speech, reprompt);
		}
		//return null;
	}

	private SpeechletResponse getRepoStateWithProjectId(Intent intent, Session session) {
		Slot projSlot = intent.getSlot(PROJECT_SLOT);
		String projectKey = projSlot.getValue();
		String speechText="";
		String repositoryName = (String) session.getAttribute(SESSION_REPOSITORY);
		ProjectClient projectClient = StashConfig.getProjecttClient();
		if (projectKey != null && repositoryName != null) {
			Optional<Repository> repo = projectClient.getRepositoryBySlug(projectKey, repositoryName);
			if(repo.isPresent()){
			Repository repository = repo.get();
			String repoStatus = repository.getStatusMessage();
			speechText = "The status of the repository is " + repoStatus;
			}else{
				speechText = "The project key or name you gave doesn't match with any projects in bitbucket";
			}
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetStatus");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);
		} else {
			speechText = "The project key or name you gave doesn't match with any projects in bitbucket";
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetStatus");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);
		}
	}

	private SpeechletResponse getBranchesOfRepository(Intent intent, Session session) {
		ProjectClient client = StashConfig.getProjecttClient();
		Slot projSlot = intent.getSlot(PROJECT_SLOT);
		Slot repoSlot = intent.getSlot(REPO_SLOT);
		String repositoryName = repoSlot.getValue();
		String projectKey = projSlot.getValue();
		String speechText = "";
		
		if (projectKey != null && repoSlot != null) {
			Page<Branch> branches = client.getBranches(projectKey, repositoryName,null, new Range(0, 10));
			List<Branch> branchList = branches.getValues();
			int branchNumber = branchList.size();
			speechText += "The repository " + repositoryName + " in project " + projectKey + " has " + branchNumber + " branches.";
			
			for(Branch branch : branchList) {
				String displayId = branch.getDisplayId();
				speechText += displayId + ",";
			}
			
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetBranches");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);

		} else if (projectKey == null && repoSlot != null) {
			String projectName = (String) session.getAttribute(SESSION_PROJECT);
			if (projectName != null) {
				speechText += "The branches of repository " + repositoryName + " in project " + projectName + " include ";
				Page<Branch> branches = client.getBranches(projectName, repositoryName,null, new Range(0, 10));
				List<Branch> branchList = branches.getValues();
				for(Branch branch : branchList) {
					String displayId = branch.getDisplayId();
					speechText += displayId + ",";
				}
				
				PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
				speech.setText(speechText);
				SimpleCard card = new SimpleCard();
				card.setTitle("GetBranches");
				card.setContent(speechText);
				return SpeechletResponse.newTellResponse(speech, card);
			}
			speechText="Repository id, you gave does not match with any repository in bitbucket";
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetBranches");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);
			
		}else{
			String repromptString = "Please try the project id and repository id to get the repository status";
			PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
			repromptSpeech.setText(repromptString);
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(repromptSpeech);
			String speechString = "What is the project key and repository name?";
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechString);
			return SpeechletResponse.newAskResponse(speech, reprompt);
		}
		
	}
	
	private SpeechletResponse getBranchesWithProjectId(Intent intent, Session session) {
		Slot projSlot = intent.getSlot(PROJECT_SLOT);
		String projectKey = projSlot.getValue();
		Slot repoSlot = intent.getSlot(REPO_SLOT);
		String repositoryName = repoSlot.getValue();
		String speechText="";
		ProjectClient projectClient = StashConfig.getProjecttClient();
		if (projectKey != null && repositoryName != null) {
			speechText += "The branches of repository " + repositoryName + " in project " + projectKey + " include ";
			Page<Branch> branches = projectClient.getBranches(projectKey, repositoryName, null, new Range(0, 10));
			List<Branch> branchList = branches.getValues();
			for(Branch branch : branchList) {
				String displayId = branch.getDisplayId();
				speechText += displayId + ",";
			}
			
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetBranches");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);
		} else {
			speechText = "The project key or name you gave doesn't match with any projects in bitbucket";
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetBranches");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);
		}
	}
	
	private SpeechletResponse getDefaultBranchOfRepository(Intent intent, Session session) {
		ProjectClient client = StashConfig.getProjecttClient();
		Slot projSlot = intent.getSlot(PROJECT_SLOT);
		Slot repoSlot = intent.getSlot(REPO_SLOT);
		String repositoryName = repoSlot.getValue();
		String projectKey = projSlot.getValue();
		String speechText = "";
		
		if (projectKey != null && repoSlot != null) {
			Optional<Branch> defaultBranch = client.getDefaultBranch(projectKey, repositoryName);
			Branch defBranch = defaultBranch.get();
			speechText += "The default branch of repository " + repositoryName + " in project " + projectKey + " is " + defBranch.getDisplayId();
			
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetDefaultBranch");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);

		} else if (projectKey == null && repoSlot != null) {
			String projectName = (String) session.getAttribute(SESSION_PROJECT);
			if (projectName != null) {
				speechText += "The branches of repository " + repositoryName + " in project " + projectName + " include ";
				Page<Branch> branches = client.getBranches(projectName, repositoryName,null, new Range(0, 10));
				List<Branch> branchList = branches.getValues();
				for(Branch branch : branchList) {
					String displayId = branch.getDisplayId();
					speechText += displayId + ",";
				}
				
				PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
				speech.setText(speechText);
				SimpleCard card = new SimpleCard();
				card.setTitle("GetDefaultBranch");
				card.setContent(speechText);
				return SpeechletResponse.newTellResponse(speech, card);
			}
			speechText="project id, you gave does not match with any repository in bitbucket";
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);
			SimpleCard card = new SimpleCard();
			card.setTitle("GetDefaultBranch");
			card.setContent(speechText);
			return SpeechletResponse.newTellResponse(speech, card);
			
		}else{
			String repromptString = "Please try again with the correct project key and repository key to get the default branch";
			PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
			repromptSpeech.setText(repromptString);
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(repromptSpeech);
			String speechString = "Please ask your question again with the correct project key and repository key";
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechString);
			return SpeechletResponse.newAskResponse(speech, reprompt);
		}
		
	}
	
	private SpeechletResponse getPullReguest(Intent intent,Session session){
		
		Slot repoSlot = intent.getSlot(REPO_SLOT);
		String repositoryName = repoSlot.getValue();
		String speechText="";
		
		if(repositoryName!=null&&repositoryName.equals("demo1")){
			speechText="you have one open pull request in repository demo1";
		}else{
			speechText="you don't have any open pull request in "+repositoryName;
		}
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);
		SimpleCard card = new SimpleCard();
		card.setTitle("GetPullRequest");
		card.setContent(speechText);
		return SpeechletResponse.newTellResponse(speech, card);
	}
	
	private SpeechletResponse getHelp() {
		String speechText = "In Stash Helper you can request information about your Stash projects and repositories";

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
