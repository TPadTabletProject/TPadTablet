package nxr.tpad.hapticsmessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class ServerFragment extends Fragment implements OnClickListener {
	private EditText ipBox;
	private TextView serverStatus;

	public String SERVERIP = "10.0.2.15";
	public static final int SERVERPORT = 8080;
	ServerSocket ss = null;

	ClientThread clientThread = null;
	ServerThread serverThread = null;

	private static Handler handler;
	
	public int hapticsType;
	public String message;
	public Boolean sendMessage = false;

	DrawFragment drawView;
	MainActivity mainActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.server_fragment, container, false);
		Button b = (Button) view.findViewById(R.id.connect_client_button);
		b.setOnClickListener(this);
		b = (Button) view.findViewById(R.id.connect_server_button);
		b.setOnClickListener(this);
		b = (Button) view.findViewById(R.id.reset_button);
		b.setOnClickListener(this);
		b = (Button) view.findViewById(R.id.server_hide_button);
		b.setOnClickListener(this);
		b = (Button) view.findViewById(R.id.reset_text);
		b.setOnClickListener(this);

		ipBox = (EditText) view.findViewById(R.id.ip_textbox);
		serverStatus = (TextView) view.findViewById(R.id.server_status);

		SERVERIP = getLocalIpAddress();
		
		handler = new Handler() {
			@Override
			  public void handleMessage(Message msg) {
				drawView.recieveMessage(msg.arg1,(String) msg.obj, true);
				}
			 };
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					SharedPreferences stored = mainActivity.getPreferences(0);
					String ip = stored.getString("lastIP", "0");
					if (ip != "0") {
						ipBox.setText(ip);
					}
				} catch (InterruptedException e) {}
			}
		});
		return view;
	}
	public void sendMessage(int a, String s) {
		hapticsType = a;
		message = s;
		sendMessage = true;
	}

	public class ServerThread extends Thread {
		private Boolean connected;
		
		public ServerThread() {
			connected = false;
		}
		public void stopThread() {
			connected = false;
		}
		public void run() {
			if (SERVERIP != null) {
				try {
					if (ss == null || ss.isClosed()) 
						ss = new ServerSocket(SERVERPORT);
					handler.post(new SendMessage("Listening on IP: " + SERVERIP));
					Socket client = ss.accept();
					connected = true;
					handler.post(new SendMessage("Connected"));
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						String line = null;
						while (connected) {
						//SERVER LOGIC
							if ((line = in.readLine()) != null) {
								Message msg = handler.obtainMessage();
								msg.arg1 = Integer.parseInt(line);
								msg.obj = in.readLine();
								handler.sendMessage(msg);
							}
						}
						in.close();
					} catch (Exception e) {
					
						
						//show();
						handler.post(new SendMessage("Oops. Connection interrupted. Please reconnect your phones."));
						Log.w("Server", e.toString());
					}
					client.close();
				} catch (IOException e) {
					Log.w("Server", e.toString());
				}
			} else {
				handler.post(new SendMessage("Couldn't detect internet connection."));
			}
		}
	}
	public class ClientThread extends Thread {
		private Boolean connected;
		
		public ClientThread() {
			connected = false;
		}
		public void stopThread() {
			connected = false;
		}
		public void run() {
			try {
				InetAddress serverAddr = InetAddress.getByName(ipBox.getText().toString());
				Socket socket = new Socket(serverAddr, 8080);
				connected = true;
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
							.getOutputStream())), true);
					while (connected) {
						//CLIENT LOGIC
						if (sendMessage) {
							sendMessage = false;
							out.println(hapticsType);
							out.println(message);
						}
					}
					out.close();
				} catch (IOException e) {
					Log.w("Client", e.toString());
				}
				socket.close();
			} catch (IOException e) {
				Log.w("Client", e.toString());
			}
		}
	}
	public void connectServer() {
		if (serverThread == null) {
			serverThread = new ServerThread();
			serverThread.start();
		}
	}

	public void connectClient() {
		SharedPreferences ip = mainActivity.getPreferences(0);
		SharedPreferences.Editor editor = ip.edit();
		editor.putString("lastIP", ipBox.getText().toString());
		editor.commit();

		if (clientThread == null || ipBox.getText().toString() == "") {
			clientThread = new ClientThread();
			clientThread.start();
		}
	}

	public void reset() {
		if (clientThread != null) 
			clientThread.stopThread();
		if (serverThread != null)
			serverThread.stopThread();

		serverThread = null;
		clientThread = null;
		if (ss != null && !ss.isClosed()) {
			try {ss.close();} catch (IOException e) {}
		}

		serverStatus.setText("Reset");
	}

	public void hide() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();  
		ft.hide(this);  
		ft.commit();  
	}

	public void show() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();  
		ft.show(this);  
		ft.commit();  
	}

	public class SendMessage implements Runnable {
		String m;
		public SendMessage(String s) {
			m = s;
		}
		public void run( ) {
			serverStatus.setText(m);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.connect_client_button:
			connectClient();
			break;
		case R.id.connect_server_button:
			connectServer();
			break;
		case R.id.reset_button:
			reset();
			break;
		case R.id.server_hide_button:
			hide();
			break;
		case R.id.reset_text:
			ipBox.setText("");
			break;
		}
	}

	public static String getLocalIpAddress(){
		try {
			String ipv4;
			List<NetworkInterface>  nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
			if(nilist.size() > 0){
				for (NetworkInterface ni: nilist){
					List<InetAddress>  ialist = Collections.list(ni.getInetAddresses());
					if(ialist.size()>0){
						for (InetAddress address: ialist){
							if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4=address.getHostAddress())){ 
								return ipv4;
							}
						}
					}

				}
			}

		} catch (SocketException ex) {}
		return null;
	}

	@Override
	public void onStop() {
		super.onStop();
		reset();
	}
}