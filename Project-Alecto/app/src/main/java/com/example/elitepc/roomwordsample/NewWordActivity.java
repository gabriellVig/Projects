package com.example.elitepc.roomwordsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewWordActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "com.example.android.wordlistsql.REPLY";

    private EditText mEditWordView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_word);
        mEditWordView = findViewById(R.id.edit_word);

        final Button button = findViewById(R.id.button_save);//Set button
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(mEditWordView.getText())) {//If it is empty
                    setResult(RESULT_CANCELED, replyIntent);//cancel
                } else {
                    String word = mEditWordView.getText().toString();//save word
                    replyIntent.putExtra(EXTRA_REPLY, word);//put into intent
                    setResult(RESULT_OK, replyIntent);//set the result
                }
                finish();//finish up.
            }
        });
    }
}
