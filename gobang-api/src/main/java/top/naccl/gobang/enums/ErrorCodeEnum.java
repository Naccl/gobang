package top.naccl.gobang.enums;

/**
 * @Auther: wAnG
 * @Date: 2021/10/31 15:50
 * @Description:
 */
public enum ErrorCodeEnum implements BaseEnum {

    // [10000,19999] 表示操作成功
    SUCCESS(10000, "成功"),

    // [20000,29999] 表示系统错误
    SERVER_ERROR(20000, "服务器异常");

    private final int code;

    private final String msg;

    ErrorCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
