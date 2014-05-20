package com.Alexander.SafeBox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RemoveElementActivity  extends Activity implements View.OnClickListener{

    TextView lblInfo;
    Button btnElementRemove;
    Button btnElementRefuseRemove;
    long groupId;
    long elementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_element);

        lblInfo = (TextView) findViewById(R.id.lblDialogText);
        btnElementRemove = (Button) findViewById(R.id.btnRemove);
        btnElementRefuseRemove = (Button) findViewById(R.id.btnRefuseRemove);

        lblInfo.setText("Please, confirm your remove opportunity.");
        Intent intent = getIntent();
        groupId = intent.getLongExtra("groupId", StartActivity.DEFAULT_ID);
        elementId = intent.getLongExtra("elementId", StartActivity.DEFAULT_ID);

        btnElementRemove.setOnClickListener(this);
        btnElementRefuseRemove.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnRemove:
            {
                Intent intent = new Intent();
                intent.putExtra("groupId", groupId);
                intent.putExtra("elementId", elementId);
                intent.putExtra("isRemove", true);
                setResult(RESULT_OK, intent);
                finish();
                break;
            }
            case R.id.btnRefuseRemove:
            {
                Intent intent = new Intent();
                intent.putExtra("groupId", groupId);
                intent.putExtra("elementId", elementId);
                intent.putExtra("isRemove", false);
                setResult(RESULT_CANCELED, intent);
                finish();
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, StartActivity.CLOSE_APP, 0, "Exit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case StartActivity.CLOSE_APP:
                setResult(StartActivity.CLOSE_APP);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
