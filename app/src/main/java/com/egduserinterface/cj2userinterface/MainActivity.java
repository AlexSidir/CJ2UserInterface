package com.egduserinterface.cj2userinterface;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity  extends AppCompatActivity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

    private TextToSpeech textToSpeech;

    private ListView listOfMatches;

    private Spinner textMatches;

    private Button speakButton;
    private Button firstLoginButton;
    private Button loginButton;

    private boolean loginFinished = false;
    private boolean firstTimeLogin = true;

    private boolean isMainActivityActive = false;

    private String PASSWORD = "PASSWORD";
    private String FIRST_LAUNCH = "firstlaunch";

    SharedPreferences prefs = null;

    private String appName = "com.egduserinterface.cj2userinterface.CJ2UserInterface";

    ArrayList<String> closestSuggestionsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(appName, MODE_PRIVATE);
        if(prefs.contains(PASSWORD)){
            firstTimeLogin = false;
            checkVoiceRecognition(loginButton);
        } else {
            firstTimeLogin = true;
            checkVoiceRecognition(firstLoginButton);
        }
        isMainActivityActive = false;
        loginFinished = false;

        if (prefs.getBoolean(FIRST_LAUNCH, true)) {
            prefs.edit().putBoolean(FIRST_LAUNCH, false).apply();
            //textToSpeech.speak("Please set a password for the application", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
            setContentView(R.layout.first_login_layout);
        } else {
            //textToSpeech.speak("Please enter your password.", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
            setContentView(R.layout.login_layout);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isMainActivityActive) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        } else return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkVoiceRecognition(Button speakButton) {
        // Check if voice recognition is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e("TTS", "TextToSpeech.OnInitListener.onInit...");
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.setPitch(1f);
                    textToSpeech.setSpeechRate(1f);
                    //TODO Changing the voice for the Text to Speech
                    //Voice v=new Voice ("it-it-x-kda#male_2-local", Locale.getDefault(),1,1,false,null);
                    //textToSpeech.setVoice(v);
                } else {
                    Log.w(TAG, "Could not open TTS Engine (onInit status=" + status + ")");
                    textToSpeech = null;
                }
            }
        });
        if (activities.size() == 0) {
            speakButton.setEnabled(false);
            speakButton.setText("Voice recognizer not present");
            String utteranceId = UUID.randomUUID().toString();
            textToSpeech.speak(speakButton.getText(), TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            Toast.makeText(this, "Voice recognizer not present",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void loginSpeak(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public void speak(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        if (textMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            Toast.makeText(this, "Please select how many matches you want to see",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int noOfMatches = Integer.parseInt(textMatches.getSelectedItem().toString());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> textMatchList = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (!loginFinished) {
                    //TODO Ability to change password
                    //TODO Ability to restore forgotten password
                    if (firstTimeLogin) {
                        firstLoginButton = (Button) findViewById(R.id.firstTimePasswordButton);
                        //checkVoiceRecognition(firstLoginButton);
                        textToSpeech.speak("Password saved", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");

                        String userPassword = textMatchList.get(0);
                        userPassword = userPassword.replace(" ","").trim();
                        prefs.edit().putString(PASSWORD, userPassword).apply();
                        firstTimeLogin = false;
                        loginFinished = true;
                        setContentView(R.layout.activity_main);

                        isMainActivityActive = true;
                        invalidateOptionsMenu();

                        listOfMatches = (ListView) findViewById(R.id.lvTextMatches);
                        textMatches = (Spinner) findViewById(R.id.sNoOfMatches);
                        speakButton = (Button) findViewById(R.id.btSpeak);
                        checkVoiceRecognition(speakButton);
                        textMatches.setSelection(4);

                        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
                        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
                        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
                    } else {
                        loginButton = (Button) findViewById(R.id.loginButton);
                        //checkVoiceRecognition(loginButton);
                        String passwordSpoken = textMatchList.get(0);
                        passwordSpoken = passwordSpoken.replace(" ", "");
                        if (passwordSpoken.equals(prefs.getString(PASSWORD, "defaultValue"))) {

                            textToSpeech.speak("Login successful.", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");

                            loginFinished = true;
                            setContentView(R.layout.activity_main);

                            isMainActivityActive = true;
                            invalidateOptionsMenu();

                            listOfMatches = (ListView) findViewById(R.id.lvTextMatches);
                            textMatches = (Spinner) findViewById(R.id.sNoOfMatches);
                            speakButton = (Button) findViewById(R.id.btSpeak);
                            checkVoiceRecognition(speakButton);
                            textMatches.setSelection(4);

                            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
                            PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
                            PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
                        } else {
                            TextView wrongPassword = (TextView) findViewById(R.id.wrongPassword);
                            wrongPassword.setVisibility(View.VISIBLE);
                            textToSpeech.speak("The password you have entered is wrong.", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                            pause300();
                            textToSpeech.speak("Please try again.", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                        }
                    }
                } else {
                    if (!textMatchList.isEmpty()) {
                        if (textMatchList.get(0).contains("search")) {
                            String searchQuery = textMatchList.get(0);
                            searchQuery = searchQuery.replace("search", "");
                            Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                            search.putExtra(SearchManager.QUERY, searchQuery);
                            startActivity(search);
                        } else if (textMatchList.get(0).contains("navigate")) {
                            showToastMessage(NavigationActivity.getDestination(textMatchList));
                            textToSpeech.speak("navigate", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                        } else if (textMatchList.get(0).contains("settings")) {
                            Intent intent = new Intent(this, SettingsActivity.class);
                            startActivity(intent);
                        } else if (textMatchList.get(0).contains("help")) {
                            helpCommand();
                        } else {
                            listOfMatches
                                    .setAdapter(new ArrayAdapter<String>(this,
                                            android.R.layout.simple_list_item_1,
                                            textMatchList));

                            closestSuggestionsList = textMatchList;
                            textToSpeech.speak("What you said was not correctly understood", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                            pause300();
                            for (int i = 0; i < textMatchList.size(); i++) {
                                textToSpeech.speak("These are the closest matches found", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                                pause300();
                                textToSpeech.speak(Integer.toString(i+1), TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                                pause300();
                                textToSpeech.speak(closestSuggestionsList.get(i), TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
                            }
                        }
                    }
                }
            } else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
                showToastMessage("Audio Error");
            } else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
                showToastMessage("Client Error");
            } else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
                showToastMessage("Network Error");
            } else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
                showToastMessage("No Match");
            } else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
                showToastMessage("Server Error");
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void helpCommand() {
        textToSpeech.speak("The Electronic Guide Dog application has the following commands that can be used:", TextToSpeech.QUEUE_ADD, null, "UTTERANCE_ID");
        pause700();
        textToSpeech.speak("1", TextToSpeech.QUEUE_ADD,null, "UTTERANCE_ID");
        pause300();
        textToSpeech.speak("search", TextToSpeech.QUEUE_ADD,null, "UTTERANCE_ID");
        pause500();
        textToSpeech.speak("2" , TextToSpeech.QUEUE_ADD,null, "UTTERANCE_ID");
        pause300();
        textToSpeech.speak("navigate" , TextToSpeech.QUEUE_ADD,null, "UTTERANCE_ID");
        pause500();
        textToSpeech.speak("3" , TextToSpeech.QUEUE_ADD,null, "UTTERANCE_ID");
        pause300();
        textToSpeech.speak("settings" , TextToSpeech.QUEUE_ADD,null, "UTTERANCE_ID");
    }

    void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void pause700() {
        textToSpeech.playSilentUtterance(700,TextToSpeech.QUEUE_ADD, null);
    }

    public void pause500() {
        textToSpeech.playSilentUtterance(500,TextToSpeech.QUEUE_ADD, null);
    }

    public void pause300() {
        textToSpeech.playSilentUtterance(300,TextToSpeech.QUEUE_ADD, null);
    }
}