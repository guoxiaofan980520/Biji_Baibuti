package com.baibuti.biji.Net.Modules.Note;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespObj.UploadStatus;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class ImgUtil {

    private static final String ImgUploadUrl = Urls.NoteUrl + "/img/upload";
    // private static final String GetImgUrl = Urls.NoteUrl + "/img/blob/%s/%s";

    public static UploadStatus uploadImg(URI uri) throws ServerErrorException {
        File img = new File(uri);
        RespType resp = NetUtil.httpPostFileSync(ImgUploadUrl, "noteimg", img, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                return new UploadStatus(msg.getDetail(), msg.getMessage());
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * ImgPopupDialog OCRActivity 用
     * 新线程 异步!!!
     */
    @WorkerThread
    public static void GetImgAsync(String url, IImageBack imageBack) {
        Log.e("", "GetImg: " + url );
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @EverythingIsNonNull
                @Override
                public void onFailure(Call call, IOException e) { }

                @EverythingIsNonNull
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream in = response.body().byteStream();
                    imageBack.onGetImg(BitmapFactory.decodeStream(in));
                }
            });
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public interface IImageBack {
        void onGetImg(Bitmap bitmap);
    }
}
