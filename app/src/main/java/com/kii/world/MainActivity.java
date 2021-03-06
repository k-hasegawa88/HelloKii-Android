//
//
// Copyright 2012 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//

package com.kii.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiACL;
import com.kii.cloud.storage.KiiACLEntry;
import com.kii.cloud.storage.KiiAnyAuthenticatedUser;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiACLCallBack;
import com.kii.cloud.storage.callback.KiiGroupCallBack;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

public class MainActivity extends Activity implements OnItemClickListener {

    private static final String TAG = "MainActivity";

    // define some strings used for creating objects
    private static final String BUCKET_NAME = "myBucket";
    private static final String OBJECT_KEY = "myObjectValue";

    // define the UI elements
    private ProgressDialog mProgress;

    // define the list
    private ListView mListView;

    // define the list manager
    private ObjectAdapter mListAdapter;

    // define the object count
    // used to easily see object names incrementing
    private int mObjectCount = 0;

    // define a custom list adapter to handle KiiObjects
    public class ObjectAdapter extends ArrayAdapter<KiiObject> {

        // define some vars
        int resource;
        String response;
        Context context;

        // initialize the adapter
        public ObjectAdapter(Context context, int resource,
                List<KiiObject> items) {
            super(context, resource, items);

            // save the resource for later
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // create the view
            LinearLayout rowView;

            // get a reference to the object
            KiiObject obj = getItem(position);

            // if it's not already created
            if (convertView == null) {

                // create the view by inflating the xml resource
                // (res/layout/row.xml)
                rowView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi;
                vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, rowView, true);

            }

            // it's already created, reuse it
            else {
                rowView = (LinearLayout) convertView;
            }

            // get the text fields for the row
            TextView titleText = (TextView) rowView
                    .findViewById(R.id.rowTextTitle);
            TextView subtitleText = (TextView) rowView
                    .findViewById(R.id.rowTextSubtitle);

            // set the content of the row texts
            titleText.setText(obj.getString(OBJECT_KEY));
            subtitleText.setText(obj.toUri().toString());

            // return the row view
            return rowView;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //ブロードキャストレシーバーを発信
        Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
        startService(intent);



        // set our view to the xml in res/layout/main.xml
        setContentView(R.layout.main);

        // create an empty object adapter
        mListAdapter = new ObjectAdapter(this, R.layout.row,
                new ArrayList<KiiObject>());

        mListView = (ListView) this.findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
        // set it to our view's list
        mListView.setAdapter(mListAdapter);

        // query for any previously-created objects
        this.loadObjects();

    }

    // load any existing objects associated with this user from the server.
    // this is done on view creation
    private void loadObjects() {

        // default to an empty adapter
        mListAdapter.clear();

        // show a progress dialog to the user
        mProgress = ProgressDialog.show(MainActivity.this, "", "Loading...",
                true);

        // create an empty KiiQuery (will retrieve all results, sorted by
        // creation date)
        KiiQuery query = new KiiQuery(null);
        query.sortByDesc("_created");

        // define the bucket to query
        KiiBucket bucket = Kii.bucket("mainBucket");

        // perform the query
        bucket.query(new KiiQueryCallBack<KiiObject>() {

            // catch the callback's "done" request
            public void onQueryCompleted(int token,
                                         KiiQueryResult<KiiObject> result, Exception e) {

                // hide our progress UI element
                mProgress.dismiss();

                // check for an exception (successful request if e==null)
                if (e == null) {

                    // add the objects to the adapter (adding to the listview)
                    List<KiiObject> objLists = result.getResult();
                    for (KiiObject obj : objLists) {
                        mListAdapter.add(obj);
//Log.i("str",obj.getString(OBJECT_KEY));
//                        showToast(obj.getString("TEST"));
                    }

                    // tell the console and the user it was a success!
                    Log.v(TAG, "Objects loaded: " + result.getResult().toString());
                    showToast("Objects loaded");

                }

                // otherwise, something bad happened in the request
                else {

                    // tell the console and the user there was a failure
                    Log.v(TAG, "Error loading objects: " + e.getLocalizedMessage());
                    showToast("Error loading objects: " + e.getLocalizedMessage());

                }
            }
        }, query);

    }

