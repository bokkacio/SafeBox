package com.Alexander.SafeBox.StorageProcessing;

import android.database.Cursor;
import android.os.Environment;
import android.text.format.Time;
import java.io.*;
import com.Alexander.SafeBox.DbProcessing.DatabaseHelper;
import com.Alexander.SafeBox.Encryption.Scrambler;

public class FileRepository {
    private File _currentDirectory = null;
    private DatabaseHelper _dbHelper = null;
    private Scrambler _scrambler = null;

    public FileRepository(File directory, DatabaseHelper dbHelper, Scrambler scrambler)
    {
        _currentDirectory = directory;
        _dbHelper = dbHelper;
        _scrambler = scrambler;
    }

    public String SavePasswordsToFile()
    {
        Time currentTime = new Time();
        currentTime.setToNow();
        String fileName = "SafeBox_" + currentTime.year + currentTime.month + currentTime.monthDay + currentTime.hour + currentTime.minute + currentTime.second;
        String stringToWrite = GetPasswordsFromDB();
        String result;

        File file = new File(_currentDirectory, fileName);
        FileOutputStream fos;

        byte[] data = stringToWrite.getBytes();
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
            result = String.format("File %s was saved successfully to %s", fileName, String.valueOf(_currentDirectory));
        } catch (FileNotFoundException e) {
            result = e.toString();
        } catch (IOException e) {
            result = e.toString();
        }

        return result;
    }

    public String ImportPasswordsFromFile()
    {
        String fileName = null;
        String result = "Can't find file for export.";
        File file = new File(String.valueOf(_currentDirectory));
        File listOfFiles[] = file.listFiles();
        for(int i = 0; i < listOfFiles.length; i++)
            if(listOfFiles[i].getName().contains("SafeBox"))
            {
                fileName =  listOfFiles[i].getName();
                break;
            }

        if(fileName != null)
        {
            // open the file for reading
            File fileSelected = new File(_currentDirectory, fileName);
            if(fileSelected.exists())
            {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(fileSelected));
                    String line;
                    String passValue;
                    long groupId = -1;
                    while ((line = br.readLine()) != null) {
                        if(!line.contains("pass:~%")) //group
                            groupId = _dbHelper.InsertGroupResult(line.replace(System.getProperty("line.separator"), ""));
                        else //child
                        {
                            passValue = line.replace(System.getProperty("line.separator"), "");
                            passValue = passValue.replace("pass:~%", "");
                            String[] values = passValue.split("  :  ");
                            if(values.length == 2)
                                _dbHelper.InsertElement(groupId, values[0], values[1]);
                        }
                    }

                    result = String.format("Import from file '%s' was finished.", fileName);
                }
                catch (IOException e) {
                    result = e.getMessage();
                }
            }
        }

        return result;
    }

    private String GetPasswordsFromDB()
    {
        StringBuilder result = new StringBuilder();
        Cursor cr = _dbHelper.GetGroupData();

        //Groups
        if (cr.moveToFirst()) {
            int idGroupColIndex = cr.getColumnIndex(DatabaseHelper.GROUP_COLUMN_ID);
            int groupColIndex = cr.getColumnIndex(DatabaseHelper.GROUP_COLUMN_NAME);
            do {
                String groupValue = _scrambler.Decrypt(cr.getString(groupColIndex));
                result.append(groupValue);
                result.append(System.getProperty("line.separator"));

                Cursor cp = _dbHelper.GetElementData(cr.getInt(idGroupColIndex));
                int elementColIndex = cp.getColumnIndex(DatabaseHelper.ELEMENT_COLUMN_VALUE);

                //Elements
                if(cp.moveToFirst())
                {
                    String pass;
                    do {
                        pass = _scrambler.Decrypt(cp.getString(elementColIndex));
                        result.append("pass:~%" + pass);
                        result.append(System.getProperty("line.separator"));
                    }
                    while (cp.moveToNext());
                }
            } while (cr.moveToNext());
        }
        return result.toString();
    }

    public static boolean IsExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
