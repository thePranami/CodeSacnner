package com.webrication.qr.code.reader.generator.scanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanResultActivity extends AppCompatActivity {

    String qr_latitude="";
    String qr_longitude="";
    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
    LinearLayout linearLayout;
    Context context;
    ImageView back;
    String result="";
    String Email="";
    String Name="";
    String Tel="";
    String Url="";
    String loc="";
    String start_day="";
    String end_day="";
    Date start_date,end_date;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager manager;
    RecyclerAdapter adapter;

    ArrayList<String> list=new ArrayList<>();

    private static final int REQUEST_APP_SETTINGS = 168;
    private static final int REQUEST_CALENDAR = 112;
    private static final int REQUEST_CONTACTS = 120;
    private static final int REQUEST_CALL = 121;

    private static final String[] requiredPermissionscall = new String[]{
            Manifest.permission.CALL_PHONE
    };
    private static final String[] requiredPermissionscontacts = new String[]{
            Manifest.permission.WRITE_CONTACTS
    };
    private static final String[] requiredPermissionscalendar = new String[]{
            Manifest.permission.WRITE_CALENDAR
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sacn_result);
        linearLayout=(LinearLayout)findViewById(R.id.details);
        back=(ImageView)findViewById(R.id.back);
        recyclerView=(RecyclerView)findViewById(R.id.recycler);
        manager= new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);

        context=ScanResultActivity.this;

        if (getIntent()!=null)
        {
            Intent intent=getIntent();
            result=intent.getStringExtra("result");
            String formate=intent.getStringExtra("formate");
        }

        Log.d("vcart",result.toString());
        Log.d("length", String.valueOf(result.length()));

        if (result!=null&&!result.isEmpty())
        {
            VcartExteract(result.toString());
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScanResultActivity.super.onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        adapter.SetOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

               TextView textView=(TextView)view.findViewById(R.id.textView);

               String text=textView.getText().toString();

               if (text.equalsIgnoreCase("share"))
               {
                   if (result!=null&&!result.isEmpty()) {

                       Intent i = new Intent(android.content.Intent.ACTION_SEND);
                       i.setType("text/plain");
                       i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject test");
                       i.putExtra(android.content.Intent.EXTRA_TEXT, result);
                       startActivity(Intent.createChooser(i, "Share via"));
                   }
               }
               else if (text.equalsIgnoreCase("contact"))
               {

                   if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissionscontacts))
                   {
                       ActivityCompat.requestPermissions(ScanResultActivity.this,
                               requiredPermissionscontacts, REQUEST_CONTACTS);
                   }
                   else
                   {
                       addContact();
                   }

               }
               else if (text.equalsIgnoreCase("Email"))
               {
                   Intent i = new Intent(Intent.ACTION_SEND);
                   i.setType("message/rfc822");
                   i.putExtra(Intent.EXTRA_EMAIL  , new String[]{Email});
                   Log.e("emailsend",Email);

//                i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
//                i.putExtra(Intent.EXTRA_TEXT   , "body of email");
                   try {
                       startActivity(Intent.createChooser(i, "Send mail..."));
                   } catch (android.content.ActivityNotFoundException ex) {
                       Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                   }
               }
               else if (text.equalsIgnoreCase("call"))
               {
                   if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissionscall))
                   {
                       ActivityCompat.requestPermissions(ScanResultActivity.this,
                               requiredPermissionscall, REQUEST_CALL);
                   }
                   else
                   {
                       call_action();
                   }
               }
               else if (text.equalsIgnoreCase("event"))
               {
                   if (Build.VERSION.SDK_INT > 22 && !hasPermissions(requiredPermissionscalendar))
                   {
                       ActivityCompat.requestPermissions(ScanResultActivity.this,
                               requiredPermissionscalendar, REQUEST_CALENDAR);
                   }
                   else {

                       Calendar beginTime = Calendar.getInstance();
                       Calendar endTime = Calendar.getInstance();
                       beginTime.setTime(start_date);
                       endTime.setTime(end_date);

                       Intent intent = new Intent(Intent.ACTION_INSERT)
                               .setData(CalendarContract.Events.CONTENT_URI)
                               .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                               .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                               .putExtra(CalendarContract.Events.TITLE, Name)
                               .putExtra(CalendarContract.Events.EVENT_LOCATION, loc)
                               .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                               .putExtra(Intent.EXTRA_EMAIL, "kumaralok092@gmail.com");
                       startActivity(intent);
                   }
               }
               else if (text.equalsIgnoreCase("map"))
               {
                   double lat=Double.parseDouble(qr_latitude);
                   double lon=Double.parseDouble(qr_longitude);
                   String label = "I'm Here!";
                   String uriBegin = "geo:" + lat + "," + lon;
                   String query = lat + "," + lon + "(" + label + ")";
                   String encodedQuery = Uri.encode(query);
                   String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                   Uri uri = Uri.parse(uriString);
                   Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                   startActivity(mapIntent);

               }
               else if (text.equalsIgnoreCase("open"))
               {
                   Intent intent = new Intent(Intent.ACTION_VIEW);
                   intent.setData(Uri.parse(Url));
                   String title ="choose";
                   Intent chooser = Intent.createChooser(intent, title);
                   startActivity(chooser);
               }
               else if (text.equalsIgnoreCase("search"))
               {
                   Websearch();
               }
            }
        });
    }

    public void VcartExteract(String result)
    {
        list.add("share");

        if (result.startsWith("BEGIN:VCARD"))
        {
            list.add("contact");

            String[] tokens = result.split("\n");

            Log.e("tokens", Arrays.toString(tokens));

            for (int i = 0; i < tokens.length; i++) {

                if (tokens[i].startsWith("BEGIN:")) {
                    String Type = tokens[i].substring(6);
                    Log.e("Type", Type);
                } else if (tokens[i].startsWith("N:")||tokens[i].startsWith("N;"))
                {
                     Name = tokens[i].substring(2);

                     if (Name!=null&&!Name.isEmpty()) {

                         TextView name = new TextView(context);
                         name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                 LinearLayout.LayoutParams.WRAP_CONTENT));
                         name.setText(Name);
                         name.setTextSize(16);
                         name.setPadding(10, 5, 10, 5);
                         name.setTextColor(Color.BLACK);
                         linearLayout.addView(name);
                     }

                    Log.e("Name", Name);
                } else if (tokens[i].startsWith("ORG:")||tokens[i].startsWith("org:")||tokens[i].startsWith("ORG;")) {

                    String Org = tokens[i].substring(4);

                    if (Org!=null&&!Org.isEmpty()) {

                        TextView name = new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(Org);
                        name.setTextSize(16);
                        name.setPadding(10, 5, 10, 5);
                        linearLayout.addView(name);
                        Log.e("org", Org);

                    }
                } else if (tokens[i].startsWith("TEL:") || tokens[i].startsWith("TEL;"))
                {
                     Tel = tokens[i].substring(4);

                     if (Tel!=null&&!Tel.isEmpty()) {
                         TextView name = new TextView(context);
                         name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                 LinearLayout.LayoutParams.WRAP_CONTENT));
                         name.setText(Tel);
                         name.setTextSize(16);
                         name.setPadding(10, 5, 10, 5);
                         linearLayout.addView(name);
                        // call.setVisibility(View.VISIBLE);

                         list.add("Tel");

                         Log.e("Tel", Tel);
                     }
                } else if (tokens[i].startsWith("URL:")||tokens[i].startsWith("URL;")) {

                    if (tokens[i].startsWith("URL:"))
                    {
                        Url = tokens[i].substring(4);
                        if (Url!=null&&!Url.isEmpty()) {


                            Pattern p = Pattern.compile(URL_REGEX);
                            Matcher m = p.matcher(Url);//replace with string to compare
                            if (m.find()) {

                               // browser.setVisibility(View.VISIBLE);

                                list.add("Url");
                            }

                            TextView name = new TextView(context);
                            name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            name.setText(Url);
                            name.setTextSize(16);
                            name.setPadding(10, 5, 10, 5);
                            linearLayout.addView(name);
                        }

                    }
                    else
                    {
                        String  Url22 = tokens[i].substring(4);

                        if (Url22 !=null&&!Url22.isEmpty())
                        {

                            TextView name = new TextView(context);
                            name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            name.setText(Url22);
                            name.setTextSize(16);
                            name.setPadding(10, 5, 10, 5);
                            linearLayout.addView(name);

                        }
                    }
                } else if (tokens[i].startsWith("EMAIL:")||tokens[i].startsWith("EMAIL;")) {
                     Email = tokens[i].substring(6);

                     if (Email!=null&&!Email.isEmpty()) {
                         TextView name = new TextView(context);
                         name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                 LinearLayout.LayoutParams.WRAP_CONTENT));
                         name.setText(Email);
                         name.setTextSize(16);
                         name.setPadding(10, 5, 10, 5);
                         linearLayout.addView(name);

                         list.add("Email");


                         Log.e("email", Email);
                     }
                } else if (tokens[i].startsWith("ADR:") || tokens[i].startsWith("ADR;")) {
                    String Adr = tokens[i].substring(4);
                  if (Adr!=null&&!Adr.isEmpty()) {
                      TextView name = new TextView(context);
                      name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                              LinearLayout.LayoutParams.WRAP_CONTENT));
                      name.setText(Adr);
                      name.setTextSize(16);
                      name.setPadding(10, 5, 10, 5);
                      linearLayout.addView(name);

                      Log.e("Adrress", Adr);
                  }

                } else if (tokens[i].startsWith("TITLE:")||tokens[i].startsWith("TITLE;")) {
                    String Note = tokens[i].substring(6);
                     if (Note!=null&&!Note.isEmpty()) {
                         TextView name = new TextView(context);
                         name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                 LinearLayout.LayoutParams.WRAP_CONTENT));
                         name.setText(Note);
                         name.setTextSize(16);
                         name.setPadding(10, 5, 10, 5);
                         linearLayout.addView(name);
                         Log.e("Note", Note);
                     }
                }
            }
        }
        else if(result.startsWith("geo:")||result.startsWith("GEO:"))
        {
            String delims = "[ , ?q= ]+";
            String[] tokens = result.split(delims);

            for (int i = 0; i < tokens.length; i++)
            {
                if (tokens[i].startsWith("geo:")||tokens[i].startsWith("GEO:"))
                {
                  String  qr_latitude = tokens[i].substring(4);
                  Log.d("lati_inner",qr_latitude);
                }
            }
           qr_latitude = tokens[0].substring(4);
           qr_longitude = tokens[1];
           if (qr_latitude!=null&&qr_longitude!=null&&!qr_latitude.isEmpty()&& !qr_longitude.isEmpty())
           {
               list.add("map");
           }

            TextView name= new TextView(context);
            name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            name.setText(result);
            name.setTextSize(16);
            name.setPadding(10,5,10,5);
            linearLayout.addView(name);

           Log.d("lat",qr_latitude);
           Log.d("long",qr_longitude);
        }
        else if(result.startsWith("https://")||(result.startsWith("www."))||result.startsWith("http://"))
        {
            Url=result;
            if (Url!=null&&!Url.isEmpty()) {

                Pattern p = Pattern.compile(URL_REGEX);
                Matcher m = p.matcher(Url);//replace with string to compare
                if (m.find()) {

                   list.add("Url");
                }
                TextView name = new TextView(context);
                name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                name.setText(result);
                name.setTextSize(16);
                name.setPadding(10, 5, 10, 5);
                linearLayout.addView(name);
            }
        }
        else if (result.startsWith("BEGIN:VEVENT")||result.startsWith("BEGIN:VCALENDAR"))
        {
           list.add("event");

            String[] Vevent= result.split("\n");
            Log.e("Vevent",Arrays.toString(Vevent));

            for(int i=0; i<Vevent.length; i++)
            {
                if (Vevent[i].startsWith("SUMMARY:")||Vevent[i].startsWith("SUMMARY;"))
                {
                    Name=Vevent[i].substring(8);

                    if (Name!=null&&!Name.isEmpty())
                    {

                        TextView name= new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(Name);
                        name.setTextSize(16);
                        name.setPadding(10,5,10,5);
                        linearLayout.addView(name);
                    }
                }
                else if (Vevent[i].startsWith("LOCATION:")||Vevent[i].startsWith("LOCATION;"))
                {
                    loc=Vevent[i].substring(9);

                    if (loc!=null&&!loc.isEmpty())
                    {
                        TextView name= new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(loc);
                        name.setTextSize(16);
                        name.setPadding(10,5,10,5);
                        linearLayout.addView(name);
                    }
                }
                else if (Vevent[i].startsWith("DTSTART:"))
                {
                     start_day=Vevent[i].substring(8);

                    if (start_day!=null&&!start_day.isEmpty())
                    {

                        TextView name= new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(start_day);
                        name.setTextSize(16);
                        name.setPadding(10,5,10,5);
                        linearLayout.addView(name);

                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

                        try {
                            start_date=simpleDateFormat.parse(start_day);
                            Log.d("date", String.valueOf(start_date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }



                    }

                }
                else if (Vevent[i].startsWith("DTEND:"))
                {
                    end_day=Vevent[i].substring(6);

                    if (end_day!=null&&!end_day.isEmpty())
                    {


                        TextView name= new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(end_day);
                        name.setTextSize(16);
                        name.setPadding(10,5,10,5);
                        linearLayout.addView(name);

                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

                        try {
                            end_date=simpleDateFormat.parse(end_day);
                            Log.d("date", String.valueOf(end_date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }



                    }

                }


            }




        }

        else if(result.startsWith("MECARD:"))
        {

            list.add("contact");

            String[] Mecard = result.split(";");

            Log.e("tokens", Arrays.toString(Mecard));

            for (int i = 0; i < Mecard.length; i++)
            {
                Log.e("mecard", String.valueOf(Mecard[i]));

               if (Mecard[i].startsWith("MECARD:N:") || Mecard[i].startsWith("MECARD:N;"))
                {
                    Name = Mecard[i].substring(9);
                    if (Name!=null&&!Name.isEmpty())
                    {
                        TextView name = new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(Name);
                        name.setTextSize(16);
                        name.setPadding(10, 5, 10, 5);
                        name.setTextColor(Color.BLACK);
                        linearLayout.addView(name);

                        Log.e("Name", Name);
                    }





                } else if (Mecard[i].startsWith("TEL:") || Mecard[i].startsWith("TEL;")) {

                       Tel=Mecard[i].substring(4);

                    if (Tel!=null&&!Tel.isEmpty()) {

                        TextView name = new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(Tel);
                        name.setTextSize(16);
                        name.setPadding(10, 5, 10, 5);
                        linearLayout.addView(name);

                        list.add("Tel");
                    }

                } else if (Mecard[i].startsWith("EMAIL:") || Mecard[i].startsWith("EMAIL;")) {

                    Email=Mecard[i].substring(6);

                    if(Email!=null&&!Email.isEmpty())
                    {
                    TextView name= new TextView(context);
                    name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    name.setText(Email);
                    name.setTextSize(16);
                    name.setPadding(10,5,10,5);
                    linearLayout.addView(name);

                    list.add("Email");

                    }

                } else if (Mecard[i].startsWith("ADR:") || Mecard[i].startsWith("ADR;")) {

                      String adress=Mecard[i].substring(4);

                      if (adress!=null&&!adress.isEmpty()) {


                          TextView name = new TextView(context);
                          name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                  LinearLayout.LayoutParams.WRAP_CONTENT));
                          name.setText(adress);
                          name.setTextSize(16);
                          name.setPadding(10, 5, 10, 5);
                          linearLayout.addView(name);

                      }


                } else if (Mecard[i].startsWith("URL:") || Mecard[i].startsWith("URL;")) {

                    if (Mecard[i].startsWith("URL:"))
                    {
                        Url=Mecard[i].substring(4);

                        if (Url!=null&&!Url.isEmpty())
                        {

                            Pattern p = Pattern.compile(URL_REGEX);
                            Matcher m = p.matcher(Url);//replace with string to compare
                            if (m.find()) {

                               list.add("Url");
                            }
                        TextView name= new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(Url);
                        name.setTextSize(16);
                        name.setPadding(10,5,10,5);
                        linearLayout.addView(name);


                        }
                    }
                    else
                    {
                        String url22=Mecard[i].substring(4);
                        TextView name= new TextView(context);
                        name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        name.setText(url22);
                        name.setTextSize(16);
                        name.setPadding(10,5,10,5);
                        linearLayout.addView(name);
                    }





                }


            }

        }
        else
        {
            TextView name= new TextView(context);
            name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            name.setText(result);
            name.setTextSize(16);
            name.setPadding(10,5,10,5);
            linearLayout.addView(name);

            list.add("web");
        }


        recyclerView.setLayoutManager(manager);
        Log.e("list", String.valueOf(list));
        adapter=new RecyclerAdapter(ScanResultActivity.this,list);
        recyclerView.setAdapter(adapter);


    }



    private void addContact() {

     Intent intent=new Intent(ContactsContract.Intents.Insert.ACTION);
     intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
     intent.putExtra(ContactsContract.Intents.Insert.EMAIL,Email);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE,ContactsContract.CommonDataKinds.Email.TYPE_HOME);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE,Tel);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        intent.putExtra(ContactsContract.Intents.Insert.NAME,Name);
          startActivity(intent);
    }






    public void call_action()
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+Tel));
        startActivity(intent);
    }


    public  void Websearch()
    {

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH );
        intent.putExtra(SearchManager.QUERY, result);
        startActivity(intent);
    }






    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                    return false;
            }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode ==REQUEST_CALL)
        {

            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {

                        if (Manifest.permission.CALL_PHONE.equals(permission)) {
                            openDialogForSetting(R.string.permission_call, R.string.permission_call_msg);
                        }

                    }
                }
            }

        }

        if (requestCode==REQUEST_CALENDAR)
        {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {

                        if (Manifest.permission.WRITE_CALENDAR.equals(permission)) {
                            openDialogForSetting(R.string.permission_calendar, R.string.permission_calendar_msg);
                        }

                    }
                }
            }
        }


        if (requestCode==REQUEST_CONTACTS)
        {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {

                        if (Manifest.permission.WRITE_CONTACTS.equals(permission)) {
                            openDialogForSetting(R.string.permission_contacts, R.string.permission_contacts_msg);
                        }

                    }
                }
            }
        }


    }

    private void openDialogForSetting(int title, int msg) {

        new AlertDialog.Builder(ScanResultActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("Setting",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //do something...
                                goToSettings();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                ).show();

    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }



}
