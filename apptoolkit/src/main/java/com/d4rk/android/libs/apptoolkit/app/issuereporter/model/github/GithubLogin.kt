package com.d4rk.android.libs.apptoolkit.app.issuereporter.model.github

class GithubLogin {
    val username: String?
    val password: String?
    val apiToken: String?

    constructor(username: String, password: String) {
        this.username = username
        this.password = password
        this.apiToken = null
    }

    constructor(apiToken: String) {
        this.username = null
        this.password = null
        this.apiToken = apiToken
    }

    val shouldUseApiToken: Boolean
        get() = username.isNullOrEmpty() || password.isNullOrEmpty()
}
