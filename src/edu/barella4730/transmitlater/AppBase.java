//Jonathan Barella
//Chris Hamm
//COSC 4730 TransmitLater

//Updated on 12/02/2014 by Jonathan Barella
	//Refined GUI design elements
	//Implemented DBAdapter class
	//Created SQLLite statements
	//Started implementing DB actions on button clicks

//Updated on  11/17/2014 by Chris Hamm
	//Added scrollbar to the fragment, so if window goes off the screen, you can scroll up and down
	//Created a time picker for the Select Time Button
	//Time picker defaults to the current time
	//Create a date picker for the Select Date button
	//Date picker defaults to the current date

//Updated on 11/26/2014 by Chris Hamm
	//Added a Dark theme to the App
	//Changed the fonts and styling to better fit the dark theme
	//fixed bug where buttons where being cut off
	//Still having trouble saving data when the phone changes orientation

//Updated on 12/1/2014 by Chris Hamm
	//Gave functionality to the Contacts button, it opens your contacts, and you select what contact you want
	//After selecting the contact, it will add the contacts number to the contacts TextView


package edu.barella4730.transmitlater;

import java.util.Calendar;
import java.lang.Object;

import edu.barella4730.transmitlater.R;
import android.app.Activity;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.os.Build;
import android.provider.ContactsContract;
import android.database.sqlite.*;
import android.telephony.SmsManager;

public class AppBase extends FragmentActivity {

	//global variables
	static TextView selectedDateTextView;
	static TextView selectedTimeTextView;
	static EditText selectedContactTextView;
	static EditText messageInput;
	String selectedDateText;
	String selectedTimeText;
	String contactPhoneNumber;
	String smsMessage;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_base);
		if (savedInstanceState == null) {
			
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}//end of if
	}//end of onCreate for activity
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(data != null)
		{
			Uri uri = data.getData();
			if(uri != null)
			{
				Cursor c = null;
				try
				{
					c = getContentResolver().query(uri,  new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE},null,null,null);
					if(c != null && c.moveToFirst())
					{
						String number = c.getString(0);
						int type = c.getInt(1);
						selectedContactTextView.setText(number);//put the selected contacts number in the textview
						contactPhoneNumber = selectedContactTextView.getText().toString();
						//showSelectedNumber(type, number); //type tells you the type of number; mobile 2, home 1, work 3
					}
				}//end of try block
				finally
				{
					if(c != null)
					{
						c.close();
					}
				}//end of finally block
			}//end of if uri != null
		}//end of if
	}//end of onActivityResult
	
	public void showSelectedNumber(int type, String number)
	{
		Toast.makeText(this, type + ": " + number,Toast.LENGTH_LONG).show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_base, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		else if (id == R.id.action_viewMessages) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}
	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_app_base, container, false);
			//Place fragment items here
			selectedDateTextView= (TextView) rootView.findViewById(R.id.Selected_Date_TextView);
			selectedTimeTextView= (TextView) rootView.findViewById(R.id.Selected_Time_TextView);
			selectedContactTextView= (EditText) rootView.findViewById(R.id.Selected_Contact_EditText);
			messageInput= (EditText) rootView.findViewById(R.id.Message_Body_inputBox);		
			setRetainInstance(true);
			return rootView;
			
		}//end of onCreateView for fragment
		
	}//end of placeholder fragment class
	
	//Select Time Picker Button onClick method
	public void showTimePickerDialog(View v) //displays the time picker
	{
		DialogFragment newFragment= new TimePickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}//end of showTimePickerDialog
	
	//Select Date Picker Button onCLick method
	public void showDatePickerDialog(View v) //displays the date picker
	{
		DialogFragment newFragment= new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}//end of showDatePickerDialog
	
	//Select Contact Button onClick Method
	public void showListOfContacts(View v) //displays the list of contacts
	{
		Intent intent= new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		startActivityForResult(intent, 1);
	}//end of showListOfContacts

	//Send sms
	public void sendSMS(View v)
	{	       
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		
		smsMessage = messageInput.getText().toString();

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,	new Intent(DELIVERED), 0);

		//---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic failure",	Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No service", 	Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off",	Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		//---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS delivered",Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
					break;                        
				}
			}
		}, new IntentFilter(DELIVERED));        

		/*
		 * These two lines below actually send the message via an intent.
		 * The default provider does not show up and this is backward compatible to 2.3.3  
		 * 
		 */
	
		SmsManager sms = SmsManager.getDefault();
		
		//if(contactPhoneNumber.length() > 0 && smsMessage.length() > 0){
		sms.sendTextMessage(contactPhoneNumber, null, smsMessage, sentPI, deliveredPI);
		//}
	}
	
	//TimePicker class
	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			//Use the current time as the default values for the picker
			final Calendar cal= Calendar.getInstance();
			int hour= cal.get(Calendar.HOUR_OF_DAY);
			int minute= cal.get(Calendar.MINUTE);
			
			
			//create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
		}//end of onCreateDialog
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute)
		{
			//Do something with the time chosen by the user
			selectedTimeTextView.setText(Integer.toString(hourOfDay)
										+ ":" + Integer.toString(minute));				
			Log.i("TimePickerFragment","The time has been set to: " + hourOfDay +":"+ minute +" ");
			
		}//end of onTimeSet
	}//end of TimePickerFragment class
	
	//DatePicker class
	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			//Use the current date as the default date in the picker
			final Calendar c= Calendar.getInstance();
			int year= c.get(Calendar.YEAR);
			int month= c.get(Calendar.MONTH);
			int day= c.get(Calendar.DAY_OF_MONTH);
			
			//create a new  instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}//end of onCreateDialog
		
		public void onDateSet(DatePicker view, int year, int month, int day)
		{
			//Do something with the date chosen by the user
			selectedDateTextView.setText(Integer.toString(month) 
										+ "/" + Integer.toString(day)
										+ "/" + Integer.toString(year));
			
			Log.i("DatePickerFragment","The Date has been set to: " + month + " " + day + " " + year);
		}//end of onDateSet
	}//end of DatePickerFragment class
	
}//end of AppBase
