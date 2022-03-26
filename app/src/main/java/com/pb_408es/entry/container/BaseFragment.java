package com.pb_408es.entry.container;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

public abstract class BaseFragment extends Fragment {
	public boolean isFragmentViewInit = false;
	public View lastView;

	@Override
	public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (lastView == null) {
			lastView = setRootView(inflater, container, savedInstanceState);
		}
		return lastView;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		if (!isFragmentViewInit) {
			super.onViewCreated(view, savedInstanceState);
			initView(view);
			initData();
			isFragmentViewInit = true;
		}
	}

	/**
	 * View初始化
	 *
	 * @param view 布局
	 */
	public abstract void initView(View view);

	/**
	 * 初始化数据
	 */
	public abstract void initData();

	/**
	 * 设置根view
	 *
	 * @param inflater           1
	 * @param container          2
	 * @param savedInstanceState 3
	 * @return
	 */
	public abstract View setRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
}
