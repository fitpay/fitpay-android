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
    public static final String FULL_SIMULATED_SE_PREFIX = "DEADBEEF";
    public static final String PARTIAL_SIMULATED_SE_PREFIX = "BADC0FFEE";

    private static Random r = new Random();

    public static String generateCasd() {
        return "7F218201B87F218201B393102016080214000200000000001122334442038949325F200C434552542E434153442E43549501825F2504201607015F240420210701450CA000000151535043415344005314F8E1CB407F2233139DC304E40B81C21C52BFB3B35F37820100BA80871A53DB1C9225326925A6D8C09BFAE7414653727B20FE7D0F0C322D0D6DB943ECE385E8CDDFE52D33521EE0D350449E4B4D8EFC62C4AA3F8DB36280BEA1E17D69CAFBFCA90715FA3281E28EE6A3458E7A21E3119AF39819246A93FE566B91C101207481391FE7AB45FB18D89F837BA55CC0E0B413AD874F5C92158199DC500D76D597B07E4C31731A0EEC4C28EB1ECD3E82691D5EDF8D33B2CF78C7A1EA5798D688B134C18ECC8F4E6F28F6EE719CA2DEF1037175B2E5396BF36DF1E0A176CD2B37275C6B875B52B6A517CF6F994307444F35409DB009C24B42E77270E2029D4D1D0E550D059163FE4E5A9ED78BC89E6C68E4CA477F83C0FD9BB2EAA03D5F38503C58AE29F3EDA45DA911E8D1DD66E2D196A6384E4008FBA3291765ABF936C5155D48C8E37FFA162C50CA9BA64DC2996B3F89A4B0A1099EC4ABBA2E57E00306FA5FD1858122FDC7E362C65F1F38DF4E1D";
    }

    public static String generateRandomSecureElementId(String prefix) {
        byte[] randomId = new byte[6];
        r.nextBytes(randomId);
        String uniqueIdentifier = Hex.bytesToHexString(randomId);

        return generateRandomSecureElementId(prefix, uniqueIdentifier);
    }

    public static String generateRandomSecureElementId(String prefix, String uniqueIdentifier) {
        if (StringUtils.isEmpty(prefix)) {
            prefix = FULL_SIMULATED_SE_PREFIX + "0002";
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
        buf.append("0823"); // IC Manufacturer - 0823 is ST
        buf.append("4272"); // IC EMbedding Date
        buf.append("0823"); // PrePerso Id
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