    // the user has chosen to create an object from the options menu.
    // perform that action here...
    public void addItem(View v) throws Exception {

        // show a progress dialog to the user
        mProgress = ProgressDialog.show(MainActivity.this, "",
                "Creating Object...", true);

        // create an incremented title for the object
        String value = "MyObject " + (++mObjectCount);

//        // get a reference to a KiiBucket
//        KiiBucket bucket = KiiUser.getCurrentUser().bucket(BUCKET_NAME);

//        KiiGroup group =  KiiGroup.groupWithID("room1");
//        bucket = group.bucket(BUCKET_NAME);

        KiiUser user = KiiUser.getCurrentUser();
//        //ここでこける
//        List<KiiGroup> existingGroup = user.memberOfGroups(new KiiUserCallBack() {
//
//        });

        // Create Application Scope Bucket
        KiiBucket appBucket = Kii.bucket("mainBucket");
//        showToast("debug info 002");


////        // 既に同じメンバーのグループが存在するかチェックする
//        boolean isExistGroup = false;
//        for (KiiGroup kiiGroup : existingGroup) {
//            if (TextUtils.equals(kiiGroup.getGroupName(),"roomtest")) {
//                isExistGroup = true;
////                return kiiGroup;
//            }
//        }
//
//        showToast("debug info 003");
//
//        if(!isExistGroup) {
//            // Chat用のグループを作成
//            KiiGroup kiiGroup = Kii.group("roomtest", null);
//            kiiGroup.save(new KiiGroupCallBack() {
//                @Override
//                public void onSaveCompleted(int token, KiiGroup group, Exception exception) {
//                    if (exception != null) {
//                        // Error handling
//                        return;
//                    }
//                    Uri groupUri = group.toUri();
//                    String groupID = group.getID();
//                }
//            });
//                //ACL エントリは初期化処理等で 1 度だけ書き換え、次回の実行時には再設定しないような実装が必要です
//            KiiBucket groupBucket = Kii.group("roomtest",null).bucket("MainBucket");
//            KiiACL bucketACL = groupBucket.acl();
//            bucketACL.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser.create(),KiiACL.BucketAction.QUERY_OBJECTS_IN_BUCKET,true));
//            bucketACL.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser.create(),KiiACL.BucketAction.CREATE_OBJECTS_IN_BUCKET,true));
////?        bucketACL.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser.create(),KiiACL.BucketAction.READ_OBJECTS_IN_BUCKET,true));
//            bucketACL.save(new KiiACLCallBack() {
//                @Override
//                public void onSaveCompleted(int token,KiiACL acl,Exception exception) {
//                    if(exception != null) {
//                        return;
//                    }
//                }
//            });
//
//        }
//
//
////        KiiUser target = KiiUser.getCurrentUser();//.createByUri(Uri.parse(this.chatFriend.getUri()));
////        target.refresh();
////        kiiGroup.addUser(target);
//
//
//
//        KiiBucket groupBucket = Kii.group("roomtest",null).bucket("MainBucket");
//


        // create a new KiiObject and set a key/value
        KiiObject obj = appBucket.object();
        obj.set(OBJECT_KEY, value);
        obj.set("TEST","ほにゃらら");

        // save the object asynchronously
        obj.save(new KiiObjectCallBack() {

            // catch the callback's "done" request
            public void onSaveCompleted(int token, KiiObject o, Exception e) {

                // hide our progress UI element
                mProgress.dismiss();

                // check for an exception (successful request if e==null)
                if (e == null) {

                    // tell the console and the user it was a success!
                    Log.d(TAG, "Created object: " + o.toString());
                    showToast("Created object");

                    // insert this object into the beginning of the list adapter
                    MainActivity.this.mListAdapter.insert(o, 0);

                }

                // otherwise, something bad happened in the request
                else {

                    // tell the console and the user there was a failure
                    Log.d(TAG, "Error creating object: " + e.getLocalizedMessage());
                    showToast("Error creating object" + e.getLocalizedMessage());

                }

            }
        });

    }

    // the user has clicked an item on the list.
    // we use this action to possibly delete the tapped object.
    // to confirm, we prompt the user with a dialog:
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
                            long arg3) {
        // build the alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to remove this item?")
                .setCancelable(true)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {

                            // if the user chooses 'yes',
                            public void onClick(DialogInterface dialog, int id) {

                                // perform the delete action on the tapped
                                // object
                                MainActivity.this.performDelete(arg2);
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // if the user chooses 'no'
                    public void onClick(DialogInterface dialog, int id) {

                        // simply dismiss the dialog
                        dialog.cancel();
                    }
                });

        // show the dialog
        builder.create().show();

    }

    // the user has chosen to delete an object
    // perform that action here...
    void performDelete(int position) {

        // show a progress dialog to the user
        mProgress = ProgressDialog.show(MainActivity.this, "",
                "Deleting object...", true);

        // get the object to delete based on the index of the row that was
        // tapped
        final KiiObject o = MainActivity.this.mListAdapter.getItem(position);

        // delete the object asynchronously
        o.delete(new KiiObjectCallBack() {

            // catch the callback's "done" request
            public void onDeleteCompleted(int token, Exception e) {

                // hide our progress UI element
                mProgress.dismiss();

                // check for an exception (successful request if e==null)
                if (e == null) {

                    // tell the console and the user it was a success!
                    Log.d(TAG, "Deleted object: " + o.toString());
                    showToast("Deleted object");

                    // remove the object from the list adapter
                    MainActivity.this.mListAdapter.remove(o);

                }

                // otherwise, something bad happened in the request
                else {

                    // tell the console and the user there was a failure
                    Log.d(TAG, "Error deleting object: " + e.getLocalizedMessage());
                    showToast("Error deleting object: " + e.getLocalizedMessage());

                }

            }
        });
    }

    // the user can add items from the options menu.
    // create that menu here - from the res/menu/menu.xml file
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            MainActivity.this.addItem(null);
        } catch (Exception e) {

        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HelloKii.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        HelloKii.activityPaused();
    }




}
