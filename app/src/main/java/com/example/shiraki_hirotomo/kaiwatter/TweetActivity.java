package com.example.shiraki_hirotomo.kaiwatter;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by shiraki-hirotomo on 2014/09/11.
 */


public class TweetActivity extends Activity {
    private EditText mInputText;
    private Twitter mTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //このアクティビティにlayoutのactivity_tweetをセット
        setContentView(R.layout.activity_tweet);
        // TwitterUtilsのgetTwitterInstanceによってコンシューマ・キーやコンシューマ・シークレット、アクセストークンがセットされたTwitter型のインスタンスが代入されている。
        mTwitter = TwitterUtils.getTwitterInstance(this);
        //ビューに対して割り当てられたリソースIDから対応するビューオブジェクトを取得する。これで、このedittextはどのビューのものか特定できる。
        mInputText = (EditText) findViewById(R.id.input_text);
        //idがtweet_buttonのボタンを押したときの反応をセットする。
        findViewById(R.id.tweet_button).setOnClickListener(new View.OnClickListener() {
            //クリックしたとき、
            @Override
            public void onClick(View v) {
                //ツイートする（このアクティビティ内にあるtweet()を実行している）
                tweet();
            }
        });
    }
    //ツイートをする。
    private void tweet() {
        //AsyncTaskにより非同期処理を行う。(非同期という言葉は、「ある処理の実行中に、別の処理を止めない」という意味である。)
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            //メインスレッドとは別のスレッドで行う処理。
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    //これによってツイートできる。
                    mTwitter.updateStatus(params[0]);
                    return true;
                } catch (TwitterException e) {
                    //スタックトレースを出力
                    e.printStackTrace();
                    return false;
                }
            }
            @Override
            protected void onPostExecute(Boolean result) {
                //doinbackgroundの処理でツイートできたかできなかったかで場合分け
                if (result) {
                    showToast("ツイートが完了しました！");
                    //このアクティビティ終わり
                    finish();
                } else {
                    showToast("ツイートに失敗しました。。。");
                }
            }
        };
        //EditText型変数mInputTextに入力したテキストをdoinbackgroundに投げる。
        task.execute(mInputText.getText().toString());
    }
    //トーストを長めに表示する
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
