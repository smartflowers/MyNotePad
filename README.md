# MyNotePad
1.0 Version

## 运行环境
AndroidStudio 3.3.1 JDK 1.8 minSdkVersion 21 TargetSdkVersion 28 模拟机API 23
## 时间戳
### 代码分析
（1）在布局文件中增加一个TextView来显示时间戳
```
<TextView
        android:id="@+id/text2"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:textSize="12dp"
        android:gravity="center_vertical"
        android:paddingLeft="10dip"
        android:singleLine="true"
        android:layout_weight="1"
        android:layout_margin="0dp"
        />
```
（2）数据库中已有文本创建时间和修改时间连个字段，在NodeEditor.java中,找到updateNode()这个函数，选取修改时间这一字段，并将其格式化存入数据库
```
Date nowTime = new Date(System.currentTimeMillis());
SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String retStrFormatNowDate = sdFormatter.format(nowTime);
```
```
values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, retStrFormatNowDate);
```
（3）在NoteList.java的PROJECTION数组中增加该字段的描述，并在SimpleCursorAdapter中的参数viewsIDs和dataColumns增加子段描述，以达到将其读出和显示的目的
```
//The columns needed by the cursor adapter
private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
    };
// The names of the cursor columns to display in the view, initialized to the title column
private String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE} ;
// The view IDs that will display the cursor columns, initialized to the TextView in
private int[] viewIDs = { R.id.text1,R.id.text2 };
```
### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/allScreen.jpg width="200" />

