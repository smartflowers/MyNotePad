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
                    String selection=NotePad.Notes.COLUMN_NAME_TITLE+" GLOB '*"+s+"*'";
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
### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColor.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorBackground1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorBackground2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorBackground4.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorText1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorText2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/changeColorText3.jpg width="200" />

## 添加标签以将文本分类
### 效果
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView1.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView2.jpg width="200" />
<img src=https://github.com/smartflowers/MyNotePad/blob/1.0/pictures/TagView3.jpg width="200" />

## 在文本中插入图片
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
