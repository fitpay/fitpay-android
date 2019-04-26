package com.fitpay.android.api.callbacks;

import androidx.annotation.NonNull;

import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.utils.StringUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Wrapper around Retrofit callback
 */
final public class CallbackWrapper<T> implements Callback<T> {

    private ApiCallback<T> mCallback;

    public CallbackWrapper(@NonNull ApiCallback<T> callback) {
        mCallback = callback;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (mCallback != null) {
            if (response.isSuccessful() && response.errorBody() == null) {
                mCallback.onSuccess(response.body());
            } else {
                @ResultCode.Code int errorCode = response.code();
                String errorStr = null;

                if (response.errorBody() != null) {
                    try {
                        errorStr = response.errorBody().string();
                    } catch (Exception e) {
                    }
                } else {
                    errorStr = response.message();
                }

                if (StringUtils.isEmpty(errorStr)) {
                    errorStr = "No error response!";
                }

                mCallback.onFailure(errorCode, errorStr);
            }
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        t.printStackTrace();   //TODO remove a little later - helpful in development
        if (mCallback != null) {
            mCallback.onFailure(ResultCode.REQUEST_FAILED, t.getMessage());
        }
    }
}
