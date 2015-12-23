package com.example.socketserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.conn.util.InetAddressUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends ActionBarActivity {
	private static final int PORT = 9999;
	private List<Socket> mList = new ArrayList<Socket>();
	private ServerSocket server = null;
	private ExecutorService mExecutorService = null; // thread pool

	private Button start;
	private TextView textView;
	private TextView tv_ip;
	private TextView contact;

	private static final int SUCCESS = 0;
	protected static final int ERROR = 1;

	private String html;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case SUCCESS:
				textView.setText((String) msg.obj);
				break;
			case ERROR:
				Toast.makeText(ServerActivity.this, "error", 0).show();
				break;
			}
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		init();
		
		String ip = getLocalIpAddress();
		tv_ip.setText("服务器端IP地址:"+ip +" 客户端中连接时填入！");
		
	}

	private void init() {
		start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// start server and listen port 9999
				new Thread(new Runnable() {
					public void run() {
						startServer();
					}
				}).start();
				contact.setText("服务已开启!");
			}
		});
	}

	private void initView() {
		start = (Button) findViewById(R.id.btn_start);
		textView = (TextView) findViewById(R.id.textView);
		tv_ip = (TextView) findViewById(R.id.tv_ip);
		contact = (TextView) findViewById(R.id.contact);
	}
	@Override
	protected void onDestroy() {
		finish();
		super.onDestroy();
	}

	private void startServer() {
		try {
			server = new ServerSocket(PORT);
			mExecutorService = Executors.newCachedThreadPool(); // create a
																// thread pool
			System.out.println("服务器已启动...");
			Socket client = null;
			while (true) {
				client = server.accept();
				// 把客户端放入客户端集合中
				mList.add(client);
				mExecutorService.execute(new Service(client)); // start a new
																// thread to
																// handle the
																// connection
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getLocalIpAddress(){ 
        try{ 
             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
                 NetworkInterface intf = en.nextElement();   
                    for (Enumeration<InetAddress> enumIpAddr = intf   
                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {   
                        InetAddress inetAddress = enumIpAddr.nextElement();   
                        if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {   
                             
                            return inetAddress.getHostAddress().toString();   
                        }   
                    }   
             } 
        }catch (SocketException e) { 
        } 
        return null;  
    }

	class Service implements Runnable {
		private Socket socket;
		private BufferedReader in = null;
		private String msg = "";

		public Service(Socket socket) {
			this.socket = socket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// 客户端只要一连到服务器，便向客户端发送下面的信息。
				msg = "服务器地址：" + this.socket.getInetAddress() + "come toal:" + mList.size() + "（服务器发送）";
				this.sendmsg();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					if ((msg = in.readLine()) != null) {
						if (msg.equals("#123")) {
							System.out.println("#123");
							msg = getHtml();
							this.sendmsgHtml();
							break;
							// 接收客户端发过来的信息msg，然后发送给客户端。
						} else if (msg.equals("exit")) {
							System.out.println("ssssssss");
							mList.remove(socket);
							in.close();
							msg = "user:" + socket.getInetAddress() + "exit total:" + mList.size();
							socket.close();
							this.sendmsg();
							break;
							// 接收客户端发过来的信息msg，然后发送给客户端。
						} else {
							msg = socket.getInetAddress() + ":" + msg + "（服务器发送）";
							this.sendmsg();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 循环遍历客户端集合，给每个客户端都发送信息。
		 */
		public void sendmsg() {
			System.out.println(msg);
			int num = mList.size();
			for (int index = 0; index < num; index++) {
				Socket mSocket = mList.get(index);
				PrintWriter pout = null;
				try {
					pout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
					pout.println(msg);
					pout.flush();
					System.out.println(pout);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void sendmsgHtml() {
			System.out.println(msg);
			int num = mList.size();
			for (int index = 0; index < num; index++) {
				Socket mSocket = mList.get(index);
				PrintWriter pout = null;
				try {
					pout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
					msg = getHtml();
					pout.println(msg);
					pout.flush();
					System.out.println(pout);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public String getHtml() {
			final String url = "http://wthrcdn.etouch.cn/weather_mini?citykey=101010100";
			new Thread(new Runnable() {
				@Override
				public void run() {
					html = getHtmlFromInternet(url);

					if (!TextUtils.isEmpty(html)) {
						Message msg = new Message();
						msg.what = SUCCESS;
						msg.obj = html;
						handler.sendMessage(msg);
					} else {
						Message msg = new Message();
						msg.what = ERROR;
						handler.sendMessage(msg);
					}
				}
			}).start();
			return html;
		}

		protected String getHtmlFromInternet(String url) {

			try {
				URL mURL = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) mURL.openConnection();

				conn.setRequestMethod("GET");
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(5000);

				int responseCode = conn.getResponseCode();

				if (responseCode == 200) {
					InputStream is = conn.getInputStream();
					String html = getStringFromInputStream(is);

					return html;
				} else {
					Log.i("TAG", "error:" + responseCode);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		private String getStringFromInputStream(InputStream is) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			is.close();

			String html = baos.toString();

			String charset = "utf-8";
			if (html.contains("gbk") || html.contains("gb2312") || html.contains("GBK") || html.contains("GB2312")) {
				charset = "gbk";
			}
			html = new String(baos.toByteArray(), charset);
			baos.close();
			return html;
		}

	}
}