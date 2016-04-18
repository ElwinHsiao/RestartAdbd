package per.elwin.restartadbd;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

//import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	static final String TAG = "RestartAdbd";
	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.textView1);
	}

	@Override
	public void onClick(View v) {
		if (mAsyncTask != null) {
			Toast.makeText(MainActivity.this, "Already started, please wait...", Toast.LENGTH_SHORT).show();
			return;
		}

		mAsyncTask = new InnerAsyncTask();
		mAsyncTask.execute();
	}

	private AsyncTask mAsyncTask;

	class InnerAsyncTask extends AsyncTask<Object, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Object... params) {
			try {
				return restartAdbd3();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean isSuccess) {
			if (isSuccess) {
				mTextView.setText("Restart adbd Success! \nRun follow command in your PC: \nadb connect " + getLocalIp() + ":5555");
			} else {
				mTextView.setText("Restart adbd Failed!");
			}

			mAsyncTask = null;
		}
	}

//	private boolean restartAdbd() throws IOException, InterruptedException {
//		String[] commands = { "setprop service.adb.tcp.port 5555", "stop adbd", "start adbd" };
//		List<String> result = Shell.SU.run(commands);
//		return result != null;
//	}

	private boolean restartAdbd3() throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec("su");
		DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

		outputStream.writeBytes("setprop service.adb.tcp.port 5555\n");
		outputStream.flush();

		outputStream.writeBytes("stop adbd\n");
		outputStream.flush();

		outputStream.writeBytes("start adbd\n");
		outputStream.flush();

		outputStream.writeBytes("exit\n");
		outputStream.flush();

		process.waitFor();
		return process.exitValue() == 0;
	}

	private String getLocalIp() {
		return getIPAddress(true);
	}

	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = addr instanceof Inet4Address;
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port suffix
								return delim<0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} // for now eat exceptions
		return "";
	}
}
