package smartfast.taplock2;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Taplock extends AppCompatActivity {

    //****declare app-wide variables
    ArrayList<Long> tapArr = new ArrayList<Long>();
    int tapCount = 0;
    long startTime, difference, nextTime;
    ArrayList<Long> RefSongArr1 = new ArrayList<Long>();
    ArrayList<Long> RefSongArr2 = new ArrayList<Long>();
    ArrayList<Long> RefSongArr3 = new ArrayList<Long>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taplock);

        //declare UI
        final Switch setSwitch = findViewById(R.id.switch1);
        final ImageButton tapButton = findViewById(R.id.imageButton);
        final ToggleButton toggleButton = findViewById(R.id.toggleButton);
        Button lockunlockButton = findViewById(R.id.button);


        //****declare and initialize listeners

        CompoundButton.OnCheckedChangeListener switchListener;

        {
            switchListener = new CompoundButton.OnCheckedChangeListener() {
                PrintMe printer = new PrintMe();
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (setSwitch.isChecked()){
                        tapButton.setImageResource(R.drawable.lock_new);
                        tapButton.setColorFilter(Color.RED);
                        printer.PrintStr("Getting tap signature");
                    }
                    else{
                        //tapButton.setImageResource(R.drawable.lock_new);
                        tapButton.setColorFilter(null);
                        tapArr = new ArrayList<Long>();
                        tapCount = 0;
                    }

                }
            };
        }

        View.OnClickListener lockunlockButtonListener;

        {
            lockunlockButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PrintMe printer = new PrintMe();
                    if (setSwitch.isChecked()){
                        if (toggleButton.isChecked()){
                            RefSongArr1 = tapArr;
                            printer.PrintStr("Save" + RefSongArr1.toString());
                            toggleButton.setChecked(false);
                            }
                            else{
                            ValidateTap validate = new ValidateTap();
                            if (validate.ValidateArr(tapArr,RefSongArr1)){
                                tapButton.setImageResource(R.drawable.unlock_new);
                                tapButton.setColorFilter(Color.GREEN);
                                printer.PrintStr("Open" + tapArr.toString());
                            }
                            else{
                                printer.PrintStr("Closed" + tapArr.toString());
                            }

                        }
                        setSwitch.setChecked(false);
                        }
                        else {
                        tapButton.setImageResource(R.drawable.lock_new);
                        printer.PrintStr("Closed");
                    }
                    }
            };
        }

        //tapbutton
        View.OnClickListener tapButtonListener;
        tapButtonListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                PrintMe printer = new PrintMe();
                if (setSwitch.isChecked()){
                    if (tapCount == 0){
                        startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                        //printer.PrintStr(String.valueOf("start"));
                    }
                    else{
                        nextTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                        difference = nextTime - startTime;
                        tapArr.add(difference);
                        //printer.PrintStr(tapArr.toString());
                        startTime = nextTime;
                    }
                    tapCount ++;
                }
            }
        };

        CompoundButton.OnCheckedChangeListener toggleListener;
        toggleListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (toggleButton.isChecked()){
                    if (!setSwitch.isChecked()){
                        setSwitch.setChecked(true);
                    }
                }

            }
        };

        //bind UI to listeners

        setSwitch.setOnCheckedChangeListener(switchListener);
        tapButton.setOnClickListener(tapButtonListener);
        lockunlockButton.setOnClickListener(lockunlockButtonListener);
        toggleButton.setOnCheckedChangeListener(toggleListener);

    }

    public class PrintMe{
        TextView textView = findViewById(R.id.textView2);
        public void PrintStr(String textmess){
            textView.setText(textmess);
        }
        public void PrintInt(Integer textmess){
            textView.setText(textmess);
        }
    }

    public class ValidateTap{
        public boolean ValidateArr(ArrayList<Long> arr1, ArrayList<Long> arr2){
            if (arr1.size() == arr2.size()){
                return true;
            }
            else return false;
        }
    }

}

