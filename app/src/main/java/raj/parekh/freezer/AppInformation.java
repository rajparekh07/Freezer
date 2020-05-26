package raj.parekh.freezer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AppInformation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_information);

        final MyPackage myPackage =(MyPackage) getIntent().getParcelableExtra("App");
        TextView tw1 = (TextView) findViewById(R.id.appName);
        tw1.setText(myPackage.getApplicationName());
        tw1 = (TextView) findViewById(R.id.packageName);
        tw1.setText(myPackage.getPackageName());
        try{
        ((ImageView)findViewById(R.id.icon)).setImageDrawable(getPackageManager().getApplicationIcon(myPackage.getPackageName()));
        }catch (Exception e){
        }
        tw1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showInstalledAppDetails(myPackage.getPackageName());
                return false;
            }
        });
    }

    public void showInstalledAppDetails(String packageName) {
        final int apiLevel = Build.VERSION.SDK_INT;
        Intent intent = new Intent();

        if (apiLevel >= 9) {
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
        } else {
            final String appPkgName = (apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName");

            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra(appPkgName, packageName);
        }

        // Start Activity
        startActivity(intent);
    }


}
