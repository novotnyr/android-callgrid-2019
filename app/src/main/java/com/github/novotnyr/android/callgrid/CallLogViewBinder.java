package com.github.novotnyr.android.callgrid;

import android.database.Cursor;
import android.provider.CallLog;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CallLogViewBinder implements SimpleCursorAdapter.ViewBinder {
    
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if(view instanceof TextView) {
                TextView textView = (TextView) view;
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                switch(type) {
                    case CallLog.Calls.INCOMING_TYPE:
                        textView.setBackgroundColor(
                                textView.getResources()
                                        .getColor(R.color.incomingCallBackground)
                                );
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        textView.setBackgroundColor(
                                textView.getResources()
                                    .getColor(R.color.outgoingCallBackground)
    
                        );
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        textView.setBackgroundColor(
                                textView.getResources()
                                     .getColor(R.color.missedCallBackground)
    
                        );
                        break;
                }
            }
            return false;
        }
    }