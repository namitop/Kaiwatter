package com.example.shiraki_hirotomo.kaiwatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.shiraki_hirotomo.kaiwatter.constants.Config;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by shiraki-hirotomo on 2014/09/11.
 */
public class TwitterUtils {
    //キー
    private static final String TOKEN = "token";
    //キー
    private static final String TOKEN_SECRET = "token_secret";
    //キー
    private static final String PREF_NAME = "twitter_access_token";
    /**
     * Twitterインスタンスを取得します。アクセストークンが保存されていれば自動的にセットします。
     *
     * @param context
     * @return
     */
    public static Twitter getTwitterInstance(Context context) {
        //String型変数comsumerkeyにcontextのtwitter_consumer_keyを入れる（twitterのapplication managementでもらったyour access tokenのaccess tokenをstringsに書いておく）
        String consumerKey = Config.CONSUMER_KEY;
        //String型変数comsumerkeyにcontextのtwitter_consumer_secretを入れる（twitterのapplication managementでもらったyour access tokenのaccess token secretをstringsに書いておく）
        String consumerSecret = Config.CONSUMER_SECRET;
        //Twitter4JのAPIに与える設定オブジェクトとTwitterの各種機能にアクセスするための窓口となるファサードオブジェクトのファクトリであるTwitterFactoryを宣言
        TwitterFactory factory = new TwitterFactory();
        //宣言したTwitter型変数twitterにfactoryのインスタンスを代入
        Twitter twitter = factory.getInstance();
        //twitterにコンシューマ・キーとコンシューマ・シークレットをセット
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        //アクセストークンが存在する場合はtrueを返します。
        if (hasAccessToken(context)) {
            //コンテキストのアクセストークンをloadAccessTokenで読み込み、twitterにセット
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        //返り値をtwitterにしている
        return twitter;
    }

    /**
     * アクセストークンをプリファレンスに保存します。
     *
     * @param context
     * @param accessToken
     */
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        // コンテキストからPREF_NAMEをキーにSharedPreferenceのインスタンスを取得している
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        // Editorのインスタンスを取得
        Editor editor = preferences.edit();
        //editorに、TOKENをキーにしてaccessTokenのtokenを追加している
        editor.putString(TOKEN, accessToken.getToken());
        //editorに、TOKENをキーにしてaccessTokenのtokensecretを追加している
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
        //editorの変更をコミットする。
        editor.commit();
    }

    /**
     * アクセストークンをプリファレンスから読み込みます。
     *
     * @param context
     * @return
     */
    public static AccessToken loadAccessToken(Context context) {
        //contextからgetSharedPreferenceする。第一引数はこのSharedPreferenceの名前となる（キーのようなもの）。
        //第二引数はSharedPreferenceの共有モードを指定できる。MODE_PRIVATEにすると、そのアプリケーションだけで使用可能なデータとなる。
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        //TOKENをキーにしてデータをpreferencesから取り出す
        String token = preferences.getString(TOKEN, null);
        //TOKENを_SECRETキーにしてデータをpreferencesから取り出す
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        //tokenとtokenSecretの両方がnullじゃない場合、一つ目の処理が行われる。
        if (token != null && tokenSecret != null) {
            //AccessTokenを作成して返り値にしている
            return new AccessToken(token, tokenSecret);
        } else {
            //nullを返す。
            return null;
        }
    }

    /**
     * アクセストークンが存在する場合はtrueを返します。
     *
     * @return
     */
    public static boolean hasAccessToken(Context context) {
        // アクセストークンをプリファレンスから読み込み、nullじゃなかったらtrueを返り値にしている
        return loadAccessToken(context) != null;
    }
}
