package com.fitpay.android.paymentdevice.impl.mock;

import com.fitpay.android.utils.Hex;
import com.fitpay.android.utils.StringUtils;

import java.util.Random;

/**
 * Created by ssteveli on 2/25/17.
 */

public class SecureElementDataProvider {
    // See the Simulated Secure Element Flow doc in the FitPay wiki for a detailed explanation of the different
    // behaviors simulated SE's provide.
    public static final String FULL_SIMULATED_SE_PREFIX = "70B1A500";
    public static final String PARTIAL_SIMULATED_SE_PREFIX = "600DCA700";

    private static Random r = new Random();

    public static String generateCasd() {
        return "7F218202A47F218201B393102016072916110000000000001122334442038949325F200C434552542E434153442E43549501825F2504201607015F240420210701450CA000000151535043415344005314F8E1CB407F2233139DC304E40B81C21C52BFB3B35F37820100D27D99221AB06EAD71B6BC3D6008661953EBC3BD5A32C49212EFE95BDE0846632D211100AD9C67C0C8904D65823DF4AF76E73360B83943DC16A45471FBFC44E4FB254433BFE678A2E364712C3FFFF86EEB718F927DB12E8E78B3C33F980BF2CE5E333F4CFA9E9A5A3AF09CD779BEB6173D2142013B45357E6B785399C80D2C283A82EDFE8E06A72DEF4E28617700EA7CBAC02197798DA3E7E2F5C84D0F23857846DEC069553E0BCF4DB86E68B3F80C8B95053F588E47910C2BEA34D95136BA4BB4F5C41D7461062EDCD9BAF43249AA2DD005888820F5174AFC626A17C0AB326F39A095E97D99509F6DACAA61C5A31E6D1027504CC31091060111E03A8F4297E15F3850B4D8B6F9282431E1009282C23133D8025A44CC2F8CCE402B79E2A51B4EFA38E9C8A378596181B6410C5A8F7E0BB354332A93DEB40B1CACBFF1FC23B5804B52EBA1811B30E40F77CAC891F42CDCB902BF7F2181E89310201608081605268F370493B60000000142038949325F200C434552542E434153442E4354950200805F2504201607015F240420210701450CA000000151535043415344005314C0AC3B49223485BE2FCFECBC19CFE14CE01CD9797F4946B04104101E87906ADD42D19DD1BBE2E31C77C46DFA573B1765AA016B27730517AECB471372BE5855EC68FA0F4EDDA449731806630B0C55B36A03DD80613B8946006367F001005F3740301848CF8A6A80888150AFC7B3FB079671D1850B67D8A3DA5A9747E45BF51A1B49D7850853175133314A2A1DCC8D5D43B92B14E75FD5DE329A236CBEEBF1F9A5";
    }

    public static String generateRandomSecureElementId(String prefix) {
        byte[] randomId = new byte[6];
        r.nextBytes(randomId);
        String uniqueIdentifier = Hex.bytesToHexString(randomId);

        return generateRandomSecureElementId(prefix, uniqueIdentifier);
    }

    public static String generateRandomSecureElementId(String prefix, String uniqueIdentifier) {
        if (StringUtils.isEmpty(prefix)) {
            prefix = FULL_SIMULATED_SE_PREFIX + "0000";
        } else if (prefix.length() < 12) {
            final int padCount = 12 - prefix.length();
            for (int i=0; i<padCount; i++) {
                prefix = "0" + prefix;
            }
        } else if (prefix.length() > 12) {
            prefix = prefix.substring(0, 12);
        }

        StringBuffer buf = new StringBuffer();

        buf.append(prefix); // ICFabricator 2, ICType 2, OS Provider Id 2
        buf.append("000B"); // OS Release Date
        buf.append("A303"); // OS Release Level
        buf.append("5287"); // IC Fabrication Date
        buf.append(uniqueIdentifier);
        buf.append("B230"); // IC Module Fabricator
        buf.append("60A4"); // IC Module Packaging Date
        buf.append("0057"); // IC Manufacturer - 0057 is NXP
        buf.append("4272"); // IC EMbedding Date
        buf.append("0051"); // PrePerso Id
        buf.append("6250"); // PrePerso Date
        buf.append("08246250"); // PrePerso Equipment
        buf.append("2041"); // Perso Id
        buf.append("6250"); // Perso Date
        buf.append("08256250"); // Perso Equipment

        return buf.toString().toUpperCase();
    }

    public static String generateRandomSecureElementId() {
        return generateRandomSecureElementId(null);
    }
}
