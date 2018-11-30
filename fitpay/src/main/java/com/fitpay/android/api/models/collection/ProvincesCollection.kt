package com.fitpay.android.api.models.collection

import com.fitpay.android.api.models.Province

/**
 * Provinces list
 */
class ProvincesCollection(val iso: String,
                          val names: Map<String, String>,
                          val name: String,
                          val provinces: Map<String, Province>) {

    fun getProvince(iso: String): Province? {
        return provinces[iso]
    }
}