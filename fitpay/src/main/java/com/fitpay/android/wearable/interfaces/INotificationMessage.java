package com.fitpay.android.wearable.interfaces;

import java.util.Date;

/**
 * Created by Vlad on 29.03.2016.
 */
public interface INotificationMessage extends IMessage{
    Date getDate();
    byte[] getData();
    byte[] getType();
}
