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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.pb_408es.entry.MainActivity;
import com.pb_408es.entry.R;
import com.pb_408es.entry.databinding.FragmentOscilloscopeBinding;

public class OscilloscopeFragment extends Fragment implements OnChartValueSelectedListener {

	private OscilloscopeViewModel oscilloscopeViewModel;
	private FragmentOscilloscopeBinding binding;
	@SuppressLint("StaticFieldLeak")
	private static View rootView;

	private static MainActivity mainAct() {
		return MainActivity.mainActivity;
	}

	private TextView chartValue = null;
	private LineChart lineChart = null;

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

		chartValue = binding.chartValue;
		lineChart = binding.lineChart;
//		lineChart = new LineChart(root.getContext());
//		binding.chartConstLayout.addView(lineChart);
		initLineChart();
		return rootView = root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	int lastValue = 0;

	public void addPoint(int value) {
		setText(value, _Color.GREY);
		addEntry(value);
		lastValue = value;
//		Log.d("point size:", Integer.toString(pointValues.size()));
//		Log.d("axis size:", Integer.toString(axisValues.size()));
	}

	protected static final float X_MIN = 0;
	protected static final float X_MAX = -1;
	protected static final float Y_MIN = 0;
	protected static final float Y_MAX = 4095;
	protected static final float X_VIEW_MIN = 7;
	protected static final float X_VIEW_MAX = 1024;

	private void initLineChart() {
		// 背景色
//		lineChart.setBackgroundColor(Color.WHITE);

		// 图表的文本描述
		lineChart.getDescription().setEnabled(false);
		lineChart.setNoDataText(getResources().getString(R.string.no_chart_data_available));

		// 手势设置
		lineChart.setTouchEnabled(true);

		// 添加监听器
		lineChart.setOnChartValueSelectedListener(this);
		lineChart.setDrawGridBackground(false);

		// 设置拖拽、缩放等
		lineChart.setDragEnabled(true);
		lineChart.setScaleEnabled(true);
		lineChart.setScaleXEnabled(true);
		lineChart.setScaleYEnabled(false);
		lineChart.setPinchZoom(true);// 设置双指缩放
		lineChart.setDragDecelerationEnabled(true);

		// 获取 Y 轴
		lineChart.getAxisRight().setEnabled(false);
		lineChart.getAxisLeft().setAxisMinimum(Y_MIN);
		lineChart.getAxisLeft().setAxisMaximum(Y_MAX);
		lineChart.getXAxis().setAxisMinimum(X_MIN);
//		lineChart.getXAxis().setAxisMaximum(10);
		lineChart.setVisibleXRangeMinimum(X_VIEW_MIN);
		lineChart.setVisibleXRangeMaximum(X_VIEW_MAX);
	}

	private void addEntry(float value) {
		if (lineChart.getData() == null) {
			lineChart.setData(new LineData());
			lineChart.invalidate();
		}
		LineData lineData = lineChart.getData();
		if (lineData != null) {
			int indexLast = lineData.getEntryCount();
			ILineDataSet lastSet = lineData.getDataSetByIndex(0);//lineData.getDataSetByIndex(indexLast);
			// set.addEntry(...); // can be called as well

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
//			Log.d("addEntry", String.format("count = %d, value = %f, dataSetIndex = %d", count, value, indexLast));
			lineData.addEntry(new Entry(count, value), 0);
			lineData.notifyDataChanged();
//			Log.d("TAG", "set.getEntryCount()=" + lastSet.getEntryCount() + " ; indexLastDataSet=" + indexLast);
		}
	}

	/*private float left = 0;
	private float right = 0;
	private float width = 100;
	private void refreshViewport_before() {
		if (lineChart.getData() == null) return;
		left = lineChart.getLowestVisibleX();
		right = lineChart.getHighestVisibleX();
		width = right - left;
	}*/

