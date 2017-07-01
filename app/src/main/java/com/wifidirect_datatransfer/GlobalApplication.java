package com.wifidirect_datatransfer;

import android.content.Context;
import android.os.Environment;

import java.util.ArrayList;

import Utils.RxBusApi;

public class GlobalApplication extends android.app.Application{


	public static RxBusApi _rxBus;


    private static Context GlobalContext;

	public static final String Path = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String Folder = "BlanccoDataTransfer";
	public static final String ImageFolder = "Pictures";
	public static final String FilesFolder = "Files";
	public static String FilePathJson = "NA";
    public static ArrayList<String> list_Sender = new ArrayList<>();

	public static int feedbackPort = 0;
	public static String feedbackHost = "NA";

	public static int ContactPort = 50142;
	public static int ImagePort =50143;
	public static int DocumentPort =50144;


	public static String mode="Normal"; // ios


	public static boolean isOnSameNetwork = false;  // true ---> to chk normal operation . false --> to chk hotspot



	@Override
	public void onCreate() {

		super.onCreate();



		if(GlobalApplication.GlobalContext == null){

			GlobalApplication.GlobalContext = getApplicationContext();

		}
	}
	

}
