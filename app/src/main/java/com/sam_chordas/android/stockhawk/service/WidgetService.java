package com.sam_chordas.android.stockhawk.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.rest.CallsListRemoteViewsFactory;

/**
 * Created by vaibhav on 22/5/16.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        RemoteViewsFactory listProvidder = new CallsListRemoteViewsFactory(this.getApplicationContext(), intent);
        return listProvidder;    }
}
