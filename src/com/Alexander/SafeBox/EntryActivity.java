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
import android.widget.Toast;
import com.Alexander.SafeBox.DbProcessing.EntryDatabaseHelper;
import com.Alexander.SafeBox.Encryption.Md5Helper;


public class EntryActivity  extends Activity implements View.OnClickListener{

    TextView lblInfo;
    TextView lblAttemptsLeft;
    Button btnEntry;
    EditText txtPassword;
    EditText txtPasswordRepeat;
    private EntryDatabaseHelper db;
    private Md5Helper _md5Helper = new Md5Helper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry);

        lblInfo = (TextView) findViewById(R.id.lblInfo);
        lblAttemptsLeft = (TextView) findViewById(R.id.lblAttemptsLeft);
        txtPassword = (EditText)  findViewById(R.id.txtPassword);
        txtPasswordRepeat = (EditText)  findViewById(R.id.txtPasswordRepeat);
        btnEntry = (Button)  findViewById(R.id.btnEntry);
        btnEntry.setOnClickListener(this);

        db = new EntryDatabaseHelper(this);
        db.open();

        if(!db.IsEntryFirstTime())
        {
            txtPasswordRepeat.setVisibility(View.INVISIBLE);
            lblInfo.setText("Please enter your password.");
        }
        else
        {
            txtPasswordRepeat.setVisibility(View.VISIBLE);
            lblInfo.setText("Welcome to SafeBox! Please enter and repeat your password. The password must contain more then five symbols.");
        }
    }

    @Override
    public void onClick(View v) {
        String userPass = txtPassword.getText().toString();
        String userPassRepeat = txtPasswordRepeat.getText().toString();

        if(!db.IsEntryFirstTime())
        {
            byte encryptedPass[] = _md5Helper.GetMd5(userPass);
            byte encryptedPassFromDb[] = db.GetEncryptedPassword();
            int attemptsLeft = db.GetAttemptsAmount();
            boolean isPasswordEquals = _md5Helper.CompareByteArrays(encryptedPass, encryptedPassFromDb);

            if(!isPasswordEquals)
            {
                attemptsLeft--;

                //Change password and set attempts to 100000
                if(attemptsLeft == 0)
                {
                    String funnyPass = "ho-ho-ho";
                    byte[] bytesFunnyPass = _md5Helper.GetMd5(funnyPass);
                    db.FreeMd5Password(bytesFunnyPass);
                    finish();
                    return;
                }

                db.UpdateAttemptsAmount(attemptsLeft);

                //Toast.makeText(this, "You can try to enter password " + attemptsLeft + " times.", Toast.LENGTH_LONG).show();
                lblAttemptsLeft.setText("You can try to enter password " + attemptsLeft + " times.");
                lblInfo.setText("Wrong password. Please try again.");
                txtPassword.setText("");
            }
            else
            {
                db.RestoreAttemptsAmount();
                StartListActivity(userPass);
            }
        }
        else if(IsPasswordsMatchCriteria(userPass, userPassRepeat))
                SetPasswordFirstTime();
    }

    private boolean IsPasswordsMatchCriteria(String userPass, String userPassRepeat)
    {
        boolean result = true;

        if(userPass == "" || userPassRepeat == "")
        {
            txtPassword.setText("");
            txtPasswordRepeat.setText("");
            lblInfo.setText("Please enter and repeat the password.");
            result = false;
        }
        else if (!userPassRepeat.equals(userPass))
        {
            txtPassword.setText("");
            txtPasswordRepeat.setText("");
            lblInfo.setText("Please repeat the password correctly.");
            result = false;
        }
        else if (userPassRepeat.length() < 6)
        {
            txtPassword.setText("");
            txtPasswordRepeat.setText("");
            lblInfo.setText("The password must contain more then five symbols.");
            result = false;
        }
        else if (userPassRepeat.equals(userPass))
            result = true;

        return result;
    }

    private void SetPasswordFirstTime()
    {
        String userPass = txtPassword.getText().toString();
        byte encryptedPass[] = _md5Helper.GetMd5(userPass);

        db.SetPasswordFirstTime(encryptedPass);
        StartListActivity(userPass);
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
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void StartListActivity(String userPassword)
    {
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("savedUserPass", userPassword);
        intent.putExtra("isEntire", true);
        startActivityForResult(intent, 90);
        db.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Close app in any way
        finish();
    }
}
