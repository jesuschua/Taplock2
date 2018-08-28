package smartfast.taplock2;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Array;
import java.security.Key;
import java.security.Signature;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class Taplock extends AppCompatActivity {

    //****declare app-wide variables
    ArrayList<Long> tapArr = new ArrayList<Long>();
    int tapCount = 0;
    long startTime, difference, nextTime;
    ArrayList<Long> RefSongArr = new ArrayList<Long>();
    LinkedList<ArrayList<Long>> KeyArr = new LinkedList<ArrayList<Long>>();
    ArrayList<Long> stdKeyMinSigma = new ArrayList<Long>();
    ArrayList<Long> stdKeyMaxSigma = new ArrayList<Long>();
    long stdKeySum = 0;
    Boolean useSigma = false;
    int valSigma = 10;

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

        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);

        CompoundButton.OnCheckedChangeListener switchListener;

        {
            switchListener = new CompoundButton.OnCheckedChangeListener() {
                PrintMe printer = new PrintMe();
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (setSwitch.isChecked()){
                        tapButton.setImageResource(R.drawable.lock_new);
                        tapButton.setColorFilter(Color.BLUE);
                        printer.PrintStr("Tap your song and press unlcck");
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
                    SaveSignature saveSig = new SaveSignature();
                    ValidateTap valTap = new ValidateTap();
                    if (setSwitch.isChecked()){
                        if (toggleButton.isChecked()){
                            RefSongArr = tapArr;

                            //validate reference keys before saving
                            if (KeyArr.size() == 0){
                                saveSig.Save(tapArr);
                                //printer.PrintStr( "Tap signature saved");
                                saveSig.UpdateSaveCounter();
                            }
                            else{
                                if (valTap.ValidateArr(tapArr,KeyArr.getLast())){
                                    saveSig.Save(tapArr);
                                    //printer.PrintStr("Tap signature saved");
                                    saveSig.UpdateSaveCounter();
                                }
                                else{
                                    tapButton.startAnimation(animShake);
                                    printer.PrintStr("Reference signature too different. Not saved.");
                                }
                            }

                            //printer.PrintStr("Save" + RefSongArr.toString());
                            toggleButton.setChecked(false);
                            }
                            //validate taps without recording
                            else{
                            ValidateTap validate = new ValidateTap();

                            //validate using Sigma. Check first if Sigma is enabled.
                            if (useSigma){
                                if (validate.ValidateArr(tapArr,RefSongArr)) {
                                    if (validate.validateSigma(tapArr)){
                                        tapButton.setImageResource(R.drawable.unlock_new);
                                        tapButton.setColorFilter(Color.GREEN);
                                        printer.PrintStr("OPEN");
                                    }
                                    else{
                                        tapButton.startAnimation(animShake);
                                        //printer.PrintStr("Tap signature does not match");
                                    }
                                }
                                else{
                                    tapButton.startAnimation(animShake);
                                    printer.PrintStr("Tap signature does not match");
                                }
                            }
                            else{
                                if (validate.ValidateArr(tapArr,RefSongArr)){
                                    tapButton.setImageResource(R.drawable.unlock_new);
                                    tapButton.setColorFilter(Color.GREEN);
                                    printer.PrintStr("OPEN");
                                }
                                else{
                                    tapButton.startAnimation(animShake);
                                    printer.PrintStr("Tap signature does not match");
                                }
                            }


                        }
                        setSwitch.setChecked(false);
                        }
                        else {
                        tapButton.setImageResource(R.drawable.lock_new);
                        tapButton.startAnimation(animShake);
                        printer.PrintStr("Locked");
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
            boolean validate1 = (arr1.size() == arr2.size());
            //getsum of arrayvalues
            if (!validate1){
                return false;
            }
            else{
                if (!ValidateArrSum(arr1,arr2)){
                    return false;
                }
                else return true;
            }
        }
        public boolean ValidateArrSum(ArrayList<Long> arr1, ArrayList<Long> arr2){
            long  tapSum1 = 0, tapSum2 = 0;
            double tapDSum1 = 0 , tapDSum2 = 0, sumFactor = 0.3, minSum = 0, maxSum = 0;;
            for (long i : arr1)
                tapSum1 += i;
                tapDSum1 = (double)tapSum1;
            for (long i : arr2)
                tapSum2 += i;
                tapDSum2 = (double)tapSum2;
            minSum = tapDSum1 - (tapDSum1 * sumFactor);
            maxSum = tapDSum1 + (tapDSum1 * sumFactor);
            if ((tapSum2 > minSum)&(tapSum2 < maxSum)){
                return true;
            }
            else{
                return false;
            }
        }
        public boolean validateSigma(ArrayList<Long> cKey){
            PrintMe printer = new PrintMe();
            Boolean result = false;
            long sumCkey = 0;
            for(int i = 0; i < cKey.size(); i++){
                sumCkey += cKey.get(i);
            }
            double keyFactor = (double)stdKeySum/sumCkey;
            for(int i = 0; i < cKey.size(); i++){
                cKey.set(i,round(cKey.get(i)*keyFactor));
            }
            for (int i = 0; i < cKey.size(); i++){
                if (cKey.get(i) < stdKeyMinSigma.get(i)|cKey.get(i) > stdKeyMaxSigma.get(i)){
                    result = false;
                    break;
                }
                result = true;
            }
            printer.PrintStr("Max:" + String.valueOf(stdKeyMaxSigma) + "\n" + "Input:" + String.valueOf(cKey) + "\n" +"Max:" + String.valueOf(stdKeyMinSigma));
            return result;
        }

        public void MakeStdKey (LinkedList<ArrayList<Long>> sourceKey){
            ArrayList<Long> stdKeyArr = new ArrayList<Long>();
            ArrayList<Long> stdDevArr = new ArrayList<Long>();
            LinkedList<ArrayList<Long>> stdKeyDevArr = new LinkedList<ArrayList<Long>>();
            long valSum = 0;
            long valAve = 0;
            double valStdDev = 0;
            long valVarSum = 0;
            int srcSize = sourceKey.getFirst().size();

            for (int i = 0; i < srcSize; i++){
                valSum = 0;
                for (int j = 0; j < (sourceKey.size()); j++){
                    valSum = valSum + sourceKey.get(j).get(i);
                }
                valAve = valSum/sourceKey.size();
                stdKeyArr.add(valAve);
                valVarSum = 0;
                for (int j = 0; j < (sourceKey.size()); j++){
                    valVarSum = valVarSum + abs((sourceKey.get(j).get(i) - valAve))^2;
                }
                valStdDev = round(sqrt(valVarSum));
                stdDevArr.add((long)valStdDev);
            }
            stdKeyDevArr.add(stdKeyArr);
            stdKeyDevArr.add(stdDevArr);
            for (int i = 0; i < stdKeyArr.size(); i++){
                stdKeySum += stdKeyArr.get(i);
                stdKeyMaxSigma.add(stdKeyArr.get(i) + (stdDevArr.get(i)) * valSigma);
                stdKeyMinSigma.add(stdKeyArr.get(i) - (stdDevArr.get(i)) * valSigma);
            }
            useSigma = true;
            PrintMe printer = new PrintMe();
            //printer.PrintStr(String.valueOf(stdKeyDevArr));
            printer.PrintStr("Sigma Checking: ON" + "\n" + "STD.KEYSUM:" + String.valueOf(stdKeySum)+ "\n" + "STD. DEVIATION:" + String.valueOf(stdDevArr) + "\n" + "Max:" +  String.valueOf(stdKeyMaxSigma) + "\n" + "Min:" + String.valueOf(stdKeyMinSigma));
        }
    }

    public class SaveSignature{
        PrintMe printer = new PrintMe();
        ValidateTap makeKey = new ValidateTap();
        public void Save(ArrayList<Long> sig){
            if (KeyArr.size() < 3){
                KeyArr.add(sig);
                printer.PrintStr(String.valueOf(KeyArr.size() + " " + sig));
                if (KeyArr.size() == 3){
                    makeKey.MakeStdKey(KeyArr);
                }
            }
            else{
                KeyArr.pollFirst();
                KeyArr.add(sig);
                printer.PrintStr(String.valueOf(KeyArr.size() + " " + sig));
                if (KeyArr.size() == 3){
                    makeKey.MakeStdKey(KeyArr);
                }
            }
        }
        public void UpdateSaveCounter(){
            ImageView iv1 = findViewById(R.id.imageView1);
            ImageView iv2 = findViewById(R.id.imageView2);
            ImageView iv3 = findViewById(R.id.imageView3);
            switch (KeyArr.size()){
                case 0:
                    iv1.setColorFilter(null);
                    iv2.setColorFilter(null);
                    iv3.setColorFilter(null);
                    break;
                case 1:
                    iv1.setColorFilter(Color.BLUE);
                    iv2.setColorFilter(null);
                    iv3.setColorFilter(null);
                    break;
                case 2:{
                    iv1.setColorFilter(Color.BLUE);
                    iv2.setColorFilter(Color.BLUE);
                    iv3.setColorFilter(null);
                    break;
                }
                case 3:{
                    iv1.setColorFilter(Color.BLUE);
                    iv2.setColorFilter(Color.BLUE);
                    iv3.setColorFilter(Color.BLUE);
                    break;
                }

            }
        }
    }

}

