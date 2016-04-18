package per.elwin.restartadbd;

import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by elwinxiao on 2016/4/18.
 */
public class DeprecatedCode {
	static final String TAG = MainActivity.TAG;

	private boolean restartAdbd2() throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec("su -c ls >/dev/null");
		process.waitFor();
		int suResult = process.exitValue();
		if (suResult != 0) {
			Log.e(TAG, "get root access failed");
			return false;
		}

		boolean isSuccess = startWork2(process);
		return isSuccess;
	}
	private boolean startWork2(Process process) throws IOException, InterruptedException {
		InputStreamReader reader = new InputStreamReader(process.getInputStream());
		OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());

		boolean isSuccess = execCmd2("stop adbd", process, reader, writer);
		if (!isSuccess) return isSuccess;

//		System.setProperty("service.adb.tcp.port", "5555");
//		String currentProp = System.getProperty("service.adb.tcp.port");
//		Log.i(TAG, "currentProp=" + currentProp);

		execCmd2("setprop service.adb.tcp.port 5555", process, reader, writer);

		isSuccess = execCmd2("start adbd", process, reader, writer);
		return isSuccess;
	}
	private boolean execCmd2(String cmd, Process process, InputStreamReader reader, OutputStreamWriter writer) throws IOException, InterruptedException {
		Log.i(TAG, "cmd: " + cmd);
		writer.write(cmd + "\n");
		writer.flush();

		process.waitFor();
		int status = process.exitValue();
		String rsp = readResponse2(reader);

		Log.i(TAG, "`-result:  status=" + status + ", response=" + rsp);
		return status == 0;
	}

	//	private void printResult(BufferedWriter writer, BufferedReader reader) throws IOException {
//		writer.write("echo $?\n");
//	}
	private String readResponse2(InputStreamReader reader) throws IOException {
		if (reader.ready()) {
			char[] buffer = new char[128];
			int len = 0;
			StringBuffer stringBuffer = new StringBuffer();
			while ((len = reader.read(buffer)) != -1) {
				stringBuffer.append(buffer);
			}

			return stringBuffer.toString();
		}

		return "";
	}

}
