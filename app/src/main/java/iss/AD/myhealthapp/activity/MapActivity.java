package iss.AD.myhealthapp.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import iss.AD.myhealthapp.R;

public class MapActivity extends AppCompatActivity {

    private WebView webView;
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        webView = findViewById(R.id.mapWebView);

        // 启用 JavaScript（如果你的地图网页需要）
        webView.getSettings().setJavaScriptEnabled(true);

        // 获取从 PublishActivity 传递过来的经纬度
        double latitude = getIntent().getDoubleExtra("latitude", -34);
        double longitude = getIntent().getDoubleExtra("longitude", 151);

        // 构建显示地图的 URL
        String mapUrl = "https://www.google.com/maps?q=" + latitude + "," + longitude + "&z=15"; // 示例 URL
        webView.loadUrl(mapUrl); // 加载 URL
    }
}