	private void refreshViewport() {
		LineData lineData = lineChart.getData();
		if (lineData == null) return;
		lineChart.notifyDataSetChanged();
		final float left = lineChart.getLowestVisibleX(),
				right = lineChart.getHighestVisibleX(),
				width = right - left;/*,
				scale = lineChart.getScaleX();
		Log.d("scale", String.valueOf(scale));*/
		//mChart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
		// this automatically refreshes the chart (calls invalidate())
//		Log.d("refreshViewport", String.format("left: %f, right: %f", left, right));
		final int last = lineData.getEntryCount();
//		Log.d("last", String.valueOf(last));
//		Log.d("last&right", String.format("last:%d,right%f",last,right));
		if (last > right) {
			lineChart.moveViewTo(last, 50f, YAxis.AxisDependency.LEFT);
//			lineChart.moveViewTo(last);
//			lineChart.setVisibleXRange(last - width, last);
//			lineChart.setScaleX(scale);
//			lineChart.zoom(1,1,last,50f);
//			Log.d("scale1", String.valueOf(lineChart.getScaleX()));
		}
	}

	/*private Viewport refreshViewport(float left, float right, float top, float bottom) {
		Viewport viewport = new Viewport();
		viewport.left = left;
		viewport.right = right;
		viewport.top = top;
		viewport.bottom = bottom;
		return viewport;
	}*/

	public void receiveText(String text) {
		if (text.isEmpty()) return;
		final String START_CODE = "add 1,0,";
		final String[] lines = text.split("\\n");
//		refreshViewport_before();
		boolean scaleFlag = lineChart.getData() != null;
		final float previousCount = scaleFlag ? lineChart.getData().getEntryCount() : 0;
//		Log.d("scale1", String.valueOf(lineChart.getScaleX()));
//		Log.d("scale2", String.valueOf(scale));
		for (String line : lines) {
			if (!(line = line.trim()).startsWith(START_CODE)) continue;
			line = line.substring(START_CODE.length()).trim();
			int value;
			try {
				value = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				Log.d("NaN", line);
				continue;
			}
			addPoint(value);
		}
		refreshViewport();
		if (scaleFlag) lineChart.zoom(lineChart.getData().getEntryCount() / previousCount, 1, 0, 50);
//		Log.d("scale3", String.valueOf(lineChart.getScaleX()));
	}

	public void clearChart() {
		lineChart.clear();
		setText("");
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onValueSelected(Entry e, Highlight h) {
		Log.d("onValueSelected", e.toString());
		setText(e.getY(), _Color.CYAN);
	}

	@Override
	public void onNothingSelected() {
		setText(lastValue, _Color.GREY);
	}

	private enum _Color {
		GREY,
		CYAN
	}

	private void setText(String text, _Color color) {
		chartValue.setText(text);
		chartValue.setTextColor(mainAct().getThemeColor(color == _Color.CYAN ? R.attr.colorSecondaryVariant : R.attr.textColorSecondary));
	}

	private void setText(int text, _Color color) {
		setText(Integer.toString(text), color);
	}

	private void setText(float text, _Color color) {
		setText(Float.toString(text), color);
	}

	private void setText(String text) {
		setText(text, _Color.GREY);
	}

	private LineDataSet createSet() {
		LineDataSet set = new LineDataSet(null, getResources().getString(R.string.waveform_dataset) + " 0");
		set.setLineWidth(2.5f); // 线宽
		set.setDrawCircles(false); // 隐藏圆圈
//		set.setCircleRadius(4.5f);
		set.setColor(mainAct().getThemeColor(R.attr.colorPrimary)); // 线条颜色
//		set.setCircleColor(Color.rgb(240, 99, 99));
		set.setHighLightColor(Color.rgb(190, 190, 190));
		set.setAxisDependency(YAxis.AxisDependency.LEFT);
		set.setDrawValues(false);
//		set.setValueTextSize(10f);
		set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 开启三次平滑曲线
//		set.setCubicIntensity(0.2f);
		return set;
	}
}