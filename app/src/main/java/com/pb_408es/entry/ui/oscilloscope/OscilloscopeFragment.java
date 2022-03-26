package com.pb_408es.entry.ui.oscilloscope;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.pb_408es.entry.MainActivity;
import com.pb_408es.entry.R;
import com.pb_408es.entry.databinding.FragmentOscilloscopeBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class OscilloscopeFragment extends Fragment {

	private OscilloscopeViewModel oscilloscopeViewModel;
	private FragmentOscilloscopeBinding binding;
	@SuppressLint("StaticFieldLeak")
	private static View rootView;

	private static MainActivity mainAct() {
		return MainActivity.mainActivity;
	}

	private LineChart waveChart = null;
	private LineChart spectrumChart = null;
	private TextView[] textViews = null;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView != null) { // 单例化
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (parent != null) parent.removeView(rootView);
			return rootView;
		}
		mainAct().fragments.scope = this;
		oscilloscopeViewModel = new ViewModelProvider(this).get(OscilloscopeViewModel.class);
		binding = FragmentOscilloscopeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		waveChart = binding.waveChart;
		spectrumChart = binding.spectrumChart;
		initWaveChart();
		initSpectrumChart();

		textViews = new TextView[] {
				binding.fms, binding.thd, binding.um1, binding.um2, binding.um3, binding.um4, binding.um5
		};
		return rootView = root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	int lastValue = 0;

	public void addPoint(int value) {
		addEntry(value);
		lastValue = value;
	}

	protected static final float X_MIN = 0;
	protected static final float X_MAX = -1;
	protected static final float Y_MIN = 0;
	protected static final float Y_MAX = 3.3f;
	protected static final float Y_MAX_CODE = 4095;
	protected static final float X_VIEW_MIN = 100;
	protected static final float X_VIEW_MAX = 1023;
	protected static final float SPECTRUM_X_MAX = 511;

	private void initWaveChart() {
		// 背景色
//		waveChart.setBackgroundColor(Color.WHITE);

		// 图表的文本描述
		waveChart.getDescription().setEnabled(false);
		waveChart.setNoDataText(getResources().getString(R.string.no_wave_data_available));
		waveChart.getLegend().setEnabled(false);

		// 手势设置
		waveChart.setTouchEnabled(true);

		// 添加监听器
		waveChart.setDrawGridBackground(false);

		// 设置拖拽、缩放等
		waveChart.setDragEnabled(true);
		waveChart.setScaleEnabled(true);
		waveChart.setScaleXEnabled(true);
		waveChart.setScaleYEnabled(false);
		waveChart.setPinchZoom(true);// 设置双指缩放
		waveChart.setDragDecelerationEnabled(true);

		// 获取 Y 轴
		waveChart.getAxisRight().setEnabled(false);
		waveChart.getAxisLeft().setAxisMinimum(Y_MIN);
		waveChart.getAxisLeft().setAxisMaximum(Y_MAX);
		waveChart.getXAxis().setAxisMinimum(X_MIN);
//		waveChart.getXAxis().setAxisMaximum(10);
		waveChart.setVisibleXRangeMinimum(X_VIEW_MIN);
		waveChart.setVisibleXRangeMaximum(X_VIEW_MAX);
	}

	private void addEntry(float... values) {
		if (waveChart.getData() == null) {
			waveChart.setData(new LineData());
			waveChart.invalidate();
		}
		LineData lineData = waveChart.getData();
		if (lineData == null) return;

		ILineDataSet lastSet = lineData.getDataSetByIndex(0);//lineData.getDataSetByIndex(indexLast);

		if (lastSet == null) {
			lastSet = createSet();
			lineData.addDataSet(lastSet);
		}
		// 这里要注意，x轴的index是从零开始的
		// 假设index=2，那么getEntryCount()就等于3了
		int count = lineData.getDataSetByIndex(0).getEntryCount();//lastSet.getEntryCount();
		// add a new x-value first 这行代码不能少
//			lineData.addXValue(count + "");

		// 位最后一个DataSet添加entry
		for (float value : values) lineData.addEntry(new Entry(count++, value), 0);
		lineData.notifyDataChanged();
	}

	private void initSpectrumChart() {
		// 图表的文本描述
		spectrumChart.getDescription().setEnabled(false);
		spectrumChart.setNoDataText(getResources().getString(R.string.no_spectrum_data_available));
		spectrumChart.getLegend().setEnabled(false);

		// 添加监听器
//		spectrumChart.setOnChartValueSelectedListener(this);
//		spectrumChart.setDrawGridBackground(false);

		// 设置拖拽、缩放等
//		spectrumChart.setDragEnabled(true);
//		spectrumChart.setScaleEnabled(true);
		spectrumChart.setScaleXEnabled(false);
		spectrumChart.setScaleYEnabled(false);
//		spectrumChart.setPinchZoom(true);// 设置双指缩放
//		spectrumChart.setDragDecelerationEnabled(true);

		// 获取 Y 轴
		spectrumChart.getAxisRight().setEnabled(false);
		spectrumChart.getAxisLeft().setAxisMinimum(Y_MIN);
		spectrumChart.getAxisLeft().setAxisMaximum(Y_MAX);
		spectrumChart.getXAxis().setAxisMinimum(X_MIN);
		spectrumChart.getXAxis().setAxisMaximum(SPECTRUM_X_MAX);
//		spectrumChart.getXAxis().setLabelCount(10);
		spectrumChart.setVisibleXRangeMinimum(X_VIEW_MIN);
		spectrumChart.setVisibleXRangeMaximum(SPECTRUM_X_MAX);

		// 初始化空列表
		for (int i = 0; i < X_VIEW_MAX * 2; i++) spectrumValues.add(new Entry(i / 2.0f, 0));
	}

	/*private void addSpectrumEntry(int index, float... values) {
		if (spectrumChart.getData() == null) {
			spectrumChart.setData(new LineData());
			spectrumChart.invalidate();
		}
		LineData lineData = spectrumChart.getData();
		if (lineData != null) {
			ILineDataSet lastSet = lineData.getDataSetByIndex(0);//lineData.getDataSetByIndex(indexLast);

			if (lastSet == null) {
				lastSet = createSpectrumSet();
				lineData.addDataSet(lastSet);
			}
			// 这里要注意，x轴的index是从零开始的
			// 假设index=2，那么getEntryCount()就等于3了
//			int count = lineData.getDataSetByIndex(0).getEntryCount();//lastSet.getEntryCount();
			// add a new x-value first 这行代码不能少
//			lineData.addXValue(count + "");

			// 位最后一个DataSet添加entry
			for (float value : values) {
				if (index > X_VIEW_MAX) break;
				lineData.removeEntry(index, 0);
				lineData.addEntry(new Entry(index++, value), 0);
			}
			lineData.notifyDataChanged();
		}
	}*/

	List<Entry> spectrumValues = new ArrayList<>();
	private void addSpectrumEntry(int index, float... values) {
		if (spectrumChart.getData() == null) {
			spectrumChart.setData(new LineData());
			spectrumChart.invalidate();
		}
		LineData lineData = spectrumChart.getData();
		if (lineData == null) return;

		for (int i = 0, j = index; i < values.length && j <= X_VIEW_MAX ; i++, j++)
			spectrumValues.set(j * 2, new Entry(j, values[i]));
		lineData.removeDataSet(0);
		LineDataSet lastSet = createSpectrumSet(spectrumValues);
		lineData.addDataSet(lastSet);
	}

	private void refreshViewport() {
		LineData lineData = waveChart.getData();
		if (lineData == null) return;
		waveChart.notifyDataSetChanged();
		spectrumChart.notifyDataSetChanged();
		spectrumChart.invalidate();
		final float right = waveChart.getHighestVisibleX();
		final int last = lineData.getEntryCount();
//		if (last <= 100) waveChart.setVisibleXRange(0, last);
//		else
		if (last > right) waveChart.moveViewTo(last, 50f, YAxis.AxisDependency.LEFT);
	}

	public void receiveText(String text) {
		if (text.isEmpty()) return;
		final String START_CODE = "audio ";
		final String[] lines = text.split("\\n");
		boolean scaleFlag = waveChart.getData() != null;
		final float previousCount = scaleFlag ? waveChart.getData().getEntryCount() : 0;
		for (String line : lines) {
			if (!(line = line.trim()).startsWith(START_CODE)) continue;
			line = line.substring(START_CODE.length()).trim();
			final String WAVE_CODE = "wave ", SPECTRUM_CODE = "spec ", INFO_CODE = "info ";
			if (line.startsWith(INFO_CODE)) {
				line = line.substring(INFO_CODE.length()).trim();
				readInfo(line.split(","));
			} else if (line.startsWith(WAVE_CODE)) {
				line = line.substring(WAVE_CODE.length()).trim();
				addEntry(stringSequence2numberArray(line, ","));
			} else if (line.startsWith(SPECTRUM_CODE)) {
				line = line.substring(SPECTRUM_CODE.length()).trim();
				String[] splits = splitOnce(line, ',');
				int index;
				try {
					index = Integer.parseInt(splits[0]);
				} catch (NumberFormatException e) {
					Log.d("NaN", line);
					continue;
				}
				addSpectrumEntry(index, stringSequence2numberArray(splits[1], ","));
			}
		}
		LineData lineData = waveChart.getData();
		if (lineData == null) return;
		refreshViewport();
		if (scaleFlag && lineData.getEntryCount() > 100) waveChart.zoom(lineData.getEntryCount() / previousCount, 1, 0, 50);
	}

	private void readInfo(String[] info) {
		if (info == null) return; // throw new NullPointerException("info 为空");
//		if (binding == null) throw new NullPointerException("binding 为空");
		final int length = Math.min(info.length, textViews.length);
		NumberFormat digitFormat = NumberFormat.getNumberInstance();
		digitFormat.setMinimumFractionDigits(3);
		for (int i = 0; i < length; i++) {
			String item = info[i];
			if (i <= 6) { // 给 THD 和 Um1 ~ Um5 全部除以 1000
				try {
					double item_double = Double.parseDouble(item);
					item_double /= 1000;
					item = digitFormat.format(item_double);
				} catch (NumberFormatException ignored) { }
			}
			textViews[i].setText(item);
		}
	}

	public void clearChart() {
		waveChart.clear();
		spectrumChart.clear();
		for (TextView textView : textViews) textView.setText("");
	}

	private LineDataSet createSet() {
		LineDataSet set = new LineDataSet(null, getResources().getString(R.string.waveform_dataset) + " 0");
		set.setLineWidth(2.5f); // 线宽
		set.setDrawCircles(false); // 隐藏圆圈
		set.setColor(mainAct().getThemeColor(R.attr.colorPrimary)); // 线条颜色
		set.setHighLightColor(Color.rgb(190, 190, 190));
		set.setAxisDependency(YAxis.AxisDependency.LEFT);
		set.setDrawValues(false);
		set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 开启三次平滑曲线
		return set;
	}

	private LineDataSet createSpectrumSet(List<Entry> yValues) {
		LineDataSet set = new LineDataSet(yValues, getResources().getString(R.string.spectrum_dataset) + " 0");
		set.setLineWidth(2.5f); // 线宽
		set.setDrawCircles(false); // 隐藏圆圈
		set.setColor(mainAct().getThemeColor(R.attr.colorPrimary)); // 线条颜色
		set.setHighLightColor(Color.rgb(190, 190, 190));
		set.setAxisDependency(YAxis.AxisDependency.LEFT);
		set.setDrawValues(false);
		set.setDrawFilled(true);
//		set.setFillDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
//				new int[] {Color.argb(230, 204, 232, 255), Color.argb(120, 204, 232, 255)}));
//		set.setFillColor(Color.rgb(204, 232, 255));
		set.setFillColor(mainAct().getThemeColor(R.attr.colorPrimary));
		set.setFillAlpha(85);
		return set;
	}

	public static float[] stringSequence2numberArray(@NonNull String stringSequence, String sep) {
		String[] strings = stringSequence.split(sep);
		float[] floats = new float[strings.length];
		for (int i = 0; i < strings.length; i++) {
			float value;
			try {
				value = Integer.parseInt(strings[i]) * Y_MAX / Y_MAX_CODE;
			} catch (NumberFormatException e) {
				Log.d("NaN", strings[i]);
				value = i == 0 ? 0 : floats[i - 1];
			}
			floats[i] = value;
		}
		return floats;
	}

	public static String[] splitOnce(@NonNull String string, char sep) {
		int firstSepPos = string.indexOf(sep);
		String[] splits = new String[2];
		splits[0] = string.substring(0, firstSepPos);
		if (firstSepPos + 1 < string.length()) splits[1] = string.substring(firstSepPos + 1);
		return splits;
	}
}
