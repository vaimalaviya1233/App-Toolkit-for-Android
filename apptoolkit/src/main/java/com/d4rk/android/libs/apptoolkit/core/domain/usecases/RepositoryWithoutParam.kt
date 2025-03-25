package com.d4rk.android.libs.apptoolkit.core.domain.usecases

interface RepositoryWithoutParam<R> {
    suspend operator fun invoke() : R
}