

public class MainActivity extends AppCompatActivity
        implements
                    AdapterView.OnItemSelectedListener,
                    NavigationView.OnNavigationItemSelectedListener,
                    AppBarLayout.OnOffsetChangedListener
{

    public static final String LAST_SEEN_LAYOUT = "LAYOUT_ID";
    public static final String LAST_SEEN_LAYOUT_NAME = "LAYOUT_NAME";

    View curVisibleLayout;
    View prevVisibleLayout;

    NavigationView leftMenu;
    BottomSheetDialogFragment myBottomSheet;
    static WebView navigationWebView;
    private ArrayList<String> groups_sem_list = new ArrayList<String>();
    ArrayAdapter<String> adapter_groups_sem;
    AutoCompleteTextView textView_groupsList;
    public static EditText textbox_group_sem;
    private Map<String,String> groups_names_to_requests =  new HashMap<String,String>();
    private Map<String,String> teachers_names_to_requests =  new HashMap<String,String>();
    ArrayList<TabLayout.Tab> tabs_raspSem;
    RaspSemPagerAdapter adapter_raspSem;
    SwitchCompat raspSwitch;
    AutoCompleteTextView input_RaspSem_Group;
    boolean _flag_groupsSemAdded = FALSE;
    String lastSucessGroupName = "";
    private ArrayList<String> groups_exam_list = new ArrayList<String>();
    ArrayAdapter<String> adapter_groups_exam;
    AutoCompleteTextView textView_groupsList_exam;
    public static EditText textbox_group_exam;
    private Map<String,String> groups_names_to_requests_exam =  new HashMap<String,String>();
    private Map<String,String> teachers_names_to_requests_exam =  new HashMap<String,String>();
    Activity_rasp_sess array_allExams;
    AutoCompleteTextView input_RaspExam_Group;
    boolean _flag_groupsExamAdded = FALSE;

    ArrayList<TabLayout.Tab> tabs_raspExam;
    RaspExamPagerAdapter adapter_raspExam;
    private DB_FacultiesInfo mDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;
    private DB_RaspSem mDatabaseHelper_rasp;
    private SQLiteDatabase mSqLiteDatabase_rasp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textbox_group_sem = (EditText) findViewById(R.id.textView_groups_sem);
        textbox_group_exam = (EditText) findViewById(R.id.textView_groups_exam);
        leftMenu = (NavigationView)findViewById(R.id.nav_view);
        leftMenu.setCheckedItem(R.id.nav_main);
        curVisibleLayout =  findViewById(R.id.window_main);
        input_RaspSem_Group = (AutoCompleteTextView)findViewById(R.id.textView_groups_sem);
        raspSwitch = (SwitchCompat)findViewById(R.id.switch_raspSem_week);
        input_RaspExam_Group = (AutoCompleteTextView)findViewById(R.id.textView_groups_exam);
        lv_past_events_titles = (ListView) findViewById(R.id.listView_events);
        adapter_suai_events = new SUAI_news_titles_adapter(this, this, newsTitles_past_events);
        lv_past_events_titles.setAdapter(adapter_suai_events);
        
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		navigationWebView = (WebView) findViewById(R.id.webView_navigation);
		navigationWebView.setHorizontalScrollBarEnabled(false);
		WebSettings webSettings = navigationWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			webSettings.setAllowFileAccessFromFileURLs(true);
			webSettings.setAllowUniversalAccessFromFileURLs(true);
		}

        class JsObject {
            @JavascriptInterface
            public void onSendMessage(final String inMessage) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, inMessage, Toast.LENGTH_LONG).show();
                    }
                });

            }
        }
        class OpenResult
        {
            @JavascriptInterface
            public void onSendMessage(final String inMessage) {

                Bundle paramsBundl = new Bundle();
				
				
				////-----------
				////FILL BUNDLE
				////-----------
				
				
                PairsDetailsResultOK newFragment = new PairsDetailsResultOK();
                newFragment.setArguments(paramsBundl);

                android.support.v4.app.FragmentManager manager = mainActivity.getSupportFragmentManager();

                newFragment.show(manager,"");

            }
        }
        navigationWebView.addJavascriptInterface(new JsObject(),"android");
        navigationWebView.addJavascriptInterface(new OpenResult(),"androidShowAud");
        navigationWebView.loadUrl("file:///android_asset/index.html");
        navigationWebView.loadUrl("javascript:MainLoadFunc();");

        navigationWebView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                navigationWebView.loadUrl("javascript:MainLoadFunc();");
            }
        });


        InitInfineon();
		bindActivity();
		menu_elements_fill();
		menu_navigation_attach();
		menu_rasp_sem();
		menu_info_fill();
		menu_settings();
		departs_fill();

		ConnectToDB(1);
		DisconnectFromDB(1);
		ConnectToDB(2);
		DisconnectFromDB(2);

		CheckInternetConnection foo = new CheckInternetConnection();
		Thread tmpThread = new Thread(foo);
		tmpThread.start();
		try {
			tmpThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		boolean value = foo.getStatus();
		if (value) {
			new Task_SUAI_parse_full_rasp_sem().execute();
			new Task_SUAI_parse_news().execute();
			new Task_SUAI_parse_full_rasp_exam().execute();
		}

        ShowAppIntro();
    }

    List<BluetoothDevice> pairedDevices;
    SensorHub mSensorHub;
    private void InitInfineon() {

        pairedDevices = new ArrayList<>();
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> allPairedDevices = mBtAdapter.getBondedDevices();
        for(BluetoothDevice d : allPairedDevices){
            if(isIfxDevice(d)) pairedDevices.add(d);
        }
        mSensorHub = new SensorHub(this, pairedDevices.get(0).getAddress());

        mSensorHub.addSensorHubListener(new SensorHubListener() {
            @Override
            public void onConnected(SensorHub s) {
                Log.d("INFINEON", "Connected");
                mSensorHub.start();
            }
            @Override
            public void onDisconnected(SensorHub s) {
                Log.d("INFINEON", "Disconnected");
            }
            @Override
            public void onConnectionError(SensorHub s) {
                Log.d("INFINEON", "Connection Error");
            }
            @Override
            public void onSensorDataReceived(SensorHub s, SensorEvent e) {
                Log.d("INFINEON",e.getTimestamp() + ":" + e.getDataId() + ":" +
                        e.getSensorValue());

                if(e.getDataId().equals("p")) {
                    // Pressure value
                    double presValue = e.getSensorValue();

                    TextView infineonValueBox = (TextView) findViewById(R.id.textView_infineonPressure);
                    infineonValueBox.setText(String.valueOf(presValue));
					
					navigationWebView.loadUrl("javascript:SetPressure(" + String.valueOf(presValue) + ");");
					
                    String id = e.getSensorId();
                    long t = e.getTimestamp();
                }

            }
            @Override
            public void onModeChanged(SensorHub s, Mode m) {
                Log.d("INFINEON", m.getSensorId() + " : " + m.getModeId() + " : " +
                        m.getValue());
            }
        });
        mSensorHub.connect();
        mSensorHub.start();
        if(mSensorHub.getSensorList().size() == 0){
            Log.d("INFINEON", "Error: no sensor detected");
            return;
        }
        Sensor sensor = mSensorHub.getSensorList().get(0);

    }
    private boolean isIfxDevice(BluetoothDevice device) {
        String deviceName = device.getName().toLowerCase();
        if(deviceName.equals("ifx_nanohub") ||
                deviceName.equals("ifx_senhub"))
            return true;
        return false;
    }
    ///------------------------------
    //END OF INFINEON
    ///-------------------------------

    private void menu_settings() {

        Spinner spinner = (Spinner) findViewById(R.id.spinner_userType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.users_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.spinner_userType)
        {
            switch (pos)
            {
                case 0:
                {
                    Class fragmentClass = FragmentSettings_groups.class;
                    Fragment fragment = null;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((FragmentSettings_groups)fragment).SetDataSet(groups_names_to_requests.keySet());
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_settings, fragment).commit();
                    break;
                }
                case 1:
                {
                    Class fragmentClass = FragmentSettings_teacher.class;
                    Fragment fragment = null;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((FragmentSettings_teacher)fragment).SetDataSet(teachers_names_to_requests.keySet());
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_settings, fragment).commit();
                    break;
                }
                case 2:
                {
                    Class fragmentClass = FragmentSettings_others.class;
                    Fragment fragment = null;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_settings, fragment).commit();
                    break;
                }
                case 3:
                {
                    Class fragmentClass = FragmentSettings_others.class;
                    Fragment fragment = null;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_settings, fragment).commit();
                    break;
                }
            }

        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    // Another interface callback
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = sharedPref.getString(GROUP_NAME, "");

        if ((defaultValue.length()==0)&&(input_RaspSem_Group!=null))
            defaultValue = input_RaspSem_Group.getText().toString();
        if (defaultValue.length()==0)
            defaultValue = lastSucessGroupName;

        outState.putString(GROUP_NAME, defaultValue);
        outState.putInt(LAST_SEEN_LAYOUT, curVisibleLayout.getId());
        outState.putString(LAST_SEEN_LAYOUT_NAME, this.getTitle().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void ShowAppIntro() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                boolean isFirstStart = getPrefs.getBoolean(FIRST_START, true);

                if (isFirstStart) {
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);

                    SharedPreferences.Editor e = getPrefs.edit();
                    e.putBoolean(FIRST_START, false);
                    e.apply();
                } else
                {
                    int curCodeVersion = GetVersionCode();
                    int savedCodeVersion = getPrefs.getInt(SAVED_CODE_VERSION, 0);

                    if (savedCodeVersion != curCodeVersion)
                    {
                        Intent i = new Intent(MainActivity.this, ChangesActivity.class);
                        startActivity(i);

                        SharedPreferences.Editor e = getPrefs.edit();
                        e.putInt(SAVED_CODE_VERSION, curCodeVersion);
                        e.apply();
                    }
                }
            }
        });
        t.start();
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }

    public void on_startActivity_Click(View v) {

    }



    public class Task_SUAI_parse_news extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... arg) {

            CheckInternetConnection foo = new CheckInternetConnection();
            Thread tmpThread = new Thread(foo);
            tmpThread.start();
            try {
                tmpThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean value = foo.getStatus();
            if (!value) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "No internet..", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }

            Document doc;
            boolean firstNewsAdded = FALSE;
            try {
                doc = Jsoup.connect("http://new.guap.ru/pubs/?page=1").get();

                if (doc == null)  {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "No internet..", Toast.LENGTH_LONG).show();
                        }
                    });
                    return null;
                }

                titles = doc.select(".item-news");

                if (titles == null)  {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "No internet", Toast.LENGTH_LONG).show();
                        }
                    });
                    return null;
                }
                newsTitles_past_events.clear();
                for (Element title : titles) {
                    Bitmap eventIcon = null;
                    Elements small_info = title.select("h3");
                    String link = title.select("a").attr("href");
                    String date = small_info.select("span").first().text();
                    small_info.select("span").first().remove();
                    Elements small_icon = title.select("img");
                    String icon_src = small_icon.attr("src");
                    icon_src = icon_src.substring(0,icon_src.indexOf("?")) + "?s=sm";

                    try {
                        InputStream in = new java.net.URL(icon_src).openStream();
                        eventIcon = BitmapFactory.decodeStream(in);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
					
					////------------
					////PARSE
					////------------

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public class CheckInternetConnection implements Runnable {
        private volatile boolean value = FALSE;

        @Override
        public void run() {
            InetAddress ipAddr = null;
            try {
                ipAddr = InetAddress.getByName(getString(R.string.test_internet_page));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (ipAddr != null)
                value =  !ipAddr.equals("");
        }

        public boolean getStatus() {
            return value;
        }
    }

    private void ConnectToDB(int db_type) {

        if (db_type==1) {
            mDatabaseHelper = new DB_FacultiesInfo(this, DB_FacultiesInfo.DATABASE_NAME, null, DB_FacultiesInfo.DATABASE_VERSION);
            mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
            mDatabaseHelper.FillDataListsFromDB(mSqLiteDatabase);
        }

        if (db_type==2) {
            mDatabaseHelper_rasp = new DB_RaspSem(this, DB_RaspSem.DATABASE_NAME, null, DB_RaspSem.DATABASE_VERSION);
            mSqLiteDatabase_rasp = mDatabaseHelper_rasp.getWritableDatabase();
            InitRaspFromDB();
        }

        if (db_type==3) {
            mDatabaseHelper_rasp = new DB_RaspSem(this, DB_RaspSem.DATABASE_NAME, null, DB_RaspSem.DATABASE_VERSION);
            mSqLiteDatabase_rasp = mDatabaseHelper_rasp.getWritableDatabase();
        }
    }

    private void DisconnectFromDB(int db_type) {

        if (db_type==1) {
            mSqLiteDatabase.close();
            mDatabaseHelper.close();
        }

        if (db_type==2) {
            mSqLiteDatabase_rasp.close();
            mDatabaseHelper_rasp.close();
        }
    }

    private void InitRaspFromDB(){
        mDatabaseHelper_rasp.FillDataListsFromDB(mSqLiteDatabase_rasp);

        for (int i=0;i<mDatabaseHelper_rasp.allPairs.size();++i){

            DB_RaspSem.DB_RaspSem_Pair tmpPair = mDatabaseHelper_rasp.allPairs.get(i);
            adapter_raspSem.AddPair(tmpPair.dayNumber,tmpPair.number,tmpPair.name,tmpPair.position,
                    tmpPair.teacher, tmpPair.type, tmpPair.curGroup, tmpPair.allGroups, tmpPair.classWeekType);
        }

        if (raspSwitch != null) {
            if (raspSwitch.isChecked())
                adapter_raspSem.FilterPairs("2");
            else
                adapter_raspSem.FilterPairs("1");
        }
        else
        {
            adapter_raspSem.FilterPairs("2");
        }
        adapter_raspSem.notifyDataSetChanged();
    }

    public void UpdateRaspSemTabFlags(int i, boolean flag_havePairs){

            if (flag_havePairs) {
                View tmpView = tabs_raspSem.get(i).getCustomView();
                tmpView.findViewById(R.id.radio_dayHavePairs).setVisibility(View.VISIBLE);
            }
            else
            {
                View tmpView = tabs_raspSem.get(i).getCustomView();
                tmpView.findViewById(R.id.radio_dayHavePairs).setVisibility(View.GONE);
            }
    }

    public void ShowAuditory(String inName)
    {
        navigationWebView.loadUrl("javascript:ClearLastResult();SetNewAudFromAndroid(false, '" + inName + "')");
        navigationWebView.loadUrl("javascript:RoomFinder(false)");

        this.setTitle("Navigation");

        prevVisibleLayout = curVisibleLayout;
        curVisibleLayout. setVisibility(android.view.View.GONE);
        curVisibleLayout = findViewById(R.id.window_navigation);
        curVisibleLayout.setVisibility(android.view.View.VISIBLE);

        leftMenu.setCheckedItem(R.id.nav_navigation);
    }
}