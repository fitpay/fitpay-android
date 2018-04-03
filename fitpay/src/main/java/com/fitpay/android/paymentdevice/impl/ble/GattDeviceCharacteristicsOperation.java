package com.fitpay.android.paymentdevice.impl.ble;

import android.bluetooth.BluetoothGatt;

import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.device.PaymentDevice;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.Hex;
import com.fitpay.android.utils.StringUtils;

import java.util.UUID;

/**
 * Read all device characteristics operation
 */
class GattDeviceCharacteristicsOperation extends GattOperation {

    private String connectorId;

    private String mAddress;

    private String manufacturerName;
    private String modelNumber;
    private String serialNumber;
    private String hardwareRevision;
    private String firmwareRevision;
    private String softwareRevision;
    private String systemId;
    private String secureElementId;
    private String casd;

    public GattDeviceCharacteristicsOperation(final String connectorId, final String macAddress) {
        this.connectorId = connectorId;

        mAddress = macAddress;

        addNestedOperation(createOperation(DeviceInformationConstants.CHARACTERISTIC_MANUFACTURER_NAME_STRING,
                data -> manufacturerName = Hex.bytesToHexString(data)));

        addNestedOperation(createOperation(DeviceInformationConstants.CHARACTERISTIC_MODEL_NUMBER_STRING,
                data -> modelNumber = Hex.bytesToHexString(data)));

        addNestedOperation(createOperation(DeviceInformationConstants.CHARACTERISTIC_SERIAL_NUMBER_STRING,
                data -> serialNumber = Hex.bytesToHexString(data)));

        addNestedOperation(createOperation(DeviceInformationConstants.CHARACTERISTIC_FIRMWARE_REVISION_STRING,
                data -> firmwareRevision = Hex.bytesToHexString(data)));

        addNestedOperation(createOperation(DeviceInformationConstants.CHARACTERISTIC_SOFTWARE_REVISION_STRING,
                data -> softwareRevision = Hex.bytesToHexString(data)));

        addNestedOperation(createOperation(DeviceInformationConstants.CHARACTERISTIC_HARDWARE_REVISION_STRING,
                data -> hardwareRevision = Hex.bytesToHexString(data)));

        addNestedOperation(createOperation(DeviceInformationConstants.CHARACTERISTIC_SYSTEM_ID,
                data -> systemId = Hex.bytesToHexString(data)));

        addNestedOperation(new GattCharacteristicReadOperation(
                PaymentServiceConstants.SERVICE_UUID,
                PaymentServiceConstants.CHARACTERISTIC_SECURE_ELEMENT_ID,
                data -> secureElementId = Hex.bytesToHexString(data)));
    }

    @Override
    public void execute(BluetoothGatt gatt) {
        Device device = new Device.Builder()
                .setBdAddress(mAddress)
                .setModelNumber(StringUtils.convertHexStringToAscii(modelNumber))
                .setManufacturerName(StringUtils.convertHexStringToAscii(manufacturerName))
                .setSerialNumber(StringUtils.convertHexStringToAscii(serialNumber))
                .setSystemId(StringUtils.convertHexStringToAscii(systemId))
                .setSecureElement(new PaymentDevice.SecureElement(
                        casd,
                        StringUtils.convertHexStringToAscii(secureElementId)))
                .setFirmwareRevision(StringUtils.convertHexStringToAscii(firmwareRevision))
                .setSoftwareRevision(StringUtils.convertHexStringToAscii(softwareRevision))
                .setHardwareRevision(StringUtils.convertHexStringToAscii(hardwareRevision))
                .build();

        RxBus.getInstance().post(connectorId, device);
    }

    private GattOperation createOperation(UUID characteristicUUID, GattBaseReadOperation.OnReadCallback callback) {
        return new GattCharacteristicReadOperation(DeviceInformationConstants.SERVICE_UUID, characteristicUUID, callback);
    }
}
