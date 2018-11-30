package com.fitpay.android.api.models

import com.fitpay.android.api.callbacks.ApiCallbackExt
import com.fitpay.android.api.models.collection.ProvincesCollection

/**
 * Country model
 */
class Country(val iso: String,
              val name: String,
              val names: Map<String, String>) : BaseModel() {

    companion object {
        private val PROVINCES = "provinces"
    }

    fun getProvinces(callback: ApiCallbackExt<ProvincesCollection>) {
        makeGetCall<ProvincesCollection>(PROVINCES, null, ProvincesCollection::class.java, callback)
    }
}


