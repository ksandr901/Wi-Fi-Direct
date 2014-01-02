package com.example.wifidirect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements ConnectionInfoListener {
	WifiP2pManager mManager;//WifiP2pManager
	Channel mChannel;//Channel
	BroadcastReceiver mReceiver;//BroadCastReceiver
	IntentFilter mIntentFilter;//IntentFilter
	boolean isWifiP2pEnable = false;//Wifip2p is enabled or not
	ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();//Wifip2pDevices
	WifiP2pDevice Me = new WifiP2pDevice();//Self Wifip2pDevice
	ArrayList<HashMap<String,String>> peermap = new ArrayList<HashMap<String,String>>();
	String [] cur_states = new String[1005]; //record curent page
	int cur_state = 0; // record current page
	int targetpeer = -1; //target peer
	ListView pl;
	InetAddress GOAddress; //Group owner's ip
	boolean isOwner = false; //isGO or not
	Queue<String> tq = new LinkedList<String>();
	//img uri
		Uri jpguri;
	int vn = 0;
	Handler notify = new Handler();
	/*type*/
	String[][] MIME_MapTable={ 
			 {".3gp",    "video/*"}, 
			 {".avi",    "video/x-msvideo"}, 
			 {".bmp",    "image/*"},
			 {".c",  "text/*"}, 
			  {".cpp",    "text/*"}, 
			  {".gif",    "image/gif"}, 
			  {".h",  "text/*"}, 
			  {".htm",    "text/*"}, 
			  {".html",   "text/*"}, 
			  {".java",   "text/*"}, 
			  {".jpeg",   "image/*"}, 
			  {".jpg",    "image/*"}, 
			   {".mp3",    "audio/*"}, 
			   {".mp4",    "video/*"}, 
			     {".mpeg",   "video/*"},   
			     {".ogg",    "audio/*"}, 
			     {".pdf",    "application/pdf"}, 
			     {".png",    "image/*"},
			     {".wma",    "audio/x-ms-wma"}, 
			     {".txt",    "text/plain"}, 
			     {".wav",    "audio/*"}, 
			     
	};
	String getMIMEType(File file) { 
		String type="no";
		 String fName = file.getName(); 
		  int dotIndex = fName.lastIndexOf("."); 
		  if(dotIndex < 0){ 
		        return type; 
		    } 
		   String end=fName.substring(dotIndex,fName.length()).toLowerCase(); 
		   if(end=="")return type; 
		   for(int i=0;i<MIME_MapTable.length;i++){ 
			   if(end.equals(MIME_MapTable[i][0])) 
		            type = MIME_MapTable[i][1]; 
		   }
		return type;
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Add actions to Receive request
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    mChannel = mManager.initialize(this, getMainLooper(), null);
	    mReceiver = new MyReceiver(mManager, mChannel, this);
	    
	    Button bv = (Button)findViewById(R.id.back);
	    bv.setOnClickListener(
	    	new OnClickListener()
	    	{

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					backstep();
					
					
				}
	    		
	    	}
	    	
	    );
	    
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
		 switch (item.getItemId())
		 {
		 case R.id.searchpeer:
			 if(!isWifiP2pEnable)
         	{
         		 Toast.makeText(MainActivity.this, "請開啟Wi-Fi Direct",
                          Toast.LENGTH_SHORT).show();
                  return true;
         	}
			 
			 mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
				 @Override
                 public void onSuccess() {
                     Toast.makeText(MainActivity.this, "開始搜尋",
                             Toast.LENGTH_SHORT).show();
                 }

                 @Override
                 public void onFailure(int reasonCode) {
                     Toast.makeText(MainActivity.this, "搜尋失敗，請在試一次" ,
                             Toast.LENGTH_SHORT).show();
                 }
			 });
			 return true;
		 case R.id.wifip2pset:
			 if (mManager != null && mChannel != null) {

                 // Since this is the system wireless settings activity, it's
                 // not going to send us a result. We will be notified by
                 // WiFiDeviceBroadcastReceiver instead.

                 startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
			 }
			 
			 return true;
		 
			 
		 	default:
             return super.onOptionsItemSelected(item);
		 }
		 
		
	 
	 }
	
	
	
	
	/* register the broadcast receiver with the intent values to be matched */
	@Override
	protected void onResume() {
	    super.onResume();
	    registerReceiver(mReceiver, mIntentFilter);
	}
	/* unregister the broadcast receiver */
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(mReceiver);
	}
	
	//get device's status
	String getDeviceStatus(int deviceStatus) {
       
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
	//Fetch the peers
	PeerListListener peerListListener = new PeerListListener()
	{
		 @Override
	        public void onPeersAvailable(WifiP2pDeviceList peerList) {
			//Update the peer list
			 	peers.clear();
	            peers.addAll(peerList.getDeviceList());
	            peermap.clear();
	            
	            //add to Hashmap of peerlist
	            for(int i=0;i<peers.size();i++)
	            {
	            	HashMap<String,String>tmp = new HashMap<String,String>();
	            	WifiP2pDevice wd = peers.get(i);
	            	tmp.put("name", wd.deviceName); 
	            	tmp.put("status",  getDeviceStatus(wd.status));
	            	tmp.put("address", wd.deviceAddress);
	            	peermap.add(tmp);
	            }
	            
	            pl = (ListView)findViewById(R.id.peerlist);
	            SimpleAdapter apdater = new  SimpleAdapter(
	            		 MainActivity.this, 
	            		 peermap,
	            		 android.R.layout.simple_list_item_2,
		   				 new String[] { "name","status" },
		   				 new int[] { android.R.id.text1, android.R.id.text2 }
	            		);
	            pl.setAdapter(apdater);
	            pl.setOnItemClickListener(
	            		new OnItemClickListener()
	            		{

							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								
	            				targetpeer=arg2;
	            				
								
							}
	            			
	            		}
	            );
	            registerForContextMenu(pl);
	            pl.setOnItemLongClickListener(
	            		new OnItemLongClickListener()
	            		{
	            			@Override
	            			public boolean onItemLongClick(AdapterView<?> parent,
	            					View view, int position,long id)
	            			{
	            				targetpeer = position;
	            				pl.showContextMenu();
	            				
	            				
	            				 return true;
	            			}
	            		}
	            
	            		
	            );
	            
	            pl.setOnCreateContextMenuListener(
	            		new OnCreateContextMenuListener()
	            		{

							@Override
							public void onCreateContextMenu(ContextMenu arg0,
									View arg1, ContextMenuInfo arg2) {
								// TODO Auto-generated method stub
								arg0.add(0,0,0,"連線");
								arg0.add(0,1,1,"取消");
								
								MainActivity.this.onCreateContextMenu(arg0,arg1,arg2);
							}
	            			
	            		}
	            		
	            );
	            
	            
		 }
	};
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		switch(item.getItemId()) {
		case 0:
			connect(targetpeer);
			break;
		default:
			
			break;
			
		
		}
		return super.onContextItemSelected(item);
	}
	
	
	//Disconnect
	
	public void disconnect()
	{
		
		
		mManager.removeGroup(mChannel, 
				new ActionListener()
				{

					@Override
					public void onFailure(int arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						
					}
			
			
			
				}
			);
	}
	
	
	 //Connect to arg'th device
	  public void connect(int arg)
	  {
		  WifiP2pDevice device = peers.get(arg);
		  WifiP2pConfig config = new WifiP2pConfig();
	      config.deviceAddress = device.deviceAddress;
	      config.wps.setup = WpsInfo.PBC;
	      mManager.connect(mChannel, config, new ActionListener() {
	        	@Override
	            public void onSuccess() {
	                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
	        		//Change View
	        		
	            }
	        	
	            @Override
	            public void onFailure(int reason) {
	                //View
	            	Toast.makeText(MainActivity.this, "連線失敗!!",
	                        Toast.LENGTH_SHORT).show();
	            }
	        });

		 
		  
	  }
	  
	  public void listfile(String filepath)
	  {
		  
		  
		  File file;
		  file =new File(filepath);
		  
		  File[] files = file.listFiles();
		  
		  ArrayList<HashMap<String,String>> flist = new ArrayList<HashMap<String,String>>();
		  
		  for(File f:files)
		  {
			  HashMap<String,String> item = new HashMap<String,String>();
			  item.put("name", f.getName());
			  
			  if(f.isDirectory())
			  {
				  item.put("isD", "目錄");
			  }
			  else
			  {
				  item.put("isD", "檔案");
			  }
			  
			  item.put("path", f.getAbsolutePath());
			  
			  flist.add(item);
			  
			  SimpleAdapter fadapter;
			   fadapter = new SimpleAdapter( 
						 this, 
						 flist,
						 android.R.layout.simple_list_item_2,
						 new String[] { "name","isD" },
						 new int[] { android.R.id.text1, android.R.id.text2 } );
			   
			   ListView fview = (ListView)findViewById(R.id.listfile);
			   fview.setVisibility(View.VISIBLE);
			   fview.setAdapter(fadapter);
			   
			   fview.setOnItemClickListener(
					   new OnItemClickListener()
					   {

						   @Override
						   public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub
							   ListView l1 = (ListView)arg0;
							   HashMap<String, String> filech =new HashMap<String,String>((HashMap<String, String>)l1.getItemAtPosition(arg2));
							   String isD = filech.get("isD");
							   
							   if(isD.equals("目錄"))
							   {
								   	cur_state++;
									  cur_states[cur_state] = "f`"+filech.get("path");
									 changeview();
								  
							   }
							   else
							   {
								   
								   String path = filech.get("path");
								   sendFile(path);
							   }
							   
						}
						   
					   }
					   
				);
			   
			   
		  }
	  }
	  ByteArrayOutputStream [] myoutputstream=new  ByteArrayOutputStream[105];
	  SurfaceView sView;
	  SurfaceHolder surfaceHolder;
	  int screenWidth, screenHeight;    
	  Camera camera;
	  boolean isPreview = false;        //是否在瀏覽中
	  public void OpenMonitor()
	  {
		  
		  screenWidth = 320;
          screenHeight = 240;
		  sView = (SurfaceView)findViewById(R.id.sV1);
		  sView.setVisibility(View.VISIBLE);
		  surfaceHolder = sView.getHolder();    
		 
		  surfaceHolder.addCallback(new Callback()
		  {

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				try{
					
				}catch(Exception e)
				{
					
				}
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				//打開攝影機
				initCamera(); 
				surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				// 如果camera不为null ,釋放攝像頭
                if (camera != null) {
                	  camera.setPreviewCallback(null);
                	  
                        if (isPreview)
                                camera.stopPreview();
                     
                        camera.release();
                        camera = null;
                       
                }
            
			}
			  
		  });
		 
		  
	  }
	  
	  public void initCamera() {
		  if (!isPreview) {
              camera = Camera.open();
		  }
		  if (camera != null && !isPreview) {
			  
			  try
			  {
				// TODO Auto-generated method stub
					//camera.setDisplayOrientation(90); //设置横行录制  
					 Camera.Parameters parameters = camera.getParameters();   
					  parameters.setPreviewSize(screenWidth, screenHeight); 
					  parameters.setPreviewFpsRange(25,25);  
					  parameters.setPictureFormat(ImageFormat.NV21);
					  parameters.setPictureSize(320, screenHeight);  
					  
					  camera.setPreviewDisplay(surfaceHolder); 
					  camera.setPreviewCallback(new StreamIt());  
					 
					  camera.startPreview();    
					  camera.autoFocus(null);        
				  
			  }
			  catch(Exception e)
			  {
				  
			  }
			  
			  
		  }
	  }
	  int ss = 0;
	  class StreamIt implements Camera.PreviewCallback {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Size size = camera.getParameters().getPreviewSize();    
			try
			{
				  //調用image.compressToJpeg（）將YUV格式圖像數據data轉为jpg格式
	            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
	            if(image!=null&&data!=null){
	            	 myoutputstream[vn] = new ByteArrayOutputStream();
	            	 image.compressToJpeg(new Rect(0, 0, size.width, size.height),60,  myoutputstream[vn]); 
	            	 
	            	 myoutputstream[vn].flush();
	            	
	            	 if(ss<5)
	            	 {
	            		 ss++;
	            		 return ;
	            	 }
	            	 
	          
	            	
	            	 SendV();
	            	 ss=0;
	            	 
	            	 
	            	//启用線程將圖像數據發送出去
	                 //Thread th = new MyThread(outstream,ipname);
	                 //th.start();    
	            	
	            }
			}
			catch(Exception e)
			{
				
			}
			
			
		}
		  
	  }
	  
	  /*send tmp Image for camera*/
	  public void SendV()
	  {
		final String filename = "tmp"+Integer.toString(vn)+".jpg";
		 if(isOwner)
		 {
			 tq.offer("v`"+filename);
		 }
		 else
		 {
			 Thread ss = new Thread(
					 new Runnable()
					 {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try{
							Socket socket = new Socket();
							   InetAddress serverAddr = null;
							   SocketAddress sc_add = null;
							   serverAddr = GOAddress;
							   sc_add = new InetSocketAddress(serverAddr,1234);
							   socket.connect(sc_add);
							
							   DataInputStream in = new DataInputStream(socket.getInputStream());
								DataOutputStream out = new
										DataOutputStream(socket.getOutputStream());
								byte[] data = new byte[2048];
								out.writeBytes("video");
								Arrays.fill(data, (byte)0);
								in.read(data);
								out.writeBytes(filename);
								Arrays.fill(data, (byte)0);
								in.read(data);
								 ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream[vn].toByteArray());
									//InputStream frr = new FileInputStream(file);
									//Transferring data
									while(true)
									{
										Arrays.fill(data,(byte)0);
										int read = inputstream.read(data);
										if(read<=0)
											break;
										out.write(data,0,read);
									}
									out.flush();
									out.close();
									myoutputstream[vn].close();
									vn = (vn+1)%100;
									inputstream.close();
								
								
								
						}
						catch (Exception e)
						{
							
						}
					 }
					 }
					 );
		 }
		  
		  
		 
	  }
	  
	  void SetDevice(WifiP2pDevice device)
	  {
		  this.Me = device;
	  }
	  int ccn = 0;
	  public void changeview()
	  {
		  pl = (ListView)findViewById(R.id.peerlist);
		  Button fv = (Button)findViewById(R.id.file);
		  Button cv = (Button)findViewById(R.id.camera);
		  Button mv = (Button)findViewById(R.id.monitor);
		  Button ev = (Button)findViewById(R.id.End);
		  ListView fl = (ListView)findViewById(R.id.listfile);
		  ImageView iv = (ImageView)findViewById(R.id.iv);
		  SurfaceView sv = (SurfaceView)findViewById(R.id.sV1);
		  
		  
		  pl.setVisibility(View.GONE);
		  fv.setVisibility(View.GONE);
		  cv.setVisibility(View.GONE);
		  ev.setVisibility(View.GONE);
		  mv.setVisibility(View.GONE);
		  fl.setVisibility(View.GONE);
		  if(cur_state>0&&cur_states[cur_state].charAt(0)!='i')
		  iv.setVisibility(View.GONE);
		  sv.setVisibility(View.GONE);
		  
		  if(cur_state==0)
		  {
			  pl.setVisibility(View.VISIBLE);
			  peermap.clear();
	            
	            //add to Hashmap of peerlist
	            for(int i=0;i<peers.size();i++)
	            {
	            	HashMap<String,String>tmp = new HashMap<String,String>();
	            	WifiP2pDevice wd = peers.get(i);
	            	tmp.put("name", wd.deviceName); 
	            	tmp.put("status",  getDeviceStatus(wd.status));
	            	tmp.put("address", wd.deviceAddress);
	            	peermap.add(tmp);
	            }
	            
	            pl = (ListView)findViewById(R.id.peerlist);
	            SimpleAdapter apdater = new  SimpleAdapter(
	            		 MainActivity.this, 
	            		 peermap,
	            		 android.R.layout.simple_list_item_2,
		   				 new String[] { "name","status" },
		   				 new int[] { android.R.id.text1, android.R.id.text2 }
	            		);
	            pl.setAdapter(apdater);
	            pl.setOnItemClickListener(
	            		new OnItemClickListener()
	            		{

							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								
	            				targetpeer=arg2;
	            				
								
							}
	            			
	            		}
	            );
	            registerForContextMenu(pl);
	            pl.setOnItemLongClickListener(
	            		new OnItemLongClickListener()
	            		{
	            			@Override
	            			public boolean onItemLongClick(AdapterView<?> parent,
	            					View view, int position,long id)
	            			{
	            				targetpeer = position;
	            				pl.showContextMenu();
	            				
	            				
	            				 return true;
	            			}
	            		}
	            
	            		
	            );
	            
	            pl.setOnCreateContextMenuListener(
	            		new OnCreateContextMenuListener()
	            		{

							@Override
							public void onCreateContextMenu(ContextMenu arg0,
									View arg1, ContextMenuInfo arg2) {
								// TODO Auto-generated method stub
								arg0.add(0,0,0,"連線");
								arg0.add(0,1,1,"取消");
								
								MainActivity.this.onCreateContextMenu(arg0,arg1,arg2);
							}
	            			
	            		}
	            		
	            );
		  }
		  else
		  {
			  String par = cur_states[cur_state];
			  if(par.charAt(0)=='m')
			  {
				  fv.setVisibility(View.VISIBLE);
				  cv.setVisibility(View.VISIBLE);
				  mv.setVisibility(View.VISIBLE);
				  ev.setVisibility(View.VISIBLE);
				  
				  fv.setOnClickListener(new OnClickListener()
				  	{

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
							cur_state++;
							cur_states[cur_state] = "f`"+filepath;
							changeview();
							
							
						}
					  
				  	}
						  
						  );
				  
				  cv.setOnClickListener(
					new OnClickListener()
					{

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//拍照
							Intent intent =  new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
							
							String imgpath = Environment.getExternalStorageDirectory().getAbsolutePath();
							imgpath = imgpath+"/WIFIDIRECT";
							
							String img = Me.deviceName+"-"+System.currentTimeMillis()+".jpg";
							
							File f = new File(imgpath);
							
							if(!f.exists())
							{
								f.mkdir();
							}
							
							imgpath = imgpath + "/Pictures";
							
							f = new File(imgpath);
							
							if(!f.exists())
							{
								f.mkdir();
							}
							
							File tmp = new File(f,img);
							jpguri= Uri.fromFile(tmp);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, jpguri);
							intent.putExtra("goal", "camera");
							startActivityForResult(intent, 0);
						}
						
					}
						  
					);
				  mv.setOnClickListener(
						  new OnClickListener()
						  {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								//Open Monitor
								//OpenMonitor();
								cur_state++;
								cur_states[cur_state] = "v`";
								changeview();
							}
						  
						  });
				  ev.setOnClickListener(
					new OnClickListener()
					{

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							disconnect();
						}
						
					}
						  
						  
					);
			  }
			  else if(par.charAt(0)=='f')
			  {
				  String [] pars = par.split("`");
				  listfile(pars[1]);
			  }
			  else if(par.charAt(0)=='v')
			  {
				  OpenMonitor();
				  
			  }
			  else if(par.charAt(0)=='i')
			  {
				
				  {
				 // Matrix matrix = new Matrix();
				  //matrix.postRotate(90);
				  
					 String [] pars = par.split("`");
				  	
					 iv.setVisibility(View.VISIBLE);
					 //String sd = Environment.getExternalStorageDirectory().toString();
					//sd+="WifiDirect/tmp/"+filename;
					 Bitmap bit =  BitmapFactory.decodeFile(pars[1]);
					 //WeakReference<Bitmap> wr =new WeakReference<Bitmap>( Bitmap.createBitmap(bit, 0, 0, 320,
						//240, matrix, true));
					 //bit = Bitmap.createBitmap(bit,0,0,bit.getWidth(),bit.getHeight(),matrix,true);
							
					 iv.setImageBitmap(bit);
					
				  }
					
					 
			  }
		  }
	  }
	  
	  
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	  {
		  super.onActivityResult(requestCode, resultCode, data);
		  
		  if (resultCode == RESULT_OK) 
		  {
			 
			
				  //Send image to target device
				  String imgpath = jpguri.getPath();
				  sendImg(imgpath);
			  
			 
			  
		  }
	  }
	  
	  
	  int cn = 0;
	  Socket[] cs = new Socket[1024];
	  Thread [] tc = new Thread[1024];
	  
 	  Runnable server = new Runnable()
	  {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				ServerSocket hs = new ServerSocket(1234);
				
				while(true)
				{
					cs[cn] = hs.accept();
					tc[cn]  = new Thread(s1);
					tc[cn].start();
					
					Thread.sleep(100);
					
					
				}
				
				
				
			}
			catch(Exception e)
			{
				
			}
		}
	  
	  };
	  
	  Runnable s1 = new Runnable()
	  {

		@Override
		public void run() {
			try{
				// TODO Auto-generated method stub
				int num = cn;
				cn = (cn+1)%1024;
				DataInputStream in = new DataInputStream(cs[num].getInputStream());
				
				DataOutputStream out = new DataOutputStream(cs[num].getOutputStream());
				//buffer
				byte [] data = new byte[2048];
				String cmd;
				Arrays.fill(data, (byte)0);
				in.read(data);
				cmd = bytetoString(data);
				
				if(cmd.equals("re"))
				{
					String task = tq.poll();
					if(task==null)
						out.writeBytes("#");
					else
					{	
						if(task.charAt(0)=='f')
						{
							out.writeBytes("file");
							
							//OK
							Arrays.fill(data, (byte)0);
							in.read(data);
							
							String[] ts = task.split("`");
							String filepath = ts[1];
							
							File file = new File(filepath);
							
							final String filename = file.getName();
							
							out.writeBytes(filename);
							
							//OK
							Arrays.fill(data, (byte)0);
							in.read(data);
							
							InputStream frr = new FileInputStream(file);
							
							//Transferring data
							while(true)
							{
								Arrays.fill(data,(byte)0);
								int read = frr.read(data);
								if(read<=0)
									break;
								out.write(data,0,read);
							}
							out.close();
							frr.close();
							cs[num].close();
							notify.post(
									new Runnable()
									{

										@Override
										public void run() {
											// TODO Auto-generated method stub
											 Toast.makeText(MainActivity.this, filename+"檔案傳送完成",
					                                 Toast.LENGTH_SHORT).show();
										}
										
									}
							);
							
							
						}
						else if(task.charAt(0)=='c')
						{
							out.writeBytes("camera");
							
							//OK
							Arrays.fill(data, (byte)0);
							in.read(data);
							
							String[] ts = task.split("`");
							String imgpath = ts[1];
							
							File file = new File(imgpath);
							
							final String filename = file.getName();
							
							out.writeBytes(filename);
							
							//OK
							Arrays.fill(data, (byte)0);
							in.read(data);
							
							InputStream frr = new FileInputStream(file);
							
							//Transferring data
							while(true)
							{
								Arrays.fill(data,(byte)0);
								int read = frr.read(data);
								if(read<=0)
									break;
								out.write(data,0,read);
							}
							out.close();
							frr.close();
							cs[num].close();
								
						}
						else if(task.charAt(0)=='v')
						{
							out.writeBytes("video");
							//OK
							Arrays.fill(data, (byte)0);
							in.read(data);
							String[] ts = task.split("`");
							final String filepath = ts[1];							
							//File file = new File(filepath);
							//final String filename = file.getName();
							out.writeBytes(filepath);
							//OK
							Arrays.fill(data, (byte)0);
							in.read(data);
							 ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream[vn].toByteArray());
							//InputStream frr = new FileInputStream(file);
							//Transferring data
							while(true)
							{
								Arrays.fill(data,(byte)0);
								int read = inputstream.read(data);
								if(read<=0)
									break;
								out.write(data,0,read);
							}
							
							out.flush();
							out.close();
							myoutputstream[vn].close();
							vn = (vn+1)%100;
							inputstream.close();
							cs[num].close();
						}
					
					}
					
					
				}else if(cmd.equals("file"))
				{
					out.writeBytes("OK");
					
					
					Arrays.fill(data, (byte)0);
					in.read(data);
					
					final String filename = bytetoString(data);
					
					out.writeBytes("OK");
					
					String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/WifiDirect";
					
					File  file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/接收的檔案";
					file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/"+filename;
					
					OutputStream fww = new FileOutputStream(filepath);
					 long startTime = System.currentTimeMillis();
					 long bytel = 0;
					while(true)
					{
						
						int read = in.read(data);
						
						if(read<=0)
							break;
						bytel+=read;
						fww.write(data,0,read);
						
						
					}
					fww.flush();
					fww.close();
					  long endTime = System.currentTimeMillis();
					  long totTime = endTime - startTime;
					  final double rate = bytel/totTime;
					/*
					  Intent intent = new Intent(); 
					 
					  intent.setAction(Intent.ACTION_VIEW); 
					  File nfile = new File(filepath);
					  String type = getMIMEType(nfile);
					  if(!type.equals("no"))
					  {
						  intent.setDataAndType(Uri.fromFile(nfile), type);
						  startActivity(intent);
					  }*/
					notify.post(
							new Runnable()
							{

								@Override
								public void run() {
									// TODO Auto-generated method stub
									 Toast.makeText(MainActivity.this, "收到檔案"+filename+"\n速率:"+Double.toString(rate/1024.0)+"MB/s",
			                                 Toast.LENGTH_SHORT).show();
								}
								
							}
					);
					
				}
				else if(cmd.equals("camera"))
				{
					out.writeBytes("OK");
					
					
					Arrays.fill(data, (byte)0);
					in.read(data);
					
					final String filename = bytetoString(data);
					
					out.writeBytes("OK");
					
					String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/WifiDirect";
					
					File  file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/接收的檔案";
					file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/"+filename;
					
					OutputStream fww = new FileOutputStream(filepath);
					
					while(true)
					{
						
						int read = in.read(data);
						if(read<=0)
							break;
						fww.write(data,0,read);
						
						
					}
					fww.flush();
					fww.close();
					
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					File imgfile = new File(filepath);
					Uri outputFileUri = Uri.fromFile(imgfile);
					intent.setDataAndType(outputFileUri, "image/*");
					startActivity(intent);
					
					
				}
				else if(cmd.equals("video"))
				{
					out.writeBytes("OK");
					Arrays.fill(data, (byte)0);
					in.read(data);
					
					final String filename = bytetoString(data);
					String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/WifiDirect";
					File  file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					filepath = filepath+"/tmp";
					file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					filepath = filepath+"/"+filename;
					
					OutputStream fww = new FileOutputStream(filepath);
					out.writeBytes("OK");
					while(true)
					{
						
						int read = in.read(data);
						if(read<=0)
							break;
						fww.write(data,0,read);
						
						
					}
					in.close();
					fww.flush();
					fww.close();
					notify.post(
							new Runnable()
							{

								@Override
								public void run() {
									// TODO Auto-generated method stub
									 //Toast.makeText(MainActivity.this, "收到檔案"+filename,
			                           //      Toast.LENGTH_SHORT).show();
									
									 String sd = Environment.getExternalStorageDirectory().toString();
									sd+="/WifiDirect/tmp/"+filename;
									if(cur_states[cur_state].charAt(0)!='i')
									{
									  cur_state++;
									  cur_states[cur_state] = "i`"+sd;
									 changeview();
									}
									else
									{
										cur_states[cur_state] = "i`"+sd;
										 changeview();
									}
								}
								
							}
					);
				}
				
			}catch(Exception e)
			{
				
			}
			
		}
		  
	  };
	  
	// bytetoString
		  String bytetoString(byte [] data1)
		  {
			  String ans = "";
			  for(int i = 0;;i++)
			  {
				  if(data1[i]==0)
					  break;
				  ans = ans + (char)data1[i];
			  }
			  return ans;
		  }
	  
		  
	  Runnable client = new Runnable()
	  {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				cn = (cn+1)%1024;
				Socket mc = new Socket();
				InetAddress serverAddr = null;
				byte[] data = new byte[2048];
				SocketAddress sc_add = null;
				serverAddr = GOAddress;
				sc_add = new InetSocketAddress(serverAddr,1234);
				mc.connect(sc_add);
				DataInputStream in = new DataInputStream(mc.getInputStream());
				DataOutputStream out = new
						DataOutputStream(mc.getOutputStream());
				out.writeBytes("re");
				
				Arrays.fill(data, (byte)0);
				in.read(data);
				
				String cmd = bytetoString(data);
				
				if(cmd.equals("#"))
				{
					mc.close();
				}
				else if(cmd.equals("file"))
				{
					out.writeBytes("OK");
					
					
					//filename
					Arrays.fill(data, (byte)0);
					in.read(data);
					
					final String filename = bytetoString(data);
					
					String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/WifiDirect";
					
					File  file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/接收的檔案";
					file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/"+filename;
					
					out.writeBytes("OK");
					
					OutputStream fww = new FileOutputStream(filepath);
					 long startTime = System.currentTimeMillis();
					 long bytel = 0;
					while(true)
					{
						
						int read = in.read(data);
						if(read<=0)
							break;
						bytel+=read;
						fww.write(data,0,read);
						
						
					}
					fww.flush();
					fww.close();
					long endTime = System.currentTimeMillis();
					  long totTime = endTime - startTime;
					  final double rate = bytel/totTime;
					/*Intent intent = new Intent(); 
					 
					  intent.setAction(Intent.ACTION_VIEW); 
					  File nfile = new File(filepath);
					  String type = getMIMEType(nfile);
					  if(!type.equals("no"))
					  {
						  intent.setDataAndType(Uri.fromFile(nfile), type);
						  startActivity(intent);
					  }*/
					notify.post(
							new Runnable()
							{

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(MainActivity.this, "收到檔案"+filename+"\n速率:"+Double.toString(rate/1024.0)+"MB/s",
			                                 Toast.LENGTH_SHORT).show();
								}
								
							}
					);
				}
				else if(cmd.equals("camera"))
				{
					out.writeBytes("OK");
					
					
					//filename
					Arrays.fill(data, (byte)0);
					in.read(data);
					
					final String filename = bytetoString(data);
					
					String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/WifiDirect";
					
					File  file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/接收的檔案";
					file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/"+filename;
					
					out.writeBytes("OK");
					
					OutputStream fww = new FileOutputStream(filepath);
					
					while(true)
					{
						
						int read = in.read(data);
						if(read<=0)
							break;
						fww.write(data,0,read);
						
						
					}
					fww.flush();
					fww.close();
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					File imgfile = new File(filepath);
					Uri outputFileUri = Uri.fromFile(imgfile);
					intent.setDataAndType(outputFileUri, "image/*");
					startActivity(intent);
				}
				else if(cmd.equals("video"))
				{
					out.writeBytes("OK");
					//filename
					Arrays.fill(data, (byte)0);
					in.read(data);
					final String filename = bytetoString(data);
					String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/WifiDirect";
					File  file = new File(filepath);
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/tmp";
					file = new File(filepath);
					
					if(!file.exists())
						file.mkdir();
					
					filepath = filepath+"/"+filename;
					out.writeBytes("OK");
					OutputStream fww = new FileOutputStream(filepath);
					
					while(true)
					{
						
						int read = in.read(data);
						if(read<=0)
							break;
						fww.write(data,0,read);
						
						
					}
					in.close();
					fww.flush();
					fww.close();
					notify.post(
							new Runnable()
							{

								@Override
								public void run() {
									// TODO Auto-generated method stub
									// Toast.makeText(MainActivity.this, "收到檔案"+filename,
			                          //       Toast.LENGTH_SHORT).show();
									
									
									 String sd = Environment.getExternalStorageDirectory().toString();
										sd+="/WifiDirect/tmp/"+filename;
										if(cur_states[cur_state].charAt(0)!='i')
										{
										  cur_state++;
										  cur_states[cur_state] = "i`"+sd;
										 changeview();
										}
										else
										{
											cur_states[cur_state] = "i`"+sd;
											 changeview();
										}
								}
								
							}
					);
				}
				
			}catch(Exception e)
			{
				
			}
		}
		  
	  };
	  
	  Runnable c1 = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true)
			{
				try{
				tc[cn] = new Thread(client);
				tc[cn].start();
				
				Thread.sleep(100);
				}catch(Exception e)
				{
					
				}
				
			}
			
			
			
		}
		  
	  };
	  
	  
	  
	  
	 public void sendFile(final String filepath)
	 {
		 if(isOwner)
		 {
			 String t = "f`"+filepath;
			 tq.offer(t);
			 
		 }
		 else
		 {
			 Thread s = new Thread(
					 new Runnable()
					 {

						@Override
						public void run() {
							try{
							// TODO Auto-generated method stub
							 Socket socket = new Socket();
							   InetAddress serverAddr = null;
							   SocketAddress sc_add = null;
							   serverAddr = GOAddress;
							   sc_add = new InetSocketAddress(serverAddr,1234);
							   socket.connect(sc_add);
							   
							   DataInputStream in = new DataInputStream(socket.getInputStream());
								DataOutputStream out = new
										DataOutputStream(socket.getOutputStream());
								byte[] data = new byte[2048];
								
								
								out.writeBytes("file");//command to request sendfile
								
								Arrays.fill(data,(byte)0);
								in.read(data);//OK
								
								File file = new File(filepath);;
							    final String filename = file.getName();
							    
							    out.writeBytes(filename);//filename
							    
							    Arrays.fill(data,(byte)0);
								in.read(data);//OK
								
								InputStream frr = new FileInputStream(file);
								
								//Transferring data
								while(true)
								{
									 Arrays.fill(data,(byte)0);
									int read = frr.read(data);
									if(read<=-1)
										break;
									out.write(data,0,read);
								}
								out.flush();
								
								frr.close();
								socket.close();
								notify.post(
										new Runnable()
										{

											@Override
											public void run() {
												// TODO Auto-generated method stub
												 Toast.makeText(MainActivity.this, filename+"檔案傳送完成",
						                                 Toast.LENGTH_SHORT).show();
											}
											
										}
								);
							   
							}catch(Exception e)
							{
								
							}
						}
						 
					 }
				);
			 
			 
			 s.start();
			 
			 
		 }
		 
		 
		 
	 }
	  
	 public void sendImg(final String imgpath)
	  {
		 if(isOwner)
		 {
			 String t = "c`"+imgpath;
			 tq.offer(t);
			 
		 }
		 else
		 {
			 Thread s = new Thread(
				new Runnable()
				{

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try{
						
						
							Socket socket = new Socket();
						   InetAddress serverAddr = null;
						   SocketAddress sc_add = null;
						   serverAddr = GOAddress;
						   sc_add = new InetSocketAddress(serverAddr,1234);
						   socket.connect(sc_add);
						
						   DataInputStream in = new DataInputStream(socket.getInputStream());
							DataOutputStream out = new
									DataOutputStream(socket.getOutputStream());
							byte[] data = new byte[2048];
							
							out.writeBytes("camera");//command to request sendfile
							
							Arrays.fill(data,(byte)0);
							in.read(data);//OK
							
							File file = new File(imgpath);;
						    final String filename = file.getName();
						    
						    out.writeBytes(filename);//filename
						    
						    Arrays.fill(data,(byte)0);
							in.read(data);//OK
							
							InputStream frr = new FileInputStream(file);
							
							//Transferring data
							while(true)
							{
								 Arrays.fill(data,(byte)0);
								int read = frr.read(data);
								if(read<=-1)
									break;
								out.write(data,0,read);
							}
							out.flush();
							
							frr.close();
							socket.close();
							
							
						
						}catch(Exception e)
						{
							
						}
					}
					
				}
			 );
			 
			 s.start();
			 
		 }
	  }
	  
	  @Override
	  public void onConnectionInfoAvailable(final WifiP2pInfo info){
		  // InetAddress from WifiP2pInfo struct.
	        InetAddress groupOwnerAddress = info.groupOwnerAddress;
	        GOAddress = groupOwnerAddress;
	        cur_state++;
	        String status = "m";
	        cur_states[cur_state] = status;
	        changeview();
	        if (info.groupFormed && info.isGroupOwner) 
	        {
	        	isOwner = true;
	        	Thread main = new Thread(server);
	        	main.start();
	        	
	        	
	        }
	        else if(info.groupFormed)
	        {
	        	isOwner = false;
	        	Thread main = new Thread(c1);
	        	main.start();
	        }
		  
		  
		  
	  }
	
	  
	  public void backstep()
	  {
		  if(cur_state>1)
			  cur_state--;
		  
		  
		  
		  changeview();
		  
		  
		  
	  }
}
