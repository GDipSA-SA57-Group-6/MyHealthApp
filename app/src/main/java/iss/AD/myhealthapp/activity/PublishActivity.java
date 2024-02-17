package iss.AD.myhealthapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentPoi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import iss.AD.myhealthapp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishActivity extends AppCompatActivity implements View.OnClickListener, DatePicker.OnDateChangedListener, TencentLocationListener {
    private Context context;
    private TextView tvDate;
    private int year, month, day;
    //在TextView上显示的字符
    private StringBuffer date;

    CircleMenu circleMenu;
    ConstraintLayout constraintLayout;

    //单次定位
    private Button btnSinglePositioning;
    //定位信息显示
    private TextView tvLocationInfo;
    //定位管理
    private TencentLocationManager mLocationManager;
    //定位请求
    private TencentLocationRequest locationRequest;
    // 危险定位权限
    private RxPermissions rxPermissions;
    // 位置打印
    public static final String TAG = "PublishActivity";


    // 提交事件处理
    private static final String BASE_URL = "http://192.168.18.35:8080/api/group-hub/";
    private OkHttpClient client = new OkHttpClient();
    private Double groupHubLongitude, groupHubLatitude;


    // 向Host Activity发送信息
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        context = this;
        date = new StringBuffer();

        initView();
        // 现在权限请求对象已经被初始化
        checkVersion();
    }

    private void initView() {
        tvDate = (TextView) findViewById(R.id.textViewGroupHubDDL);

        circleMenu = (CircleMenu) findViewById(R.id.circle_menu);

        constraintLayout = findViewById(R.id.publishActivityLayout);

        tvDate.setOnClickListener(this::onClick);

        // Map demo
        btnSinglePositioning = findViewById(R.id.btn_single_positioning);
        btnSinglePositioning.setOnClickListener(this);
        tvLocationInfo = findViewById(R.id.tv_location_info);
        // 初始化定位信息
        //获取TencentLocationManager实例
        mLocationManager = TencentLocationManager.getInstance(this);
        //获取定位请求TencentLocationRequest 实例
        locationRequest = TencentLocationRequest.create();
        //设置定位时间间隔，1s
        locationRequest.setInterval(1000);
        //位置信息的详细程度 REQUEST_LEVEL_ADMIN_AREA表示获取经纬度，位置所处的中国大陆行政区划
        locationRequest.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);
        //是否允许使用GPS
        locationRequest.setAllowGPS(true);
        //是否需要获取传感器方向，提高室内定位的精确度
        locationRequest.setAllowDirection(true);
        //是否需要开启室内定位
        locationRequest.setIndoorLocationMode(true);


        // 旋转按钮
        circleMenu.setMainMenu(Color.parseColor("#EFF396"), R.mipmap.confirm, R.mipmap.cancel)
                .addSubMenu(Color.parseColor("#F8E559"), R.mipmap.submit)
                .addSubMenu(Color.parseColor("#FFFC9B"), R.mipmap.map)
                .setOnMenuSelectedListener(new OnMenuSelectedListener() {
                    @Override
                    public void onMenuSelected(int index) {
                        switch (index) {
                            case 1:
                                Intent mapIntent = new Intent(PublishActivity.this, MapActivity.class);
                                mapIntent.putExtra("latitude", groupHubLatitude);
                                mapIntent.putExtra("longitude", groupHubLongitude);
                                startActivity(mapIntent);
                                break;
                            case 0:
//                                Toast.makeText(PublishActivity.this, "Submit", Toast.LENGTH_SHORT).show();
                                constraintLayout.setBackgroundColor(Color.parseColor("#F8EDFF"));

                                // 获取事件名字
                                EditText editTextName = findViewById(R.id.groupHubName);
                                String name = editTextName.getText().toString();

                                // 获取事件数量
                                EditText editTextNumber = findViewById(R.id.groupHubQuantity);
                                int quantity =  Integer.parseInt(editTextNumber.getText().toString());

                                // 获取截止日期
                                String dateString = tvDate.getText().toString();
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy年MM月dd日");
                                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                String date = new String();
                                try {
                                    Date dateValue = inputFormat.parse(dateString);
                                    date = outputFormat.format(dateValue);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                // 事件提交逻辑
                                createGroupHub(1, name, quantity, groupHubLongitude, groupHubLatitude, date);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(2000); // 1000 毫秒即 1 秒
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        Intent intent = new Intent(PublishActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }).start();
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            ArrayList<GroupHub> groupHubList = NetworkUtils.fetchAllGroupHub(); // 通过网络获取到所有的对象
//
//                                        } catch (Exception e) {
//                                            Log.e(TAG, "Error fetching data", e);
//                                        }
//                                    }
//                                }).start();
                                break;
                        }
                    }
                });
    }

    private void initDateTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 点击事件处理
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.textViewGroupHubDDL) {
            // 处理 ll_date 被点击的逻辑
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setPositiveButton("Set up", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (date.length() > 0) { //清除上次记录的日期
                        date.delete(0, date.length());
                    }
                    tvDate.setText(date.append(String.valueOf(year)).append("年").append(String.valueOf(month)).append("月").append(day).append("日"));
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog = builder.create();
            View dialogView = View.inflate(context, R.layout.dialog_date, null);
            final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);

            dialog.setTitle("set up date");
            dialog.setView(dialogView);
            dialog.show();
            //初始化日期监听事件
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                datePicker.init(LocalDate.now().getYear(),LocalDate.now().getMonth().getValue() , LocalDate.now().getDayOfYear(), this);
            }
        }

        if(v.getId() == R.id.btn_single_positioning) {
            showMsg("监测到按钮点击");
//            mLocationManager.requestLocationUpdates(locationRequest, this);
            mLocationManager.requestSingleFreshLocation(locationRequest,this, Looper.getMainLooper());
//            mLocationManager.requestSingleFreshLocation(null,this, Looper.getMainLooper());

        }
    }

    /**
     * 日期组件
     * @param view The view associated with this listener.
     * @param year The year that was set.
     * @param monthOfYear The month that was set (0-11) for compatibility
     *            with {@link Calendar}.
     * @param dayOfMonth The day of the month that was set.
     */
    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear+1;
        this.day = dayOfMonth;
    }

    /**
     * 腾讯地图组件
     * @param tencentLocation
     * @param i
     * @param s
     */
    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        showLocationInfo(tencentLocation);
    }

    /**
     * 腾讯地图组件
     * @param s
     * @param i
     * @param s1
     */
    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        Log.d(TAG, "name：" + s + " desc：" + statusUpdate(s, i));
    }

    /**
     * 显示定位信息
     *
     * @param location
     */
    private void showLocationInfo(TencentLocation location) {
        //经度
        double longitude = location.getLongitude();

        this.groupHubLongitude = longitude;

        //纬度
        double latitude = location.getLatitude();

        this.groupHubLatitude = latitude;

        //准确性
        float accuracy = location.getAccuracy();
        //地址信息
        String address = location.getAddress();
        //海拔高度
        double altitude = location.getAltitude();
        //面积统计
        Integer areaStat = location.getAreaStat();
        //方向
        float bearing = location.getBearing();
        double direction = location.getDirection();
        //城市
        String city = location.getCity();
        //城市代码
        String cityCode = location.getCityCode();
        //城市电话代码
        String cityPhoneCode = location.getCityPhoneCode();
        //坐标类型
        int coordinateType = location.getCoordinateType();
        //区
        String district = location.getDistrict();
        //经过时间
        long elapsedRealtime = location.getElapsedRealtime();
        //Gps信息
        int gpsRssi = location.getGPSRssi();
        //室内建筑
        String indoorBuildingFloor = location.getIndoorBuildingFloor();
        //室内建筑编码
        String indoorBuildingId = location.getIndoorBuildingId();
        //室内位置类型
        int indoorLocationType = location.getIndoorLocationType();
        //名称
        String name = location.getName();
        //国家
        String nation = location.getNation();
        //周边poi信息列表
        List<TencentPoi> poiList = location.getPoiList();
        //提供者
        String provider = location.getProvider();
        //省
        String province = location.getProvince();
        //速度
        float speed = location.getSpeed();
        //街道
        String street = location.getStreet();
        //街道编号
        String streetNo = location.getStreetNo();
        //时间
        long time = location.getTime();
        //镇
        String town = location.getTown();
        //村
        String village = location.getVillage();

        StringBuffer buffer = new StringBuffer();
        buffer.append("Longitude：" + longitude + "\n");
        buffer.append("Latitude：" + latitude + "\n");
//        buffer.append("国家：" + nation + "\n");
//        buffer.append("省：" + province + "\n");
//        buffer.append("市：" + city + "\n");
//        buffer.append("县/区：" + district + "\n");
//        buffer.append("街道：" + street + "\n");
//        buffer.append("名称：" + name + "\n");
//        buffer.append("提供者：" + provider + "\n");
//        buffer.append("详细地址：" + address + "\n");
        tvLocationInfo.setText(buffer.toString());
        Log.d(TAG, buffer.toString());
    }

    /**
     * 检查Android版本
     */
    private void checkVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0或6.0以上
            //动态权限申请
            permissionsRequest();
        } else {
            showMsg("您不需要动态获得权限，可以直接定位");
        }
    }

    /**
     * Toast提示
     *
     * @param msg 内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 动态权限申请
     */
    @SuppressLint("CheckResult")
    private void permissionsRequest() {//使用这个框架使用了Lambda表达式，设置JDK版本为 1.8或者更高
        rxPermissions = new RxPermissions(this);
        rxPermissions.request(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {//申请成功
                        //发起连续定位请求
                        showMsg("您已获得权限，可以定位了");
                    } else {//申请失败
                        showMsg("权限未开启");
                    }
                });
    }

    /**
     * 定位状态判断
     *
     * @param name   GPS、WiFi、Cell
     * @param status 状态码
     * @return
     */
    private String statusUpdate(String name, int status) {
        if ("gps".equals(name)) {
            switch (status) {
                case 0:
                    return "GPS开关关闭";
                case 1:
                    return "GPS开关打开";
                case 3:
                    return "GPS可用，代表GPS开关打开，且搜星定位成功";
                case 4:
                    return "GPS不可用";
                default:
                    return "";
            }
        } else if ("wifi".equals(name)) {
            switch (status) {
                case 0:
                    return "Wi-Fi开关关闭";
                case 1:
                    return "Wi-Fi开关打开";
                case 2:
                    return "权限被禁止，禁用当前应用的 ACCESS_COARSE_LOCATION 等定位权限";
                case 5:
                    return "位置信息开关关闭，在android M系统中，此时禁止进行Wi-Fi扫描";
                default:
                    return "";
            }
        } else if ("cell".equals(name)) {
            switch (status) {
                case 0:
                    return "cell 模块关闭";
                case 1:
                    return "cell 模块开启";
                case 2:
                    return "定位权限被禁止，位置权限被拒绝通常发生在禁用当前应用的 ACCESS_COARSE_LOCATION 等定位权限";
                default:
                    return "";
            }
        }
        return "";
    }

    /**
     * 发送 创建的请求
     * @param userId
     * @param name
     * @param quantity
     * @param latitude
     * @param longitude
     * @param endTime
     */
    public void createGroupHub(int userId, String name, int quantity, double latitude, double longitude, String endTime) {
        // 构建请求体
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("quantity", quantity);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("endTime", endTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        // 现在默认为1 之后从preference里面取
        Request request = new Request.Builder()
                .url(BASE_URL + "create?userId=1")
                .post(body)
                .build();

        // 发送请求并处理响应
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 处理请求失败
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // 处理成功响应
                    System.out.println("Response: " + responseBody);
                } else {
                    // 处理错误响应
                    System.out.println("Error: " + response.code() + " " + response.message());
                }
            }
        });


    }
}