package com.fitpay.android.api.models

import com.fitpay.android.api.callbacks.ApiCallbackExt
import com.fitpay.android.api.models.collection.ProvinceCollection

/**
 * Country model
 */
class Country(val iso: String,
              val name: String,
              val names: Map<String, String>) : BaseModel() {

    companion object {
        private val PROVINCES = "provinces"
    }

    /**
     * Get list of provinces
     *
     * @param callback result
     */
    fun getProvinces(callback: ApiCallbackExt<ProvinceCollection>) {
        makeGetCall<ProvinceCollection>(PROVINCES, null, ProvinceCollection::class.java, callback)
    }

    /**
     * Is provinces endpoint available
     *
     * @return is url available
     */
    fun canGetProvinces(): Boolean {
        return getLink(PROVINCES) != null
    }
}


