package com.pb_408es.entry.ui.serialassistant;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SerialAssistantViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SerialAssistantViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}