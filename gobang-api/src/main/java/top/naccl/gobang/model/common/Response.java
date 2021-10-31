package top.naccl.gobang.model.common;

/**
 * @Auther: wAnG
 * @Date: 2021/10/31 16:07
 * @Description:
 */

public class Response<T> {

    /**
     * 响应码
     */
    private int errorCode;

    /**
     * 响应码对应的内容
     */
    private String errorMsg;

    /**
     * 响应数据
     */
    private T data;

    public Response(Integer code, String msg, T data) {
        this.errorCode = code;
        this.errorMsg = msg;
        this.data = data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
