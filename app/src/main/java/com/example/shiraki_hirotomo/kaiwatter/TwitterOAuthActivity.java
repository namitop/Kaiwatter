package com.example.shiraki_hirotomo.kaiwatter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by shiraki-hirotomo on 2014/09/11.
 */
public class TwitterOAuthActivity extends Activity {
    //キー
    private static final String REQUEST_TOKEN = "request_token";
    //mCallbackURLを宣言
    private String mCallbackURL;
    //mTwitterを宣言
    private Twitter mTwitter;
    //RequestToken型変数mRequestTokenを宣言
    private RequestToken mRequestToken;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //viewをセットする。
        setContentView(R.layout.activity_twitter_oauth);
        //mCallbackURLにtwitter_callback_urlの文字列を代入
        mCallbackURL = getString(R.string.twitter_callback_url);
        // TwitterUtilsのgetTwitterInstanceによってコンシューマ・キーやコンシューマ・シークレット、アクセストークンがセットされたTwitter型のインスタンスが代入されている。
        mTwitter = TwitterUtils.getTwitterInstance(this);
        //idがbutton_twitter_oauthのボタンをおしたときの反応の設定
        findViewById(R.id.button_tweet_oauth).setOnClickListener(new View.OnClickListener() {
            //クリックした場合
            @Override
            public void onClick(View v) {
                //OAuth認証（厳密には認可）を開始します。
                startAuthorize();
            }
        });
    }
    //アクティビティの中断などにより呼び出される。
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //bundleにリクエストトークンを保存している。（キーはREQUEST_TOKEN）（リクエストトークンはserializableされている。）
        outState.putSerializable(REQUEST_TOKEN, mRequestToken);
    }

    //中断されたアクティビティが再開されるとき。
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //キーをREQUEST_TOKENでデータを呼び出し、代入する。
        mRequestToken = (RequestToken) savedInstanceState.getSerializable(REQUEST_TOKEN);
    }

    /**
     * OAuth認証（厳密には認可）を開始します。
     *
     * @param //listener
     */
    private void startAuthorize() {
        //AsyncTaskがた変数taskを宣言。
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            //doInBackGroundにより、メインスレッドとは別のスレッドで実行している。
            @Override
            protected String doInBackground(Void... params) {
                try {
                    //mTwitterのRequestTokenを取得し、代入。（RequestToken型）
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
                    //認証のためにブラウザを開くが、そのブラウザのURLを返り値にしている
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    //スタックトレースを出力
                    e.printStackTrace();
                }
                //返り値をnullにする
                return null;
            }
            //doInBackgroundメソッドの戻り値をこのメソッドの引数として受け取っている。メインスレッドで実行される。
            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    //Intentによってブラウザに送る。(String action, Uri uri)になっている。uriのほうにアクセスする
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    // 失敗。。。
                }
            }
        };
        //設定したAsyncTask型変数taskを呼び出す。
        task.execute();
    }

    //onNewIntentにブラウザからIntentが送られてくる。
    @Override
    public void onNewIntent(Intent intent) {
        //intentがnullの場合、intentのgetDataがnullの場合、intentのgetDataのstringの先頭がmCallbackURLと同じじゃない場合は、
        //（mCallbackURLの先頭と同じじゃないとだめなのは、ブラウザからアプリに戻るときに、スキームの後ろにクエリがついているから。）
        if (intent == null
                || intent.getData() == null
                || !intent.getData().toString().startsWith(mCallbackURL)) {
            //返り値なしで終了
            return;
        }
        //"oauth_varifier"をキーにString型変数verifierに値を代入する。verifierは認証関係のやつ
        String verifier = intent.getData().getQueryParameter("oauth_verifier");

        //AsyncTask<String, Void, AccessToken>
        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    //mTwitterのOAuthAccessToken返り値にする。（キーは、mRequestTokenとverifierのparams[0]）
                    return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
                } catch (TwitterException e) {
                    //スタックとレースを出力
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                //accesstokenがあれば成功
                if (accessToken != null) {
                    // 認証成功！
                    //トースト表示
                    showToast("認証成功！");
                    //successouathメソッドをaccesstokenを引数に実行
                    successOAuth(accessToken);
                } else {
                    // 認証失敗。。。
                    showToast("認証失敗。。。");
                }
            }
        };
        //verifierを引数にtaskを呼び出す。verifierの値は、doinbackgroundに渡される。
        task.execute(verifier);
    }

    //successOAuth
    private void successOAuth(AccessToken accessToken) {
        TwitterUtils.storeAccessToken(this, accessToken);
        //intent先をMainActivity
        Intent intent = new Intent(this, MainActivity.class);
        //設定されたintentを実行
        startActivity(intent);
        //今いるアクティビティを終了
        finish();
    }
    //トーストを長めに表示
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
