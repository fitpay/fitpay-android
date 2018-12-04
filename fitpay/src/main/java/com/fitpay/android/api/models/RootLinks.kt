package com.fitpay.android.api.models

/**
 * RootLinks api resource
 *
 */
class RootLinks(val train: String) : BaseModel() {
    companion object {
        private val WEBAPP_PRIVACY_POLICY = "webapp.privacyPolicy"
        private val WEBAPP_TERMS = "webapp.terms"
    }

    /**
     * Get webappPrivacyPolicy url
     *
     * @return webappPrivacyPolicy url
     */
    fun getWebappPrivacyPolicy(): Link? {
        return getLink(WEBAPP_PRIVACY_POLICY)
    }

    /**
     * Get webappTerms url
     *
     * @return webappTerms url
     */
    fun getWebappTerms(): Link? {
        return getLink(WEBAPP_TERMS)
    }
}