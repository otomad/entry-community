package com.pb_408es.entry.ui.serialassistant;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pb_408es.entry.MainActivity;
import com.pb_408es.entry.databinding.FragmentSerialAssistantBinding;

import java.util.ArrayList;
import java.util.List;

public class SerialAssistantFragment extends Fragment {
	private SerialAssistantViewModel serialAssistantViewModel;
	private FragmentSerialAssistantBinding binding;
	private List<Msg> msgList = new ArrayList<>();
	private RecyclerView chatView;
	private EditText sendText;
	private MsgAdapter adapter;
	@SuppressLint("StaticFieldLeak")
	private static View rootView;
	private static MainActivity mainAct() {
		return MainActivity.mainActivity;
	}

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView != null) { // 单例化
			ViewGroup parent = (ViewGroup)rootView.getParent();
			if (parent != null) parent.removeView(rootView);
			return rootView;
		}
		mainAct().fragments.serial = this;
		serialAssistantViewModel = new ViewModelProvider(this).get(SerialAssistantViewModel.class);
		Log.e("createView", "SerialAssistantFragment is created");

		binding = FragmentSerialAssistantBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		/*final TextView textView = binding.textGallery;
		serialAssistantViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				textView.setText(s);
			}
		});*/

		chatView = binding.chatListView;//root.findViewById(R.id.chatListView);
		sendText = binding.sendText;//root.findViewById(R.id.sendText);
		Button sendBtn = binding.sendBtn;//root.findViewById(R.id.sendBtn);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		adapter = new MsgAdapter(msgList = getData());

		chatView.setLayoutManager(layoutManager);
		chatView.setAdapter(adapter);

		sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mainAct().focusEditTextAndShowKeyboard(sendText);
				String content = sendText.getText().toString();
				if (content.isEmpty()) return;
				boolean ok = mainAct().socket.send(content);
				if (!ok) return;
				msgList.add(new Msg(content, Msg.Type.SEND));
				adapter.notifyItemInserted(msgList.size() - 1);
				chatView.scrollToPosition(msgList.size() - 1);
				if (mainAct().autoClearSend()) sendText.setText("");
			}
		});
		return rootView = root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private List <Msg> getData() {
		return new ArrayList<>();
		//list.add(new Msg("Hello", Msg.Type.RECEIVED));
	}

	public void receiveText(String text) {
		if (text.isEmpty()) return;
		msgList.add(new Msg(text, Msg.Type.RECEIVED));
		adapter.notifyItemInserted(msgList.size() - 1);
		chatView.scrollToPosition(msgList.size() - 1);
	}

	public void clearAllView() {
		chatView.removeAllViews();
		adapter.clearAllView();
	}
}