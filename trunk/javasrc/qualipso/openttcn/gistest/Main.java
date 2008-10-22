package qualipso.openttcn.gistest;



import qualipso.openttcn.gistest.tri.TriCommunicationSA;

import com.openttcn.sdk.api.StartHere;
import com.openttcn.sdk.api.GeneralFailure;

/**
 * Executable class that binds the Adapters and Codecs to a Test Session
 */   
public class Main {
    private static final int SLEEP_INTERVAL_IN_MILLIS = 100000;
    private final static String SESSION_NAME = "my_test_session";
    private static void sleepForever() {
        while (true) {
            try { Thread.sleep(SLEEP_INTERVAL_IN_MILLIS); } catch (Exception e) { }
        }
    }  
    /** 
     * Main function
     * @param args Does not take any arguments
     * @see http://wiki.openttcn.com/media/index.php/OpenTTCN/Developer_corner/Creating_adapter_with_Java_SDK#Creating_Main.java
     */

    public static void main(String[] args) {
        try {
            StartHere.initialize();
            // Register the adapter to OpenTTCN server:
            StartHere.registerSelfToSession(SESSION_NAME);
	        com.openttcn.sdk.tri.StartHereSA.
	        	setCallbackHandler(new TriCommunicationSA());
	        com.openttcn.sdk.tri.StartHerePA.
	            setCallbackHandler(new com.openttcn.sdk.tri.TriPlatformPA());
	        com.openttcn.sdk.tci.StartHereCD.
	            setCallbackHandler(new qualipso.openttcn.gistest.tci.TciCDProvided());
	        // Enable printout of library diagnostic information:
            StartHere.setVerbose(true);
        } 
        catch (GeneralFailure e) {
            System.err.println("Error: " + e.getErrorCode() + ": " + e.getMessage());
            System.exit(1);
        }
        System.out.println("Init OK!");
        sleepForever();
    }
}
