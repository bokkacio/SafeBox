package com.Alexander.SafeBox.UserInterfaceHelpers;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import com.Alexander.SafeBox.Encryption.Scrambler;

public class DecryptViewBinder implements SimpleCursorTreeAdapter.ViewBinder {
    private Scrambler _customScrambler = null;

    public DecryptViewBinder(String password)
    {
        _customScrambler = new Scrambler(password);
    }

    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if(view instanceof TextView) {
            TextView tv = (TextView) view;
            String encryptedText = cursor.getString(columnIndex);
            tv.setText(_customScrambler.Decrypt(encryptedText));
            return true;
        }
        return false;
    }
}