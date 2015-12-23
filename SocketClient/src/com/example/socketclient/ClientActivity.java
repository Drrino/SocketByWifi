package com.example.socketclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ClientActivity extends ActionBarActivity {
	private static String HOST;
	private static final int PORT = 9999;
	private Socket socket = null;
	private BufferedReader in;
	private PrintWriter out;

	private Button btn_sendData;

	private Button btn_ip;
	private EditText ed_ip;
	private TextView tv_html;
	private String buffer;
	private TextView tv_contact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
	}

	private void initView() {
		btn_sendData = (Button) findViewById(R.id.btn_sendData);
		tv_html = (TextView) findViewById(R.id.tv_html);
		btn_ip = (Button) findViewById(R.id.btn_ip);
		ed_ip = (EditText) findViewById(R.id.ed_ip);
		tv_contact = (TextView) findViewById(R.id.tv_contact);

		btn_ip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HOST = ed_ip.getText().toString().trim();
				// connect to server
				new Thread(new Runnable() {
					public void run() {
						connectToServer();
					}
				}).start();
				 
			}
		});
		btn_sendData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				out.println("#123");
				out.flush();
			}
		});

	 
	}
	
	private void updateTextView(final TextView textView, final String buffer){
		new Handler(getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				// 在这里执行你要想的操作 比如直接在这里更新ui或者调用回调在 在回调中更新ui
				textView.setText(buffer);
				 
			}
		});
	}

	private void connectToServer() {
		try {
			socket = new Socket(HOST, PORT);
			String status = (socket == null) ? "未连接" : "已连接";
			updateTextView(tv_contact, status);
			//
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			new Thread(new Runnable() {
				public void run() {
					// 读取发来服务器信息
					String line = null;
					buffer = "";
					try {
						while ((line = in.readLine()) != null) {
							buffer = line + buffer;
							
							buffer += "\n";
							
							new Handler(getMainLooper()).post(new Runnable() {

								@Override
								public void run() {
									// 在这里执行你要想的操作 比如直接在这里更新ui或者调用回调在 在回调中更新ui
									updateTextView(tv_html, buffer);
								}
							});
							
							
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}).start();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
