package stashhelper;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.ccreanga.bitbucket.rest.client.ProjectClient;
import com.ccreanga.bitbucket.rest.client.http.BitBucketClientFactory;
import com.ccreanga.bitbucket.rest.client.http.BitBucketCredentials;
import com.ccreanga.bitbucket.rest.client.model.Project;
import com.ccreanga.bitbucket.rest.client.model.Repository;

public class StashConfig {

	private static final String DEMO_USER = "demo";
	private static final String DEMO_PASSWORD = "demo123";
	private static final String DEMO_SERVER = "http://sensefy1.zaizicloud.net:7990";

	private static BitBucketClientFactory bitBucketClientFactory;
	private static ProjectClient projectClient; 

	public static BitBucketClientFactory getClientFactory() {
		if (bitBucketClientFactory == null) {
			bitBucketClientFactory = new BitBucketClientFactory(DEMO_SERVER,
					new BitBucketCredentials(DEMO_USER, DEMO_PASSWORD));
		}
		return bitBucketClientFactory;
	}

	public static ProjectClient getProjecttClient(){
		if(projectClient == null){
			projectClient = getClientFactory().getProjectClient();
		}
		return projectClient;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ProjectClient client = getClientFactory().getProjectClient();
		Set<Repository> repos = client.getAllRepositories();
		for (Repository repo : repos) {
			System.out.println("repo name: " + repo.getName() + " slug : " + repo.getName() + " repo status : "
					+ repo.getStatusMessage());
		}
		
		Set<Project> projects = client.getProjects();
		for (Iterator iterator = projects.iterator(); iterator.hasNext();) {
			Project project = (Project) iterator.next();
			System.out.println("Project id : " + project.getId() + " name : " + project.getName() + " key : " + project.getKey());
			
		}
		System.out.println("the status of the project example : " + client.getProjectByKey("EX").get().getName());
		
		//test with project key and repo name
		Optional<Repository>  repo = client.getRepositoryBySlug("ex", "solr");
		Repository repository = repo.get();
		System.out.println("The repository : " + repository.getName() + "is : " + repository.getStatusMessage());
		
	}

}
