package stashhelper;

import com.ccreanga.bitbucket.rest.client.http.BitBucketClientFactory;
import com.ccreanga.bitbucket.rest.client.http.BitBucketCredentials;

public class StashConfig {
	
	private static final String DEMO_USER = "demo";
	private static final String DEMO_PASSWORD = "demo123";
	private static final String DEMO_SERVER = "http://sensefy1.zaizicloud.net:7990";
	
	
	private static BitBucketClientFactory bitBucketClientFactory;
	
	public static BitBucketClientFactory getClientFactory(){
		if(bitBucketClientFactory == null){
			bitBucketClientFactory = new BitBucketClientFactory(DEMO_SERVER, new BitBucketCredentials(DEMO_USER, DEMO_PASSWORD));
		}
		return bitBucketClientFactory;
	}

}
