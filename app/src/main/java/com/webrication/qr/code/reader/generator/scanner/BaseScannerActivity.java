package com.webrication.qr.code.reader.generator.scanner;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Created by pc on 1/12/2018.
 */

public class BaseScannerActivity extends AppCompatActivity
{
    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.clear_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
