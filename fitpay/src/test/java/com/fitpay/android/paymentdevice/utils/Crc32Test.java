package com.fitpay.android.paymentdevice.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.zip.CRC32;

public class Crc32Test {

    @Test
    public void testChecksum(){
        byte[] data = "data".getBytes();
        long checksum1 = Crc32.getCRC32Checksum(data);

        CRC32 crc32 = new CRC32();
        crc32.update(data);
        long checksum2 = crc32.getValue();

        Assert.assertEquals(checksum1, checksum2);
    }
}
