/**
 * Class for the main Traffic Collector Activity.
 * @author Fratila Catalin Ionut
 */

package ro.pub.acs.traffic.collector;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import ro.pub.acs.traffic.utils.CryptTool;
import ro.pub.acs.traffic.utils.Database;
import ro.pub.acs.traffic.utils.HashString;
import ro.pub.acs.traffic.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TrafficCollectorMain extends Activity {
	private Button start, pause, upload, share, stopShare, regenerate;
	private TextView statusText;
	private WebView wv;
	private CheckedTextView realTime;
	
	private String status;
	
	private Toast toast;
	private long lastBackPressTime = 0;
	
	private Intent collector;
	
	private LocationManager locationManager;
	
	private Thread uploadThread;
	
	Handler toastHandler = new Handler();
    
    private Database db_statistics;
    private Database db_user;
    
    private Activity thisActivity;
    private TrafficCollectorMain traffic;
    
    private PowerManager powerManager;
    private PowerManager.WakeLock uploadWakeLock;
    
    private String name;
    private String facebook;
    private String twitter;
    
    private boolean real = false;
    
    private boolean pressedStart = false;
    
    public static final String DATE_FORMAT_NOW = "yyyy_MM_dd_HH_mm_ss";
    
    private static String uniqueID = null;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	private String id_user = null;
	
	private static final String stopShareURL = "http://cipsm.hpc.pub.ro/traffic/stop.php";
	private static final String regenerateURL = "http://cipsm.hpc.pub.ro/traffic/remove.php";
    
    Runnable toastRunnableStart = new Runnable() {
    	public void run() {
    		statusText.setText("Upload Status: Running");
    		Toast.makeText(getApplicationContext(), "Upload started", Toast.LENGTH_LONG)
    			.show();
    	}
    };
    
    Runnable toastRunnableError = new Runnable() {
    	public void run() {
    		statusText.setText("Upload Status: Sending Error! Please try again!");
    		Toast.makeText(getApplicationContext(), "Upload Error", Toast.LENGTH_LONG)
    			.show();
    	}
    };
    
    Runnable toastRunnableNoData = new Runnable() {
    	public void run() {
    		statusText.setText("Upload Status: No data to Upload");
    		Toast.makeText(getApplicationContext(), "No Data to Upload", Toast.LENGTH_LONG)
    			.show();
        }
    };
    
    Runnable toastRunnableFinish = new Runnable() {
    	public void run() {
    		Toast.makeText(getApplicationContext(), "Upload finished", Toast.LENGTH_LONG)
				.show();
			statusText.setText("Upload Status: Finished");
			statusText.setVisibility(View.GONE);
    	}
    };
    
    public synchronized String getId() {
    	return id_user;
    }
    
    public synchronized String generateId() {
    	String id = UUID.randomUUID().toString();
    	Utils.sendPost(traffic, id, regenerateURL, 1, "");
    	db_statistics.update(new String[] {"uuid"}, new String[] {id});
    	return id;
    }
    
    public synchronized static String id(Context context) {
	    if (uniqueID == null) {
	        SharedPreferences sharedPrefs = context.getSharedPreferences(
	                PREF_UNIQUE_ID, Context.MODE_PRIVATE);
	        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
	        if (uniqueID == null) {
	            uniqueID = UUID.randomUUID().toString();
	            Editor editor = sharedPrefs.edit();
	            editor.putString(PREF_UNIQUE_ID, uniqueID);
	            editor.commit();
	        }
	    }
	    return uniqueID;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		thisActivity = this;
		traffic = this;
		
		powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		uploadWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UploadWL");
		
		setContentView(R.layout.main);
		db_user = new Database(this, "collector", "user", new String[] { "name", "username", "password", "uuid" });
		if(db_user.isEmpty() || db_user.get("username").equals("")){
			db_user.insert(new String[] {"", "", "", TrafficCollectorMain.id(thisActivity)});
			final Dialog alertDialog = new Dialog(this);
			
			alertDialog.setContentView(R.layout.account);
			alertDialog.setTitle("Create Account");
			// Set an EditText view to get user input 
			final EditText username = (EditText) alertDialog.findViewById(R.id.username);
			final EditText password = (EditText) alertDialog.findViewById(R.id.password);
			final EditText password_check = (EditText) alertDialog.findViewById(R.id.password_check);
			final Button save = (Button) alertDialog.findViewById(R.id.account_save);
			final Button cancel = (Button) alertDialog.findViewById(R.id.account_cancel);
			
			username.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					username.setText("");
				}
			});
			
			save.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					HashString hs = new HashString("SHA1");
					String _username = username.getText().toString();
					String _password = password.getText().toString();
					String _password_check = password_check.getText().toString();
					String errorText = null;
					
					if(_username == null || _username.equals(""))
						errorText = "Please provide username!";
					else if(!Utils.isValidEmailAddress(_username))
						errorText = "Username needs to be your email!";
					else if(_password == null || _password.equals(""))
						errorText = "Please provide password!";
					else if(_password_check == null || _password_check.equals(""))
						errorText = "Please provide password check!";
					else if(!_password_check.equals(_password))
						errorText = "Passwords do not match!";
					
					if(errorText != null)
						Utils.showDialog(traffic, errorText);
					else {
						Utils.showDialog(traffic, "User account saved!");
						db_user.update(new String[] {"username", "password"}, new String[] {_username, hs.hash(_password)});
						alertDialog.cancel();
					}
				}
			});
			
			cancel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					alertDialog.cancel();
				}
			});
			
			alertDialog.show();
		}
		
		start = (Button) this.findViewById(R.id.bstart);
		pause = (Button) this.findViewById(R.id.bpause);
		upload = (Button) this.findViewById(R.id.bupload);
		share = (Button) this.findViewById(R.id.bshare);
		stopShare = (Button) this.findViewById(R.id.bstopshare);
		regenerate = (Button) this.findViewById(R.id.bregenerate);
		
		
		
		db_statistics = new Database(this, "collector", "statistics", new String[] { "speed", "max_speed", "avg_speed", "length", "no", "uuid" });
        if(db_statistics.isEmpty())
			db_statistics.insert(new String[] { "-1", "-1", "-1", "-1", "-1", "-1"});
        if(db_statistics.get("uuid").equals("-1")) {
        	id_user = this.generateId();
        }
        else
        	id_user = db_statistics.get("uuid");
		
		pause.setBackgroundResource(R.drawable.btn_default_normal_disable);
		
		start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pause.setClickable(true);
				start.setBackgroundResource(R.drawable.btn_default_normal_disable);
				pause.setBackgroundResource(R.drawable.btn_default_normal_red);
				start.setClickable(false);
				realTime.setVisibility(View.GONE);
				upload.setVisibility(View.GONE);
				statusText.setVisibility(View.GONE);
				regenerate.setVisibility(View.GONE);
				HandleStart();
			}
		});
		
		pause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				HandlePause();
				start.setBackgroundResource(R.drawable.btn_default_normal_green);
				pause.setBackgroundResource(R.drawable.btn_default_normal_disable);
				pause.setClickable(false);
				realTime.setVisibility(View.VISIBLE);
				start.setClickable(true);
				if(!realTime.isChecked()) {
					upload.setVisibility(View.VISIBLE);
					statusText.setVisibility(View.VISIBLE);
				}
				else {
					regenerate.setVisibility(View.VISIBLE);
				}
			}
		});
		pause.setClickable(false);
		upload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				statusText.setVisibility(View.VISIBLE);
				HandleUpload();
			}
		});
		
		status = "none";
		statusText = (TextView)findViewById(R.id.status);
        statusText.setText("Upload Status: " + status);
        statusText.setVisibility(View.GONE);
        
        realTime = (CheckedTextView) findViewById(R.id.real_time);
        realTime.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CheckedTextView temp = (CheckedTextView) v;
				if (!temp.isChecked()){
					if(!Utils.checkInternetConnection(thisActivity)){ 
						AlertDialog alertDialog = new AlertDialog.Builder(thisActivity).create();
						alertDialog.setMessage("Please enable internet connection to use upload function");
						alertDialog.setButton("Wireless And Network Settings", new DialogInterface.OnClickListener() {
						      public void onClick(DialogInterface dialog, int which) {
						    	  startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
						      }
						});
						alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
						      public void onClick(DialogInterface dialog, int which) {
						    	  dialog.cancel();
						      }
						});
						alertDialog.show();
					}
					temp.setChecked(true);
					real = true;
		            statusText.setVisibility(View.GONE);
		            upload.setVisibility(View.GONE);
		            share.setVisibility(View.VISIBLE);
		            stopShare.setVisibility(View.VISIBLE);
		            regenerate.setVisibility(View.VISIBLE);
		        }
		        else {
		        	real = false;
		            temp.setChecked(false);
		            share.setVisibility(View.GONE);
		            
		            stopShare.setVisibility(View.GONE);
		            regenerate.setVisibility(View.GONE);
		            if(start.isClickable()) {
		            	statusText.setVisibility(View.VISIBLE);
		            	upload.setVisibility(View.VISIBLE);
		            }
		        }
			}
		});
        
        share.setVisibility(View.GONE);
        stopShare.setVisibility(View.GONE);
        share.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				//i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"catalin.fratila@cti.pub.ro"});
				i.putExtra(Intent.EXTRA_SUBJECT, "[TrafficCollector App] Follow My Position");
				i.putExtra(Intent.EXTRA_TEXT   , "You can access my position: http://cipsm.hpc.pub.ro/traffic/view.php?id_user=" + (traffic.getId()));
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(thisActivity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        stopShare.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(stopShare.getText().toString().contains("Stop Sharing")) {
					Utils.sendPost(traffic, traffic.getId(), stopShareURL, 0, "1");
					stopShare.setText("Start Sharing");
				}
				else {
					Utils.sendPost(traffic, traffic.getId(), stopShareURL, 1, "0");
					stopShare.setText("Stop Sharing");
				}
			}
		});
        //upload.setVisibility(View.GONE);
        upload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				statusText.setVisibility(View.VISIBLE);
				HandleUpload();
			}
		});
        
        regenerate.setVisibility(View.GONE);
        regenerate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				id_user = traffic.generateId();
				toast = Toast.makeText(thisActivity, "User id has been regenerated", 4000);
				toast.show();
			}
		});
        
        wv = (WebView) findViewById(R.id.where);
        String content = "To view statistics follow:<br/> <a href=\"http://cipsm.hpc.pub.ro/traffic\">http://cipsm.hpc.pub.ro/traffic</a>";
        String html = "<html>" +
		 "<head></head>" + 
		 "<body style=\"text-align:justify;color:gray;background-color:black; font-size: 18px;\">" + 
		  content +
		 "</body>" +
		"</html>";
		wv.setVerticalScrollBarEnabled(false);
		wv.setHorizontalScrollBarEnabled(false);
		wv.loadData(html, "text/html", "utf-8");
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
        name = prefs.getString("settings_name", "0");
        facebook = prefs.getString("settings_facebook", "0");
        twitter = prefs.getString("settings_twitter", "0");
        if(name == null) name = "0";
        if(facebook == null) facebook = "0";
        if(twitter == null) twitter = "0";
	}
	
	public void HandleStart() {
		pressedStart = true;
		
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
		
		if(!isGPS) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setMessage("Please enable GPS to use application");
			alertDialog.setButton("GPS Settings", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
			      }
			});
			alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  dialog.cancel();
			    	  if(collector != null)
			    		  stopService(collector);
			      }
			});
			alertDialog.show();
		}
		else {
			collector = new Intent(this, TrafficCollectorService.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean("realTime", real);
			bundle.putString("uuid", getId());
			bundle.putString("name", name);
			bundle.putString("facebook", facebook);
			bundle.putString("twitter", twitter);
			collector.putExtras(bundle);
			startService(collector);
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(pressedStart) {
			if(requestCode == 0) {
				locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
				
				boolean isGPS = locationManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
				
				if(!isGPS) {
					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setMessage("Please enable GPS to use application");
					alertDialog.setButton("GPS Settings", new DialogInterface.OnClickListener() {
					      public void onClick(DialogInterface dialog, int which) {
					    	  startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
					      }
					});
					alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
					      public void onClick(DialogInterface dialog, int which) {
					    	  dialog.cancel();
					    	  if(collector != null)
					    		  stopService(collector);
					      }
					});
					alertDialog.show();
				}
				else {
					collector = new Intent(this, TrafficCollectorService.class);
					Bundle bundle = new Bundle();
					bundle.putBoolean("realTime", real);
					bundle.putString("name", name);
					bundle.putString("facebook", facebook);
					bundle.putString("twitter", twitter);
					collector.putExtras(bundle);
					startService(collector);
				}
			}
		}
    }
	
	public void HandlePause() {
		pressedStart = false;
		pause.setClickable(false);
		if(collector != null)
			stopService(collector);
	}

	@Override
	public void onBackPressed() {
		if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
			toast = Toast.makeText(this, "Press back again to close this app", 4000);
			toast.show();
			this.lastBackPressTime = System.currentTimeMillis();
		} else {
			if (toast != null) {
				toast.cancel();
			}
			if(collector != null)
				stopService(collector);
			System.exit(0);
		}
	}
	
	private void HandleUpload() {
		if(!Utils.checkInternetConnection(thisActivity)){ 
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setMessage("Please enable internet connection to use upload function");
			alertDialog.setButton("Wireless And Network Settings", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 1);
			      }
			});
			alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  dialog.cancel();
			      }
			});
			alertDialog.show();
		}
		else {
			uploadThread = new UploadThread(this, this.getApplicationContext());
			uploadThread.start();
		}
    }
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		//Handle item selection
		switch (item.getItemId()) 
		{
			case R.id.settings:
			{
				Intent i = new Intent();
				i.setClass(this, TrafficCollectorSettings.class);
            	this.startActivity(i);
				return true;
			}
			case R.id.plot:
			{
				Intent i = new Intent();
				i.setClass(this, TrafficCollectorSpeed.class);
            	this.startActivity(i);
				return true;
			}
			case R.id.map:
			{
				if(!Utils.checkInternetConnection(thisActivity)){ 
					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setMessage("Please enable internet connection to use this function");
					alertDialog.setButton("Wireless And Network Settings", new DialogInterface.OnClickListener() {
					      public void onClick(DialogInterface dialog, int which) {
					    	  startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 1);
					      }
					});
					alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
					      public void onClick(DialogInterface dialog, int which) {
					    	  dialog.cancel();
					      }
					});
					alertDialog.show();
				}
				else {
					Intent i = new Intent();
					i.setClass(this, TrafficMap.class);
	            	this.startActivity(i);
				}
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void onStop() {
		super.onStop();
		db_statistics.close();
		db_user.close();
	}
	
	public void removeData(Database db) {
		db.clearTable();
		if(Utils.testCard()) {
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File (sdCard.getAbsolutePath() + "/trafficCollector/");
			File file = new File(dir, "journey.txt");
			if(file.exists())
				file.delete();
		}
	}
	
	class UploadThread extends Thread {
		
		TrafficCollectorMain parentActivity;
		Context context;
		Database db;
		
		public UploadThread(TrafficCollectorMain parentActivity, Context context) {
			this.parentActivity = parentActivity;
			this.context = context;
		}
		
		public void run() {
			boolean error = false;
			Socket socket;
			PrintWriter out;
			BufferedReader in;
			
			uploadWakeLock.acquire();
			toastHandler.post(toastRunnableStart);
			db = new Database(thisActivity, "collector", "routes", new String[] { "lat", "long", "speed", "timestamp" });
			db_user = new Database(thisActivity, "collector", "user", new String[] { "name", "username", "password", "uuid" });
			ArrayList<ArrayList<String>> values = db.getList("");
			if(values.size() != 0)
			{
				
				try {
					String reply = "";
					socket = new Socket("mobiway.hpc.pub.ro", 8082);
					// socket = new Socket("cipsm.hpc.pub.ro", 8082);
					out = new PrintWriter(socket.getOutputStream(), true);
	                in = new BufferedReader(new InputStreamReader(
	                		socket.getInputStream()));
	                while (!reply.equals("ACK")) {
	                    // send first message.
	                	CryptTool ct = new CryptTool();
	                	String lname = db_user.isEmpty() ? "0" : db_user.get("name").equals("") ? "0" : CryptTool.bytes2String(Base64.decode(db_user.get("name"), Base64.NO_WRAP));
	                	String lusername = db_user.isEmpty() ? "0" : db_user.get("username").equals("") ? "0" : db_user.get("username");
	                	String lpassword = db_user.isEmpty() ? "0" : db_user.get("password").equals("") ? "0" : ct.encrypt(db_user.get("password"));
	                	
	                	out.println("#s#" + lname + "#" + (facebook.equals("") ? "0" : facebook) + "#" + (twitter.equals("") ? "0" : twitter) + "#" + traffic.getId()  + "#" + lusername + "#" + lpassword + "#" + ct.encrypt(db_user.get("uuid")));
	                    
	                    // wait for reply.
	                    reply = in.readLine();
	                }
	                
	                for(int i = 0; i < values.size(); i++)
	                {
	                	ArrayList<String> value = values.get(i);
	                	out.println(value.get(1) + " " + value.get(2) + " " + value.get(3) + " " + value.get(4));
	                }
	                out.println("#f#");
	                socket.close();
					in.close();
					out.close();
				} catch (Exception e) {
					error = true;
			    	toastHandler.post(toastRunnableError);
			    	db.close();
				}
				
				if(!error) {
					toastHandler.post(toastRunnableFinish);
					removeData(db);
					db.close();
				}
				/*String toSend = elements.toString();
				try {
					URL url = new URL("http://cipsm.hpc.pub.ro/MACollector/collector_1.4.php");
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	
					connection.setDoInput(true);
					connection.setDoOutput(true);
					connection.setUseCaches(false);
					connection.setRequestMethod("POST");
	
					connection.setRequestProperty("Connection", "Keep-Alive");
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	
					DataOutputStream dataOut = new DataOutputStream(connection.getOutputStream());
					Calendar cal = Calendar.getInstance();
				    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
					String filename = "journey" + URLEncoder.encode(sdf.format(cal.getTime()), "UTF-8") + ".txt";
					String sentName = URLEncoder.encode(name, "UTF-8");
					String sentFacebook = URLEncoder.encode(facebook, "UTF-8");
					String sentTwitter = URLEncoder.encode(twitter, "UTF-8");
					dataOut.writeBytes("name=" + sentName + "&facebook=" + sentFacebook +
								"&twitter=" + sentTwitter + "&filename=" + filename + 
								"&elements=" + URLEncoder.encode(toSend, "UTF-8"));
					dataOut.flush();
					
					BufferedReader dataIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				    String line = dataIn.readLine();
				    if(line == null) {
				    	error = true;
				    	toastHandler.post(toastRunnableError);
				    	db.close();
				    } else if(!line.equals("200") ) {
				    	error = true;
				    	toastHandler.post(toastRunnableError);
				    	db.close();
				    }
				    dataOut.close();
				    dataIn.close();
				} catch (Exception e) {
					error = true;
					toastHandler.post(toastRunnableError);
			    	db.close();
					e.printStackTrace();
				}
				if(!error) {
					toastHandler.post(toastRunnableFinish);
					removeData(db);
					db.close();
				}*/
			}
			else {
				toastHandler.post(toastRunnableNoData);
				db.close();
			}
			if (uploadWakeLock.isHeld())
				uploadWakeLock.release();
		}
	}
}