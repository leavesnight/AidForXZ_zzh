package com.example.aidforxzv10;

import android.app.Activity;
import android.app.AlarmManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.format.Time;
import android.util.Log;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Instrumentation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
	private Button start,close;
	private TextView text;
	private EditText edit_text;
	private Spinner spinnerA,spinnerC;
	private static final String[] mSpAITEMS={"不执行活动本",
			"活动本(买2体力、1金币)","活动本全开"};
	private ArrayList<String> allSpAItems,allSpCItems;
	private ArrayAdapter<String> aASpAItems,aASpCItems;
	private final int cbNUM=4,taskNUM=12;
	private CheckBox[] XZ_cb=new CheckBox[cbNUM];
	private CheckBox[] Task_cb=new CheckBox[taskNUM];
	
	private boolean[][] mXZTask=new boolean[cbNUM][taskNUM-1];
	private int[] mXZSpinnerTask=new int[cbNUM];//for spinnerA.ID
	private int mXZEssenceId;
	private boolean mOrderFlag;
	
	private PowerManager.WakeLock mWakeLock;
	private OutputStream os;
	private PowerManager.WakeLock wakeLock;
	private KeyguardManager.KeyguardLock kl;
	private Handler handlerUI;
	private AlarmManager mAm;
	private BroadcastReceiver mBReceiver;
	private final String mAction="com.example.aidforxzv10.alarm";
	private PendingIntent mPIntent,mPIntent2,mPIntent3;
	
	private Time mTime;
	private boolean mFirstFlag=true,mOKFlag=false;
	private int cbCIndexNow=0,cbCNum=0,mTimeFlag,mLeftTimes=0,arrEIIndex;
	private int[] cbChecked=new int[cbNUM];
	private final int arrEssIdNUM=6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		text=(TextView)findViewById(R.id.text);
		start=(Button)findViewById(R.id.start);
		close=(Button)findViewById(R.id.close);
		edit_text=(EditText)findViewById(R.id.edit_text);
		spinnerA=(Spinner)findViewById(R.id.spinnerActivity);
		allSpAItems=new ArrayList<String>();
		for (int i=0;i<mSpAITEMS.length;i++)
			allSpAItems.add(mSpAITEMS[i]);
		aASpAItems=new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,allSpAItems);
		aASpAItems.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spinnerA.setAdapter(aASpAItems);
		spinnerA.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (cbCNum>0)
					mXZSpinnerTask[cbChecked[spinnerC.getSelectedItemPosition()]]
							=position;
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});
		spinnerC=(Spinner)findViewById(R.id.spinnerClient);
		allSpCItems=new ArrayList<String>();
		aASpCItems=new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,allSpCItems);
		aASpCItems.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spinnerC.setAdapter(aASpCItems);
		spinnerC.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				for (int i=0;i<taskNUM-1;i++){
					Task_cb[i].setChecked(mXZTask[cbChecked[position]][i]);
				}
				spinnerA.setSelection(mXZSpinnerTask[cbChecked[position]]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub	
				//Log.i("test","Nothing");
			}
		});
		
		XZ_cb[0]=(CheckBox)findViewById(R.id.KF_cb);
		XZ_cb[1]=(CheckBox)findViewById(R.id.GP_cb);
		XZ_cb[2]=(CheckBox)findViewById(R.id.HW_cb);
		XZ_cb[3]=(CheckBox)findViewById(R.id.BD_cb);
		for (int j=0;j<cbNUM;j++){
			final int stIndex=j;
			XZ_cb[j].setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if (isChecked){
						switch (stIndex){
						case 0:
							allSpCItems.add("快返");
							break;
						case 1:
							allSpCItems.add("果盘");
							break;
						case 2:
							allSpCItems.add("华为");
							break;
						case 3:
							allSpCItems.add("百度");
							break;
						}
						aASpCItems.notifyDataSetChanged();
						cbChecked[cbCNum++]=stIndex;
						//Log.i("test","add:"+Long.toString(spinnerC.getSelectedItemId()));
						//Log.i("test","add:"+Long.toString(spinnerC.getSelectedItemPosition()));
					}else{
						switch (stIndex){
						case 0:
							allSpCItems.remove("快返");
							break;
						case 1:
							allSpCItems.remove("果盘");
							break;
						case 2:
							allSpCItems.remove("华为");
							break;
						case 3:
							allSpCItems.remove("百度");
							break;
						}
						aASpCItems.notifyDataSetChanged();
						int i=0;
						for (i=0;i<cbCNum;i++){
							if (cbChecked[i]==stIndex){
								break;
							}
						}
						int intTmp=-1;
						if (spinnerC.getSelectedItemPosition()==i&&cbCNum>i+1){
							intTmp=i;
						}
						if (i==cbCNum-1)
							cbCNum--;
						else{
							for (int j=i;j<cbCNum-1;j++){
								cbChecked[j]=cbChecked[j+1];
							}
							cbCNum--;
						}
						if (intTmp>-1){
							spinnerC.getOnItemSelectedListener().onItemSelected(spinnerC, spinnerC, intTmp, intTmp);
						}
						//Log.i("test","delete:"+Long.toString(spinnerC.getSelectedItemId()));
						//Log.i("test","delete:"+Long.toString(spinnerC.getSelectedItemPosition()));
					}
				}
			});
		}
		Task_cb[0]=(CheckBox)findViewById(R.id.MonthCard_cb);
		Task_cb[1]=(CheckBox)findViewById(R.id.Stamina_cb);
		Task_cb[2]=(CheckBox)findViewById(R.id.SignIn_cb);
		Task_cb[3]=(CheckBox)findViewById(R.id.Excavator_cb);
		Task_cb[4]=(CheckBox)findViewById(R.id.FlyingChess_cb);
		Task_cb[5]=(CheckBox)findViewById(R.id.GuildBattle_cb);
		Task_cb[6]=(CheckBox)findViewById(R.id.simpleActivity_cb);
		Task_cb[7]=(CheckBox)findViewById(R.id.Challenge_cb);
		Task_cb[8]=(CheckBox)findViewById(R.id.FriendlyCall_cb);
		Task_cb[9]=(CheckBox)findViewById(R.id.BrushMaterials_cb);
		Task_cb[10]=(CheckBox)findViewById(R.id.Sell_cb);
		Task_cb[11]=(CheckBox)findViewById(R.id.Jump_cb);
		for (int j=0;j<taskNUM-1;j++){
			final int stIndex=j;
			Task_cb[j].setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if (cbCNum>0)
						mXZTask[cbChecked[spinnerC.getSelectedItemPosition()]]
								[stIndex]=isChecked;
				}
			});
		}
		readAll();
		
		PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);
		wakeLock=pm.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP
				|PowerManager.SCREEN_DIM_WAKE_LOCK,"TAG");
				//PowerManager.PARTIAL_WAKE_LOCK,"TAG");
		KeyguardManager km=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
		kl=km.newKeyguardLock("unLock");
		
		handlerUI=new Handler();
		mAm=(AlarmManager)getSystemService(ALARM_SERVICE);
		mBReceiver=new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				int client=intent.getIntExtra("client",-1);
				wakeLock.acquire();
				kl.disableKeyguard();
				final String msg=intent.getStringExtra("msg");
				Log.d("test",msg);
				Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
				autoRunXZ(client,mTime);
			}
		};
		IntentFilter ifilter=new IntentFilter();
		ifilter.addAction(mAction);
		registerReceiver(mBReceiver,ifilter);//dynamically register
		
		try{
			if (os==null){
				os=Runtime.getRuntime().exec("su").getOutputStream();
				//wait for user's confirmation
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		start.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){//just control the cbChecked[0] run time
				handlerUI.removeCallbacksAndMessages(null);//avoid changed check box
				cbCIndexNow=0;
				//allSpAItems.removeAll(allSpAItems);//aASpAItems.clear();//the same
				//aASpAItems.notifyDataSetChanged();	
				/*handlerUI.postDelayed(new Runnable(){
					@Override
					public void run() {
						int x=540,y=1230;
						int xTmp,yTmp=800+220*3;
						xTmp=200+220*((x-1)%4);//x=tm.monthDay
						if (x<=12){
							yTmp=800+220*((x-1)/4);
						}
						int colorTmp=getPixel(xTmp+60,yTmp+40);
						//Color.red(colorTmp)<141&&Color.green(colorTmp)<117
						//148 105 81;27 27 27;254 206 33;193 147 116
						x=getPixel(x,y);//(xTmp+60,yTmp+40);//254,206,33;28,28,28
						Log.i("test","color red:"+Integer.toString(Color.red(x)));
						Log.i("test","color green:"+Integer.toString(Color.green(x)));
						Log.i("test","color blue:"+Integer.toString(Color.blue(x)));
					}
				},5000);*/
				Time tm=new Time("GMT+8");
				tm.setToNow();
				if (tm.hour>=24-8){
					plusOneDay(tm);
				}
				String str=edit_text.getText().toString();
				String regex="(\\d+):(\\d+)";
				Pattern p=Pattern.compile(regex);
				Matcher m=p.matcher(str);
				if (m.lookingAt()){
					Log.d("test",m.group(2));
					Log.d("test",m.group(1));
					int intTmp=tm.weekDay;
					tm.set(0,Integer.parseInt(m.group(2)),Integer.parseInt(m.group(1)),
							tm.monthDay,tm.month,tm.year);
					//tm.normalize(true);//here is very important!else weekday=0!
					tm.weekDay=intTmp;
				}else{
					tm.hour=(tm.hour+8)%24;
				}
				//automatically open the order for arrest
				if (str.equals("开通缉")){
					mOrderFlag=true;
					edit_text.setText("自动开");
				}else if (str.equals("关通缉")){
					mOrderFlag=false;
					edit_text.setText("不自动开");
				}
				if (mOrderFlag&&(tm.weekDay==Time.WEDNESDAY||tm.weekDay==Time.SATURDAY)&&
						(tm.hour<21||tm.hour<22&&Task_cb[11].isChecked())){//allow 30min error
					//if over 21 please do it manually unless Jump_cb is checked 
					int tmLeft=21*60+30-tm.hour*60-tm.minute;
					if (tmLeft>0)
						autoRunXZAlarm(tmLeft*60*1000,-4);//kuaifan sisi
					else
						autoRunXZAlarm(10000,-4);
				}
				regex="小号(\\d+):(\\d+)";
				p=Pattern.compile(regex);
				m=p.matcher(str);
				if (m.lookingAt()){
					arrEIIndex=0;//if mXZEssenceId is 3 now, then 4 5 0 1 2; 0 means uc
					mLeftTimes=Integer.parseInt(m.group(1));
					int tmLeft=Integer.parseInt(m.group(2)),intTmp=-2;
					if (mXZEssenceId==0)//uc use client -3
						intTmp=-3;
					if (tmLeft>0)
						autoRunXZAlarm(tmLeft*60*1000,intTmp);//for the aid essence of the planes activity!
					else
						autoRunXZAlarm(11000,intTmp);
					Toast.makeText(MainActivity.this,
							"Ready to execute the essence plan!"+tmLeft+" min left...",
							Toast.LENGTH_SHORT).show();
				}
				
				int tmLeft=0;
				if (tm.hour<8){
					mFirstFlag=true;
					mTimeFlag=0;
					tmLeft=8*60-tm.hour*60-tm.minute;
				}else if (tm.hour<12){
					mTimeFlag=1;
					tmLeft=12*60-tm.hour*60-tm.minute;
				}else{
					if (tm.weekDay==Time.SUNDAY){//sunday use a safe mode for flying chess
						if (tm.hour<14){
							if (mOKFlag||Task_cb[11].isChecked()){//if this time is over
								mTimeFlag=-3;
								tmLeft=20*60-tm.hour*60-tm.minute;
							}else
								mTimeFlag=1;
						}else if (tm.hour<20){//20:00
							mTimeFlag=-3;
							tmLeft=20*60-tm.hour*60-tm.minute;
						}else if (tm.hour<21){//21:00
							mTimeFlag=3;
							tmLeft=21*60-tm.hour*60-tm.minute;
						}else if (tm.hour<23){//23:00
							if (mOKFlag||Task_cb[11].isChecked()){//if this time is over
								mTimeFlag=-3;
								tmLeft=23*60-tm.hour*60-tm.minute;
							}else
								mTimeFlag=3;
						}else{
							mFirstFlag=true;
							mTimeFlag=0;
							plusOneDay(tm);
							tmLeft=24*60+8*60-tm.hour*60-tm.minute;
						}
					}else if (tm.hour<14){
						if (mOKFlag||Task_cb[11].isChecked()){//if this time is over
							mTimeFlag=3;
							tmLeft=21*60-tm.hour*60-tm.minute;
						}else
							mTimeFlag=1;
					}else if (tm.hour<21){
						mTimeFlag=3;
						tmLeft=21*60-tm.hour*60-tm.minute;
					}else if (tm.hour<23){
						if (mOKFlag||Task_cb[11].isChecked()){//if this time is over
							mFirstFlag=true;
							mTimeFlag=0;
							plusOneDay(tm);
							tmLeft=24*60+8*60-tm.hour*60-tm.minute;
						}else
							mTimeFlag=3;
					}else{
						mFirstFlag=true;
						mTimeFlag=0;
						plusOneDay(tm);
						tmLeft=24*60+8*60-tm.hour*60-tm.minute;
					}
				}
				mOKFlag=false;
				Log.d("test","weekday:"+tm.weekDay);
				mTime=tm;
				
				if (cbCNum==0){
					text.setText("你还未勾选客户端！无法启动！");
				}else{
					saveAll("保存成功");
					//System.out.println("just for test");
					//autoRunXZAlarm(10000,cbChecked[0]);//10s just for test
					if (tmLeft>0)
						autoRunXZAlarm(1000*60*tmLeft,cbChecked[0]);
					else
						autoRunXZAlarm(10000,cbChecked[0]);//give users 10s for preparation*/
					text.setText("Program's ready to go... "+tmLeft+" min left...");
				}
			}
		});
		close.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				mAm.cancel(mPIntent);
				mAm.cancel(mPIntent2);
				mAm.cancel(mPIntent3);
				handlerUI.removeCallbacksAndMessages(null);
				finish();
				//onDestroy();//or we can use finish() and add some clear commands
				//android.os.Process.killProcess(android.os.Process.myPid());//haven't called onDestroy()
			}
		});
	}
	@Override
	public void onDestroy(){
		mAm.cancel(mPIntent);
		mAm.cancel(mPIntent2);
		mAm.cancel(mPIntent3);
		handlerUI.removeCallbacksAndMessages(null);
		unregisterReceiver(mBReceiver);
		try{
			os.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		//os=null;//not finish()
		
		super.onDestroy();
	}
	private void plusOneDay(Time tm){
		tm.weekDay=(tm.weekDay+1)%7;//weekDay use the China data
		switch (tm.month){//monthday+1
		case 1://February only 28 days
			if (tm.year%4==0 && tm.year%100!=0 || tm.year%400==0){//if leap year
				tm.monthDay=tm.monthDay%29+1;
			}else
				tm.monthDay=tm.monthDay%28+1;
			break;
		case 3:case 5:case 8:case 10://only 30 days
			tm.monthDay=tm.monthDay%30+1;
			break;
		default://31 days
			tm.monthDay=tm.monthDay%31+1;
		}
	}
	private void saveAll(String str){
		try {
			FileOutputStream outputStream=new FileOutputStream(
					getExternalFilesDir("").getPath()+"/record.txt");
			for (int j=0;j<cbNUM;j++){
				for (int i=0;i<taskNUM-1;i++){
					outputStream.write((mXZTask[j][i]==
							true?"1":"0").getBytes());
				}
				outputStream.write(" ".getBytes());
				outputStream.write(Integer.toString(
						mXZSpinnerTask[j]).getBytes());
				outputStream.write("\n".getBytes());
				outputStream.flush();
			}
			outputStream.write(Integer.toString(mXZEssenceId).getBytes());
			outputStream.write(" ".getBytes());
			outputStream.write(Boolean.toString(mOrderFlag).getBytes());
			outputStream.write("\n".getBytes());
			outputStream.flush();
			outputStream.close();
			Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void readAll(){
		try{
			FileInputStream inputStream=new FileInputStream(
					getExternalFilesDir("").getPath()+"/record.txt");
			byte[] bytes=new byte[1024];
			ByteArrayOutputStream arrayOutputStream=new ByteArrayOutputStream();
			while (inputStream.read(bytes)!=-1){
				arrayOutputStream.write(bytes,0,bytes.length);
			}
			inputStream.close();
			arrayOutputStream.close();
			String content=new String(arrayOutputStream.toByteArray());
			int tmpStart1=0,tmpStart2=0;
			for (int j=0;j<cbNUM;j++){
				for (int i=0;i<taskNUM-1;i++){
					mXZTask[j][i]=content.substring(
							tmpStart1+i,tmpStart1+i+1).equals("1")?true:false;
				}
				tmpStart1=content.indexOf('\n',tmpStart1)+1;
				tmpStart2=content.indexOf(' ',tmpStart2)+1;
				mXZSpinnerTask[j]=Integer.parseInt(
						content.substring(tmpStart2,tmpStart1-1));
			}
			tmpStart2=content.indexOf(' ',tmpStart2)+1;
			mXZEssenceId=Integer.parseInt(
					content.substring(tmpStart1,tmpStart2-1));
			tmpStart1=content.indexOf('\n',tmpStart1)+1;
			mOrderFlag=Boolean.parseBoolean(
					content.substring(tmpStart2,tmpStart1-1));
		}catch (FileNotFoundException e){
			e.printStackTrace();
			for (int j=0;j<cbNUM;j++){
				for (int i=0;i<taskNUM-1;i++){
					mXZTask[j][i]=Task_cb[i].isChecked();
				}
				mXZSpinnerTask[j]=spinnerA.getSelectedItemPosition();
			}
			mXZEssenceId=1;
			mOrderFlag=false;
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	//public static class MyReceiver extends BroadcastReceiver{
		//static register can only use static or public outer class(new java files)
	
	private void autoRunXZAlarm(int tmMSLeft,final int client){
		Intent intent=new Intent(mAction);
		intent.putExtra("msg","该自动玩血族了T^T客户端"+client);
		intent.putExtra("client",client);
		PendingIntent tmpPIntent;
		/*if (client<-1){
			tmpPIntent=mPIntent2;
		}else{
			tmpPIntent=mPIntent;
		}
		mAm.cancel(tmpPIntent);//the same as use flag CANCEL_CURRENT*/
		//we don't need to cancel the PendingIntent for the same requestCode &? the same intent
		if (client<-3){//-4
			tmpPIntent=mPIntent3=PendingIntent.getBroadcast(this,-1,intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
		}else if (client<-1){
			tmpPIntent=mPIntent2=PendingIntent.getBroadcast(this,-1,intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
		}else{
			tmpPIntent=mPIntent=PendingIntent.getBroadcast(this,0,intent,
					PendingIntent.FLAG_CANCEL_CURRENT);//if flag==0, when only extra is changed
			//it doesn't create a new mPIntent or update the old one
		}
		Log.d("test",new StringBuilder(client+" "+tmMSLeft).toString());
		mAm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime()+tmMSLeft,tmpPIntent);
	}
	private void autoRunXZ(final int client,final Time tm){
		text.setText("Program's running!");
		handlerUI.postDelayed(new Runnable(){
			public void run(){
				if (arrEIIndex==0&&mLeftTimes-arrEIIndex>arrEssIdNUM){//set if there's a next start
					if (mXZEssenceId==0)
						autoRunXZAlarm(1000*60*61,-3);
					else
						autoRunXZAlarm(1000*60*61,-2);
					Log.d("test","start XZ:"+client+"; 61 min later again!");
				}
				Intent intent=new Intent();
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				switch (client){
				case 0:case -4:case -5:
					intent.setClassName("com.sdg.woool.xuezu.shuyou",
							"com.sdg.woool.xuezu.XueZu");
					break;
				case 1:case -2:
					intent.setClassName("com.sdg.woool.xuezu.guopan",
							"com.sdg.woool.xuezu.XueZu");
					break;
				case 2:
					intent.setClassName("com.sdg.woool.xuezu.huawei",
							"com.sdg.woool.xuezu.XueZu");
					break;
				case 3:
					intent.setClassName("com.sdg.woool.xuezu.duoku",
							"com.sdg.woool.xuezu.XueZu");
					break;
				case -3:
					intent.setClassName("com.sdg.woool.xuezu.uc",
							"com.sdg.woool.xuezu.XueZu");
					break;
				default:
				}
				/*intent.setClassName("com.sdg.woool.xuezu",
				"com.sdg.woool.xuezu.XueZu");*/
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				
				//login
				login(client);
				if (client<-1){
					getEssence(client);
				}else{
					//start daily tasks
					switch (mTimeFlag){
					case 0://8:00
						if (mXZTask[client][2])
							signIn(tm);
						if (mXZTask[client][8]){
							friendlyCall();
						}
						if (mXZTask[client][0])//must be here
							getTaskReward(1);//get month card
						if (mXZSpinnerTask[client]>0){//it's better here!here must use mXZSpinnerTask[] not spinnerA.getSelectedPosition!fuck!
							clearActivity(client,tm);
						}
						if (mXZTask[client][9]){
							brushMaterial(client);
						}
						if (mXZTask[client][10]){
							autoSell();
						}
						if (mXZTask[client][4]){//must be here
							if (tm.weekDay!=Time.MONDAY)
								flyingChess(6);
							else
								flyingChess(4);
							getTaskReward(1);//avoid for flyingChess reward
						}
						getTaskReward(1);//avoid for 10 times activity reward
						getTaskReward(1);//avoid for 3 times level up reward!!!
						break;
					case 1://12:00
						if (mXZTask[client][1])
							getTaskReward(1);//get stamina
						if (mXZTask[client][3])
							digger(mTimeFlag);
						if (mXZTask[client][7]){//it's better here
							clearChallenge();
							if (mXZTask[client][10]){//need to sell here
								autoSell();
							}
						}
						//no need to brush material
						if (mXZTask[client][4]){
							flyingChess(2);
							getTaskReward(1);//avoid for flyingChess reward
						}
						getTaskReward(1);//avoid for 10 times activity reward
						getTaskReward(1);//avoid for 3 times level up reward!!!
						break;
					case -3:
						if (tm.hour<20){//20:00,only Sunday will enter this part
							if (mXZTask[client][4]){
								flyingChess(4);
								getTaskReward(1);//avoid for flyingChess reward
							}
						}else{//>=21,23:00,only Sunday will enter this part
							if (mXZTask[client][4])
								flyingChess(1);//fly the last step of the chess
						}
						break;
					case 3://21:00
						if (mXZTask[client][1]){
							for (int i=0;i<2;i++)//get stamina*2
								getTaskReward(1);
						}
						if (mXZTask[client][3])
							digger(mTimeFlag);
						if (mXZTask[client][9]){
							brushMaterial(client);
						}
						if (mXZTask[client][10]){
							autoSell();
						}
						if (mXZTask[client][4]&&tm.weekDay!=Time.SUNDAY){
							flyingChess(4);
							getTaskReward(1);//avoid for flyingChess reward
						}
						if (mXZTask[client][5]){
							if (tm.weekDay==Time.MONDAY||tm.weekDay==Time.FRIDAY)
								guildBattle(0);//sign up on Monday and Friday
						}
						getTaskReward(1);//avoid for 10 times activity reward
						getTaskReward(1);//avoid for 3 times level up reward!!!
						break;
					default:
					}
				}
				//Kill XZ
				switch (client){
				case 0:case -4:case -5:
					forceKill("com.sdg.woool.xuezu.shuyou");
					break;
				case 1:case -2:
					forceKill("com.sdg.woool.xuezu.guopan");
					break;
				case 2:
					forceKill("com.sdg.woool.xuezu.huawei");
					break;
				case 3:
					forceKill("com.sdg.woool.xuezu.duoku");
					break;
				case -3:
					forceKill("com.sdg.woool.xuezu.uc");
					break;
				default:
				}
				if (client<-1){
					if (client==-5){
						if (edit_text.getText().toString()!=mTimeFlag+" Over")
							edit_text.setText("Order opened");
						
						kl.reenableKeyguard();
						wakeLock.release();
					}else if (client==-4){
						autoRunXZ(-5,tm);//change the account
					}else if (mLeftTimes<=0){
						if (edit_text.getText().toString()!="Plan Over")
							edit_text.setText("Plan Over");
						edit_text.invalidate();
						//save Id
						saveAll("暂存");
						
						kl.reenableKeyguard();
						wakeLock.release();
					}else{
						mLeftTimes--;
						mXZEssenceId=(mXZEssenceId+1)%arrEssIdNUM;//if mXZEssenceId is 3 now, then 4 5 0 1 2; 0 means uc
						if (mLeftTimes<=0)
							autoRunXZ(-2,tm);//change the account
						else{
							arrEIIndex++;
							if (arrEIIndex==arrEssIdNUM){
								arrEIIndex=0;
								if (edit_text.getText().toString()!="60 min left")
									edit_text.setText("60 min left");
								edit_text.invalidate();
								
								kl.reenableKeyguard();
								wakeLock.release();
							}else{
								if (mXZEssenceId==0)
									autoRunXZ(-3,tm);
								else
									autoRunXZ(-2,tm);
							}
						}
					}
					return ;
				}
				if (cbCIndexNow==0)
					mOKFlag=true;
				
				/*Intent intentThis=new Intent();
				intentThis.setAction(Intent.ACTION_MAIN);
				intentThis.addCategory(Intent.CATEGORY_LAUNCHER);
				intentThis.setClass(MainActivity.this, MainActivity.class);
				intentThis.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentThis);*/
				//Continue to the next client or postdelayed
				if (cbCIndexNow==cbCNum-1){
					if (edit_text.getText().toString()!=mTimeFlag+" Over")
						edit_text.setText(mTimeFlag+" Over");
					start.performClick();
					text.invalidate();
					
					kl.reenableKeyguard();
					wakeLock.release();
				}else
					autoRunXZ(cbChecked[++cbCIndexNow],tm);
			}
		},5000);
	}
	private void login(final int client){
		try{
			Thread.sleep(15000);
			//sendKeyCode(KeyEvent.KEYCODE_VOLUME_UP);
			simulateTap(550,1600);//press "start game"
			Thread.sleep(15000);//load the server
			if (client==-2){
				simulateSwipe(400,760,400,760,1000);
				String str1="",str2="";
				if (mLeftTimes<=0){//release notice!!!
					str1="name";
					str2="key";
				}else switch (mXZEssenceId){
				case 1:case 2:case 3:case 4:case 5:
					str1="lwxh00"+mXZEssenceId;
					str2=str1;
					break;
				default:
				}
				for (int i=0;i<str1.length();i++){
					simulateText(str1.substring(i,i+1));
					//notice that if it's not divided into pieces
					//maybe get sth like lwx0h03
				}
				simulateSwipe(400,910,400,910,1000);
				for (int i=0;i<str2.length();i++){
					simulateText(str2.substring(i,i+1));
				}
				simulateKey(KeyEvent.KEYCODE_BACK);
				Thread.sleep(3000);
			}else if (client==-4||client==-5){
				simulateSwipe(400,830,400,830,1000);
				String str1="",str2="";
				if (client==-4){//release notice!!!
					str1="name";
					str2="key";
				}else{
					str1="name";
					str2="key";
				}
				for (int i=0;i<str1.length();i++){
					simulateText(str1.substring(i,i+1));
					//notice that if it's not divided into pieces
					//maybe get sth like lwx0h03
				}
				simulateSwipe(400,570,400,570,1000);
				for (int i=0;i<str2.length();i++){
					simulateText(str2.substring(i,i+1));
				}
				simulateKey(KeyEvent.KEYCODE_BACK);
				Thread.sleep(3000);
			}
			if (client==3){//BD doesn't need manual login
				Log.d("test","nowpush!");
			}else if (client==2){//HW doesn't need manual login, but may have problems
				Thread.sleep(5000);
				simulateTap(540,1600);
			}else if (client!=-3){
				simulateTap(550,1150);//press "login";GP needs no check for auto login
				Thread.sleep(3000);
				simulateTap(550,1150);//press it again for robustness
			}
			Thread.sleep(40000);
			if (true){//mFirstFlag){//for robustness
				simulateTap(540,1580);//everyday entrance
				Thread.sleep(5000);//if it misses, just enter the territory
				if (client<-1){
					simulateTap(540,1580);//missed award
					Thread.sleep(5000);
				}
				if (cbCIndexNow==cbCNum-1)
					mFirstFlag=false;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private void getEssence(final int client){
		if (client==-4){//arrest
			try{
				simulateTap(450,1650);//territory
				Thread.sleep(5000);
				simulateTap(550,750);//guild
				Thread.sleep(5000);
				simulateSwipe(750,950,350,950,200);
				simulateTap(900,1150);//order for arrest
				Thread.sleep(5000);
				simulateSwipe(750,950,350,950,200);
				simulateTap(540,1480);//start
				Thread.sleep(3000);
				simulateTap(680,1090);//confirm
				Thread.sleep(3000);
			}catch (Exception e){
				e.printStackTrace();
			}
		}else{
			if (mLeftTimes<=0||client==-5)//do nothing
				return ;
			try {
				simulateTap(95,1650);//main page
				Thread.sleep(5000);
				simulateTap(930,1070);//time limit activity
				Thread.sleep(3000);
				simulateTap(540,560);//planes entrance
				Thread.sleep(8000);
				simulateTap(540,1430);//start battle
				Thread.sleep(5000);
				simulateTap(540,690);//the 1st friend
				Thread.sleep(3000);
				simulateTap(540,1490);//start
				Thread.sleep(10000);
				simulateTap(1000,200);//auto battle
				Thread.sleep(60000);//wait for end
				simulateTap(540,960);//record
				Thread.sleep(3000);
				simulateTap(540,960);//back
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void getTaskReward(int times){
		try{
			simulateTap(630,1650);//task
			Thread.sleep(3000);
			simulateTap(520,600);//everyday task
			Thread.sleep(3000);
			for (int i=0;i<times;i++){//get task reward*times
				simulateTap(900,850);
				Thread.sleep(3000);
			}
			
			simulateKey(KeyEvent.KEYCODE_BACK);//allow for one empty get
			Thread.sleep(5000);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	private void signIn(final Time tm){
		try{
			simulateTap(95,1650);//main page
			Thread.sleep(5000);
			simulateTap(200,875);//sign in
			Thread.sleep(3000);
			simulateKey(KeyEvent.KEYCODE_BACK);//back to main page for robustness
			Thread.sleep(5000);
			simulateTap(200,875);//sign in again
			Thread.sleep(3000);
			int xTmp,yTmp=800+220*3;
			xTmp=200+220*((tm.monthDay-1)%4);
			if (tm.monthDay<=12){
				yTmp=800+220*((tm.monthDay-1)/4);
			}
			int colorTmp=getPixel(xTmp+60,yTmp+40);
			//148 105 81;27 27 27;254 206 33(right one);193 147 116
			if (Color.red(colorTmp)>254-10&&Color.green(colorTmp)>206-10){
			}else{//if not yellow, sign in
				simulateTap(xTmp,yTmp);
				Thread.sleep(5000);
			}
			simulateTap(900,500);//continuous sign in or no effect
			Thread.sleep(3000);
			simulateTap(620,1650);//receive or press like (200+220*1,800+220*3)
			Thread.sleep(3000);
			simulateKey(KeyEvent.KEYCODE_BACK);//back under any situation
			Thread.sleep(3000);
			
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	//times>0 means turn left or swipe right
	//times<0 means turn right or swipe left
	private void swipeLR(int times,int mS){
		for (int i=0;i<Math.abs(times);i++){
			if (times<0)//swipe left, turn right
				simulateSwipe(750,950,350,950,mS);
			else//swipe right, turn left
				simulateSwipe(350,950,750,950,mS);
			try{
				Thread.sleep(3000);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	private void digger(final int mTimeFlag){
		try{
			simulateTap(450,1650);//territory
			Thread.sleep(5000);
			simulateTap(800,1350);//excavator
			Thread.sleep(5000);//+2s
			//simple realization
			simulateTap(540,1600);//receive
			Thread.sleep(7000);//+2s
			simulateTap(540,1090);//confirm
			Thread.sleep(5000);//+2s
			simulateTap(550,950);//start;step1
			Thread.sleep(5000);
			simulateTap(540,1650);//next step;step2, 2 steps allow for chosen digger 
			Thread.sleep(3000);
			simulateTap(680,1090);//confirm
			Thread.sleep(3000);
			swipeLR(1,500);//select the blue digger
			Thread.sleep(3000);
			simulateTap(540,1700);//hire it
			Thread.sleep(5000);
			
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			
			if (mTimeFlag==3){//total 2 times when mTimeFlag==3
				getTaskReward(1);//get digger*2 reward
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	//+up-down
	private void swipeUD(int times,int mS){
		for (int i=0;i<Math.abs(times);i++){
			if (times<0)//swipe up, turn down
				simulateSwipe(540,1400,540,700,mS);//swipe up
			else//swipe down, turn up
				simulateSwipe(540,700,540,1400,mS);
			try{
				Thread.sleep(3000);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	private void flyingChess(int times){
		try{
			simulateTap(450,1650);//territory
			Thread.sleep(5000);
			simulateTap(550,750);//guild
			Thread.sleep(5000);
			simulateTap(200,1150);//flying chess
			Thread.sleep(5000);
			for (int i=0;i<times;i++){
				swipeUD(-1,200);//fly once
				Thread.sleep(10000);
				simulateTap(550,1150);//confirm the dialog or no action
				Thread.sleep(3000);
			}
			
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	private void guildBattle(int action){
		try{
			simulateTap(450,1650);//territory
			Thread.sleep(5000);
			simulateTap(550,750);//guild
			Thread.sleep(5000);
			simulateTap(540,1150);//guild battle
			Thread.sleep(5000);
			if (action==0){//sign up
				simulateTap(540,1600);
				Thread.sleep(5000);
			}
			
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	private void brushTYM(int times,int y,int mode){
		try{
			if (times>0){//fast with BACK(mode==1) or without BACK(mode>1)
				simulateTap(940,y);//brush
				Thread.sleep(10000);
				for (int i=0;i<(times-3)/10+1;i++){
					simulateTap(850,1650);//brush*10
					Thread.sleep(10000);
				}
				if ((times-2)%10==0){
					simulateTap(550,1650);//brush again
					Thread.sleep(10000);
				}
				if (mode==4){
					simulateTap(410,1090);//cancel insufficient stamina for challenge
					Thread.sleep(3000);
				}
				if (mode==2){//allow for insufficient stamina /& brushed situation
					simulateKey(KeyEvent.KEYCODE_BACK);
					Thread.sleep(3000);
				}else{
					simulateTap(240,1650);//close
					Thread.sleep(3000);
				}
				if (mode==1){
					simulateTap(80,460);//allow for empty career 7 & brushed career
					Thread.sleep(3000);
				}else if (mode==3){//allow for brushed career 7
					simulateTap(540,1090);
					Thread.sleep(3000);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private int getPixel(int x,int y){
		int pixel=0;
		try{
			exec("screencap "+getExternalFilesDir("").getPath()+"/AFXZTmp.png\n");
			//Environment.getExternalStorageDirectory().getPath()=="/sdcard"
			//getFilesDir().getPath()=="/data/data/com.example.aidforxzv10/files"
			Bitmap bitmap=null;
			File f=new File(getExternalFilesDir("").getPath()+"/AFXZTmp.png");
			while (f.exists()==false){
				//wait for it exists
			}
			while (bitmap==null){//wait for png is fully created
				FileInputStream fis=new FileInputStream(
						getExternalFilesDir("").getPath()+"/AFXZTmp.png");
				bitmap=BitmapFactory.decodeStream(fis);
				fis.close();
			}
			exec("rm "+getExternalFilesDir("").getPath()+"/AFXZTmp.png\n");
			pixel=bitmap.getPixel(x,y);
		}catch (Exception e){
			Log.d("test",e.toString());
			e.printStackTrace();
		}
		return pixel;
	}
	//num means firstly turn the team number N to N+num
	//ensure it can be brushed
	private int brushEventYYN(int y1,int y2,int num,int times){
		int bTeamChangedNum=num;
		try{
			simulateTap(550,y1);//choose y1 event
			Thread.sleep(5000);
			if (Color.red(getPixel(540,660))>40){//only if it wasn't brushed)
				if (num!=0){
					simulateTap(550,720);//the top instance
					Thread.sleep(5000);
					simulateTap(550,690);//the 1st friend
					Thread.sleep(3000);
					swipeLR(-num,200);//swipe right*num,choose team N+num1
					simulateKey(KeyEvent.KEYCODE_BACK);
					Thread.sleep(5000);
					simulateKey(KeyEvent.KEYCODE_BACK);
					Thread.sleep(3000);
				}//team N+num is chosen or failed when brushed
				brushTYM(times,y2,2);//brush the y2 level*times
			}else
				bTeamChangedNum=0;
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
		}catch (Exception e){
			e.printStackTrace();
		}
		return bTeamChangedNum;
	}
	private int brushEventYY(int y1,int y2,int num){
		int bTeamChangedNum=num;
		try{
			simulateTap(540,y1);//choose y1 event
			Thread.sleep(5000);
			if (Color.red(getPixel(540,660))>40){//only if it wasn't brushed)
				//brushTYM(times,y2,2);//brush the y2 level*times
				simulateTap(540,y2);//the top instance
				Thread.sleep(5000);
				simulateTap(540,690);//the 1st friend
				Thread.sleep(3000);
				if (num!=0){
					swipeLR(-num,200);//swipe right*num,choose team N+num1
				}//team N+num is chosen or failed when brushed
				simulateTap(540,1490);//start
				Thread.sleep(10000);
				simulateTap(1000,200);//auto battle
				Thread.sleep(120000);//wait for end
				//?
			}else
				bTeamChangedNum=0;
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
		}catch (Exception e){
			e.printStackTrace();
		}
		return bTeamChangedNum;
	}
	private int clearActivity(final int client,final Time tm){
		try{
			//buy money*1 && buy stamina*2
			simulateTap(95,1650);//main page
			Thread.sleep(5000);
			simulateTap(1040,250);//+money
			Thread.sleep(3000);
			simulateTap(800,480);//+buy money
			Thread.sleep(3000);
			simulateTap(540,1370);//buy
			Thread.sleep(3000);
			simulateTap(680,1090);//confirm
			Thread.sleep(3000);
			getTaskReward(1);//get money task
			simulateTap(270,353);//+stamina
			Thread.sleep(3000);
			int timesStamina=1;
			if (tm.weekDay==Time.SUNDAY||mXZSpinnerTask[client]==2)//here also fuck!
				timesStamina=2;
			for (int i=0;i<timesStamina;i++){//buy*2 or 1
				simulateTap(540,1370);
				Thread.sleep(3000);
				simulateTap(680,1090);//confirm
				Thread.sleep(3000);
				simulateKey(KeyEvent.KEYCODE_BACK);//confirm the successful info
				Thread.sleep(3000);
			}
			//suppose team 1 is for brushing material with experience leader
			//team 2 is for brushing material with money leader
			//team 3 is for challenge in the activity
			//team 4 is for money in the activity
			simulateTap(275,1650);//character
			Thread.sleep(3000);
			simulateTap(800,680);//team edit
			Thread.sleep(5000);
			swipeLR(9,200);//At most 10 teams
			//Now team 1 must be chosen
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			simulateTap(95,1650);//main page
			Thread.sleep(5000);
			
			swipeUD(-1,200);
			simulateTap(540,1250);//activity
			Thread.sleep(5000);
			//brush career*2
			simulateTap(550,520);//career
			Thread.sleep(5000);
			simulateTap(560,1630);//confirm
			Thread.sleep(3000);
			brushTYM(2,910,1);//brush 7 with mode 1 to avoid the problem of empty 7 & brushed career
			brushTYM(2,1150,3);//brush 6 with fast mode 3 to avoid brushed 7
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			//get career task!!!
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			getTaskReward(1);//get career task reward
			simulateTap(95,1650);//main page
			Thread.sleep(5000);
			swipeUD(-1,200);
			simulateTap(540,1250);//activity
			Thread.sleep(5000);
			
			int tmpNum=0,tmpDay=tm.weekDay;
			if (mXZSpinnerTask[client]==2){//fuck*3!
				tmpDay=Time.SUNDAY;
			}
			switch (tmpDay){
			case Time.FRIDAY:case Time.WEDNESDAY:case Time.MONDAY:
				//brush 2nd event*2
				if (!mXZTask[client][6])
					tmpNum=brushEventYYN(850,670,3,2);//use team 1+3 to brush money top*2
				//brush 3rd event*2
				if (tmpDay!=Time.MONDAY)//don't brush tomato
					tmpNum+=brushEventYYN(1170,670,-tmpNum,2);//use team 4-3 to brush material/element/tomato top*2
				//brush 4th event
				if (!mXZTask[client][6]){
					//empty realization...
					//tmpNum+=brushEventYY(1500,670,-tmpNum+3);//use team 3 to challenge the card pieces activity
					//if festival it goes to the 5th, maybe still 4th
				}
				break;
			case Time.SATURDAY:case Time.THURSDAY:case Time.TUESDAY:
				brushEventYYN(850,670,0,2);//use team 1 to brush experience top*2
				if (tmpDay!=Time.THURSDAY)//don't brush tomato
					brushEventYYN(1170,670,0,2);//use team 1 to brush element/tomato/material top*2
				if (!mXZTask[client][6]){
					//empty realization...
					//brushEventYY(1500,670,3);
					//if festival it goes to the 5th, maybe still 4th
				}
				break;
			case Time.SUNDAY:
				if (!mXZTask[client][6]){
					tmpNum=brushEventYYN(1170,670,3,2);//use team 1+3 to brush money top*2
				}
				tmpNum+=brushEventYYN(850,670,-tmpNum,2);//use team 1 to brush experience top*2
				tmpNum+=brushEventYYN(1500,670,-tmpNum,2);//use team 1 to brush element top*2
				//brush 5th event
				tmpNum+=brushEventYYN(1715,670,-tmpNum,2);//use team 1 to brush material top*2
				//swipeUD(-1,200);
				//swipeUD(-1,1500);
				//don't brush tomato
				//tmpNum+=brushEventYYN(1300,670,-tmpNum,2);//use team 1 to brush tomato top*2
				if (!mXZTask[client][6]){
					//empty realization...
					//brushEventYY(1500,670,3);
					//if festival it goes to an another place
				}
				break;
			default:
			}
			
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			getTaskReward(1);//get event*1 task
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	private int clearChallenge(){
		try{
			//suppose team 1 is for brushing material with experience leader
			simulateTap(275,1650);//character
			Thread.sleep(3000);
			simulateTap(800,680);//team edit
			Thread.sleep(5000);
			swipeLR(9,200);//At most 10 teams
			//Now team 1 must be chosen
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			simulateTap(95,1650);//main page
			Thread.sleep(5000);
			//brush challenge
			swipeUD(-2,200);//may bug!
			int colorTmp=getPixel(540,1230);//rwb:86 16 24;245 245 245;224 204 162
			if (Color.red(colorTmp)>224-10&&Color.red(colorTmp)<224+10
					&&Color.green(colorTmp)>204-10&&Color.green(colorTmp)<204+10
					&&Color.blue(colorTmp)>162-10&&Color.blue(colorTmp)<162+10){
			}else{
				return -1;
			}
			simulateTap(540,1250);//challenge
			Thread.sleep(3000);
			simulateSwipe(540,1400,540,1000,1000);
			//accurate control to avoid the unchallenged problem
			//use team 1 to brush SQZH/YXZP*5
			int y1=1610,y2=915,times=5;
			simulateTap(550,y1);//choose y1 event
			Thread.sleep(5000);
			//no matter whether it's brushed or not, only back once!
			if (Color.red(getPixel(540,660))>40){//only if it wasn't brushed)
				brushTYM(times,y2,4);//brush the y2 level*times & back once
			}else{//avoid for brushed situation
				simulateKey(KeyEvent.KEYCODE_BACK);//key_back once
				Thread.sleep(5000);
			}
			simulateKey(KeyEvent.KEYCODE_BACK);//here is the 2nd key_back!
			Thread.sleep(5000);
			
			getTaskReward(1);//get challenge task reward
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return 5;
	}
	private void friendlyCall(){
		try{
			simulateTap(800,1650);//call
			Thread.sleep(5000);
			swipeLR(1,200);
			int intTmp=getPixel(380,1640);//41,82,115;94,63,162;132,69,42
			if (Color.blue(intTmp)>80){//or there's a bug
				if (Color.blue(intTmp)<140){//115
				}else//162
					swipeLR(1,200);
				simulateTap(540,1450);//friendly call
				Thread.sleep(3000);
				simulateTap(540,1350);//call
				Thread.sleep(5000);
				simulateTap(900,260);//jump
				Thread.sleep(3000);
				simulateKey(KeyEvent.KEYCODE_BACK);
				Thread.sleep(3000);
			}
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(3000);
			
			getTaskReward(1);//get call reward
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private int brushMaterial(final int client){
		try{
			//team 2 is for brushing material with money leader
			if (mTimeFlag==0&&mXZSpinnerTask[client]>0){//fuck*6!
				//team 1 is already ensured
				simulateTap(275,1650);//character
				Thread.sleep(3000);
				simulateTap(800,680);//team edit
				Thread.sleep(5000);
				swipeLR(-1,200);
			}else{
				simulateTap(275,1650);//character
				Thread.sleep(3000);
				simulateTap(800,680);//team edit
				Thread.sleep(5000);
				swipeLR(9,200);//At most 10 teams
				swipeLR(-1,200);
			}
			//Now team 2 must be chosen
			simulateKey(KeyEvent.KEYCODE_BACK);
			Thread.sleep(5000);
			simulateTap(95,1650);//main page
			Thread.sleep(5000);
			//brush material
			simulateTap(915,875);//menu
			Thread.sleep(3000);
			simulateTap(800,900);//properties handbook
			Thread.sleep(3000);
			swipeUD(-1,1000);
			Thread.sleep(2000);
			simulateTap(130,1420);//moxue
			Thread.sleep(3000);
			simulateTap(880,1660);//go
			Thread.sleep(3000);
			brushTYM(21,670,2);//brush material called moxue with fast mode 2
			//to avoid the insufficient stamina problem 
			//& fortunately the unchallenged problem		
		}catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	private void autoSell(){
		try{
			simulateTap(275,1650);//character
			Thread.sleep(3000);
			simulateTap(230,1400);//sell,old (300,1350) may cause summon bug!
			Thread.sleep(3000);
			simulateTap(180,1500);//select by one key
			Thread.sleep(3000);
			simulateTap(540,1500);//sell
			Thread.sleep(3000);
			simulateTap(540,1500);//confirm
			Thread.sleep(5000);
			
			simulateKey(KeyEvent.KEYCODE_BACK);//allow for nothing sold
			Thread.sleep(3000);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		return super.onOptionsItemSelected(item);
	}
	
	/*private void sendKeyCode(final int keyCode){
		new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					Instrumentation inst=new Instrumentation();
					inst.sendKeyDownUpSync(keyCode);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}).start();
	}*///it seems to have problems on system signature...  
	//execute shell command
	public final void exec(String cmd){
		try{
			if (os==null){
				os=Runtime.getRuntime().exec("su").getOutputStream();
			}
			os.write(cmd.getBytes());
			os.flush();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	//simulate a global key event on background
	public final void simulateKey(int keyCode){
		exec("input keyevent "+keyCode+"\n");
	}
	public final void simulateTap(int x,int y){
		exec("input tap "+x+" "+y+"\n");
	}
	public final void simulateSwipe(int x1,int y1,int x2,int y2,int mS){
		exec("input swipe "+x1+" "+y1+" "+x2+" "+y2+" "+mS+"\n");
	}
	public final void simulateText(String str){
		exec("input text "+str+"\n");
	}
	public final void forceKill(String packName){
		exec("am force-stop "+packName+"\n");
	}
}
