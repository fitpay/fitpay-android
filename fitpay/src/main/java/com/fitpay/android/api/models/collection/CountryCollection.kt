package com.fitpay.android.api.models.collection

import com.fitpay.android.api.models.Country

/**
 * Country collection
 */
class CountryCollection(val collection: Map<String, Country>) {

    /**
     * Get country
     *
     * @param iso country ISO
     * @return country
     */
    fun getCountry(iso: String): Country? {
        return collection[iso]
    }
}