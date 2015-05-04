package _recorder._recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class RecorderLauncher {

	public final static void main(String[] args) {
		String timeout = "60";
		String outputDir = "/home/tonigor/btcdata";
		String jarPath = "/home/tonigor/JavaBTCproject/_recorder/target/recorder-3.0.1-SNAPSHOT.jar";
		if (args.length>0) {
			timeout = args[0];
			outputDir = args[1];
			jarPath = args[2];
		}
		String javaExecutable = System.getProperty("java.home") + "/bin/java";
		List<Process> processes = new ArrayList<Process>();
		for (int i = 0; i < Recorder.watchList.length; i++) {
			try {
				processes.add(Runtime.getRuntime().exec("timeout " + timeout 
						+ " " + javaExecutable + " -cp " + jarPath +" _recorder._recorder.Recorder " 
						+ outputDir + " " + Integer.toString(i)));
				System.out.println("timeout " + timeout 
						+ " " + javaExecutable + " -cp " + jarPath +" _recorder._recorder.Recorder " 
						+ outputDir + " " + Integer.toString(i));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
