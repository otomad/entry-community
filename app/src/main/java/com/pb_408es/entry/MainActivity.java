package com.pb_408es.entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.pb_408es.entry.databinding.ActivityMainBinding;
import com.pb_408es.entry.ui.home.HomeFragment;
import com.pb_408es.entry.ui.oscilloscope.OscilloscopeFragment;
import com.pb_408es.entry.ui.serialassistant.SerialAssistantFragment;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
	private AppBarConfiguration mAppBarConfiguration = null;
	private ActivityMainBinding binding = null;
	public SocketActivity socket = new SocketActivity();
	public boolean isConnect = false;
	private FloatingActionButton connectLanBtn = null;
	private DrawerLayout drawer = null;
	private Menu menu = null;
	private Menu funcMenu = null;
	private NavController navController = null;
	public static class _Fragments { // 不声明成 public 就访问不了
		public HomeFragment home;
		public SerialAssistantFragment serial;
		public OscilloscopeFragment scope;
	}
	public _Fragments fragments = new _Fragments(); // 单例类
	public static MainActivity mainActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mainActivity = this;
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setSupportActionBar(binding.appBarMain.toolbar); // binding.appBarMain.toolbar
		connectLanBtn = binding.appBarMain.connectLanBtn; // binding.appBarMain.connectLanBtn
		connectLanBtn.setOnClickListener(view -> connectLan(!socket.isConnect()));

		drawer = binding.drawerLayout;
		NavigationView navigationView = binding.navView;
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(
			R.id.nav_home, R.id.nav_serial_assistant, R.id.nav_oscilloscope
		).setDrawerLayout(drawer).build();
		navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);
