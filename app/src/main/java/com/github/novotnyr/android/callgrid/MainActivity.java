package com.github.novotnyr.android.callgrid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private GridView callLogGridView;

    private SimpleCursorAdapter callLogGridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callLogGridView = findViewById(R.id.callLogGridView);
        callLogGridView.setOnItemClickListener(this);

        String[] from = {CallLog.Calls.NUMBER};
        int[] to = {R.id.grid_item_text};
        callLogGridViewAdapter = new SimpleCursorAdapter(this, R.layout.grid_item, null, from, to, 0);
        callLogGridViewAdapter.setViewBinder(new CallLogViewBinder());
        callLogGridView.setAdapter(callLogGridViewAdapter);

        // Mám pridelené oprávnenie čítať históriu volaní
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // Ak nemáme pridelené oprávnenie, požiadajme oňho.
            // Overme, či používateľ náhodou už nezamietol oprávnenie.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)) {
                // Ak používateľ už kedysi zamietol oprávnenia, skúsme ho ešte raz vyzvať k potvrdeniu
                Snackbar.make(callLogGridView, "Appka potrebuje oprávnenie k histórii volaní", Snackbar.LENGTH_LONG)
                        .setAction("Požiadať", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] permissions = {Manifest.permission.READ_CALL_LOG};
                                ActivityCompat.requestPermissions(MainActivity.this, permissions, 0);
                            }
                        })
                        .show();
            } else {
                // Ak sa appka spúšťa úplne prvý krát, požiadajme o oprávnenie.
                // Alternatívne tu vyriešme prípad, keď používateľ už zamietol oprávnenie a začiarkol
                // 'Do Not Show again'. V takom prípade síce požiadajme o oprávnenie, ale rovno
                // sa nám zamietne.
                String[] permissions = {Manifest.permission.READ_CALL_LOG};
                ActivityCompat.requestPermissions(this, permissions, 0);
            }
        } else {
            // Ak áno, všetko je v poriadku.
            initLoader();
        }
    }

    private void initLoader() {
        getSupportLoaderManager().initLoader(0, Bundle.EMPTY, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            // Neboli udelené žiadne oprávnenia, alebo dialóg pre potvrdenie bol zrušený
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initLoader();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(CallLog.Calls.CONTENT_URI);
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        callLogGridViewAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        callLogGridViewAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));

        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse(WebView.SCHEME_TEL + number));
        startActivity(callIntent);

    }
}
