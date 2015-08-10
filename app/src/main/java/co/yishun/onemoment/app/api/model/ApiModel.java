package co.yishun.onemoment.app.api.model;

import java.io.Serializable;

/**
 * Created by Carlos on 2015/8/8.
 */
public class ApiModel implements Serializable {
    public int code;
    public int errorCode;
    public String msg;

    public ApiModel() {

    }

    public ApiModel(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}