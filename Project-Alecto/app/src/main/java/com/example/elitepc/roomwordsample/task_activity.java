package com.example.elitepc.roomwordsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;




public class task_activity extends AppCompatActivity{

    public static final String EXTRA_TASKREPLY = "com.example.android.wordlistsql.TASKREPLY";//use this to get the extras out on the other side

    //Textviews
    private TextView tv_taskName;
    private TextView tv_description;
    private TextView tv_goal;

    //Buttons
    private Button b_pickCategory;
    private Button b_pickParent;
    private Button b_pickChild;
    private Button b_save;

    //Checkboxes
    private CheckBox cb_pomodoro;
    private CheckBox cb_stopWatch;
    private CheckBox cb_timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        set_views();

    }
    private String gimmeText(TextView input){
        CharSequence value = input.getText();
        if(TextUtils.isEmpty(value)){
            return null;
        } else {
            return value.toString();
        }
    }
    private void buildTask(){
        Oppgave newTask = new Oppgave();
        String test = gimmeText(tv_taskName);
        if(test != null){
            newTask.setTname(test);
        }
    }

    public void set_onClick(){
        b_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String s_taskName;
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(tv_taskName.getText())) {//If taskname is empty
                    setResult(RESULT_CANCELED, replyIntent);//cancel
                } else {
                    s_taskName = tv_taskName.getText().toString();//save taskname
                    replyIntent.putExtra(EXTRA_TASKREPLY, s_taskName);//put into intent
                    setResult(RESULT_OK, replyIntent);//set the result
                }
                finish();//finish up.
            }
        });
    }

    public boolean set_views(){
        try{
            tv_taskName = findViewById(R.id.tv_taskName);
            tv_description = findViewById(R.id.tv_description);
            tv_goal = findViewById(R.id.tv_goal);

            b_pickCategory = findViewById(R.id.b_pickCategory);
            b_pickChild = findViewById(R.id.b_pickChild);
            b_pickParent = findViewById(R.id.b_pickParent);
            b_save = findViewById(R.id.b_save);

            cb_pomodoro = findViewById(R.id.cb_pomodoro);
            cb_stopWatch = findViewById(R.id.cb_stopWatch);
            cb_timer = findViewById(R.id.cb_timer);
            return true;
        } catch (Error e){
            System.out.format("Error from task_activity while setting views. Error:\n%s\n", e);
            return false;
        }
    }
}
