package com.dahai.file;


public class BaseResponse<T> {

    public String reason;
    public String error_code = "0";
    public T result;

    @Override
    public String toString() {
        return "BaseResponse{" +
                "reason='" + reason + '\'' +
                ", error_code='" + error_code + '\'' +
                ", result=" + result +
                '}';
    }
}

