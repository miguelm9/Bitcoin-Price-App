package example.miguelmartin.bitcoinprice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String BPI_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private ProgressDialog progressDialog;
    private TextView txt;
    public TextView balanceValueText, eurText, errorText;
    EditText btcText;
    Button equalButton;

    int counter = 0;
    Double currentBtc = 1.00; //INTRODUCE HERE THE AMOUNT OF BTC YOU OWN IN ORDER TO KEEP TRACK OF YOUR HODLING'S
    Double btcPrice;
    String btcPriceString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = (TextView) findViewById(R.id.txt);
        balanceValueText = (TextView)findViewById(R.id.balanceValue);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please, wait ...");
        btcText = (EditText) findViewById(R.id.btcText);
        eurText = (TextView) findViewById(R.id.eurText);
        errorText = (TextView) findViewById(R.id.errorText);
        equalButton = (Button) findViewById(R.id.equalBtn);

        equalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btcPrice == null){
                    counter++;
                    errorText.setText("Please double tap the <<LOAD>> button first: " + counter);
                }
                else {
                    convert();
                    errorText.setText("");
                }
            }
        });


    }

    private void convert() {
        try{
            DecimalFormat df = new DecimalFormat("#.##");

            Double btcs = Double.valueOf(btcText.getText().toString());
            Double eurs = btcs * btcPrice;
            String eursFormatted = df.format(eurs);
            eurText.setText(eursFormatted + " USD");

        }catch (NumberFormatException e){
            System.out.println("souting" + e);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_load) {
            load();
        }

        return super.onOptionsItemSelected(item);
    }

    private void load() {
        Request request = new Request.Builder()
                .url(BPI_ENDPOINT)
                .build();

        progressDialog.show();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Error during BPI loading : "
                        + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                final String body = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (btcPriceString == null){
                        }
                        else{
                            errorText.setText("");
                           btcPriceString =  btcPriceString.replaceAll(",", "");
                           btcPrice = Double.valueOf(btcPriceString);
                            Double balanceValue = btcPrice*currentBtc;
                            DecimalFormat df = new DecimalFormat("#.##");

                            balanceValueText.setText(df.format(balanceValue) + " $");

                        }
                        progressDialog.dismiss();
                        parseBpiResponse(body);

                    }
                });
            }
        });

    }


    private void parseBpiResponse(String body) {
        try {
            StringBuilder builder = new StringBuilder();

            JSONObject jsonObject = new JSONObject(body);


            JSONObject bpiObject = jsonObject.getJSONObject("bpi");
            JSONObject usdObject = bpiObject.getJSONObject("USD");
            builder.append(usdObject.getString("rate")).append("$").append("\n");

            builder.append("\n");

            JSONObject euroObject = bpiObject.getJSONObject("EUR");
            builder.append(euroObject.getString("rate")).append("€").append("\n");

            txt.setText(builder.toString());

            btcPriceString = usdObject.getString("rate");

        } catch (Exception e) {

        }
    }

}