package com.android.translator_hin_eng;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et_1;
    TextView txt, txt_lan_1, txt_lan_2;
    Translator englishHindiTranslator, hindiEnglishTranslator;
    ClipboardManager clipboard;
    ClipData clip;
    MDToast mdToast;
    Boolean flag = true;
    Dialog dialog;
    TranslatorOptions options_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_1 = findViewById(R.id.et_1);
        txt = findViewById(R.id.txt);
        txt_lan_1 = findViewById(R.id.txt_lan_1);
        txt_lan_2 = findViewById(R.id.txt_lan_2);

        findViewById(R.id.swap).setOnClickListener(this);
        findViewById(R.id.mic).setOnClickListener(this);
        findViewById(R.id.cp_1).setOnClickListener(this);
        findViewById(R.id.cp_2).setOnClickListener(this);

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        dialog = new Dialog(MainActivity.this, android.R.style.Theme_Dialog);
        openDialog();

        et_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (flag) {
                    translateHin(et_1.getText().toString());
                } else {
                    translateEng(et_1.getText().toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Create an English-Hindi translator:
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build();

        options_2 = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.HINDI)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();

        englishHindiTranslator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        englishHindiTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // Model downloaded successfully. Okay to start translating.
                        downloadHinEng();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Model couldn't be downloaded or other internal error.
                        txt.setText(e.getMessage());
                        dialog.dismiss();
                    }
                });
    }

    void downloadHinEng() {
        hindiEnglishTranslator = Translation.getClient(options_2);
        hindiEnglishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // Model downloaded successfully. Okay to start translating.
                        txt.setText(null);
                        dialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Model couldn't be downloaded or other internal error.
                        txt.setText(e.getMessage());
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mic:
                voice();
                break;

            case R.id.swap:
                swap();
                break;

            case R.id.cp_1:
                copy(et_1.getText().toString());
                break;

            case R.id.cp_2:
                copy(txt.getText().toString());
                break;
        }
    }

    void toast(String message, int type) {
        mdToast = MDToast.makeText(getApplicationContext(), message, MDToast.LENGTH_SHORT, type);
        mdToast.show();
    }

    void voice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (flag)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        else
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi");
        try {
            startActivityForResult(intent, 200);
        } catch (ActivityNotFoundException a) {
            toast("Intent Problem", 3);
        }
    }

    public void openDialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setCancelable(false);
        dialog.show();
    }

    void swap() {
        String a = txt_lan_1.getText().toString();
        String b = txt_lan_2.getText().toString();
        a = a + b;
        b = a.substring(0, a.length() - b.length());
        a = a.substring(b.length());
        txt_lan_1.setText(a);
        txt_lan_2.setText(b);
        flag = !flag;
        et_1.setText(null);
        txt.setText(null);
        toast("Language Changed", 1);
    }

    void copy(String text) {
        if (!text.equals("")) {
            clip = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(clip);
            toast("Text Copied", 1);
        } else {
            toast("There is no text", 2);
        }
        mdToast.show();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                assert result != null;
                et_1.setText(result.get(0));

                if (flag) {
                    translateHin(et_1.getText().toString().trim());
                } else {
                    translateEng(et_1.getText().toString().trim());
                }
            }
        }
    }

    void translateHin(String text) {
        englishHindiTranslator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String translatedText) {
                        txt.setText(translatedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        txt.setText(e.getMessage());
                    }
                });
    }

    void translateEng(String text) {
        hindiEnglishTranslator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String translatedText) {
                        txt.setText(translatedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        txt.setText(e.getMessage());
                    }
                });
    }
}
