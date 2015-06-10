package com.brachialste.earthquakemonitor.web.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.brachialste.earthquakemonitor.ApplicationManager;

import android.util.Log;

public class JSONParser {
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	// Debug
	private static final String TAG = "JSONParser";

	// constructor
	public JSONParser() {

	}

	public JSONObject getJSONFromUrl(String url, List<NameValuePair> params) {

		// realizamos el request HTTP
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(params));

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "n");
			}
			is.close();
			json = sb.toString();
			if (ApplicationManager.D) {
				Log.e(TAG, json);
			}
		} catch (Exception e) {
			if (ApplicationManager.D) {
				Log.e(TAG, "Error convirtiendo el resultado " + e.toString());
			}
		}

		// intentamos convertir el string a un objeto JSON
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			if (ApplicationManager.D) {
				Log.e(TAG, "Error convirtiendo los datos " + e.toString());
			}
		}

		// regresamos la cadena JSON
		return jObj;

	}
}