## 搜索笔记以title为关键字段
### 代码分析
（1）在NodeList.java的布局文件中新增SearchView控件（备注：引用的是support.v7的包，如未找到，需在build.gradle(Module.app）中添加依赖
```
<android.support.v7.widget.SearchView
            android:id="@+id/sv"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            />
```
（2）在NodeList.java中创建一个函数专门来配置SeachView，实现查询的基本思想是新创建一个Cursor来通过SeacrhView搜索的字段在数据库中进行模糊搜索从而装配数据，并在ListView中即时显示，无需点击搜索按键，最后在onCreate()中调用
```
private void SearchView(){
        searchView=findViewById(R.id.sv);
        // a display model
        searchView.onActionViewExpanded();
        //default display
        searchView.setQueryHint("搜索笔记");
        //display submit button
        searchView.setSubmitButtonEnabled(true);
        //implement SearchView TextListener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // when text has been submitted
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            // when text is changing
            @Override
            public boolean onQueryTextChange(String s) {
                if(!s.equals("")){
                    String selection=NotePad.Notes.COLUMN_NAME_TITLE+" GLOB '*"+s+"*'";//query selection condition
                    updatecursor = getContentResolver().query(
                            getIntent().getData(),            // Use the default content URI for the provider.
                            PROJECTION,                       // Return the note ID and title for each note.
                            selection,                             // No where clause, return all records.
                            null,                             // No where clause, therefore no where column values.
                            NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                    );
                }
               else {
                    updatecursor = getContentResolver().query(
                            getIntent().getData(),            // Use the default content URI for the provider.
                            PROJECTION,                       // Return the note ID and title for each note.
                            null,                             // No where clause, return all records.
                            null,                             // No where clause, therefore no where column values.
                            NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                    );
                }
                // change adapter from SimpleCursorAdapter cursor
                adapter.swapCursor(updatecursor);
               // adapter.notifyDataSetChanged();
                return false;
            }
        });
    }
```

### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/searchView1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/searchView2.jpg width="200" />

## 修改背景色及字体颜色
### 代码分析
（1）在OptionMenu中添加修改颜色这一选项，并划分为两类，修改背景颜色和修改字体颜色
```
<item
        android:title="改变颜色">
        <menu>
            <item
                android:title="改变背景颜色"
                android:id="@+id/background-color">
            </item>
            <item android:id="@+id/text-color"
                android:title="改变字体颜色">
            </item>
        </menu>
</item>
```
（2）采用AlertDialog来实现界面交互效果，并采用自定义布局定义UI界面，因为两种修改颜色调用的是同一个函数showColor（），因此定义一个boolean变量flag来判断
```
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
```
AlertDialog布局文件
```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="horizontal">
    <Button
        android:id="@+id/orange"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="@color/orange"
        android:layout_weight="1"
        android:onClick="onClick"/>
    <Button
        android:id="@+id/chocolate"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="@color/chocolate"
        android:layout_weight="1"
        android:onClick="onClick"/>
    <Button
        android:id="@+id/aqua"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="@color/aqua"
        android:layout_weight="1"
        android:onClick="onClick"/>
    <Button
        android:id="@+id/gray"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="@color/gray"
        android:layout_weight="1"
        android:onClick="onClick"/>
    <Button
        android:id="@+id/pink"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="@color/colorAccent"
        android:layout_weight="1"
        android:onClick="onClick"/>
    <Button
        android:id="@+id/green"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="@color/green"
        android:layout_weight="1"
        android:onClick="onClick"/>
</LinearLayout>
```
布局文件按钮的点击事件
```
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
```
（3）在onOptionItemSelected（）中添加点击事件
```
case R.id.background_color:
    isFlag=true;
    showColor();
    break;
case R.id.text_color:
    isFlag=false;
    showColor();
    break;
```
（4）点击保存之后将颜色的16进制保存到数据库,也是通过updateNote()函数，具体实现与时间戳功能类似，这里说明一下数据库新增字段的实现<br>
1.在NotePad.java中的内部类Notes添加契约
```
/*
* NoteEditor.java EditText setBackGroundColor
* */
public static final String COLUMN_BACKGROUND_COLOR="bColor";
/*
 * NoteEditor.java EditText setTextColor
 * */
public static final String COLUMN_TEXT_COLOR="tColor";
```
2.在NotePadProvider.java的静态构造块往sNotesProjectionMap添加相应的键值对
```
//Maps "bColor" to "bColor"
sNotesProjectionMap.put(
        NotePad.Notes.COLUMN_BACKGROUND_COLOR,
        NotePad.Notes.COLUMN_BACKGROUND_COLOR
);
//Maps "tColor" to "tColor"
sNotesProjectionMap.put(
        NotePad.Notes.COLUMN_TEXT_COLOR,
        NotePad.Notes.COLUMN_TEXT_COLOR
);
```
备注：如果已经在手机中安装app之后要新增或删减字段，需要在onUpgrade（）中更换版本号，或者卸载重装。
### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColor.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorBackground1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorBackground2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorBackground4.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorText1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorText2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorText3.jpg width="200" />

## 添加标签以将文本分类
### 代码分析
（1）在数据库中添加标签字段，与修改颜色功能类似，不作相应代码显示
（2）在NodeEditor.java布局文件中添加Button控件，点击弹出AlertDialog显示类别选项，设置按钮文本
```
//tag selection defined
private final String items[]={"Default","Travel","Work","Study","Life"};
```
```
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
```
（3）点击保存之后会将按钮文本保存到数据库，与时间戳功能类似，不再作具体演示
（4）在NoteList.java添加抽屉布局，以显示侧滑菜单栏的效果<br>
1.在AndroidManifest.xml中更改主题，禁用ActionBar，查看引用，在styles.xml中进行修改
```
android:theme="@style/AppTheme"
```
```
style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar"
```
2.在布局文件中使用DrawerLayout布局和toolbar，还有NavigationView，与之前同理，导入的分别是v4和v7的包，如果没有，需要添加依赖（注：CoordinatorLayout是之前想做一个浮动布局加的，但后来放弃了，所以这个布局是可以删除的，只要修改号里面控件的width和height就可以）
```
<android.support.v4.widget.DrawerLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/dl"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:focusableInTouchMode="true"
        android:focusable="true"

       >
        <android.support.v7.widget.Toolbar
            android:id="@+id/tb"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/colorPrimary"
            app:titleTextColor="@color/white"
            app:title="MyNotePad"
            app:navigationIcon="@android:drawable/ic_menu_sort_by_size"
            >

        </android.support.v7.widget.Toolbar>
        <android.support.v7.widget.SearchView
            android:id="@+id/sv"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            />
        <android.support.design.widget.CoordinatorLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            xmlns:app="http://schemas.android.com/apk/res-auto"
            >
            <ListView
                android:id="@+id/tv"
                android:layout_width="match_parent"
                android:layout_height="match_parent"/>

        </android.support.design.widget.CoordinatorLayout>
    </LinearLayout>
    <android.support.design.widget.NavigationView
        android:id="@+id/nv"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:fitsSystemWindows="true"
        android:layout_gravity="left"
        app:headerLayout="@layout/nv_header"
        app:menu="@menu/nv_menu"
        >

    </android.support.design.widget.NavigationView>
</android.support.v4.widget.DrawerLayout>
```
3.NavigationView的菜单编写
```
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <group>
        <item android:title="NotePad">
            <menu>
                <item
                    android:id="@+id/notepad"
                    android:title="MyNotePad">
                </item>
            </menu>
        </item>
    </group>
    <group>
        <item android:title="tag">
            <menu>
                <item
                    android:id="@+id/travel"
                    android:title="Travel">
                </item>
                <item
                    android:id="@+id/work"
                    android:title="Work">
                </item>
                <item
                    android:id="@+id/study"
                    android:title="Study">
                </item>
                <item
                    android:id="@+id/life"
                    android:title="Life">
                </item>
                <item
                    android:id="@+id/def"
                    android:title="Default">
                </item>
            </menu>
        </item>
    </group>
</menu>
```
（3）编写toolbar的监听事件和NavigationView的监听事件
```
setSupportActionBar(toolbar); //set support toolbar in this Activity
toolbar.setNavigationOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        drawerLayout.openDrawer(Gravity.START);// set from left open NavigationView Menu
    }
});
```
```
navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.notepad:
                        tagCursor = getContentResolver().query(
                                getIntent().getData(),            // Use the default content URI for the provider.
                                PROJECTION,                       // Return the note ID and title for each note.
                                null,                             // No where clause, return all records.
                                null,                             // No where clause, therefore no where column values.
                                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                        );
                        adapter.swapCursor(tagCursor);//swap cursor from SimpleCursorAdapter
                        toolbar.setTitle("MyNotePad");
                        drawerLayout.closeDrawer(navigationView);//close NavigationView menu
                        break;
                    case R.id.travel:
                        tagSelection=NotePad.Notes.COLUMN_TAG_SELECTION_INDEX+" = 'Travel'";
                        tagCursor = getContentResolver().query(
                                getIntent().getData(),            // Use the default content URI for the provider.
                                PROJECTION,                       // Return the note ID and title for each note.
                                tagSelection,                             // No where clause, return all records.
                                null,                             // No where clause, therefore no where column values.
                                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                        );
                        adapter.swapCursor(tagCursor);//swap cursor from SimpleCursorAdapter
                        toolbar.setTitle("Travel");
                        drawerLayout.closeDrawer(navigationView);//close NavigationView menu
                        break;
                    case R.id.work:
                        tagSelection=NotePad.Notes.COLUMN_TAG_SELECTION_INDEX+" = 'Work'";
                        tagCursor = getContentResolver().query(
                                getIntent().getData(),            // Use the default content URI for the provider.
                                PROJECTION,                       // Return the note ID and title for each note.
                                tagSelection,                             // No where clause, return all records.
                                null,                             // No where clause, therefore no where column values.
                                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                        );
                        adapter.swapCursor(tagCursor);//swap cursor from SimpleCursorAdapter
                        toolbar.setTitle("Work");
                        drawerLayout.closeDrawer(navigationView);//close NavigationView menu
                        break;
                    case R.id.study:
                        tagSelection=NotePad.Notes.COLUMN_TAG_SELECTION_INDEX+" = 'Study'";
                        tagCursor = getContentResolver().query(
                                getIntent().getData(),            // Use the default content URI for the provider.
                                PROJECTION,                       // Return the note ID and title for each note.
                                tagSelection,                             // No where clause, return all records.
                                null,                             // No where clause, therefore no where column values.
                                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                        );
                        adapter.swapCursor(tagCursor);//swap cursor from SimpleCursorAdapter
                        toolbar.setTitle("Study");
                        drawerLayout.closeDrawer(navigationView);//close NavigationView menu
                        break;
                    case R.id.life:
                        tagSelection=NotePad.Notes.COLUMN_TAG_SELECTION_INDEX+" = 'Life'";
                        tagCursor = getContentResolver().query(
                                getIntent().getData(),            // Use the default content URI for the provider.
                                PROJECTION,                       // Return the note ID and title for each note.
                                tagSelection,                             // No where clause, return all records.
                                null,                             // No where clause, therefore no where column values.
                                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                        );
                        adapter.swapCursor(tagCursor);//swap cursor from SimpleCursorAdapter
                        toolbar.setTitle("Life");
                        drawerLayout.closeDrawer(navigationView);//close NavigationView menu
                        break;
                    case R.id.def:
                        tagSelection=NotePad.Notes.COLUMN_TAG_SELECTION_INDEX+" = 'Default'";
                        tagCursor = getContentResolver().query(
                                getIntent().getData(),            // Use the default content URI for the provider.
                                PROJECTION,                       // Return the note ID and title for each note.
                                tagSelection,                             // No where clause, return all records.
                                null,                             // No where clause, therefore no where column values.
                                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
                        );
                        adapter.swapCursor(tagCursor);//swap cursor from SimpleCursorAdapter
                        toolbar.setTitle("Default");
                        drawerLayout.closeDrawer(navigationView);//close NavigationView menu
                        break;
                }
                return true;
            }
        });
```
### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView3.jpg width="200" />

## 在文本中插入图片
### 代码分析
（1）在NodeEditor.java的optionmenu中添加选项
```
<item android:title="插入图片"
        android:icon="@android:drawable/ic_menu_gallery"
        app:showAsAction="always|withText">
        <menu>
            <item
                android:title="相册"
                android:icon="@android:drawable/ic_menu_gallery"
                android:id="@+id/insert_album">
            </item>
            <item
                android:id="@+id/insert_camera"
                android:icon="@android:drawable/ic_menu_camera"
                android:title="拍照">
            </item>
        </menu>
    </item>
```
（2）拍照要在AndroidManifest.xml中设置权限，API23以上还要在代码中设置动态权限，API28以上权限设置更为严格，本功能并不适用。而从图库中选取照片权限要求较低.
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
```
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
```
（3）编写onActivityResult定义返回动作
```
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
                            Uri uri = data.getData();//获取Intent uri
                            Bitmap bitmap = null;
                            //判断该路径是否存在
                            try {
                                Bitmap originalBitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri));
                                bitmap = resizeImage(originalBitmap, 200, 200);//图片缩放
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            if(bitmap != null){//如果图片存在
                                //将选择的图片追加到EditText中光标所在位置
                                int index = mText.getSelectionStart(); //获取光标所在位置
                                Editable edit_text = mText.getEditableText();//编辑文本框
                                if(index <0 || index >= edit_text.length()){
                                    edit_text.append(uri.toString());
                                    updateNoteText(mText.getText().toString());//更新到数据库
                                }else{
                                    edit_text.insert(index,uri.toString());
                                    updateNoteText(mText.getText().toString());//更新到数据库
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
                    //判断图片是否存在
                    try{
                        originalBitmap1=BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    if(originalBitmap1 != null){//如果图片存在保存URI
                        //将选择的图片追加到EditText中光标所在位置
                        int index = mText.getSelectionStart(); //获取光标所在位置
                        Editable edit_text = mText.getEditableText();//编辑文本框
                        if(index <0 || index >= edit_text.length()){
                            edit_text.append(imageUri.toString());
                            updateNoteText(mText.getText().toString());//更新到数据库
                        }else{
                            edit_text.insert(index, imageUri.toString());
                            updateNoteText(mText.getText().toString());//更新到数据库
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
```
（4）定义两个正则表达式，提取文本的图片路径，用SpannableString进行图片文字替换，从而实现图片的插入
```
private static final String regex="content://com.android.providers.media.documents/"
    +"document/image%\\w{4}";
private static final String reg="file:///storage/emulated/0/\\d+.jpg";
```
onResumn（）和cancelNote（）中编写
```
    int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);//提取数据库中的文本

    String note = mCursor.getString(colNoteIndex);
    ArrayList<String> contentList=new ArrayList<>();//存放图片路径
    ArrayList<Integer> startList=new ArrayList<>();//存放图片路径起点位置
    ArrayList<Integer> endList=new ArrayList<>();//存放图片路径终点位置
    Pattern p=Pattern.compile(regex);
    Matcher m=p.matcher(note);
    //当匹配到图库相应资源地址，将对应信息存入List
    while(m.find()){
        contentList.add(m.group());
        startList.add(m.start());
        endList.add(m.end());
        flag=true;
    }
    p=Pattern.compile(reg);
    m=p.matcher(note);
    //当匹配到拍照的图片相应资源地址，将对应信息存入List
    while(m.find()){
        contentList.add(m.group());
        startList.add(m.start());
        endList.add(m.end());
        flag=true;
    }
    //判断文本中是否有图片资源地址
    if(!flag){
        mText.setText(note);
    }else{
        pushPicture(note,contentList,startList,endList);
    }
```
图片处理
```
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
```
### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/insertPhoto1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/insertPhoto2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/insertPhoto3.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/insertPhoto4.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/insertByCamera1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/insertByCamera2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/insertByCamera3.jpg width="200" />

## 设置各文本的提醒时间
### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setNotiDate.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setDate1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setDate2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setDate3.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setTime1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setTime2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setTime3.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/setAnotherNoti.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/notifiResults.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/deleteNoti.jpg width="200" />
