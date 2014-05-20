package com.Alexander.SafeBox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddElementActivity  extends Activity implements View.OnClickListener {

    EditText txtElementTitle;
    EditText txtElementValue;
    TextView lblTitle;
    TextView lblValue;
    TextView lblErrorTitle;
    Button btnElementAdd;

    long groupId;
    boolean isElement = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_element);

        lblErrorTitle = (TextView) findViewById(R.id.lblErrorTitle);
        lblTitle = (TextView) findViewById(R.id.lblTitle);
        lblValue = (TextView) findViewById(R.id.lblValue);
        txtElementValue = (EditText)  findViewById(R.id.txtElementValue);
        txtElementTitle = (EditText)  findViewById(R.id.txtElementTitle);
        btnElementAdd = (Button) findViewById(R.id.btnProcessAdding);
        btnElementAdd.setOnClickListener(this);

        Intent intent = getIntent();
        groupId = intent.getLongExtra("groupId", StartActivity.DEFAULT_ID);
        isElement = intent.getBooleanExtra("isElement", false);

        if(isElement)
        {
            lblTitle.setText("Login");
            lblValue.setText("Password");
            txtElementTitle.setVisibility(View.VISIBLE);
            lblTitle.setVisibility(View.VISIBLE);
        }
        else
        {
            lblValue.setText("Group title");
            txtElementTitle.setVisibility(View.INVISIBLE);
            lblTitle.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    public void onClick(View v) {
        String elementTitle = txtElementTitle.getText().toString();
        String elementValue = txtElementValue.getText().toString();

        boolean elementTitleFilled =  elementTitle != null && !elementTitle.isEmpty();
        boolean elementValueFilled =  elementValue != null && !elementValue.isEmpty();

        if((elementValueFilled && !isElement) || (elementTitleFilled && elementValueFilled))
        {
            Intent intent = new Intent(this, StartActivity.class);
            intent.putExtra("elementValue", elementValue);
            intent.putExtra("elementTitle", elementTitle);
            intent.putExtra("groupId", groupId);
            intent.putExtra("isInsert", true);
            setResult(RESULT_OK, intent);
            lblErrorTitle.setText("");
            finish();
        }
        else
            lblErrorTitle.setText(isElement ? "Login and password can't be empty." :  "Group title can't be empty.");
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
