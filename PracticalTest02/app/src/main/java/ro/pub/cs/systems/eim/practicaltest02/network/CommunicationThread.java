
package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.CurrencyInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (currency type)!");

            String informationType = bufferedReader.readLine();
            if (informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (currency type)!");
                return;
            }
            HashMap<String, CurrencyInformation> data = serverThread.getData();
            CurrencyInformation currencyInformation = null;
            if (data.containsKey(informationType)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                String dateCurrencyString =  data.get(informationType).getUpdated();
                Date dateCurrency = new Date(dateCurrencyString);
                Date now = new Date();

                Long diff = getDateDiff(dateCurrency, now, TimeUnit.MINUTES);
                if (diff > 1) {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Invalidated cache...");
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice because c...");
                    HttpClient httpClient = new DefaultHttpClient();
                    String pageSourceCode = "";

                    HttpGet httpGet = new HttpGet("https://api.coindesk.com/v1/bpi/currentprice/" + informationType + ".json");
                    HttpResponse httpGetResponse = httpClient.execute(httpGet);
                    HttpEntity httpGetEntity = httpGetResponse.getEntity();
                    if (httpGetEntity != null) {
                        pageSourceCode = EntityUtils.toString(httpGetEntity);

                    }

                    if (pageSourceCode == null) {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                        return;
                    } else
                        Log.i(Constants.TAG, pageSourceCode);


                    JSONObject content = new JSONObject(pageSourceCode);

                    JSONObject main = content.getJSONObject("bpi");
                    JSONObject currencyDetails = main.getJSONObject(informationType);

                    String code = currencyDetails.getString("code");
                    String rate = currencyDetails.getString("rate");
                    String description = currencyDetails.getString("description");
                    String rate_float = currencyDetails.getString("rate_float");

                    JSONObject time = content.getJSONObject("time");
                    String updated = time.getString("updated");

                    currencyInformation = new CurrencyInformation(
                            code, rate, description, rate_float, updated
                    );
                    serverThread.setData(informationType, currencyInformation);
                } else {
                    currencyInformation = data.get(informationType);
                }
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

                HttpGet httpGet = new HttpGet("https://api.coindesk.com/v1/bpi/currentprice/" + informationType + ".json");
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);

                }

                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else
                    Log.i(Constants.TAG, pageSourceCode);


                JSONObject content = new JSONObject(pageSourceCode);

                JSONObject main = content.getJSONObject("bpi");
                JSONObject currencyDetails = main.getJSONObject(informationType);

                String code = currencyDetails.getString("code");
                String rate = currencyDetails.getString("rate");
                String description = currencyDetails.getString("description");
                String rate_float = currencyDetails.getString("rate_float");

                JSONObject time = content.getJSONObject("time");
                String updated = time.getString("updated");

                currencyInformation = new CurrencyInformation(
                        code, rate, description, rate_float, updated
                );
                serverThread.setData(informationType, currencyInformation);
            }
            if (currencyInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }
            String result = currencyInformation.toString();

            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            ioException.printStackTrace();
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            jsonException.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    ioException.printStackTrace();
                }
            }
        }
    }
}
