import java.text.NumberFormat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MortgageCalculatorActivity extends Activity
        implements OnEditorActionListener, OnSeekBarChangeListener,
        OnCheckedChangeListener, OnItemSelectedListener,OnKeyListener, OnClickListener{

    // define variables for the widgets
    private EditText principal;
    private TextView montlyRate;
    private SeekBar mRateBar;
    private Spinner mNUMSpinner;
    private TextView weeklyRate;
    private SeekBar wRateBar;
    private EditText wNum;
    private RadioGroup RBtnGrp;
    private RadioButton monthlyRBtn;
    private RadioButton weeklyRBtn;
    private Button calculateBtn;
    private TextView singlePayment;
    private TextView totalPayment;


    // define the SharedPreferences object
    private SharedPreferences savedValues;

    // define rounding constants
    private final int PAY_MONTH = 0;
    private final int PAY_WEEK = 1;

    // define instance variables
    private String principalString = "";
    private String numberOfWeeksString = "";
    private float monthlyRatePercent = .15f;
    private float weeklyRatePercent = .15f;
    private int rounding = PAY_MONTH;
    private int numberOfMonths = 1;
    private float progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mortgage_calculator);

        // get references to the widgets
        principal = (EditText) findViewById(R.id.principal);
        montlyRate = (TextView) findViewById(R.id.montlyRate);
        mRateBar = (SeekBar) findViewById(R.id.mRateBar);
        mNUMSpinner = (Spinner) findViewById(R.id.mNUMSpinner);
        weeklyRate = (TextView) findViewById(R.id.weeklyRate);
        wRateBar = (SeekBar) findViewById(R.id.wRateBar);
        wNum = (EditText) findViewById(R.id.wNum);
        RBtnGrp = (RadioGroup)
                findViewById(R.id.RBtnGrp);
        monthlyRBtn = (RadioButton)
                findViewById(R.id.monthlyRBtn);
        weeklyRBtn = (RadioButton)
                findViewById(R.id.weeklyRBtn);
        calculateBtn = (Button) findViewById(R.id.CalculateBtn);
        singlePayment = (TextView) findViewById(R.id.singlePayment);
        totalPayment = (TextView) findViewById(R.id.totalPayment);

        // set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.month_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mNUMSpinner.setAdapter(adapter);

        // set the listeners
        principal.setOnEditorActionListener(this);
        principal.setOnKeyListener(this);
        wNum.setOnEditorActionListener(this);
        wNum.setOnKeyListener(this);
        mRateBar.setOnSeekBarChangeListener(this);
        mRateBar.setOnKeyListener(this);
        wRateBar.setOnSeekBarChangeListener(this);
        wRateBar.setOnKeyListener(this);
        RBtnGrp.setOnCheckedChangeListener(this);
        RBtnGrp.setOnKeyListener(this);
        mNUMSpinner.setOnItemSelectedListener(this);
        calculateBtn.setOnClickListener(this);

        // get SharedPreferences object
        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
    }

    @Override
    public void onPause() {
        // save the instance variables
        Editor editor = savedValues.edit();
        editor.putString("principalString", principalString);
        editor.putString("numberOfWeeksString", numberOfWeeksString);
        editor.putFloat("monthlyRatePercent", monthlyRatePercent);
        editor.putFloat("weeklyRatePercent", weeklyRatePercent);
        editor.putInt("rounding", rounding);
        editor.putInt("numberOfMonths", numberOfMonths);
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // get the instance variables
        principalString = savedValues.getString("principalString", "");
        numberOfWeeksString = savedValues.getString("numberOfWeeksString", "");
        monthlyRatePercent = savedValues.getFloat("monthlyRatePercent", 0.15f);
        weeklyRatePercent = savedValues.getFloat("weeklyRatePercent", 0.15f);
        rounding = savedValues.getInt("rounding", PAY_MONTH);
        numberOfMonths = savedValues.getInt("numberOfMonths", 1);

        // set the bill amount on its widget
        principal.setText(principalString);
        wNum.setText(numberOfWeeksString);

        // set the tip percent on its widget



        // set rounding on radio buttons
        // NOTE: this executes the onCheckedChanged method,
        // which executes the calculateAndDisplay method
        if (rounding == PAY_MONTH) {
            monthlyRBtn.setChecked(true);
            progress = Math.round(monthlyRatePercent*100 );
            mRateBar.setProgress((int)progress);


        } else if (rounding == PAY_WEEK) {
            progress = Math.round(weeklyRatePercent*100 );
            wRateBar.setProgress((int)progress);
            weeklyRBtn.setChecked(true);

        }

        // set number of months on spinner
        // NOTE: this executes the onItemSelected method,
        // which executes the calculateAndDisplay method
        int position = numberOfMonths - 1;
        mNUMSpinner.setSelection(position);
    }

   public void calculateAndDisplay() {
        float paymentTimes=.0f;
        double eachPayment=0;

        //get principal
       principalString = principal.getText().toString();
       float pricipalNumber;
       if (principalString.equals("")) {
           pricipalNumber = 0;
       }
       else {
           pricipalNumber = Float.parseFloat(principalString);
       }

             if (rounding == PAY_MONTH) {
            progress =mRateBar.getProgress();
                 paymentTimes = (float)numberOfMonths;
        }
        else if (rounding == PAY_WEEK) {
            progress = wRateBar.getProgress();
                 paymentTimes = (float)Integer.parseInt(numberOfWeeksString);
        }


        // calculate single payment
        eachPayment = (progress*pricipalNumber*Math.pow((1+progress),paymentTimes)/(Math.pow((1+progress),paymentTimes)-1));


        // display the results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        singlePayment.setText(currency.format(eachPayment));
        totalPayment.setText(currency.format(eachPayment*paymentTimes));

    }

    //*****************************************************
    // Event handler for the EditText
    //*****************************************************
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            //  calculateAndDisplay();
        }
        return false;
    }

    //*****************************************************
    // Event handler for the SeekBar
    //*****************************************************
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
/*
        if (rounding == PAY_MONTH){
            montlyRate.setText(progress);
        }

        if (rounding == PAY_WEEK){
            weeklyRate.setText(progress);
        } */

    }
    public void onProgressChanged(SeekBar seekBar, int progress) {
        /*
        if (rounding == PAY_MONTH){
            montlyRate.setText(progress);
        }

        if (rounding == PAY_WEEK){
            weeklyRate.setText(progress);
        }
        */
    }



    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //calculateAndDisplay();
    }


    //*****************************************************
    // Event handler for the RadioGroup
    //*****************************************************
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.monthlyRBtn:
                rounding = PAY_MONTH;
                break;
            case R.id.weeklyRBtn:
                rounding = PAY_WEEK;
                break;
        }
        //calculateAndDisplay();
    }

    //*****************************************************
    // Event handler for the Spinner
    //*****************************************************
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position,
                               long id) {
        numberOfMonths = position + 1;
        // calculateAndDisplay();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing

    }

    //*****************************************************
    // Event handler for the calculate button
    //*****************************************************

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.CalculateBtn:
                calculateAndDisplay();
                break;

        }
    }

    //*****************************************************
    // Event handler for the keyboard and DPad
    //*****************************************************
    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        /*
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:

                // calculateAndDisplay();

                // hide the soft keyboard
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        principal.getWindowToken(), 0);
                // hide the soft keyboard
                InputMethodManager im = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        wNum.getWindowToken(), 0);
                // consume the event
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (view.getId() == R.id.mRateBar) {
                    //calculateAndDisplay();
                } else if (view.getId() == R.id.wRateBar) {

                }
                break;
        } */
        // don't consume the event
        return false;
    }

}
