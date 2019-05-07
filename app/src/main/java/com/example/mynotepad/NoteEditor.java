/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mynotepad;


import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Activity handles "editing" a note, where editing is responding to
 * {@link Intent#ACTION_VIEW} (request to view data), edit a note
 * {@link Intent#ACTION_EDIT}, create a note {@link Intent#ACTION_INSERT}, or
 * create a new note from the current contents of the clipboard {@link Intent#ACTION_PASTE}.
 *
 * NOTE: Notice that the provider operations in this Activity are taking place on the UI thread.
 * This is not a good practice. It is only done here to make the code more readable. A real
 * application should use the {@link android.content.AsyncQueryHandler}
 * or {@link android.os.AsyncTask} object to perform operations asynchronously on a separate thread.
 */
public class NoteEditor extends AppCompatActivity {
    // For logging and debugging purposes
    private static final String TAG = "NoteEditor";
    private static final int PHOTO_FROM_GALLERY = 1;
    private static final int PHOTO_FROM_CAMERA = 2;
    private static final String regex="content://com.android.providers.media.documents/"
            +"document/image%\\w{4}";
    private static final String reg="file:///storage/emulated/0/\\d+.jpg";
    /*
     * Creates a projection that returns the note ID and the note contents.
     */
    private static final String[] PROJECTION =
        new String[] {
                NotePad.Notes._ID,
                NotePad.Notes.COLUMN_NAME_TITLE,
                NotePad.Notes.COLUMN_NAME_NOTE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                NotePad.Notes.COLUMN_TAG_SELECTION_INDEX,
                NotePad.Notes.COLUMN_BACKGROUND_COLOR,
                NotePad.Notes.COLUMN_TEXT_COLOR,
                NotePad.Notes.COLUMN_TEXT_NOTIFICATION_DATE,
    };

    // A label for the saved state of the activity
    private static final String ORIGINAL_CONTENT = "origContent";

    // This Activity can be started by more than one action. Each action is represented
    // as a "state" constant
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    // Global mutable variables
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String mOriginalContent;
    private Menu amenu;
    private TextView textView;
    private final String items[]={"Default","Travel","Work","Study","Life"};
    private int checkItem=0;
    private Button button;
    private boolean flag=false;
    private Uri imageUri;
    private boolean isFlag;
    private String colorBack="null";
    private String colorText="null";
    private Button dateButton;
    private String date=null;
    private String time=null;

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.orange:
                if(isFlag){
                    mText.setBackgroundColor(Color.parseColor("#FF8C00"));
                    colorBack="#FF8C00";
                }else{
                    mText.setTextColor(Color.parseColor("#FF8C00"));
                    colorText="#FF8C00";
                }
                break;
            case R.id.chocolate:
                if(isFlag){
                    mText.setBackgroundColor(Color.parseColor("#D2691E"));
                    colorBack="#D2691E";
                }else{
                    mText.setTextColor(Color.parseColor("#D2691E"));
                    colorText="#D2691E";
                }
                break;
            case R.id.aqua:
                if(isFlag){
                    mText.setBackgroundColor(Color.parseColor("#00FFFF"));
                    colorBack="#00FFFF";
                }else{
                    mText.setTextColor(Color.parseColor("#00FFFF"));
                    colorText="#00FFFF";
                }
                break;
            case R.id.gray:
                if(isFlag){
                    mText.setBackgroundColor(Color.parseColor("#696969"));
                    colorBack="#696969";
                }else{
                    mText.setTextColor(Color.parseColor("#696969"));
                    colorText="#696969";
                }
                break;
            case R.id.pink:
                if(isFlag){
                    mText.setBackgroundColor(Color.parseColor("#D81B60"));
                    colorBack="#D81B60";
                }else{
                    mText.setTextColor(Color.parseColor("#D81B60"));
                    colorText="#D81B60";
                }
                break;
            case R.id.green:
                if(isFlag){
                    mText.setBackgroundColor(Color.parseColor("#00FF7F"));
                    colorBack="#00FF7F";
                }else{
                    mText.setTextColor(Color.parseColor("#00FF7F"));
                    colorText="#00FF7F";
                }
                break;
        }
    }

    /**
     * Defines a custom EditText View that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends android.support.v7.widget.AppCompatEditText {
        // This constructor is used by LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            // Creates a Rect and a Paint object, and sets the style and color of the Paint object.
            //setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            setSingleLine(false);
            setHorizontallyScrolling(false);
        }
    }

    /**
     * This method is called by Android when the Activity is first started. From the incoming
     * Intent, it determines what kind of editing is desired, and then does it.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Creates an Intent to use when the Activity object's result is sent back to the
         * caller.
         */
        final Intent intent = getIntent();

        /*
         *  Sets up for the edit, based on the action specified for the incoming Intent.
         */

        // Gets the action that triggered the intent filter for this Activity
        final String action = intent.getAction();

        // For an edit action:
        if (Intent.ACTION_EDIT.equals(action)) {

            // Sets the Activity state to EDIT, and gets the URI for the data to be edited.
            mState = STATE_EDIT;
            mUri = intent.getData();

            // For an insert or paste action:
        } else if (Intent.ACTION_INSERT.equals(action)
                || Intent.ACTION_PASTE.equals(action)) {

            // Sets the Activity state to INSERT, gets the general note URI, and inserts an
            // empty record in the provider
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);

            /*
             * If the attempt to insert the new note fails, shuts down this Activity. The
             * originating Activity receives back RESULT_CANCELED if it requested a result.
             * Logs that the insert failed.
             */
            if (mUri == null) {

                // Writes the log identifier, a message, and the URI that failed.
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());

                // Closes the activity.
                finish();
                return;
            }

            // Since the new entry was created, this sets the result to be returned
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        // If the action was other than EDIT or INSERT:
        } else {

            // Logs an error that the action was not understood, finishes the Activity, and
            // returns RESULT_CANCELED to an originating Activity.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        /*
         * Using the URI passed in with the triggering Intent, gets the note or notes in
         * the provider.
         * Note: This is being done on the UI thread. It will block the thread until the query
         * completes. In a sample app, going against a simple provider based on a local database,
         * the block will be momentary, but in a real app you should use
         * android.content.AsyncQueryHandler or android.os.AsyncTask.
         */
        mCursor = getContentResolver().query(
                mUri,         // The URI that gets multiple notes from the provider.
                PROJECTION,   // A projection that returns the note ID and note content for each note.
                null,         // No "where" clause selection criteria.
                null,         // No "where" clause selection values.
                null          // Use the default sort order (modification date, descending)
        );


        // For a paste, initializes the data from clipboard.
        // (Must be done after mCursor is initialized.)
        if (Intent.ACTION_PASTE.equals(action)) {
            // Does the paste
            performPaste();
            // Switches the state to EDIT so the title can be modified.
            mState = STATE_EDIT;
        }

        // Sets the layout for this Activity. See res/layout/note_editor.xml
        setContentView(R.layout.note_editor);

        // Gets a handle to the EditText in the the layout.
        mText = findViewById(R.id.note);
        mText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(amenu!=null){
                    amenu.findItem(R.id.menu_revert).setVisible(true);
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        /*
         * If this Activity had stopped previously, its state was written the ORIGINAL_CONTENT
         * location in the saved Instance state. This gets the state.
         */
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
        textView=findViewById(R.id.etv);
        button=findViewById(R.id.eb);
        dateButton=findViewById(R.id.dateButtom);
        dateButton.setVisibility(View.GONE);
    }
    public void tagSelect(View v){
        AlertDialog.Builder tagbuilder;
        AlertDialog alertDialog;
        tagbuilder=new AlertDialog.Builder(this);
        tagbuilder.setTitle("Tag Selection:");
        tagbuilder.setIcon(R.mipmap.ic_launcher);

        tagbuilder.setSingleChoiceItems(items, checkItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkItem=which;
                button.setText(items[checkItem]);

            }
        });
        tagbuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog=tagbuilder.create();
        alertDialog.show();

    }

    /**
     * This method is called when the Activity is about to come to the foreground. This happens
     * when the Activity comes to the top of the task stack, OR when it is first starting.
     *
     * Moves to the first note in the list, sets an appropriate title for the action chosen by
     * the user, puts the note contents into the TextView, and saves the original text as a
     * backup.
     */
    @Override
    protected void onResume() {
        super.onResume();
        /*
         * mCursor is initialized, since onCreate() always precedes onResume for any running
         * process. This tests that it's not null, since it should always contain data.
         */
        if (mCursor != null) {
            // Requery in case something changed while paused (such as the title)
            mCursor.requery();

            /* Moves to the first record. Always call moveToFirst() before accessing data in
             * a Cursor for the first time. The semantics of using a Cursor are that when it is
             * created, its internal index is pointing to a "place" immediately before the first
             * record.
             */
            mCursor.moveToFirst();

            // Modifies the window title for the Activity according to the current Activity state.
            if (mState == STATE_EDIT) {
                // Set the title of the Activity to include the note title
                int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                String title = mCursor.getString(colTitleIndex);
                Resources res = getResources();
                String text = String.format(res.getString(R.string.title_edit), title);
                setTitle(text);
                int colTagIndex=mCursor.getColumnIndex(NotePad.Notes.COLUMN_TAG_SELECTION_INDEX);
                String tag = mCursor.getString(colTagIndex);
                for(int i=0;i<items.length;i++){
                    if(items[i].equals(tag)){
                        checkItem=i;
                        break;
                    }
                }
                int colModifyDate=mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
                String date=mCursor.getString(colModifyDate);
                textView.setText(date);
                button.setText(items[checkItem]);
                colorBack=mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_BACKGROUND_COLOR));
                colorText=mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_TEXT_COLOR));
                if(!"null".equals(colorBack)&&colorBack!=null){
                    mText.setBackgroundColor(Color.parseColor(colorBack));
                }
                if(!"null".equals(colorText)&colorText!=null){
                    mText.setTextColor(Color.parseColor(colorText));
                }
                String calender=mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_TEXT_NOTIFICATION_DATE));
                dateButton.setText(calender);

                if(dateButton.getText().toString()!=null
                        && !(dateButton.getText().toString().equalsIgnoreCase("nullnull"))
                        &&!(dateButton.getText().toString().equalsIgnoreCase(""))){
                    dateButton.setVisibility(View.VISIBLE);
                }
            // Sets the title to "create" for inserts
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
                int colTagIndex=mCursor.getColumnIndex(NotePad.Notes.COLUMN_TAG_SELECTION_INDEX);
                String tag = mCursor.getString(colTagIndex);
                for(int i=0;i<items.length;i++){
                    if(items[i].equals(tag)){
                        checkItem=i;
                        break;
                    }
                }
                button.setText(items[checkItem]);
                Date nowTime = new Date(System.currentTimeMillis());
                SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String retStrFormatNowDate = sdFormatter.format(nowTime);
                textView.setText(retStrFormatNowDate);
            }

            /*
             * onResume() may have been called after the Activity lost focus (was paused).
             * The user was either editing or creating a note when the Activity paused.
             * The Activity should re-display the text that had been retrieved previously, but
             * it should not move the cursor. This helps the user to continue editing or entering.
             */

            // Gets the note text from the Cursor and puts it in the TextView, but doesn't change
            // the text cursor's position.
            int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);

            String note = mCursor.getString(colNoteIndex);
            ArrayList<String> contentList=new ArrayList<>();
            ArrayList<Integer> startList=new ArrayList<>();
            ArrayList<Integer> endList=new ArrayList<>();
            Pattern p=Pattern.compile(regex);
            Matcher m=p.matcher(note);

            while(m.find()){
                contentList.add(m.group());
                startList.add(m.start());
                endList.add(m.end());
                flag=true;
            }
            p=Pattern.compile(reg);
            m=p.matcher(note);
            while(m.find()){
                contentList.add(m.group());
                startList.add(m.start());
                endList.add(m.end());
                flag=true;
            }

            if(!flag){
                mText.setText(note);
            }else{
                pushPicture(note,contentList,startList,endList);
            }
            //mText.setText(note);


            //Stores the original note text, to allow the user to revert changes.
            if (mOriginalContent == null) {
                mOriginalContent = note;
            }

        /*
         * Something is wrong. The Cursor should always contain data. Report an error in the
         * note.
         */
        } else {
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }
    }
    private void pushPicture(String note,ArrayList<String> contentList,ArrayList<Integer> startList,ArrayList<Integer> endList) {
        //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
        SpannableString spannableString = new SpannableString(note);
        for(int i=0;i<contentList.size();i++) {
            Uri uri = Uri.parse(contentList.get(i));
            Bitmap bitmap = null;
            try {
                Bitmap originalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                bitmap = resizeImage(originalBitmap, 200, 200);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                //根据Bitmap对象创建ImageSpan对象
                ImageSpan imageSpan = new ImageSpan(NoteEditor.this, bitmap);

                //  用ImageSpan对象替换face
                spannableString.setSpan(imageSpan, startList.get(i), endList.get(i), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        mText.setText("");
        Editable edit_text = mText.getEditableText();
        edit_text.append(spannableString);
    }

    /**
     * This method is called when an Activity loses focus during its normal operation, and is then
     * later on killed. The Activity has a chance to save its state so that the system can restore
     * it.
     *
     * Notice that this method isn't a normal part of the Activity lifecycle. It won't be called
     * if the user simply navigates away from the Activity.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    /**
     * This method is called when the Activity loses focus.
     *
     * For Activity objects that edit information, onPause() may be the one place where changes are
     * saved. The Android application model is predicated on the idea that "save" and "exit" aren't
     * required actions. When users navigate away from an Activity, they shouldn't have to go back
     * to it to complete their work. The act of going away should save everything and leave the
     * Activity in a state where Android can destroy it if necessary.
     *
     * If the user hasn't done anything, then this deletes or clears out the note, otherwise it
     * writes the user's work to the provider.
     */
    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Tests to see that the query operation didn't fail (see onCreate()). The Cursor object
         * will exist, even if no records were returned, unless the query failed because of some
         * exception or error.
         *
         */
        if (mCursor != null) {

            // Get the current note text.
            String text = mText.getText().toString();
            int length = text.length();

            /*
             * If the Activity is in the midst of finishing and there is no text in the current
             * note, returns a result of CANCELED to the caller, and deletes the note. This is done
             * even if the note was being edited, the assumption being that the user wanted to
             * "clear out" (delete) the note.
             */
            if (isFinishing() && (length == 0)) {
                setResult(RESULT_CANCELED);
                deleteNote();

                /*
                 * Writes the edits to the provider. The note has been edited if an existing note was
                 * retrieved into the editor *or* if a new note was inserted. In the latter case,
                 * onCreate() inserted a new empty note into the provider, and it is this new note
                 * that is being edited.
                 */
            } else if (mState == STATE_EDIT) {
                // Creates a map to contain the new values for the columns
                //updateNote(text, null);

            } else if (mState == STATE_INSERT) {
                //updateNote(text, text);
                mState = STATE_EDIT;
          }
        }

    }
    /*
    * the method insert resources
    *
    *
    * */
    //从相册取图片
    public void getPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PHOTO_FROM_GALLERY);
    }
    //拍照取照片
    public void takeCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] { Manifest.permission.CAMERA }, PHOTO_FROM_CAMERA);
        }
        else {
            File file=new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis()+".jpg");
            try {
                if(file.exists()){
                    file.delete();
                }
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            imageUri=Uri.fromFile(file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
            startActivityForResult(intent, PHOTO_FROM_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ContentResolver resolver = getContentResolver();
        super.onActivityResult(requestCode, resultCode, data);
        //第一层switch
        switch (requestCode) {
            case PHOTO_FROM_GALLERY:
                //第二层switch
                switch (resultCode) {
                    case RESULT_OK:
                        if (data != null) {
                            Uri uri = data.getData();
                            Bitmap bitmap = null;
                            try {
                                Bitmap originalBitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri));
                                bitmap = resizeImage(originalBitmap, 200, 200);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            if(bitmap != null){//如果图片存在
                                //将选择的图片追加到EditText中光标所在位置
                                int index = mText.getSelectionStart(); //获取光标所在位置
                                Editable edit_text = mText.getEditableText();
                                if(index <0 || index >= edit_text.length()){
                                    edit_text.append(uri.toString());
                                    updateNoteText(mText.getText().toString());
                                }else{
                                    edit_text.insert(index,uri.toString());
                                    updateNoteText(mText.getText().toString());
                                }
                            }else{
                                Toast.makeText(NoteEditor.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case RESULT_CANCELED:
                        break;
                }
                break;
            case PHOTO_FROM_CAMERA:
                if (resultCode == RESULT_OK) {
                    Bitmap originalBitmap1=null;
                    try{
                        originalBitmap1=BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    if(originalBitmap1 != null){//如果图片存在保存URI
                        //将选择的图片追加到EditText中光标所在位置
                        int index = mText.getSelectionStart(); //获取光标所在位置
                        Editable edit_text = mText.getEditableText();
                        if(index <0 || index >= edit_text.length()){
                            edit_text.append(imageUri.toString());
                            updateNoteText(mText.getText().toString());
                        }else{
                            edit_text.insert(index, imageUri.toString());
                            updateNoteText(mText.getText().toString());
                        }
                    }else{
                        Toast.makeText(NoteEditor.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Log.e("result", "is not ok" + resultCode);
                }
                break;
            default:
                break;
        }
    }
    /**
     * 图片缩放
     * @param originalBitmap 原始的Bitmap
     * @param newWidth 自定义宽度
     * @return 缩放后的Bitmap
     */
    private Bitmap resizeImage(Bitmap originalBitmap, int newWidth, int newHeight){
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        //定义欲转换成的宽、高
//            int newWidth = 200;
//            int newHeight = 200;
        //计算宽、高缩放率
        float scanleWidth = (float)newWidth/width;
        float scanleHeight = (float)newHeight/height;
        //创建操作图片用的matrix对象 Matrix
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(scanleWidth,scanleHeight);
        //旋转图片 动作
        //matrix.postRotate(45);
        // 创建新的图片Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(originalBitmap,0,0,width,height,matrix,true);
        return resizedBitmap;
    }
    private void showColor(){
        AlertDialog alertDialog=new AlertDialog.Builder(this).setTitle("请选择颜色").
                setIcon(R.mipmap.ic_launcher).setView(R.layout.color_layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }
    /**
     * This method is called when the user clicks the device's Menu button the first time for
     * this Activity. Android passes in a Menu object that is populated with items.
     *
     * Builds the menus for editing and inserting, and adds in alternative actions that
     * registered themselves to handle the MIME types for this application.
     *
     * @param menu A Menu object to which items should be added.
     * @return True to display the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_options_menu, menu);
        amenu=menu;
        // Only add extra menu items for a saved note 
        if (mState == STATE_EDIT) {
            // Append to the
            // menu items for any other activities that can do stuff with it
            // as well.  This does a query on the system for any activities that
            // implement the ALTERNATIVE_ACTION for our data, adding a menu item
            // for each one that is found.
            Intent intent = new Intent(null, mUri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, NoteEditor.class), null, intent, 0, null);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Check if note has changed and enable/disable the revert option

        menu.findItem(R.id.menu_revert).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * This method is called when a menu item is selected. Android passes in the selected item.
     * The switch statement in this method calls the appropriate method to perform the action the
     * user chose.
     *
     * @param item The selected MenuItem
     * @return True to indicate that the item was processed, and no further work is necessary. False
     * to proceed to further processing as indicated in the MenuItem object.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case R.id.menu_save:
                String text = mText.getText().toString();
                updateNote(text, null);
                notifyMessage();
                finish();
                break;
            case R.id.menu_delete:
                deleteNote();
                finish();
                break;
            case R.id.menu_revert:
                cancelNote();
                break;
            case R.id.insert_album:
                 getPhoto();
                 break;
            case R.id.insert_camera:
                takeCamera();
                break;
            case R.id.background_color:
                isFlag=true;
                showColor();
                break;
            case R.id.text_color:
                isFlag=false;
                showColor();
                break;
            case R.id.set_date:
                createDateDialog();
                break;
            case R.id.set_time:
                createTimeDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void notifyMessage(){
        if(time!=null&&date!=null){
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
            long t=System.currentTimeMillis()+5000;
            try {
                t=simpleDateFormat.parse(date+time).getTime();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            Intent intent=new Intent(NoteEditor.this, RemindActionBroadcast.class);

            intent.putExtra("title",mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE)));
            intent.putExtra("context",mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE)));
            PendingIntent pendingIntent=PendingIntent.getBroadcast(NoteEditor.this,mCursor.getInt(mCursor.getColumnIndex(NotePad.Notes._ID)),intent,PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND,(int)((t-System.currentTimeMillis())/1000));
            //calendar.add(Calendar.SECOND,5);
            Log.i("ASDAWDAWD",String.valueOf(((t-System.currentTimeMillis())/1000)));
            AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
        }
    }
    private void createDateDialog(){
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_DARK,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date=year+"-"+(month+1)+"-"+dayOfMonth;
                        if(time!=null){
                            dateButton.setText(date+time);
                        }else{
                            String text=calendar.get(Calendar.HOUR_OF_DAY)+":"+(calendar.get(Calendar.MINUTE)+5);
                            time=" "+text;
                            dateButton.setText(date+" "+text);
                        }

                        dateButton.setVisibility(View.VISIBLE);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
        dialog.setTitle("选择日期：");
        dialog.show();
    }
    private void createTimeDialog(){
        final Calendar calendar=Calendar.getInstance();
        TimePickerDialog dialog=new TimePickerDialog(this, AlertDialog.THEME_HOLO_DARK,new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String text=calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);
                if(text.equals(date)||date==null){
                    if(hourOfDay<=calendar.get(Calendar.HOUR_OF_DAY))
                        if(minute-5<=calendar.get(Calendar.MINUTE)){
                            time=" "+calendar.get(Calendar.HOUR_OF_DAY)+":"+(calendar.get(Calendar.MINUTE)+5);
                        }
                        else{
                            time=" "+calendar.get(Calendar.HOUR_OF_DAY)+":"+minute;
                        }
                    else{
                        time=" "+hourOfDay+":"+minute;
                    }
                }else{
                    time=" "+hourOfDay+":"+minute;
                }
                if(date!=null){
                    dateButton.setText(date+time);
                }else{
                    date=text;
                    dateButton.setText(text+time);
                }
                dateButton.setVisibility(View.VISIBLE);
            }
        },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true);
        dialog.setTitle("选择时间：");

        dialog.show();
    }
    public void dateClick(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("请确认是否删除提醒时间：").setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                date=null;
                time=null;
                dateButton.setVisibility(View.GONE);
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

//BEGIN_INCLUDE(paste)
    /**
     * A helper method that replaces the note's data with the contents of the clipboard.
     */
    private final void performPaste() {

        // Gets a handle to the Clipboard Manager
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        // Gets a content resolver instance
        ContentResolver cr = getContentResolver();

        // Gets the clipboard data from the clipboard
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {

            String text=null;
            String title=null;

            // Gets the first item from the clipboard data
            ClipData.Item item = clip.getItemAt(0);

            // Tries to get the item's contents as a URI pointing to a note
            Uri uri = item.getUri();

            // Tests to see that the item actually is an URI, and that the URI
            // is a content URI pointing to a provider whose MIME type is the same
            // as the MIME type supported by the Note pad provider.
            if (uri != null && NotePad.Notes.CONTENT_ITEM_TYPE.equals(cr.getType(uri))) {

                // The clipboard holds a reference to data with a note MIME type. This copies it.
                Cursor orig = cr.query(
                        uri,            // URI for the content provider
                        PROJECTION,     // Get the columns referred to in the projection
                        null,           // No selection variables
                        null,           // No selection variables, so no criteria are needed
                        null            // Use the default sort order
                );

                // If the Cursor is not null, and it contains at least one record
                // (moveToFirst() returns true), then this gets the note data from it.
                if (orig != null) {
                    if (orig.moveToFirst()) {
                        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                        int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                        text = orig.getString(colNoteIndex);
                        title = orig.getString(colTitleIndex);
                    }

                    // Closes the cursor.
                    orig.close();
                }
            }

            // If the contents of the clipboard wasn't a reference to a note, then
            // this converts whatever it is to text.
            if (text == null) {
                text = item.coerceToText(this).toString();
            }

            // Updates the current note with the retrieved title and text.
            updateNote(text, title);
        }
    }
//END_INCLUDE(paste)

    /**
     * Replaces the current note contents with the text and title provided as arguments.
     * @param text The new note contents to use.
     * @param title The new note title to use
     */
    private final void updateNote(String text, String title) {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String retStrFormatNowDate = sdFormatter.format(nowTime);

        // Sets up a map to contain values to be updated in the provider.
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, retStrFormatNowDate);
        values.put(NotePad.Notes.COLUMN_TAG_SELECTION_INDEX,items[checkItem]);
        values.put(NotePad.Notes.COLUMN_BACKGROUND_COLOR,colorBack);
        values.put(NotePad.Notes.COLUMN_TEXT_COLOR,colorText);

        values.put(NotePad.Notes.COLUMN_TEXT_NOTIFICATION_DATE,date+time);

        // If the action is to insert a new note, this creates an initial title for it.
        if (mState == STATE_INSERT) {

            // If no title was provided as an argument, create one from the note text.
            if (title == null) {

                // Get the note's length
                int length = text.length();

                // Sets the title by getting a substring of the text that is 31 characters long
                // or the number of characters in the note plus one, whichever is smaller.
                title = text.substring(0, Math.min(5, length));

                // If the resulting length is more than 30 characters, chops off any
                // trailing spaces
                if (length > 30) {
                    int lastSpace = title.lastIndexOf(' ');
                    if (lastSpace > 0) {
                        title = title.substring(0, lastSpace);
                    }
                }
            }

            // In the values map, sets the value of the title
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);

        } else if (title != null) {
            // In the values map, sets the value of the title
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        }

        // This puts the desired notes text into the map.
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);

        /*
         * Updates the provider with the new values in the map. The ListView is updated
         * automatically. The provider sets this up by setting the notification URI for
         * query Cursor objects to the incoming URI. The content resolver is thus
         * automatically notified when the Cursor for the URI changes, and the UI is
         * updated.
         * Note: This is being done on the UI thread. It will block the thread until the
         * update completes. In a sample app, going against a simple provider based on a
         * local database, the block will be momentary, but in a real app you should use
         * android.content.AsyncQueryHandler or android.os.AsyncTask.
         */

        getContentResolver().update(
                mUri,    // The URI for the record to update.
                values,  // The map of column names and new values to apply to them.
                null,    // No selection criteria are used, so no where columns are necessary.
                null     // No where columns are used, so no where arguments are necessary.
        );

    }
    private final void updateNoteText(String text) {

        ContentValues values = new ContentValues();
        // This puts the desired notes text into the map.
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
        if(mCursor!=null){
            values.put(NotePad.Notes.COLUMN_NAME_TITLE,mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE)));
        }else {
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, text.substring(0, Math.min(5, text.length())));
        }
        getContentResolver().update(
                mUri,    // The URI for the record to update.
                values,  // The map of column names and new values to apply to them.
                null,    // No selection criteria are used, so no where columns are necessary.
                null     // No where columns are used, so no where arguments are necessary.
        );

    }

    /**
     * This helper method cancels the work done on a note.  It deletes the note if it was
     * newly created, or reverts to the original text of the note i
     */
    private final void cancelNote() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                /*mCursor.close();
                mCursor = null;*/
                ContentValues values = new ContentValues();
                values.put(NotePad.Notes.COLUMN_NAME_NOTE, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                deleteNote();
            }
        }
        setResult(RESULT_CANCELED);
        ArrayList<String> contentList=new ArrayList<>();
        ArrayList<Integer> startList=new ArrayList<>();
        ArrayList<Integer> endList=new ArrayList<>();
        Pattern p=Pattern.compile(regex);
        Matcher m=p.matcher(mOriginalContent);

        while(m.find()){
            contentList.add(m.group());
            startList.add(m.start());
            endList.add(m.end());
            flag=true;
            //Log.i("dawdawd",notes[i]);
        }
        p=Pattern.compile(reg);
        m=p.matcher(mOriginalContent);
        while(m.find()){
            contentList.add(m.group());
            startList.add(m.start());
            endList.add(m.end());
            flag=true;
        }


        if(flag==false){
            mText.setText(mOriginalContent);
        }else{
            pushPicture(mOriginalContent,contentList,startList,endList);
        }

    }

    /**
     * Take care of deleting a note.  Simply deletes the entry.
     */
    private final void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }


}
