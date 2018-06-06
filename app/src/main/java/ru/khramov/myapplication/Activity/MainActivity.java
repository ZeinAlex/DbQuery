package ru.khramov.myapplication.Activity;

import android.database.Cursor;
import android.database.SQLException;
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
        button = findViewById(R.id.btn_run);
        button.setOnClickListener(view -> {
            textView.setText(requestFromDb());
        });
        findViewById(R.id.btn_table).setOnClickListener(view -> {
            Db.getInstance().beginTransaction();
            Cursor cursor = Db.getInstance().selectSQL("select * from SQLITE_MASTER WHERE type = 'table'");
            if (cursor != null) {
                textView.setText(fillString(cursor));
                cursor.close();
            }
        });
    }

    private String requestFromDb() {
        StringBuilder string = new StringBuilder(editText.getText().toString().toLowerCase());
        try {
            Db.getInstance().beginTransaction();
            if (!string.toString().startsWith("select")) {
                string.append(" \n").append(Db.getInstance().execSQL(string.toString()));
                Db.getInstance().commitTransaction();
            } else {
                Cursor cursor = Db.getInstance().selectSQL(string.toString());
                if (cursor != null) {
                    fillString(cursor);
                    string.append(fillString(cursor));
                    cursor.close();
                }
            }
        } catch (SQLException e) {
            string = new StringBuilder(e.toString());
        } finally {
            Db.getInstance().endTransaction();
        }
        return string.toString();
    }

    private String fillString(Cursor cursor) {
        StringBuilder string = new StringBuilder();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int columnCount = cursor.getColumnCount();
            string.append(" :\n\n");
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
        return string.toString();
    }
}
