package com.fitpay.android.api.models

/**
 * Province model
 */
data class Province(val iso: String,
                    val name: String,
                    val names: Map<String, String>)