//		navController.addOnDestinationChangedListener(this::onDestinationChanged); // 不覆盖原生导航方法
		navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected); // 还是覆盖重写吧
		funcMenu = navigationView.getMenu().findItem(R.id.menu_functional).getSubMenu();
		/*for (int i = funcMenu.size() - 1; i >= 0; i--) // 遍历一遍所有 Fragment，仅仅是为了都先 CreateView
			navController.navigate(funcMenu.getItem(i).getItemId());*/
		getRandomBackgroundImage();
		checkedWifiConnection();
		pref = new _Pref();
	}

	/*private void onDestinationChanged(NavController controller, NavDestination destination, Bundle arguments) {
		// Handle navigation view item clicks here.
		drawer.closeDrawers();
		CharSequence title = destination.getLabel();
		int selected = destination.getId();
		if (selected == R.id.nav_home) connectLanBtn.show();
		else connectLanBtn.hide();
		binding.appBarMain.collapsingToolbar.setTitle(title);
		navController.navigate(selected);
//		System.out.println(toolbar.getTitle().toString());
//		final MenuItem clrMsg = findViewById(R.id.action_clrMsg);
//		clrMsg.setVisible(selected == R.id.nav_serial_assistant);
	}*/
	private int currentNavigateItem = R.id.nav_home;

	private boolean onNavigationItemSelected(@NotNull MenuItem item) {
		// Handle navigation view item clicks here.
		drawer.closeDrawers();
		for (int i = 0; i < funcMenu.size(); i++)
			funcMenu.getItem(i).setChecked(false);
		item.setChecked(true);
		CharSequence title = item.getTitle();
		int selected = item.getItemId();
		if (selected == R.id.nav_home) {
			connectLanBtn.show();
			title = getResources().getString(R.string.nav_header_title);
		}
		else connectLanBtn.hide();
		binding.appBarMain.collapsingToolbar.setTitle(title);
		if (currentNavigateItem != selected) navController.navigate(selected);
		currentNavigateItem = selected;
//		System.out.println(toolbar.getTitle().toString());
		menu.findItem(R.id.action_clrMsg).setVisible(selected == R.id.nav_serial_assistant || selected == R.id.nav_oscilloscope);
		menu.findItem(R.id.action_autoClrSend).setVisible(selected == R.id.nav_serial_assistant);
		closeKeyboard();
		return true;
	}

	public boolean autoClearSend() {
		return menu.findItem(R.id.action_autoClrSend).isChecked();
	}

	private static class lambda {
		private interface Void {
			void set(boolean enabled);
		}
	}

	public int getThemeColor(int resAttr) {
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(resAttr, typedValue, true);
		return typedValue.data;
	}

	/**
	 * 请求使某个输入框获得焦点并且自动弹出输入法软键盘
	 * @param searchText 待聚焦的输入框
	 */
	public void focusEditTextAndShowKeyboard(EditText searchText) {
		searchText.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(searchText, InputMethodManager.SHOW_FORCED); // SHOW_IMPLICIT
	}

	public void connectLan(boolean connect) {
		lambda.Void btnSetEnabled = (boolean enabled) -> {
			connectLanBtn.setContentDescription(enabled ? getResources().getString(R.string.stop_connect) : getResources().getString(R.string.start_connect));
			/*connectLanBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this,
					enabled ? R.color.green_500 : R.color.design_default_color_error
			)));*/
			connectLanBtn.setBackgroundTintList(ColorStateList.valueOf(
					enabled ? getThemeColor(R.attr.colorSuccess) : getThemeColor(R.attr.colorError)
			));
			connectLanBtn.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
					enabled ? R.drawable.ic_baseline_link_24 : R.drawable.ic_baseline_link_off_24
			));
			fragments.home.setEnabled(!enabled); // 连接输入框失能
			Animation mAnimation = AnimationUtils.loadAnimation(MainActivity.this,
					enabled ? R.anim.alpha_fade_out : R.anim.alpha_fade_in
			);
			binding.appBarMain.appBarImage.startAnimation(mAnimation);
		};
		if (!connect) {
			socket.stopSocket();
			btnSetEnabled.set(isConnect = false);
			return;
		}
		EditText ipAddressText = findViewById(R.id.server_ip_address_text),
				 portText = findViewById(R.id.server_port_text);
		String ipAddress = ipAddressText.getText().toString(),
				port_str = portText.getText().toString();
		System.out.println("IP: " + ipAddress + "\nPort: " + port_str);
		if (ipAddress.isEmpty() || ipAddress.split("\\.").length != 4) {
			toast(R.string.please_input_ip_address);
			focusEditTextAndShowKeyboard(ipAddressText);
			return;
		}
		if (port_str.isEmpty()) {
			toast(R.string.please_input_port);
			focusEditTextAndShowKeyboard(portText);
			return;
		}
		pref.save();
		int port = Integer.parseInt(portText.getText().toString());

		boolean ok = socket.openSocket(ipAddress, port);
		btnSetEnabled.set(isConnect = ok);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		MenuItem switchToLandscape = menu.findItem(R.id.switch_to_landscape);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			switchToLandscape.setChecked(true);
		return true;
	}

	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
	}

	//定义菜单响应事件
	@SuppressLint({ "SourceLockedOrientationActivity", "NonConstantResourceId", "WrongConstant" })
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: // 打开侧滑菜单的汉堡按钮，因为一旦引入重写的 onOptionsItemSelected 这个函数，按钮功能就废了
				drawer.openDrawer(Gravity.START);
				break;
			case R.id.switch_to_landscape: // 横竖屏切换按钮
				if (!item.isChecked()) {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				} else {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				item.setChecked(!item.isChecked());
				break;
			case R.id.action_autoClrSend:
				item.setChecked(!item.isChecked());
				break;
			case R.id.action_clrMsg:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				AlertDialog alert = builder.setTitle(R.string.clear_message)
						.setMessage(R.string.ensure_clear_all)
						.setNegativeButton(R.string.button_ok, (dialogInterface, i) -> {
							if (currentNavigateItem == R.id.nav_serial_assistant) fragments.serial.clearAllView();
							else if (currentNavigateItem == R.id.nav_oscilloscope) fragments.scope.clearChart();
						})
						.setPositiveButton(R.string.button_cancel, null)
						.create();
				alert.show();
				break;
			default:
				break;
		}
		return true;
	}

	public void toast(String text) {
		Log.d("Toast Text", text);
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	public void toast(int resId) {
		toast(getResources().getString(resId));
	}

	public void test() { // 测试是否走到这一步的函数
		toast(R.string.test_step);
	}

	@Override
	public void onConfigurationChanged(@NotNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		MenuItem switchToLandscape = menu.findItem(R.id.switch_to_landscape);
		// 检测屏幕的方向：纵向或横向
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//当前为横屏， 在此处添加额外的处理代码
			switchToLandscape.setChecked(true);
		}
		else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//当前为竖屏， 在此处添加额外的处理代码
			switchToLandscape.setChecked(false);
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.zoom_close_in, R.anim.zoom_close_out);
	}

	@Override
	public void onBackPressed() {
		Menu navMenu = binding.navView.getMenu();
		final int HOME = R.id.nav_home;
		MenuItem homeItem = navMenu.findItem(HOME);
		if (currentNavigateItem != HOME) onNavigationItemSelected(homeItem);
		else exit();
	}

	private static boolean isExit = false;
	private void exit() {
//		final int DELAY = 2000;
		if (!isExit) {
			isExit = true;
			toast(R.string.press_again_to_exit);
			new Handler().postDelayed(() -> {
				isExit = false;
			}, 2000); // 延时2秒
		} else {
			finish();
			new Handler().postDelayed(() -> {
				System.exit(0);
			}, 500);
		}
	}

	/**
	 * 导航到各个页面并处理相关操作
	 * @param navId 要导航去的地方的 ID
	 */
	public void navigate(int navId) {}

	/**
	 * 关闭软键盘
	 */
	public void closeKeyboard() {
		View view = getWindow().getDecorView();
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 保存用户个性化数据
	 */
	public class _Pref {
		private final SharedPreferences pref = getSharedPreferences("LanConnectInfo", Context.MODE_PRIVATE);
		private final String IpAddress = "IpAddress";
		private final String Port = "Port";
		private String getValue(@NotNull EditText editText) {
			return editText.getText().toString();
		}
		private void setValue(@NotNull EditText editText, String value) {
			editText.setText(value);
		}
		private EditText ipAddressText() {
			return findViewById(R.id.server_ip_address_text);
		}
		private EditText portText() {
			return findViewById(R.id.server_port_text);
		}

		public void read() {
			setValue(ipAddressText(), pref.getString(IpAddress, ""));
			setValue(portText(), pref.getString(Port, "10500"));
		}
		public void save() {
			SharedPreferences.Editor editor = pref.edit();
			editor.putString(IpAddress, getValue(ipAddressText()));
			editor.putString(Port, getValue(portText()));
			editor.apply();
		}
	}
	public _Pref pref = null; // 单例化

	private boolean hasAlreadyGetRandomBackgroundImage = false;
	private void getRandomBackgroundImage() {
		if (hasAlreadyGetRandomBackgroundImage) return;
		final ImageView originalImage = binding.appBarMain.appBarImage,
				prettyImage = binding.appBarMain.appBarImageSecondary;
		final String originalAffix = "_original", prettyAffix = "_pretty";
		final String[] picNames = {"tjb", "zqh", "yxw"};
		String randomPicName = picNames[randomBetween(0, picNames.length - 1)];
		int originalImageId = getResources().getIdentifier(randomPicName + originalAffix, "drawable", getPackageName()),
			prettyImageId = getResources().getIdentifier(randomPicName + prettyAffix, "drawable", getPackageName());
		originalImage.setImageResource(originalImageId);
		prettyImage.setImageResource(prettyImageId);
		hasAlreadyGetRandomBackgroundImage = true;
	}

	public static int randomBetween(int min, int max) {
		return (int)(Math.random() * (max + 1 - min) + min);
	}

	public static boolean isConnectedWifi(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifiNetworkInfo.isConnected();
	}

	private void checkedWifiConnection() {
		if (isConnectedWifi(this)) return;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog alert = builder.setTitle(R.string.uncaught_wifi_connect)
				.setMessage(R.string.uncaught_wifi_connect_info)
				.setNegativeButton(R.string.button_cancel, null)
				.setPositiveButton(R.string.to_connect, (dialogInterface, i) -> {
					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				})
				.create();
		alert.show();
	}
}