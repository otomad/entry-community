package com.pb_408es.entry;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@SuppressLint("HandlerLeak")
public class SocketActivity extends AppCompatActivity {
	//定义相关变量,完成初始化
	private Socket socket = null;
//	private BufferedReader in = null;
//	private PrintWriter out = null;
	private InputStream in;
	private OutputStream out;
	private String content = "";
	private MainActivity mainAct() {
		return MainActivity.mainActivity;
	}
	private final static int IS_RECEIVE_1_CODE = 0x123;

//	public SocketActivity(MainActivity parent) {
//		this.main = parent;
//	}

	//定义一个handler对象,用来刷新界面
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == IS_RECEIVE_1_CODE) {
				Log.d("receive_text", content);
				if (mainAct().fragments.serial != null) mainAct().fragments.serial.receiveText(content);
//				if (mainAct().fragments.scope != null) mainAct().fragments.scope.receiveText(content);
				if (mainAct().fragments.scope != null) runOnUiThread(() -> mainAct().fragments.scope.receiveText(content));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public boolean isConnect() {
		if (socket == null) return false;
//		Log.d("isConnected", String.valueOf(socket.isConnected()));
//		Log.d("isInputShutdown", String.valueOf(socket.isInputShutdown()));
		return socket.isConnected() && !socket.isInputShutdown();
	}

	public boolean openSocket(@NotNull final String ipAddress, final int port) {
		if (ipAddress.isEmpty()) return false;
		//当程序一开始运行的时候就实例化Socket对象,与服务端进行连接,获取输入输出流
		//因为4.0以后不能再主线程中进行网络操作,所以需要另外开辟一个线程
		final boolean[] ret = new boolean[1]; // 一个读取标量的神奇操作
		Thread openNewSocketThread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket(ipAddress, port);
//					socket.setSoTimeout(2 * 1000); // 设置超时时间
//					in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
//					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
					in = socket.getInputStream();
					out = socket.getOutputStream();
					ret[0] = true;
					toast(R.string.open_client_successful);
				} catch (IOException e) {
					ret[0] = false;
//					toastErr(e);
					e.printStackTrace();
					toast(R.string.open_client_error);
				}
			}
		};
		openNewSocketThread.start();
		try {
			openNewSocketThread.join();
//			while (!ret[1]) Thread.sleep(100);
		} catch (InterruptedException e) {
			toastErr(e);
			ret[0] = false;
		}
		if (ret[0]) receiveThread();
		return ret[0];
	}
	public enum StopSocketState {
		unset,
		passive,
		initiative
	}
	public StopSocketState requestStopSocket = StopSocketState.unset;
	private final int RECEIVE_MAX_SIZE = 65536;//1024;
	private void receiveThread() {
		new Thread() {
			//重写run方法,在该方法中输入流的读取
			@Override
			public void run() {
				while (isConnect()) {
					try {
						// 得到的是16进制数，需要进行解析
						byte[] bt = new byte[RECEIVE_MAX_SIZE]; // 获取接收到的字节和字节数
						int length = in.read(bt); // 获取正确的字节
						byte[] bs = new byte[length];
						System.arraycopy(bt, 0, bs, 0, length);
						String str = new String(bs, StandardCharsets.UTF_8);
						if (/*!(content = str).isEmpty()*/!str.isEmpty()) {
							handler.sendEmptyMessage(IS_RECEIVE_1_CODE);
							Log.d("receive", str);
							runOnUiThread(() -> {
								if (mainAct().fragments.serial != null) mainAct().fragments.serial.receiveText(str);
								if (mainAct().fragments.scope != null) mainAct().fragments.scope.receiveText(str);
							});
						}
					} catch (Exception e) {
//						toastErr(e);
						e.printStackTrace();
						requestStopSocket = StopSocketState.passive;
						// 失能操作
						runOnUiThread(() -> mainAct().connectLan(false));
						requestStopSocket = StopSocketState.unset;
						break;
					}
				}
				/*try {
					while (true) {
						if (socket.isConnected() && !socket.isInputShutdown()) {
							if ((content = in.readLine()) != null) {
								handler.sendEmptyMessage(IS_RECEIVE_CODE);
							}
						} else throw new Exception(getResources().getString(R.string.that_has_disconnected));
					}
				} catch (Exception e) {
					toastErr(e);
					// 失能操作
				}*/
			}
		}.start();
	}

	public void stopSocket() {
		/*if (isConnect()) {
			try {
				if (out != null) out.close();
				socket.close();
				if (socket.isClosed()) {
					if (disconnectedCallback != null) {
						disconnectedCallback.callback(new IOException("断开连接"));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		/*new Thread() {
			@Override
			public void run() {
				try {
					socket.shutdownOutput();//关闭输出流
					socket.close();
				} catch (IOException e) {
					toastErr(e);
				}
			}
		}.start();*/
		if (isConnect()) {
			try {
				if (out != null) out.close();
				socket.close();
				if (socket.isClosed()) {
					Log.d("Stop Connection", "断开连接");
					toast(requestStopSocket != StopSocketState.passive ? R.string.stop_connect : R.string.that_has_disconnected);
					requestStopSocket = requestStopSocket != StopSocketState.passive ? StopSocketState.unset : StopSocketState.initiative;
				}
				socket = null;
			} catch (IOException e) {
				toastErr(e);
			}
		}
		//失能操作
	}

	public boolean send(String msg) {
		final boolean[] ret = new boolean[1];
		Thread thread = new Thread() {
			@Override
			public void run() {
				/*if (isConnect()) {
					out.println(msg);
				} else toast(R.string.unconnected_server_info);*/
				if (isConnect()) {
					try {
						out.write(msg.getBytes());
						out.flush();
						ret[0] = true;
					} catch (IOException e) {
						toastErr(e);
					}
				} else toast(R.string.unconnected_server_info);
			}
		};
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			toastErr(e);
		}
		return ret[0];
	}

	@NotNull
	private String exception2str(@NotNull Exception e) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		return result.toString();
	}

	private void toast(String text) {
		runOnUiThread(() -> mainAct().toast(text));
	}

	private void toast(int text) {
		runOnUiThread(() -> mainAct().toast(text));
	}

	private void toastErr(Exception e) {
		e.printStackTrace();
		toast(exception2str(e).split("\\n")[0]); // 只 toast 第一行
	}


}