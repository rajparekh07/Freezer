package raj.parekh.freezer;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Home extends AppCompatActivity {
    SparseArray<AppGroup> appGroup = new SparseArray<AppGroup>();
    ExpandableListView listView;
    FloatingActionButton fab;
    final int groupNames[] ={R.string.group0,R.string.group1,R.string.group2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, raj.parekh.freezer.Home.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Snackbar.make(getWindow().getDecorView().getRootView(), "Building the list!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        listView = (ExpandableListView) findViewById(R.id.listView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        doIt();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else {
            doIt();
        }

        return super.onOptionsItemSelected(item);
    }
    public void doIt(){
        View mView = this.findViewById(R.id.listView);
        BuildList bl = new BuildList(this);
        for(int i=0;i<3;i++){
            bl = new BuildList(this);
            bl.execute(i);
            appGroup.append(i,new AppGroup(getResources().getString(groupNames[i])+" ",i,bl.getFinList()));
        }
        MyExpandableListAdapter myExpandableListAdapter = new MyExpandableListAdapter(this, appGroup);
        listView.setAdapter(myExpandableListAdapter);
    }
}
class BuildList extends AsyncTask<Integer,Void,List<MyPackage>>{
    int mode;
    ProgressDialog pg;
    List<PackageInfo> packList;
    List<MyPackage> finList = new ArrayList<MyPackage>();
    Context callingContext;
    boolean sysPack,disPack,normPack;
    Home callingActivity;
    PackageManager pm;
    BuildList(Context context){
        callingContext = context;
        pm = callingContext.getPackageManager();
        callingActivity = (Home) context;
    }
    @Override
    public void onPreExecute(){
       // pg.get().animate().alpha(1f).setDuration(500);
        pg = new ProgressDialog(callingContext);

        pg.setMessage("Building");
        pg.show();
    }
    @Override
    protected List<MyPackage> doInBackground(Integer... params){
        mode = params[0];

        switch(mode){
            case 0:
                sysPack = false;
                disPack = false;
                normPack = true;
                break;
            case 1:
                sysPack = true;
                disPack = false;
                normPack = false;
                break;
            case 2:
                sysPack = false;
                disPack = true;
                normPack = false;
                break;
        }
        packList = pm.getInstalledPackages(0);
        for (int i=0; i < packList.size(); i++) {
            PackageInfo packInfo = packList.get(i);
            boolean temp1 = isSystemPackage(packInfo), temp2 = isPackageDisabled(packInfo), temp3 = isNormPack(packInfo);
            if(sysPack && temp1 ||disPack && temp2|| normPack && temp3){
                String appName = packInfo.applicationInfo.loadLabel(pm).toString();
                finList.add(new MyPackage(appName,packInfo.packageName,temp3,temp1,temp2));
            }
        }
        return finList;
    }
    private boolean isNormPack(PackageInfo pkgInfo){
        return !isSystemPackage(pkgInfo)&&!isPackageDisabled(pkgInfo);
    }
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
    private boolean isPackageDisabled(PackageInfo packageInfo){
        return pm.getApplicationEnabledSetting(packageInfo.packageName) ==
                (pm.COMPONENT_ENABLED_STATE_DISABLED);
    }
    @Override
    public void onPostExecute(List<MyPackage> result){
        pg.dismiss();
        pg.hide();
        finList = result;
        Collections.sort(finList, new Comparator<MyPackage>() {
            @Override
            public int compare(MyPackage lhs, MyPackage rhs) {
                return lhs.compareTo(rhs);
            }
        });
    }
    public List<MyPackage> getFinList(){

        return finList;
    }
}
class AppGroup {
    String head;
    int id;
    List<MyPackage> appList;
    AppGroup(String head, int id,List<MyPackage> apps){
        appList = apps;
        this.head = head;
        this.id = id;}
}
class MyExpandableListAdapter extends BaseExpandableListAdapter{
    private final SparseArray<AppGroup> groups;
    public LayoutInflater inflater;
    public Activity activity;
    public MyExpandableListAdapter(Activity act, SparseArray<AppGroup> groups) {
        activity =(Activity) act;
        this.groups = groups;
        inflater = act.getLayoutInflater();
    }
    @Override
    public Object getChild(int groupPosition, int childPosition){
        return groups.get(groupPosition).appList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition+childPosition;
    }
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
        final MyPackage child = (MyPackage) getChild(groupPosition,childPosition);
        if(convertView == null){
           convertView = inflater.inflate(R.layout.listview_test, null);
        }
        TextView tw = (TextView) convertView.findViewById(R.id.label);
        TextView tw2 = (TextView) convertView.findViewById(R.id.label2);
        ImageView iw = (ImageView) convertView.findViewById(R.id.icon);
        final PackageManager pm = activity.getPackageManager();
        final View view = convertView;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                final AppGroup callingGroup = groups.get(groupPosition);
                final AppGroup disabledGroup = groups.get(2);
                v.animate().alpha(0f).setDuration(200).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (callingGroup != disabledGroup) {
                            callingGroup.appList.remove(child);
                            disabledGroup.appList.add(child);
                            child.setDis(true);

                        }
                        notifyDataSetChanged();
                        view.setAlpha(1);
                    }
                });

                    String abc = "";
                    Toast ts = Toast.makeText(activity, abc, Toast.LENGTH_SHORT);
                    try {
                        ts.setText("App Disabled");
                    } catch (Exception e) {
                        ts.setText("Failed");
                    } finally {
                        ts.show();
                        ts.getView().animate().alpha(1).setDuration(501);
                    }*/
                Intent i = new Intent(v.getContext(),AppInformation.class);
                i.putExtra("App",child);
                v.getContext().startActivity(i);
            }
        });
        try {
            tw.setText(child.getApplicationName());
            tw2.setText(child.getPackageName());
            iw.setImageDrawable(pm.getApplicationIcon(child.getPackageName()));
            tw.setTextColor(Color.BLUE);
        }catch(Exception e){
        }
        return convertView;
    }
    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).appList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listhead_layout, null);
        }
        AppGroup group = (AppGroup) getGroup(groupPosition);
        TextView tw = (TextView) convertView.findViewById(R.id.label_head);
        tw.setText(group.head);
        return convertView;
    }


}
class MyPackage implements Comparable<MyPackage>,Parcelable{
    String packageName;
    String applicationName;
    boolean isSys,isDis,isNorm;
    int prev;
    MyPackage(String a, String b,boolean c,boolean d,boolean e) {
        packageName = b;
        applicationName = a;
        isNorm = c;
        isSys = d;
        isDis = e;
    }
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flag){
        dest.writeString(packageName);
        dest.writeString(applicationName);
        dest.writeBooleanArray(new boolean[]{isSys,isDis,isNorm});
        dest.writeInt(prev);
    }
    private MyPackage(Parcel in){
        packageName = in.readString();
        applicationName = in.readString();
        boolean[] barr = new boolean[3];
        in.readBooleanArray(barr);
        isSys = barr[0];
        isDis = barr[1];
        isNorm = barr[2];
        prev = in.readInt();

    }
    public static final Parcelable.Creator<MyPackage> CREATOR = new Parcelable.Creator<MyPackage>() {
        public MyPackage createFromParcel(Parcel source) {
            return new MyPackage(source);
        }

        @Override
        public MyPackage[] newArray(int size) {
            return new MyPackage[size];
        }
    };
    public String getPackageName(){
        return packageName;
    }
    public String getApplicationName(){
        return applicationName;
    }
    public void setDis(boolean a){
        isDis = a;
        if(!isDis){
            if(prev == 555){
                setNorm(true);
            }else
            {
                setSys(true);
            }
            prev = 666;
            return;
        }
        if(isNorm){
            prev = 555;
        }else
        {
            prev = 444;
        }
        isSys = isNorm = false;
    }
    public void setSys(boolean a){isSys = a;}
    public void setNorm(boolean a){isNorm = a;}
    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o == this) return true;
        MyPackage mp = (MyPackage) o;
        return mp.getPackageName()==getPackageName()&mp.getApplicationName()==getApplicationName();
    }
    @Override
    public int compareTo(MyPackage myPackage){
        return applicationName.compareTo(myPackage.applicationName);
    }
}