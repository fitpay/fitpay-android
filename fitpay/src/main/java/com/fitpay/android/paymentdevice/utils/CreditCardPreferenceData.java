package com.fitpay.android.paymentdevice.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for set/get credit card acceptTerms sessionData
 */
public class CreditCardPreferenceData {

    private CreditCardPreferenceData() {
    }

    protected static SharedPreferences getPreferences(@NonNull Context context, @NonNull String userId) {
        return context.getSharedPreferences("user_creditCards_" + userId, Context.MODE_PRIVATE);
    }

    protected static SharedPreferences.Editor getEditor(@NonNull Context context, @NonNull String userId) {
        SharedPreferences prefs = getPreferences(context, userId);
        return prefs.edit();
    }

    /**
     * Update credit cards acceptTerms link with sessionData from prefs
     *
     * <p>
     * @see User#createCreditCard
     * </p>
     *
     * @param context    context
     * @param creditCard credit card
     */
    public static void update(@NonNull Context context, @NonNull String userId, @NonNull CreditCard creditCard) {
        update(context, userId, Collections.singletonList(creditCard));
    }

    /**
     * Update credit cards acceptTerms link with sessionData from prefs
     *
     * <p>
     * @see User#createCreditCard
     * </p>
     *
     * @param context     context
     * @param userId      user id
     * @param creditCards list of credit cards
     */
    public static void update(@NonNull Context context, @NonNull String userId, @NonNull List<CreditCard> creditCards) {
        SharedPreferences prefs = getPreferences(context, userId);
        SharedPreferences.Editor editor = prefs.edit();
        if (creditCards.size() == 0) {
            editor.clear().apply();
        } else {
            Map<String, String> map = (Map<String, String>) prefs.getAll();
            Set<String> savedKeys = map.keySet();
            for (CreditCard card : creditCards) {
                final String cardId = card.getCreditCardId();
                if (savedKeys.contains(cardId)) {
                    if (card.canAcceptTerms()) {
                        final String sessionData = map.get(cardId);
                        if (!StringUtils.isEmpty(sessionData)) {
                            card.updateSessionData(sessionData);
                            continue;
                        }
                    }
                    break;
                } else {
                    editor.remove(cardId).commit();
                }
            }
        }
    }

    /**
     * Store acceptTerms sessionData into prefs
     *
     * <p>
     * @see User#createCreditCard
     * </p>
     *
     * @param context    context
     * @param creditCard credit card
     */
    public static void store(@NonNull Context context, @NonNull CreditCard creditCard) {
        getEditor(context, creditCard.getUserId())
                .putString(creditCard.getCreditCardId(), creditCard.getSessionData())
                .commit();
    }

    /**
     * Remove sessionData from prefs
     *
     * <p>
     * @see User#createCreditCard
     * </p>
     *
     * @param context    context
     * @param creditCard credit card
     */
    public static void remove(@NonNull Context context, @NonNull CreditCard creditCard) {
        getEditor(context, creditCard.getUserId())
                .remove(creditCard.getCreditCardId())
                .commit();
    }
}
