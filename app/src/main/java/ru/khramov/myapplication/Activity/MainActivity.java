package ru.khramov.myapplication.Activity;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import ru.khramov.myapplication.BusinessLayer.Db;
import ru.khramov.myapplication.R;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDb();
        initView();
    }

    private void initDb() {
        try {
            Db.getInstance().createDataBase();
            Db.getInstance().beginTransaction();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            textView.setText(requestFromDb());
        });
    }

    private String requestFromDb() {
        StringBuilder string = new StringBuilder(editText.getText().toString().toLowerCase());
        try {
            Db.getInstance().beginTransaction();
            if (!string.toString().startsWith("select")) {
                Db.getInstance().execSQL(string.toString());
                Db.getInstance().commitTransaction();
            } else {
                Cursor cursor = Db.getInstance().selectSQL(string.toString());
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int columnCount = cursor.getColumnCount();
                        string.append(":\n");
                        string.append("\n");
                        for (int i = 0; i < columnCount; i++) {
                            string.append(cursor.getColumnName(i)).append("\t|\t");
                        }
                        string.append("\n");
                        do {
                            for (int i = 0; i < columnCount; i++) {
                                string.append(cursor.getString(i)).append("\t|\t");
                            }
                            string.append("\n");
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            }
        } catch (Exception e) {
            string = new StringBuilder(e.toString());
        } finally {
            Db.getInstance().endTransaction();
        }
        return string.toString();
    }
}
