package com.Alexander.SafeBox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.Alexander.SafeBox.DbProcessing.DatabaseHelper;
import com.Alexander.SafeBox.Encryption.Scrambler;
import com.Alexander.SafeBox.StorageProcessing.FileRepository;
import com.Alexander.SafeBox.UserInterfaceHelpers.DecryptViewBinder;

public class StartActivity extends Activity {

    final int CREATE_ELEMENT = 1;
    final int REMOVE_ELEMENT = 2;
    final int CREATE_GROUP = 3;
    final int REMOVE_GROUP = 5;
    final int EXPORT_PASSWORDS = 89;
    final int IMPORT_PASSWORDS = 90;

    public static final long DEFAULT_ID = -1;
    public static final int CLOSE_APP = 99;

    private String _dbPassword = "";

    private ExpandableListView elvMain;
    private TextView lblInfo;
    private DatabaseHelper db;
    private SimpleCursorTreeAdapter sctAdapter;
    private Scrambler _localScrambler = null;
    private FileRepository _fileHelper;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        lblInfo = (TextView) findViewById(R.id.textView);
        lblInfo.setText("The list of your passwords");

        Intent entireIntent = getIntent();

        if(!entireIntent.hasExtra("savedUserPass"))
            finish();
        else
        {
            _dbPassword = "afdghb,ea" + entireIntent.getStringExtra("savedUserPass") + ";ehjpjrfv";
            InitPasswordList();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, CREATE_GROUP, 0, "Add group");
        menu.add(0, EXPORT_PASSWORDS, 0, "Export to file");
        menu.add(0, IMPORT_PASSWORDS, 0, "Import from file");
        menu.add(0, CLOSE_APP, 0, "Exit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case CREATE_GROUP:
            {
                Intent intent = new Intent(this, AddElementActivity.class);
                intent.putExtra("groupId", DEFAULT_ID);
                startActivityForResult(intent, 2);
                break;
            }
            case EXPORT_PASSWORDS:
            {
                Toast.makeText(this, _fileHelper.SavePasswordsToFile(), Toast.LENGTH_LONG).show();
                break;
            }
            case IMPORT_PASSWORDS:
            {
                Toast.makeText(this, _fileHelper.ImportPasswordsFromFile(), Toast.LENGTH_LONG).show();
                InitPasswordList();
                break;
            }
            case CLOSE_APP:
            {
                setResult(CLOSE_APP);
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type =
                ExpandableListView.getPackedPositionType(info.packedPosition);

        // Only create a context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            menu.add(0, REMOVE_ELEMENT, 0, "Remove element");
        }

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
        {
            menu.add(0, CREATE_ELEMENT, 0, "Add element");
            menu.add(0, REMOVE_GROUP, 0, "Remove group");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD && item.getItemId() == REMOVE_ELEMENT) {
            Intent intent = new Intent(this, RemoveElementActivity.class);
            intent.putExtra("elementId", info.id);
            startActivityForResult(intent, 2);
        }

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP && item.getItemId() == CREATE_ELEMENT) {
            Intent intent = new Intent(this, AddElementActivity.class);
            intent.putExtra("groupId", info.id);
            intent.putExtra("isElement", true);
            startActivityForResult(intent, 2);
        }

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP && item.getItemId() == REMOVE_GROUP) {
            Intent intent = new Intent(this, RemoveElementActivity.class);
            intent.putExtra("groupId", info.id);
            startActivityForResult(intent, 2);
        }

        return super.onContextItemSelected(item);
    }

    private void InitPasswordList()
    {
        // подключаемся к БД
        db = new DatabaseHelper(this);
        _localScrambler = new Scrambler(_dbPassword);

        db.open(_dbPassword);
        _fileHelper = new FileRepository(FileRepository.IsExternalStorageWritable() ?  Environment.getExternalStorageDirectory() : this.getFilesDir(), db, _localScrambler);

        Cursor cursor = db.GetGroupData();
        // сопоставление данных и View для групп
        String[] groupFrom = { DatabaseHelper.GROUP_COLUMN_NAME };
        int[] groupTo = { android.R.id.text1 };
        // сопоставление данных и View для элементов
        String[] childFrom = { DatabaseHelper.ELEMENT_COLUMN_VALUE };
        int[] childTo = { android.R.id.text1 };

        // создаем адаптер и настраиваем список
        sctAdapter = new MyAdapter(this, cursor,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, android.R.layout.simple_list_item_1, childFrom,
                childTo);
        sctAdapter.setViewBinder(new DecryptViewBinder(_dbPassword));
        elvMain = (ExpandableListView) findViewById(R.id.elvMain);
        elvMain.setAdapter(sctAdapter);

        registerForContextMenu(elvMain);

        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Exit menu  was pressed
        if (data == null && resultCode == CLOSE_APP)
        {
            setResult(CLOSE_APP);
            finish();
            return;
        }

        //Null data
        if (data == null) return;

        boolean isRemove = data.getBooleanExtra("isRemove", false);
        boolean isInsert = data.getBooleanExtra("isInsert", false);
        long groupId = data.getLongExtra("groupId", DEFAULT_ID);
        long elementId = data.getLongExtra("elementId", DEFAULT_ID);
        String elementTitle = data.getStringExtra("elementTitle");
        String elementValue = data.getStringExtra("elementValue");

        if(resultCode == RESULT_OK && isRemove)
        {
            if(elementId != DEFAULT_ID)
            {
                //remove element
                db.DeleteElement(elementId);
                sctAdapter.notifyDataSetChanged();
            }
            else if(groupId != DEFAULT_ID && elementId == DEFAULT_ID)
            {
                //remove group
                db.DeleteGroup(groupId);
                InitPasswordList();
            }

            return;
        }

        if(resultCode == RESULT_OK && isInsert)
        {
            if(elementValue != "" && groupId == DEFAULT_ID)
            {
                //insert group
                db.InsertGroup(elementValue);
                InitPasswordList();
            }
            else if(elementValue != "" && groupId != DEFAULT_ID)
            {
                //insert element
                db.InsertElement(groupId, elementTitle, elementValue);
                sctAdapter.notifyDataSetChanged();
            }

            return;
        }
    }

    class MyAdapter extends SimpleCursorTreeAdapter {

        public MyAdapter(Context context, Cursor cursor, int groupLayout,
                         String[] groupFrom, int[] groupTo, int childLayout,
                         String[] childFrom, int[] childTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo,
                    childLayout, childFrom, childTo);
        }

        protected Cursor getChildrenCursor(Cursor groupCursor) {
            // получаем курсор по элементам для конкретной группы
            int idColumn = groupCursor.getColumnIndex(DatabaseHelper.GROUP_COLUMN_ID);
            return db.GetElementData(groupCursor.getInt(idColumn));
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
