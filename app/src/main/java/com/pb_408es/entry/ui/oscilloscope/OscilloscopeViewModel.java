package com.pb_408es.entry.ui.oscilloscope;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OscilloscopeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public OscilloscopeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}