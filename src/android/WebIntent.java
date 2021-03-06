package com.kazakov.webintent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.Toast;
import com.cordova.IntentApp.R;
import org.apache.cordova.CordovaActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;

/**
 * WebIntent is a PhoneGap plugin that bridges Android intents and web
 * applications:
 * 
 * 1. web apps can spawn intents that call native Android applications. 2.
 * (after setting up correct intent filters for PhoneGap applications), Android
 * intents can be handled by PhoneGap web applications.
 * 
 * @author vitaly.kazakov@gmail.com
 * 
 */
public class WebIntent extends CordovaPlugin {

    //private String onNewIntentCallback = null;
    private CallbackContext callbackContext = null;

    //public boolean execute(String action, JSONArray args, String callbackId) {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            this.callbackContext = callbackContext;

            if (action.equals("startActivity")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                // Parse the arguments
				final CordovaResourceApi resourceApi = webView.getResourceApi();
                JSONObject obj = args.getJSONObject(0);
                String type = obj.has("type") ? obj.getString("type") : null;
                Uri uri = obj.has("url") ? resourceApi.remapUri(Uri.parse(obj.getString("url"))) : null;
                JSONObject extras = obj.has("extras") ? obj.getJSONObject("extras") : null;
                Map<String, String> extrasMap = new HashMap<String, String>();

                // Populate the extras if any exist
                if (extras != null) {
                    JSONArray extraNames = extras.names();
                    for (int i = 0; i < extraNames.length(); i++) {
                        String key = extraNames.getString(i);
                        String value = extras.getString(key);
                        extrasMap.put(key, value);
                    }
                }

                startActivity(obj.getString("action"), uri, type, extrasMap);
                //return new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                return true;

            } else if (action.equals("hasExtra")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }
                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String extraName = args.getString(0);
                //return new PluginResult(PluginResult.Status.OK, i.hasExtra(extraName));
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, i.hasExtra(extraName)));
                return true;

            } else if (action.equals("getExtra")) {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }
                Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();
                String extraName = args.getString(0);
                if (i.hasExtra(extraName)) {
                    //return new PluginResult(PluginResult.Status.OK, i.getStringExtra(extraName));
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, i.getStringExtra(extraName)));
                    return true;
                } else {
                    //return new PluginResult(PluginResult.Status.ERROR);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                    return false;
                }
            } else if (action.equals("getUri")) {
                if (args.length() != 0) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                try {
                    // Get input stream for content Uri

                    Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();

                    Uri uri = i.getData();

                    ContentResolver resolver = this.cordova.getActivity().getContentResolver();
                    InputStream inputStream = resolver.openInputStream(uri);


                    if(inputStream != null) {
                        String filename = System.currentTimeMillis()+".skt";
                        String tempFolder = getAttachmentFolderName();
                        String filePath = tempFolder + File.separator + filename;
                        File file = new File(filePath);
                        if(!file.exists())
                            file.createNewFile();

                        FileOutputStream fileOutputStream = new FileOutputStream(file);

                        byte[] bytes = new byte[1000];
                        int count;
                        while((count = inputStream.read(bytes)) > 0) {
                            fileOutputStream.write(bytes, 0, count);
                        }

                        fileOutputStream.close();
                        inputStream.close();

                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, filePath));
                        return true;
                    }
                    else {
                        Toast t = Toast.makeText(this.cordova.getActivity(), "unknown error", Toast.LENGTH_LONG);
                        t.show();
                        return false;
                    }
                }
                catch (Exception ex) {
                    Toast t = Toast.makeText(this.cordova.getActivity(), ex.getMessage(), Toast.LENGTH_LONG);
                    t.show();
                    Log.e("Activity", "In outer catch: " + ex.getMessage());
                    ex.printStackTrace();
                    return false;
                }
                
                
               /* InputStream zis;
				try {
					zis = new ZipInputStream(((CordovaActivity)this.cordova.getActivity()).getContentResolver().openInputStream(__uri));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                
                
                String filePath = null;
                Uri _uri = i.getData();
                filePath = _uri.getPath();
               */
                
                
                
             /*   Log.d("","URI = "+ _uri);                                       
                if (_uri != null && "content".equals(_uri.getScheme())) {
                	
                	String[] proj = { MediaStore.Images.Media.DATA };
                	
                   /* Cursor cursor = ((CordovaActivity)this.cordova.getActivity()).getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                    cursor.moveToFirst();   
                    filePath = cursor.getString(0);
                    cursor.close();
                	
                }
                else {
                    filePath = _uri.getPath();
                }
                Log.d("","Chosen path = "+ filePath);      */        
                                
                
                //return new PluginResult(PluginResult.Status.OK, uri);
            } else if (action.equals("onNewIntent")) {
                if (args.length() != 0) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                //this.onNewIntentCallback = callbackId;
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
                return true;
                //return result;
            } else if (action.equals("sendBroadcast")) 
            {
                if (args.length() != 1) {
                    //return new PluginResult(PluginResult.Status.INVALID_ACTION);
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                    return false;
                }

                // Parse the arguments
                JSONObject obj = args.getJSONObject(0);

                JSONObject extras = obj.has("extras") ? obj.getJSONObject("extras") : null;
                Map<String, String> extrasMap = new HashMap<String, String>();

                // Populate the extras if any exist
                if (extras != null) {
                    JSONArray extraNames = extras.names();
                    for (int i = 0; i < extraNames.length(); i++) {
                        String key = extraNames.getString(i);
                        String value = extras.getString(key);
                        extrasMap.put(key, value);
                    }
                }

                sendBroadcast(obj.getString("action"), extrasMap);
                //return new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                return true;
            }
            //return new PluginResult(PluginResult.Status.INVALID_ACTION);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            String errorMessage=e.getMessage();
            //return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION,errorMessage));
            return false;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (this.callbackContext != null) {
            this.callbackContext.success(intent.getDataString());
        }
    }

    void startActivity(String action, Uri uri, String type, Map<String, String> extras) {
        Intent i = (uri != null ? new Intent(action, uri) : new Intent(action));
        
        if (type != null && uri != null) {
            i.setDataAndType(uri, type); //Fix the crash problem with android 2.3.6
        } else {
            if (type != null) {
                i.setType(type);
            }
        }
        
        for (String key : extras.keySet()) {
            String value = extras.get(key);
            // If type is text html, the extra text must sent as HTML
            if (key.equals(Intent.EXTRA_TEXT) && type.equals("text/html")) {
                i.putExtra(key, Html.fromHtml(value));
            } else if (key.equals(Intent.EXTRA_STREAM)) {
                // allowes sharing of images as attachments.
                // value in this case should be a URI of a file
				final CordovaResourceApi resourceApi = webView.getResourceApi();
                i.putExtra(key, resourceApi.remapUri(Uri.parse(value)));
            } else if (key.equals(Intent.EXTRA_EMAIL)) {
                // allows to add the email address of the receiver
                i.putExtra(Intent.EXTRA_EMAIL, new String[] { value });
            } else {
                i.putExtra(key, value);
            }
        }
        ((CordovaActivity)this.cordova.getActivity()).startActivity(i);
    }

    void sendBroadcast(String action, Map<String, String> extras) {
        Intent intent = new Intent();
        intent.setAction(action);
        for (String key : extras.keySet()) {
            String value = extras.get(key);
            intent.putExtra(key, value);
        }

        ((CordovaActivity)this.cordova.getActivity()).sendBroadcast(intent);
    }

    public String getAttachmentFolderName() {
        File root = new File(Environment.getExternalStorageDirectory() + File.separator + "EmailAttachments" + File.separator);

        if(root.mkdirs()) {
            return root.getAbsolutePath();
        }
        else if (root.exists()) {
            if(root.isDirectory()) {
                return root.getAbsolutePath();
            }
            else {
                root.delete();
                root.mkdirs();
                return root.getAbsolutePath();
            }
        }
        else {
            return null;
        }
    }
}
