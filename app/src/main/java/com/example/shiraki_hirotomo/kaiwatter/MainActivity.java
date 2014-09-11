package com.example.shiraki_hirotomo.kaiwatter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.image.SmartImageView;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;



public class MainActivity extends ListActivity {

    private TweetAdapter mAdapter;
    private Twitter mTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //親クラスをonCreateしている
        super.onCreate(savedInstanceState);
        //hasAccessTokenはアクセストークンが存在する場合はtrueを返すので、存在しない場合最初の処理になる。
        if (!TwitterUtils.hasAccessToken(this)) {
            //intentを宣言。TwitterOAuthActivityにintentする設定をしている。
            Intent intent = new Intent(this, TwitterOAuthActivity.class);
            //設定したActivityへintentを送る。
            startActivity(intent);
            //今いるアクティビティを終了
            finish();
        } else {
            //TwitterAdapterによってthisをtwitter用のリストにしている
            mAdapter = new TweetAdapter(this);
            //アクティビティにmAdapterを設定している
            setListAdapter(mAdapter);
            // TwitterUtilsのgetTwitterInstanceによってコンシューマ・キーやコンシューマ・シークレット、アクセストークンがセットされたTwitter型のインスタンスが代入されている。
            mTwitter = TwitterUtils.getTwitterInstance(this);
            //AsyncTaskを使ってタイムラインをリロードしている。
            reloadTimeLine();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflaterを宣言
        MenuInflater inflater = getMenuInflater();
        //MenuInflaterからXMLの取得
        inflater.inflate(R.menu.main, menu);
        //オーバーライドしたものをsuperで呼び出し実行する。
        return super.onCreateOptionsMenu(menu);
    }

    //メニューの中身を選択されたときの反応。
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //メニューの中のどのボタンを押したかidで判断しswitch
        switch (item.getItemId()) {
            //メニューのタイムラインのリロードが選択された場合
            case R.id.menu_reload:
                //タイムラインをリロードする
                reloadTimeLine();
                return true;
            //メニューのツイート機能が選択された場合
            case R.id.menu_tweet:
                //intent先にTweetActivityを設定する。
                Intent intent = new Intent(this, TweetActivity.class);
                //設定したActivityにintentを送る。
                startActivity(intent);
                return true;
        }
        //オーバーライドしたものをsuperで呼び出し実行する。
        return super.onOptionsItemSelected(item);
    }

    //twitterの内容をいい感じ整理してリストにするもの
    private class TweetAdapter extends ArrayAdapter<Status> {
        //LayoutInflaterは、ほかのxmlリソースのViewを取り扱える仕掛けです。
        private LayoutInflater mInflater;
        //TwitterAdapterのコンストラクタ。初期化をしている。
        public TweetAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            //layoutinflaterはgetSystemServiceメソッドから取得する。getSystemServiceメソッドを使えば、wifiやusbなどハードウェア関連のサービスも使えるようになる。
            mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                //動的にViewオブジェクトを得ている。layoutのlist_item_tweetを使っている。
                convertView = mInflater.inflate(R.layout.list_item_tweet, null);
            }
            //リストのpositionにあるもののstatusを取り出す
            Status item = getItem(position);
            //convertViewにあるidがnameのviewを取り出し、textview型変数nameに代入(ツイートしたユーザの名前)
            TextView name = (TextView) convertView.findViewById(R.id.name);
            //textview型変数nameにitemのuserのnameをセットする
            name.setText(item.getUser().getName());
            //convertViewにあるidがscreen_nameのviewを取り出し、textview型変数screennameに代入(ツイートしたユーザのID)
            TextView screenName = (TextView) convertView.findViewById(R.id.screen_name);
            //textview型変数screennameにitemのuserのscreennameをセットする
            screenName.setText("@" + item.getUser().getScreenName());
            //convertViewにあるidがtextのviewを取り出し、textview型変数textに代入(ツイートの投稿内容)
            TextView text = (TextView) convertView.findViewById(R.id.text);
            //textview型変数textにitemのtextをセットする
            text.setText(item.getText());
            //convertViewのidがiconのものをSmartImageView型変数iconに代入する
            SmartImageView icon = (SmartImageView) convertView.findViewById(R.id.icon);
            //SmartImageView型変数iconにitemのuserのprofileimageURLをセットする
            icon.setImageUrl(item.getUser().getProfileImageURL());
            //いろんなものをセットしたconvertviewを戻り値として返す。
            return convertView;
        }
    }
    //タイムラインをリロードする
    private void reloadTimeLine() {
        //AsyncTaskの引数は、デフォルトでは AsyncTask<Params, Progress, Result>です。
        //これはそれぞれ <入力パラメータ、進行度、結果のデータ型> を表しています。
        //AsyncTask型変数taskを宣言している。
        AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {

            //AsyncTaskにはonPreExecute,doInBackground,onPostExecuteという関数が用意されている。preExecute以外はオーバーライドしなければならない。
            //また、onPreExecute,doInBackground,onPostExecuteの順に実行されていく。
            //AsyncTaskの第一引数はonPreExecuteに,第二引数はdoInBackgroundに,第三引数はonPostExecuteに入る。

            //doInbackGroundはメインスレッドとは別のスレッドで実行されます。
            //doInbackGroundには非同期で行いたい処理を記述する。このメソッドから画面を操作しようとすると例外が発生する。
            //引数にはAsyncTaskの第二引数であるVoidが入る。
            @Override
            protected List<twitter4j.Status> doInBackground(Void... params) {
                //try-catch文 TwitterExceptionでキャッチする
                try {
                    //mTwitterのgetHomeTimeLineを返り値にする
                    return mTwitter.getHomeTimeline();
                } catch (TwitterException e) {
                    //スタックとレースを出力
                    e.printStackTrace();
                }
                //返り値はnull
                return null;
            }

            //onPostExecuteはdoInBackgroundメソッドの実行後にメインスレッドで実行されます。
            //doInBackgroundメソッドの戻り値をこのメソッドの引数として受け取り、その結果を画面に反映させることができる。
            @Override
            protected void onPostExecute(List<twitter4j.Status> result) {
                if (result != null) {
                    // mAdapterの中身をなくす
                    mAdapter.clear();
                    //引数のresultで拡張for文。
                    for (twitter4j.Status status : result) {
                        // listの中身をmAdapterに入れていく
                        mAdapter.add(status);
                    }
                    //getlistview()によって得たlistviewの先頭に移動する。(ここで指してるlistviewはこのActivityかしら？)
                    getListView().setSelection(0);
                } else {
                    //toast表示
                    showToast("タイムラインの取得に失敗しました。。。");
                }
            }
        };
        //設定したAsyncTask型変数taskを呼び出す。
        task.execute();
    }
    //toastを表示する。長めに表示。
    private void showToast(String text) {
        //引数のtextを長めに表示
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
