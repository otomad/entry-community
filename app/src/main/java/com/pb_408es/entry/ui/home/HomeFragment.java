package com.pb_408es.entry.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pb_408es.entry.MainActivity;
import com.pb_408es.entry.R;
import com.pb_408es.entry.databinding.FragmentHomeBinding;

import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public class HomeFragment extends Fragment {

	private HomeViewModel homeViewModel;
	private FragmentHomeBinding binding;
	private boolean isChanging = false;
	@SuppressLint("StaticFieldLeak")
	private static View rootView;
	private static MainActivity mainAct() {
		return MainActivity.mainActivity;
	}

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView != null) { // 单例化
			System.out.println("exist home view");
			ViewGroup parent = (ViewGroup)rootView.getParent();
			if (parent != null) parent.removeView(rootView);
			return rootView;
		}
		mainAct().fragments.home = this;
		System.out.println("new home view");
		homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		/*final TextView textView = binding.textHome;
		homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				textView.setText(s);
			}
		});*/
		final EditText ipText = binding.serverIpAddressText, portText = binding.serverPortText;
		ipText.addTextChangedListener(new TextWatcher() { // IP 地址的格式验证
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void afterTextChanged(Editable editable) {
				if (isChanging) return;
				isChanging = true;
				String text = editable.toString();
				text = text.replaceAll("[^0-9.]", "").replaceAll("\\.{2,}", "."); // 去掉非法字符、去掉多余的句点
				if (text.length() == 0 || text.equals(".")) {
					ipText.setText("");
					isChanging = false;
					return;
				}
				int location = ipText.getSelectionStart();
				if (text.charAt(0) == '.') text = text.substring(1); // 如果开头是点，去掉
				String[] text_split = text.split("\\."); // 用点分隔字符串
				if (text_split.length > 4) text_split = java.util.Arrays.copyOf(text_split, 4); // 如果分隔出来发现超过 4 组数，截取前 4 组
				for (int i = 0; i < text_split.length; i++) {
					//if (text_split[i].isEmpty()) continue; // 为空跳过，一般只会出现在末尾
					int bit = Integer.parseInt(text_split[i]); // 转换为数字
					while (bit > 255) bit /= 10; // 如果大于 255， 则取最高位
					text_split[i] = Integer.toString(bit); // 转换回字符串
				}
				final boolean lastDot = text.charAt(text.length() - 1) == '.' && text_split.length < 4; // 末尾的点会削除？
//				StringJoiner textJoiner = new StringJoiner("."); // 所以 Java 不支持数组转字符串？
				text = joinArray(text_split, ".");
//				text = Arrays.stream(text_split).reduce((a, b) -> a + "." + b).toString();
				if (lastDot) text += '.';
				ipText.setText(text);
				if (location > text.length()) location = text.length();
				ipText.setSelection(location);
				isChanging = false;
			}
		});
		portText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void afterTextChanged(Editable editable) {
				if (isChanging) return;
				isChanging = true;
				String text = editable.toString();
				text = text.replaceAll("[^0-9]", "");
				if (text.length() == 0) {
					portText.setText("");
					isChanging = false;
					return;
				}
				int location = portText.getSelectionStart();
				final int MAX_PORT = 65536;
				if (Integer.parseInt(text) > MAX_PORT) text = Integer.toString(MAX_PORT);
				portText.setText(text);
				if (location > text.length()) location = text.length();
				portText.setSelection(location);
				isChanging = false;
			}
		});
		binding.entryBtn.setOnClickListener(view -> mainAct().entryToOscilloscope());
		binding.loginFloatingActionButton.setOnClickListener(view -> mainAct().entryToOscilloscope());
		binding.navHomeLayout.post(() -> mainAct().pref.read()); // 加载用户配置数据
		return rootView = root;
	}

//	/**
//	 * 数组转字符串
//	 * @param array 字符串数组。
//	 * @return 返回拼接后的字符串
//	 */
//	@NotNull
//	private static String joinArray(String[] array) { // Java 只支持重载，不支持赋默认值
//		return joinArray(array, ",");
//	}
//
//	/**
//	 * 数组转字符串
//	 * @param array 字符串数组。
//	 * @param sep 分隔符。
//	 * @return 返回拼接后的字符串
//	 */
//	@NotNull
//	private static String joinArray(@NotNull String[] array, String sep) {
//		StringBuilder str = new StringBuilder();
//		for (int i = 0; i < array.length; i++)
//			str.append(i != 0 ? sep : "").append(array[i]);
//		return str.toString();
//	}

	/**
	 * 数组转字符串
	 * @param array 字符串数组。
	 * @param sep 分隔符。
	 * @return 返回拼接后的字符串
	 */
	@NotNull
	private static String joinArray(@NotNull String[] array, String sep) {
		StringJoiner stringJoiner = new StringJoiner(sep);
		for (String str : array) stringJoiner.add(str);
		return stringJoiner.toString();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	public final boolean ENABLED_AS_LAN_SERVER = false;
	public void setEnabled(boolean enabled) {
		rootView.findViewById(R.id.as_server_radio).setEnabled(enabled && ENABLED_AS_LAN_SERVER);
		rootView.findViewById(R.id.as_client_radio).setEnabled(enabled);
		rootView.findViewById(R.id.server_ip_address_text).setEnabled(enabled);
		rootView.findViewById(R.id.server_port_text).setEnabled(enabled);
//		binding.asServerRadio.setEnabled(enabled && ENABLED_AS_LAN_SERVER);
//		binding.asClientRadio.setEnabled(enabled);
//		binding.serverIpAddressText.setEnabled(enabled);
//		binding.serverPortText.setEnabled(enabled);
	}
}