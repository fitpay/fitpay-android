package com.fitpay.android.api.models

/**
 * RootLinks api resource
 *
 */
class RootLinks(val train: String) : BaseModel() {
    companion object {
        private val WEBAPP_PRIVACY_POLICY = "webapp.privacy-policy"
        private val WEBAPP_TERMS = "webapp.terms"
    }

    /**
     * Get webappPrivacyPolicy url
     *
     * @return webappPrivacyPolicy url
     */
    fun getPrivacyPolicy(): Link? {
        return getLink(WEBAPP_PRIVACY_POLICY)
    }

    /**
     * Get webappTerms url
     *
     * @return webappTerms url
     */
    fun getTerms(): Link? {
        return getLink(WEBAPP_TERMS)
    }
}