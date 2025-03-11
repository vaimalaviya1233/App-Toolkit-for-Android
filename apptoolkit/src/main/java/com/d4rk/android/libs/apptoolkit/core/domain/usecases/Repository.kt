package com.d4rk.android.libs.apptoolkit.core.domain.usecases

interface Repository<T , R> {
    suspend operator fun invoke(param : T) : R
}