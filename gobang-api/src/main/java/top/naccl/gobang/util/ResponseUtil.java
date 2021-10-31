package top.naccl.gobang.util;

import top.naccl.gobang.enums.BaseEnum;
import top.naccl.gobang.enums.ErrorCodeEnum;
import top.naccl.gobang.model.common.Response;

/**
 * @Auther: wAnG
 * @Date: 2021/10/31 16:14
 * @Description:
 */

public class ResponseUtil<T> {

    public static Response success() {
        return buildResult(ErrorCodeEnum.SUCCESS, null);
    }

    public static Response success(Object data) {
        return buildResult(ErrorCodeEnum.SUCCESS, data);
    }

    public static Response success(BaseEnum errorCode, Object data) {
        return buildResult(errorCode, data);
    }

    public static Response fail(BaseEnum errorCode) {
        return buildResult(errorCode, null);
    }

    public static Response fail(int errorCode, String errorMsg) {
        return buildResult(errorCode, errorMsg, null);
    }

    private static Response buildResult(BaseEnum errorCode, Object data) {
        return buildResult(errorCode.getCode(), errorCode.getMsg(), data);
    }

    private static Response buildResult(int errorCode, String errorMsg, Object data) {
        return new Response(errorCode, errorMsg, data);
    }
}
