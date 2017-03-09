package stashhelper;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public class StashSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
	
	private static final Set<String> suportedApplicationIds=new HashSet<>();
	static{
		suportedApplicationIds.add("amzn1.ask.skill.291ee3aa-1ab1-47ea-b8c3-3af5d56b6adb");
	}
	
	public StashSpeechletRequestStreamHandler(){
		super(new StashSpeechHandler(),suportedApplicationIds);
	}

}
