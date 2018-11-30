package com.fitpay.android.api.models.collection

import com.fitpay.android.api.models.Country

/**
 * Countries list
 */
class CountryCollection(val collection: Map<String, Country>) {

    fun getCountry(iso: String): Country? {
        return collection[iso]
    }
}