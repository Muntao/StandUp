package com.example.muntao.standup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String LISTA = "Lista";
    private List<String> personList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListAdapter();
        addOnLongTouchListener();
        restoreLastPersonList();
        updatePersonCountLabel();
    }

    private void restoreLastPersonList() {
        sharedPreferences = getSharedPreferences("StandUp", Context.MODE_PRIVATE);
        Set<String> personSet = sharedPreferences.getStringSet("Lista", new HashSet<String>());
        personList.addAll(personSet);
    }

    public void addPerson(View view) {
        String name = getTextFromInput();
        if (name.isEmpty()) {
            showToast("Wprowadź imię!");
        } else {
            addPersonToList(name);
        }
    }

    private String getTextFromInput() {
        EditText input = (EditText) findViewById(R.id.input);
        String name = input.getText().toString();
        if (name.length() > 0) {
            input.setText("");
            return name;
        } else {
            return "";
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setListAdapter() {
        ListView list = (ListView) findViewById(R.id.listView);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, personList);
        list.setAdapter(listAdapter);
    }

    private void addPersonToList(String name) {
        if (personList.contains(name)) {
            showToast("Taka osoba już istnieje!");
        } else if(name.trim().isEmpty() || name.length() > 100) {
            showToast("A ti ti!");
        } else {
            listAdapter.add(name);
            savePersonListToSharedPreferences();
            updatePersonCountLabel();
        }

    }

    private void savePersonListToSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(LISTA, new HashSet<>(personList));
        editor.commit();
    }

    private void addOnLongTouchListener() {
        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                showRemoveElementDialog(parent, position);
                return true;
            }
        });
    }

    private void showRemoveElementDialog(AdapterView<?> parent, final int position) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Usunać?");
        adb.setMessage("Jesteś pewny, że chcesz usunąć " + parent.getItemAtPosition(position));
        adb.setNegativeButton("Anuluj", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                removeElementFromListView(position);
                savePersonListToSharedPreferences();
                updatePersonCountLabel();
            }
        });
        adb.show();
    }

    private void removeElementFromListView(int position) {
        listAdapter.remove(listAdapter.getItem(position));
        listAdapter.notifyDataSetChanged();
    }


    public void randomPairs(View view) {
        if(personList.size() < 3){
            showToast("Liczba osób musi być większa od 2!");
        } else if (isEven()) {
            List<String> clone = clonePersonList();
            shuffle(clone);
            Map<String, String> map = makeMapFromList(clone);
            String keyValue = prepareKeyValueStringFromMap(map);
            showPairMap(keyValue);
        } else {
            showToast("Liczba osób musi być parzysta!");
        }
    }

    private boolean isEven() {
        return personList.size() % 2 == 0;
    }

    private List<String> clonePersonList() {
        List<String> clone = new ArrayList<>(personList.size());
        for (String item : personList) clone.add(item);
        return clone;
    }


    private void shuffle(List<String> list) {
        long seed = System.nanoTime();
        Collections.shuffle(list, new Random(seed));
    }

    private void showPairMap(String message){
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Lista par");
        adb.setMessage(message);
        adb.setNegativeButton("Ok", null);
        adb.show();
    }

    private Map<String, String> makeMapFromList(List<String> list){
        int size = list.size();
        int mapSize = size / 2;

        Map<String, String> map = new HashMap<>(mapSize);

        for(int i = 0; i < mapSize; i++){
            map.put(list.get(i), list.get(i+mapSize));
        }
        return map;
    }

    private String prepareKeyValueStringFromMap(Map<String, String> map){
        String mapToPrint = "";
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            String value = map.get(key);
            mapToPrint += key + " \t\t-\t\t " + value + "\n";
        }
        return mapToPrint;
    }

    private int personListSize(){
        return personList.size();
    }

    private void updatePersonCountLabel(){
        TextView count = (TextView) findViewById(R.id.summary);
        count.setText(String.valueOf(personListSize()));
    }
}
